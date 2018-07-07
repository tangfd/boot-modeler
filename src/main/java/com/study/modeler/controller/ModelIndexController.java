package com.study.modeler.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author TangFD@HF 2018/7/6
 */
@Controller
public class ModelIndexController {

    @Resource
    private RepositoryService repositoryService;

    @RequestMapping("/list")
    public String list(org.springframework.ui.Model model) {
        List<Model> modelList = repositoryService.createModelQuery().orderByCreateTime().desc().list();
        model.addAttribute("modelList", modelList);
        return "list";
    }

    @RequestMapping("/")
    public String init() {
        return "redirect:list";
    }

    @RequestMapping("/modeler")
    public String modeler(org.springframework.ui.Model model, @RequestParam String modelId) {
        return "modeler";
    }

    /**
     * 创建模型
     */
    @RequestMapping("/create")
    public String create(@RequestParam String name, @RequestParam String key, @RequestParam String description) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode editorNode = objectMapper.createObjectNode();
            editorNode.put("id", "canvas");
            editorNode.put("resourceId", "canvas");
            ObjectNode stencilSetNode = objectMapper.createObjectNode();
            stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
            editorNode.put("stencilset", stencilSetNode);
            Model modelData = repositoryService.newModel();

            ObjectNode modelObjectNode = objectMapper.createObjectNode();
            modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, name);
            modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
            description = StringUtils.defaultString(description);
            modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description);
            modelData.setMetaInfo(modelObjectNode.toString());
            modelData.setName(name);
            modelData.setKey(StringUtils.defaultString(key));

            repositoryService.saveModel(modelData);
            repositoryService.addModelEditorSource(modelData.getId(), editorNode.toString().getBytes("utf-8"));

            return "redirect:modeler?modelId=" + modelData.getId();
        } catch (Exception e) {
            System.out.println("创建模型失败：" + e.getMessage());
        }

        return "redirect:list";
    }


    @RequestMapping(value = "/delete/{modelId}")
    public String delete(@PathVariable("modelId") String modelId) {
        repositoryService.deleteModel(modelId);
        return "redirect:list";
    }
}
