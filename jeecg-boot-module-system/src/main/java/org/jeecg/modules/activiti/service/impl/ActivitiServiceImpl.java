package org.jeecg.modules.activiti.service.impl;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.image.ProcessDiagramGenerator;
import org.jeecg.modules.activiti.service.ActivitiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;

@Service
@Slf4j
public class ActivitiServiceImpl implements ActivitiService {

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

	@Override
	public void startProcesses(String id, String business_key) {
		ProcessInstance pi = runtimeService.startProcessInstanceByKey(id, business_key);
		System.out.println("流程启动成功，流程id:" + pi.getId());
	}

	@Override
	public List<Task> findTasksByUserId(String userId) {
		List<Task> resultTask = taskService.createTaskQuery().processDefinitionKey("process")
				.taskCandidateOrAssigned(userId).list();
		return resultTask;
	}

	@Override
	public Task findTaskById(String taskId) {
		List<Task> resultTask = taskService.createTaskQuery().taskId(taskId).list();
		if (resultTask != null) {
			return resultTask.get(0);
		}
		return null;
	}

	@Override
	public void completeTask(String taskId, String userId, String result) {
		// 获取流程实例
		taskService.claim(taskId, userId);
		// 获取任务
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		// 获取流程实例ID
		String proInsId = task.getProcessInstanceId();
		// 获取流程实例
		ProcessInstance process = runtimeService.createProcessInstanceQuery().processInstanceId(proInsId)
				.singleResult();
		// 获取业务外键
		String business_key = process.getBusinessKey();
		String[] array = business_key.split(":");
		String business_Id = array[1];
		// TODO 业务处理

		taskService.complete(taskId);
	}

	@Override
	public void updateBizStatus(DelegateExecution execution, String status) {
		String bizId = execution.getProcessBusinessKey();
		// 根据业务id自行处理业务表
		System.out.println("业务表[" + bizId + "]状态更改成功，状态更改为：" + status);
	}

	@Override
	public void queryProImg(String processInstanceId) throws Exception {
		// 获取历史流程实例
		HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery()
				.processInstanceId(processInstanceId).singleResult();

		// 根据流程定义获取输入流
		InputStream is = repositoryService.getProcessDiagram(processInstance.getProcessDefinitionId());
		BufferedImage bi = ImageIO.read(is);
		File file = new File("demo2.png");
		if (!file.exists())
			file.createNewFile();
		FileOutputStream fos = new FileOutputStream(file);
		ImageIO.write(bi, "png", fos);
		fos.close();
		is.close();
		System.out.println("图片生成成功");

		List<Task> tasks = taskService.createTaskQuery().taskCandidateUser("userId").list();
		for (Task t : tasks) {
			System.out.println(t.getName());
		}
	}

	/**
	 * 流程图高亮显示 首先启动流程，获取processInstanceId，替换即可生成
	 * 
	 * @throws Exception
	 */
	@Override
	public String queryProHighLighted(String processInstanceId) throws Exception {
		// 获取历史流程实例
		HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery()
				.processInstanceId(processInstanceId).singleResult();
		// 获取流程图
		BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());

		ProcessDiagramGenerator diagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
		ProcessDefinitionEntity definitionEntity = (ProcessDefinitionEntity) repositoryService
				.getProcessDefinition(processInstance.getProcessDefinitionId());

		List<HistoricActivityInstance> highLightedActivitList = historyService.createHistoricActivityInstanceQuery()
				.processInstanceId(processInstanceId).list();
		// 高亮环节id集合
		List<String> highLightedActivitis = new ArrayList<String>();

		// 高亮线路id集合
		List<String> highLightedFlows = getHighLightedFlows(definitionEntity, highLightedActivitList);

		for (HistoricActivityInstance tempActivity : highLightedActivitList) {
			String activityId = tempActivity.getActivityId();
			highLightedActivitis.add(activityId);
		}
		// 配置字体
		InputStream imageStream = diagramGenerator.generateDiagram(bpmnModel, "png", highLightedActivitis,
				highLightedFlows, "宋体", "微软雅黑", "黑体", null, 2.0);
		BufferedImage bi = ImageIO.read(imageStream);
//        File file = new File("demo2.png");
//        if(!file.exists()) file.createNewFile();
//        FileOutputStream fos = new FileOutputStream(file);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ImageIO.write(bi, "png", bos);
		byte[] bytes = bos.toByteArray();// 转换成字节
		BASE64Encoder encoder = new BASE64Encoder();
		String png_base64 = encoder.encodeBuffer(bytes);// 转换成base64串
		png_base64 = png_base64.replaceAll("\n", "").replaceAll("\r", "");// 删除 \r\n
		bos.close();
		imageStream.close();
		return png_base64;
	}

	/**
	 * 获取需要高亮的线
	 * 
	 * @param processDefinitionEntity
	 * @param historicActivityInstances
	 * @return
	 */
	private List<String> getHighLightedFlows(ProcessDefinitionEntity processDefinitionEntity,
			List<HistoricActivityInstance> historicActivityInstances) {

		List<String> highFlows = new ArrayList<String>();// 用以保存高亮的线flowId
		for (int i = 0; i < historicActivityInstances.size() - 1; i++) {// 对历史流程节点进行遍历
			ActivityImpl activityImpl = processDefinitionEntity
					.findActivity(historicActivityInstances.get(i).getActivityId());// 得到节点定义的详细信息
			List<ActivityImpl> sameStartTimeNodes = new ArrayList<ActivityImpl>();// 用以保存后需开始时间相同的节点
			ActivityImpl sameActivityImpl1 = processDefinitionEntity
					.findActivity(historicActivityInstances.get(i + 1).getActivityId());
			// 将后面第一个节点放在时间相同节点的集合里
			sameStartTimeNodes.add(sameActivityImpl1);
			for (int j = i + 1; j < historicActivityInstances.size() - 1; j++) {
				HistoricActivityInstance activityImpl1 = historicActivityInstances.get(j);// 后续第一个节点
				HistoricActivityInstance activityImpl2 = historicActivityInstances.get(j + 1);// 后续第二个节点
				if (activityImpl1.getStartTime().equals(activityImpl2.getStartTime())) {
					// 如果第一个节点和第二个节点开始时间相同保存
					ActivityImpl sameActivityImpl2 = processDefinitionEntity
							.findActivity(activityImpl2.getActivityId());
					sameStartTimeNodes.add(sameActivityImpl2);
				} else {
					// 有不相同跳出循环
					break;
				}
			}
			List<PvmTransition> pvmTransitions = activityImpl.getOutgoingTransitions();// 取出节点的所有出去的线
			for (PvmTransition pvmTransition : pvmTransitions) {
				// 对所有的线进行遍历
				ActivityImpl pvmActivityImpl = (ActivityImpl) pvmTransition.getDestination();
				// 如果取出的线的目标节点存在时间相同的节点里，保存该线的id，进行高亮显示
				if (sameStartTimeNodes.contains(pvmActivityImpl)) {
					highFlows.add(pvmTransition.getId());
				}
			}
		}
		return highFlows;
	}

}
