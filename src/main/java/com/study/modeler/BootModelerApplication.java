package com.study.modeler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author TangFD@HF
 */
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class,
        org.activiti.spring.boot.SecurityAutoConfiguration.class})
@SpringBootApplication
public class BootModelerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BootModelerApplication.class, args);
        System.out.println("--------------SERVER OK--------------");
        /*ProcessEngine defaultProcessEngine = ProcessEngines.getDefaultProcessEngine();
        DeploymentBuilder deployment = defaultProcessEngine.getRepositoryService().createDeployment();
        deployment.addClasspathResource("processes/leave.bpmn20.xml").deploy();
        System.out.println("--------------PROCESSES OK--------------");*/
    }
}
