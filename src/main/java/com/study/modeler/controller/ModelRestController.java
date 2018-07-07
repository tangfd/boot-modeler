package com.study.modeler.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.study.modeler.domain.ModelBase;
import com.study.modeler.utils.JsonResult;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author TangFD@HF 2018/7/6
 */
@RestController
public class ModelRestController {
    private static final Log logger = LogFactory.getLog(ModelRestController.class);

    @Resource
    private RepositoryService repositoryService;
    @Resource
    private ObjectMapper objectMapper;

    @RequestMapping(value = "/model/{modelId}/json", produces = {"application/json"})
    public ObjectNode getEditorJson(@PathVariable String modelId) {
        Model model = this.repositoryService.getModel(modelId);
        if (model == null) {
            return null;
        }

        try {
            ObjectNode modelNode;
            if (StringUtils.isNotEmpty(model.getMetaInfo())) {
                modelNode = (ObjectNode) objectMapper.readTree(model.getMetaInfo());
            } else {
                modelNode = objectMapper.createObjectNode();
                modelNode.put("name", model.getName());
            }

            modelNode.put("modelId", model.getId());
            ObjectNode e = (ObjectNode) objectMapper.readTree(new String(repositoryService.getModelEditorSource(model.getId()), "utf-8"));
            modelNode.put("model", e);
            return modelNode;
        } catch (Exception var5) {
            System.out.println("Error creating model JSON" + var5.getMessage());
            throw new ActivitiException("Error creating model JSON", var5);
        }
    }

    @RequestMapping(value = "/editor/stencilset", produces = {"application/json;charset=utf-8"})
    public String getStencilset() {
        InputStream stencilsetStream = this.getClass().getClassLoader().getResourceAsStream("stencilset-zh.json");

        try {
            return IOUtils.toString(stencilsetStream, "utf-8");
        } catch (Exception var3) {
            throw new ActivitiException("Error while loading stencil set", var3);
        }
    }

    @RequestMapping(value = {"/model/{modelId}/save"}, method = {RequestMethod.PUT})
    @ResponseStatus(HttpStatus.OK)
    public void saveModel(@PathVariable String modelId, ModelBase modelBase) {
        try {
            Model e = repositoryService.getModel(modelId);
            ObjectNode modelJson = (ObjectNode) this.objectMapper.readTree(e.getMetaInfo());
            modelJson.put("name", modelBase.getName());
            modelJson.put("description", modelBase.getDescription());
            e.setMetaInfo(modelJson.toString());
            e.setName(modelBase.getName());
            repositoryService.saveModel(e);
            repositoryService.addModelEditorSource(e.getId(), modelBase.getJson_xml().getBytes("utf-8"));
            ByteArrayInputStream svgStream = new ByteArrayInputStream(modelBase.getSvg_xml().getBytes("utf-8"));
            TranscoderInput input = new TranscoderInput(svgStream);
            PNGTranscoder transcoder = new PNGTranscoder();
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            TranscoderOutput output = new TranscoderOutput(outStream);
            transcoder.transcode(input, output);
            byte[] result = outStream.toByteArray();
            repositoryService.addModelEditorSourceExtra(e.getId(), result);
            outStream.close();
        } catch (Exception var11) {
            throw new ActivitiException("Error saving model", var11);
        }
    }

    @RequestMapping("/download/{modelId}")
    public void downloadBPMN(@PathVariable String modelId, HttpServletResponse response) {
        Model model = repositoryService.getModel(modelId);
        if (model == null) {
            return;
        }

        try {
            byte[] modelEditorSource = repositoryService.getModelEditorSource(modelId);
            JsonNode editorNode = new ObjectMapper().readTree(modelEditorSource);
            BpmnModel bpmnModel = new BpmnJsonConverter().convertToBpmnModel(editorNode);

            // 处理异常
            if (bpmnModel.getMainProcess() == null) {
                response.setStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
                response.getOutputStream().println("no main process, can't export for BPMN");
                response.flushBuffer();
                return;
            }

            String filename, type = "";
            byte[] exportBytes;
            String mainProcessId = bpmnModel.getMainProcess().getId();
            if ("JSON".equals(type)) {
                exportBytes = modelEditorSource;
                filename = mainProcessId + ".json";
            } else {
                BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
                exportBytes = xmlConverter.convertToXML(bpmnModel);
                filename = mainProcessId + ".bpmn20.xml";
            }

            ByteArrayInputStream in = new ByteArrayInputStream(exportBytes);
            IOUtils.copy(in, response.getOutputStream());
            response.setHeader("Content-Disposition", "attachment; filename=" + filename);
            response.flushBuffer();
        } catch (Exception e) {
            logger.error("导出model的xml文件失败", e);
        }
    }

    @RequestMapping("/img/{modelId}")
    public void img(@PathVariable String modelId, HttpServletResponse response) {
        Model model = repositoryService.getModel(modelId);
        if (model == null) {
            return;
        }
        try {
            byte[] sourceExtra = repositoryService.getModelEditorSourceExtra(modelId);
            ByteArrayInputStream in = new ByteArrayInputStream(sourceExtra);
            IOUtils.copy(in, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/deploy/{modelId}")
    public JsonResult deploy(@PathVariable String modelId) {
        JsonResult result = new JsonResult();
        try {
            Model modelData = repositoryService.getModel(modelId);
            if (modelData == null) {
                result.setMessage("MODEL_NOT_EXIST");
                return result;
            }
            byte[] modelEditorSource = repositoryService.getModelEditorSource(modelData.getId());
            ObjectNode modelNode = (ObjectNode) new ObjectMapper().readTree(modelEditorSource);
            BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
            byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);
            String processName = modelData.getName() + ".bpmn20.xml";
            Deployment deployment = repositoryService.createDeployment().name(modelData.getName()).addString(processName, new String(bpmnBytes)).deploy();
            result.setMessage(true, deployment.getId());
            return result;
        } catch (Exception e) {
            logger.error("根据模型部署流程失败", e);
            result.setMessage("MODEL_DEPLOY_FAILED");
        }

        return result;
    }

}
