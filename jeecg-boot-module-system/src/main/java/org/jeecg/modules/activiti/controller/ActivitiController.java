package org.jeecg.modules.activiti.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipInputStream;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.task.Task;
import org.jeecg.modules.activiti.service.ActivitiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("activiti")
public class ActivitiController {
	@Autowired
	private RuntimeService runtimeService;
	@Autowired
	private TaskService taskService;
	@Autowired
	private HistoryService historyService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private ProcessEngineConfigurationImpl processEngineConfiguration;
	@Autowired
	private ActivitiService activitiService;
	@Value("${activiti.workflow}")
	private String workflow;
		
	@GetMapping("/initTables")
	public String initTables() throws IOException {
		InputStream bpmn = this.getClass().getResourceAsStream("/MyProcess.bpmn");
		InputStream png = this.getClass().getResourceAsStream("/MyProcess.png");
		repositoryService.createDeployment().name(workflow)
		.addInputStream("MyProcess.bpmn", bpmn)
		.addInputStream("MyProcess.png", png).deploy();
		return "success";
	}
	
	@GetMapping("/startProcess")
	public String startProcess() {
		String processDefinitionKey = "helloWorld";
		runtimeService.startProcessInstanceByKey(processDefinitionKey , "1");
		return "success";
	}
	
	@GetMapping("/findTask")
	public Object findTask(String username) {
		Task singleResult = taskService.createTaskQuery().taskAssignee(username).singleResult();
		return singleResult.toString();
	}
	
	@GetMapping("/completeTask")
	public Object completeTask(String username) {
		Task singleResult = taskService.createTaskQuery().taskAssignee(username).singleResult();

		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("action", "approve");
		taskService.complete(singleResult.getId(), variables);
		return "success: " + singleResult.getId();
	}

}
