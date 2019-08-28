package org.jeecg.modules.activiti.service;

import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.task.Task;

public interface ActivitiService {

	void startProcesses(String id, String business_key);

	List<Task> findTasksByUserId(String userId);

	Task findTaskById(String taskId);

	void completeTask(String taskId, String userId, String result);

	void updateBizStatus(DelegateExecution execution, String status);

	void queryProImg(String processInstanceId) throws Exception;

	String queryProHighLighted(String processInstanceId) throws Exception;

}
