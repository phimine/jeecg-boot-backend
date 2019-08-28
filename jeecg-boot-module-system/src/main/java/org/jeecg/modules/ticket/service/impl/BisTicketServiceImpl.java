package org.jeecg.modules.ticket.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.task.Task;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysRole;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.entity.SysUserDepart;
import org.jeecg.modules.system.entity.SysUserRole;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysRoleService;
import org.jeecg.modules.system.service.ISysUserDepartService;
import org.jeecg.modules.system.service.ISysUserRoleService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.ticket.constants.TicketConstants;
import org.jeecg.modules.ticket.entity.BisTicket;
import org.jeecg.modules.ticket.entity.SignRecord;
import org.jeecg.modules.ticket.enums.SignStatusEnum;
import org.jeecg.modules.ticket.enums.TickertStatusEnum;
import org.jeecg.modules.ticket.enums.TicketApproveActionEnum;
import org.jeecg.modules.ticket.enums.TicketRoleEnum;
import org.jeecg.modules.ticket.enums.TicketTakerKeyEnum;
import org.jeecg.modules.ticket.mapper.BisTicketMapper;
import org.jeecg.modules.ticket.service.IBisTicketService;
import org.jeecg.modules.ticket.service.ISignRecordService;
import org.jeecg.modules.ticket.vo.ApproveVO;
import org.jeecg.modules.ticket.vo.CounterSignVO;
import org.jeecg.modules.ticket.vo.ManagerAdaptVO;
import org.jeecg.modules.ticket.vo.ManagerReviewVO;
import org.jeecg.modules.ticket.vo.OperateVO;
import org.jeecg.modules.ticket.vo.Outcome;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.handler.UserRoleAuthorizationInterceptor;

import com.alibaba.druid.sql.ast.statement.SQLIfStatement.Else;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

/**
 * @Description: 业务请示
 * @Author: jeecg-boot
 * @Date:   2019-08-19
 * @Version: V1.0
 */
@Service
@Slf4j
@Transactional
public class BisTicketServiceImpl extends ServiceImpl<BisTicketMapper, BisTicket> implements IBisTicketService {
	
	@Autowired
	private RuntimeService runtimeService;
	@Autowired
	private TaskService taskService;
	@Autowired
	private HistoryService historyService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private ISysUserDepartService sysUserDepartService;
	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	private ISysUserRoleService sysUserRoleService;
	@Autowired
	private ISysRoleService sysRoleService;
	@Autowired
	private ISysDepartService sysDepartService;
	@Autowired
	private ISignRecordService signRecordService;
	
	@Value("${activiti.workflow}")
	private String workflow;
	
	private static final Map<String, String> nextTakerKeyMapping = new HashMap<String, String>() {
		{
			put(TicketConstants.ACTION_SUBMIT, TicketConstants.PARAM_REVIEWER);
			put(TicketConstants.ACTION_REVIEW, TicketConstants.PARAM_MANAGER);
			put(TicketConstants.ACTION_MANAGER_REVIEW, TicketConstants.PARAM_ASSIGNEE_LIST);
			put(TicketConstants.ACITON_ASSIGN_TASK, TicketConstants.PARAM_FIRST);
			put(TicketConstants.ACTION_FIRST_SIGN, TicketConstants.PARAM_SECOND);
			put(TicketConstants.ACTION_SECOND_SIGN, TicketConstants.PARAM_THIRD);
			put(TicketConstants.ACTION_MANAGER_ADAPT, TicketConstants.PARAM_VP);
			put(TicketConstants.ACTION_VP_APPROVE, TicketConstants.PARAM_PRESIDENT);
			put(TicketConstants.ACTION_PRESIDENT_APPROVE, TicketConstants.PARAM_CHAIRMAN);
		}
	};
	
	private static final Map<String, String> taskTicketStatusMapping = new HashMap<String, String>() {
		{
			put(TicketConstants.ACTION_SUBMIT, TickertStatusEnum.DRAFTED.getCode());
			put(TicketConstants.ACTION_REVIEW, TickertStatusEnum.REVIEWING.getCode());
			put(TicketConstants.ACTION_MANAGER_REVIEW, TickertStatusEnum.REVIEW_PASS.getCode());
			put(TicketConstants.ACITON_ASSIGN_TASK, TickertStatusEnum.COUNTERSIGNING.getCode());
			put(TicketConstants.ACTION_FIRST_SIGN, TickertStatusEnum.COUNTERSIGNING.getCode());
			put(TicketConstants.ACTION_SECOND_SIGN, TickertStatusEnum.COUNTERSIGNING.getCode());
			put(TicketConstants.ACTION_THIRD_SIGN, TickertStatusEnum.COUNTERSIGNING.getCode());
			put(TicketConstants.ACTION_MANAGER_ADAPT, TickertStatusEnum.COUNTERSIGNED.getCode());
			put(TicketConstants.ACTION_ADAPT, TickertStatusEnum.ADAPTING.getCode());
			put(TicketConstants.ACTION_VP_APPROVE, TickertStatusEnum.APPROVING.getCode());
			put(TicketConstants.ACTION_PRESIDENT_APPROVE, TickertStatusEnum.APPROVING.getCode());
			put(TicketConstants.ACTION_CHARIMAN_APPROVE, TickertStatusEnum.APPROVING.getCode());
			
		}
	};

	@Override
 	public void draft(BisTicket bisTicket) {
		// 1. 获取起草人信息
		String drafterId = bisTicket.getDrafterId();
		
		// 2. 保存
		bisTicket.setStatus(TickertStatusEnum.DRAFTED.getCode());
		bisTicket.setCreateTime(new Date());
		bisTicket.setUpdateTime(new Date());
		boolean saved = save(bisTicket);
		
		// 3. 启动流程
		if (saved) {
			Map<String, Object> variables = new HashMap<String, Object>();
			variables.put("drafter", drafterId);
			runtimeService.startProcessInstanceByKey(workflow, bisTicket.getId(), variables);
		} else {
			log.error("业务请示保存失败：{}", JSON.toJSONString(bisTicket));
		}	
	}

	@Override
	public void submit(BisTicket bisTicket) {
		// 1. 获取审核人信息
		String drafterId = bisTicket.getDrafterId();
		List<SysUserDepart> list = sysUserDepartService.getDepartsOfUser(drafterId);
		String departId = list.get(0).getDepId();
		
		List<SysUser> reviewers = sysUserService.getUserByDeptAndRole(departId, TicketRoleEnum.REVIEWER.getCode());
		if (null == reviewers || reviewers.isEmpty()) {
			log.error("部门审核人缺失");
			throw new RuntimeException("部门审核人缺失");
		}
		SysUser reviewer = reviewers.get(0);
		
		// 2. 提交任务
		if (null == bisTicket.getId()) {
			// 不存在等待提交的任务，则先执行起草任务
			if (0 == taskService.createTaskQuery().processDefinitionKey(workflow).taskAssignee(drafterId).taskName("提交").count()) {
				draft(bisTicket);
			} else {
				// TODO 用户存在等待提交的任务时，允许再次起草新请求吗?
			}
		}
		Task draftTask = taskService.createTaskQuery().processDefinitionKey(workflow)
			.processInstanceBusinessKey(bisTicket.getId().toString())
			.taskCandidateOrAssigned(drafterId)
			.singleResult();
		if (null == draftTask) {
			log.error("不存在待提交的任务, assignee:{}, businessKey:{}", sysUserService.getById(drafterId).getRealname(), bisTicket.getId());
			throw new RuntimeException("不存在待提交的任务");
		}
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("reviewer", reviewer.getId().toString());
		taskService.complete(draftTask.getId(), variables);
		
		// 3. 保存
		bisTicket.setStatus(TickertStatusEnum.REVIEWING.getCode());
		bisTicket.setUpdateTime(new Date());
		this.saveOrUpdate(bisTicket);
	}
	
	@Override
	public void review(String ticketId, String reviewerId, Boolean pass, String comment) {
		// 1. 获取负责人信息
		List<SysUserDepart> list = sysUserDepartService.getDepartsOfUser(reviewerId);
		String departId = list.get(0).getDepId();
		
		List<SysUser> managers = sysUserService.getUserByDeptAndRole(departId, TicketRoleEnum.MANAGER.getCode());
		if (null == managers || managers.isEmpty()) {
			log.error("部门负责人缺失");
			throw new RuntimeException("部门负责人缺失");
		}
		SysUser manager = managers.get(0);
		
		// 2. 获取任务并处理
		Task reviewTask = taskService.createTaskQuery().processDefinitionKey(workflow)
				.taskCandidateOrAssigned(reviewerId)
				.processInstanceBusinessKey(ticketId)
				.singleResult();
		if (null == reviewTask) {
			log.error("不存在待审核的任务, assignee:{}, businessKey:{}", sysUserService.getById(reviewerId).getRealname(), ticketId);
			throw new RuntimeException("不存在待审核的任务");
		}
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("pass", pass);
		if (pass) {
			variables.put("manager", manager.getId().toString());
		}
		taskService.addComment(reviewTask.getId(), reviewTask.getProcessInstanceId(), comment);
		taskService.complete(reviewTask.getId(), variables);
		
		// 3. 更新数据库
		BisTicket bisTicket = this.getById(ticketId);
		bisTicket.setStatus(pass ? TickertStatusEnum.REVIEW_PASS.getCode() : TickertStatusEnum.REVIEW_FAIL.getCode());
		bisTicket.setUpdateTime(new Date());
		saveOrUpdate(bisTicket);
	}

	@Override
	public void managerReview(ManagerReviewVO managerReviewVO) {
		String ticketId = managerReviewVO.getTicketId();
		String managerId = managerReviewVO.getManagerId();
		Boolean pass = managerReviewVO.getPass();
		String comment = managerReviewVO.getComment();
		List<String> departList = managerReviewVO.getDepartList();
		
		// 1. 获取任务
		Task managerTask = taskService.createTaskQuery().processDefinitionKey(workflow)
				.taskCandidateOrAssigned(managerId)
				.processInstanceBusinessKey(ticketId)
				.singleResult();
		if (null == managerTask) {
			log.error("不存在部门负责人待审核的任务, assignee:{}, businessKey:{}", sysUserService.getById(managerId).getRealname(), ticketId);
			throw new RuntimeException("不存在部门负责人待审核的任务");
		}
		
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("pass", pass);
		if (pass) {
			// 2. 根据部门编号获取各个负责人
			List<String> managers = sysUserService.getUserIdsByDeptsAndRole(departList, TicketRoleEnum.MANAGER.getCode());
			variables.put("assigneeList", managers);
		}
		taskService.addComment(managerTask.getId(), managerTask.getProcessInstanceId(), comment);
		taskService.complete(managerTask.getId(), variables);
		
		// 3. 更新数据库
		BisTicket bisTicket = this.getById(ticketId);
		bisTicket.setStatus(pass ? TickertStatusEnum.COUNTERSIGNING.getCode() : TickertStatusEnum.REVIEWING.getCode());
		bisTicket.setUpdateTime(new Date());
		saveOrUpdate(bisTicket);
	}

	@Deprecated
	@Override
	public void assignSign(String ticketId, String managerId, String takerId) {
		// 1. 获取任务
		Task signTask = taskService.createTaskQuery().processDefinitionKey(workflow)
				.taskAssignee(managerId)
				.processInstanceBusinessKey(ticketId)
				.singleResult();
		if (null == signTask) {
			log.error("不存在部门负责人待指派审核的会签任务, assignee:{}, businessKey:{}", sysUserService.getById(managerId).getRealname(), ticketId);
			throw new RuntimeException("不存在部门负责人待指派审核的会签任务");
		}
		
		// 2. 指派会签承办人
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("firstTaker", takerId);
		variables.put("done", false);
		taskService.complete(signTask.getId(), variables);
	}
	
	@Override
	public void sign(CounterSignVO counterSignVO) {
		String ticketId = counterSignVO.getTicketId();
		String takerId = counterSignVO.getTakerId();
		String nextTakerId = counterSignVO.getNextTakerId();
		boolean done = counterSignVO.getDone();
		String comment = counterSignVO.getComment();
		
		// 1. 获取任务
		Task signTask = taskService.createTaskQuery().processDefinitionKey(workflow)
				.taskCandidateOrAssigned(takerId)
				.processInstanceBusinessKey(ticketId)
				.singleResult();
		if (null == signTask) {
			log.error("不存在待处理的会签任务, assignee:{}, businessKey:{}", sysUserService.getById(takerId).getRealname(), ticketId);
			throw new RuntimeException("不存在待处理的会签任务");
		}
		
		String currentAction = runtimeService.createExecutionQuery().processDefinitionKey(workflow)
			.executionId(signTask.getExecutionId())
			.singleResult().getActivityId();
		String takerKey = this.getNextTakerVariableKey(currentAction);
		
		// 2. 指派会签承办人
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("done", done);
		if (!done) {
			if (!StringUtils.isEmpty(takerKey)) {
				variables.put(takerKey, nextTakerId);
			}
			if (!StringUtils.isEmpty(comment)) {
				taskService.addComment(signTask.getId(), signTask.getProcessInstanceId(), comment);
			}
		}
		taskService.complete(signTask.getId(), variables);
		
		// 3. 会签结束，更新数据库
		if (taskService.createTaskQuery().processDefinitionKey(workflow)
					.taskDefinitionKey("managerAdapt").count() == 1) {
			BisTicket bisTicket = this.getById(ticketId);
			bisTicket.setStatus(TickertStatusEnum.COUNTERSIGNED.getCode());
			bisTicket.setUpdateTime(new Date());
			saveOrUpdate(bisTicket);
		}
	
	}
	
	@Override
	public void managerAdapt(ManagerAdaptVO managerAdaptVO) {
		String ticketId =managerAdaptVO.getTicketId();
		String managerId = managerAdaptVO.getManagerId();
		boolean pass = managerAdaptVO.getPass();
		String comment = managerAdaptVO.getComment();
		String vpId = managerAdaptVO.getVpId();
		
		// 1. 获取任务并处理
		Task adaptTask = taskService.createTaskQuery().processDefinitionKey(workflow)
				.taskCandidateOrAssigned(managerId)
				.processInstanceBusinessKey(ticketId)
				.singleResult();
		if (null == adaptTask) {
			log.error("不存在待处理的调整任务, assignee:{}, businessKey:{}", sysUserService.getById(managerId).getRealname(), ticketId);
			throw new RuntimeException("不存在待处理的调整任务");
		}
		
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("pass", pass);
		if (pass) {
			variables.put("vp", vpId);
		}
		
		if (!StringUtils.isEmpty(comment)) {
			taskService.addComment(adaptTask.getId(), adaptTask.getProcessInstanceId(), comment);
		}
		taskService.complete(adaptTask.getId(), variables);
		
		// 2. 更新数据库
		BisTicket bisTicket = this.getById(ticketId);
		bisTicket.setStatus(pass ? TickertStatusEnum.APPROVING.getCode() : TickertStatusEnum.ADAPTING.getCode());
		bisTicket.setUpdateTime(new Date());
		this.saveOrUpdate(bisTicket);
	}
	
	@Override
	public void drafterAdapt(BisTicket bisTicket) {
		boolean hasChange = saveOrUpdate(bisTicket);
		if (!hasChange) {
			log.error("内容没有修改痕迹");
			throw new RuntimeException("内容没有修改痕迹");
		}
		
		String drafterId = bisTicket.getDrafterId();
		String ticketId = bisTicket.getId();
		
		// 1. 获取任务并处理
		Task adaptTask = taskService.createTaskQuery().processDefinitionKey(workflow)
				.taskCandidateOrAssigned(drafterId)
				.processInstanceBusinessKey(ticketId)
				.singleResult();
		if (null == adaptTask) {
			log.error("不存在待处理的调整任务, assignee:{}, businessKey:{}", sysUserService.getById(drafterId).getRealname(), ticketId);
			throw new RuntimeException("不存在待处理的调整任务");
		}

		taskService.complete(adaptTask.getId());
		
		// 2. 更新数据库
		bisTicket.setStatus(TickertStatusEnum.ADAPTED.getCode());
		bisTicket.setUpdateTime(new Date());
		saveOrUpdate(bisTicket);
	}
	
	@Override
	public void approve(ApproveVO approveVO) {
		String ticketId = approveVO.getTicketId();
		String takerId = approveVO.getTakerId();
		String comment = approveVO.getComment();
		String action = approveVO.getAction();
		
		// 1. 获取任务并处理
		Task adaptTask = taskService.createTaskQuery().processDefinitionKey(workflow)
				.taskCandidateOrAssigned(takerId)
				.processInstanceBusinessKey(ticketId)
				.singleResult();
		if (null == adaptTask) {
			log.error("不存在待处理的批阅任务, assignee:{}, businessKey:{}", sysUserService.getById(takerId).getRealname(), ticketId);
			throw new RuntimeException("不存在待处理的批阅任务");
		}
		
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("action", action);
		
		// 指定下个批阅者
		String currentAction = runtimeService.createExecutionQuery().processDefinitionKey(workflow)
				.executionId(adaptTask.getExecutionId())
				.singleResult().getActivityId();
		if (TicketApproveActionEnum.REQUEST.getAction().equals(action)) {
			if (currentAction.equals("vpApprove")) {
				variables.put("president", sysUserService.getPresident().getId());
			} else if (currentAction.equals("presidentApprove")) {
				variables.put("chairman", sysUserService.getChairman().getId());
			}
		}

		if (!StringUtils.isEmpty(comment)) {
			taskService.addComment(adaptTask.getId(), adaptTask.getProcessInstanceId(), comment);
		}
		taskService.complete(adaptTask.getId(), variables);
		
		// 2. 更新数据库
		BisTicket bisTicket = this.getById(ticketId);
		bisTicket.setUpdateTime(new Date());
		if (TicketApproveActionEnum.APPROVE.getAction().equals(action)) {
			bisTicket.setStatus(TickertStatusEnum.APPROVED.getCode());
		} else if (TicketApproveActionEnum.REFUSE.getAction().equals(action)) {
			bisTicket.setStatus(TickertStatusEnum.REFUSED.getCode());
		} else if (TicketApproveActionEnum.FALLBACK.getAction().equals(action)) {
			bisTicket.setStatus(TickertStatusEnum.APPROVE_FAIL.getCode());
		}
		this.saveOrUpdate(bisTicket);
	}
	
	/**
     * 获取当前任务完成之后的连线走向，在前台展示
     */
    @Override
    public List<Outcome> findOutComeListByTaskId(String ticketId, String takerId) {
        ArrayList<Outcome> outComeList = new ArrayList<>();
        // 通过任务id获取任务对象
        Task task = taskService.createTaskQuery().processDefinitionKey(workflow)
        		.processInstanceBusinessKey(ticketId)
        		.taskCandidateOrAssigned(takerId)
        		.singleResult();
        // Task ==> 流程定义ID ==> 流程定义实例（对应.bpmn文件）
        String processDefinitionId = task.getProcessDefinitionId();
        ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(processDefinitionId);

        // Task ==> 执行实例ID ==> 执行实例 ==> 当前活动ID ==> 活动对象
        String executionId = task.getExecutionId();
        Execution execution = runtimeService.createExecutionQuery().processDefinitionKey(workflow)
        		.executionId(executionId).singleResult();
        String activityId = execution.getActivityId();
        ActivityImpl activity = processDefinitionEntity.findActivity(activityId);
        
        List<String> execlusiveList = this.getExeclusive(ticketId, takerId);
        
        // 获取当前活动结束后的连线名称
        List<PvmTransition> list = activity.getOutgoingTransitions();
        if (null == list || list.isEmpty()) {
        	log.error("后续没有有效的连线！");
        	throw new RuntimeException("后续没有有效的连线！");
        }
        // 排它网关情况
        if (list.size() == 1 && list.get(0).getDestination().isExclusive()) {
        	list = list.get(0).getDestination().getOutgoingTransitions();
        }
        for (PvmTransition pvmTransition : list) {
        	Outcome outcome = new Outcome();
            // 获取连线名称、conditionText
            String name = (String) pvmTransition.getProperty("name");
            outcome.setName(name);
            String condition = (String) pvmTransition.getProperty("conditionText");
			if (!StringUtils.isEmpty(condition)) {
				String conditionText = condition.toString().trim()
						.replace("'", "").replace("${", "").replace("}", "");
				String[] couple = conditionText.split("==");
				if (execlusiveList.contains(couple[1].trim())) {
					// 排除的连线
					continue;
				}
				outcome.setConditionKey(couple[0].trim());
				outcome.setConditionValue(couple[1].trim());
			}
			if (!StringUtils.isEmpty(name)) {
                outComeList.add(outcome);
            } else {
            	// 一般不会有这种情况
                outComeList.add(Outcome.defaOutcome());
            }
        }
        return outComeList;
    }
	
	private String getNextTakerVariableKey(String currentAction) {
		return nextTakerKeyMapping.get(currentAction);
	}

	@Override
	public List<BisTicket> findTaskListByUserId(String userId) {
		// 1. 根据assign获取businessKey List
		List<Task> taskList = taskService.createTaskQuery().processDefinitionKey(workflow)
				.taskAssignee(userId).active().list();
		
		
		if (null == taskList || taskList.isEmpty()) {
			log.info("{}没有需要处理的任务。", userId);
			return null;
		}
		
		// 2. 根据businessKey获取BisTicket
		List<BisTicket> tickets = new ArrayList<BisTicket>();
		for (Task task : taskList) {
			String processInstanceId = task.getProcessInstanceId();
			String key = runtimeService.createProcessInstanceQuery().processDefinitionKey(workflow)
							.processInstanceId(processInstanceId).singleResult()
							.getBusinessKey();
			tickets.add(this.getById(key));
		}
		return tickets;
	}

	@Override
	public Map<String, String> getCurrentAction(String ticketId, String takerId) {
		Task task = taskService.createTaskQuery().processDefinitionKey(workflow)
						.processInstanceBusinessKey(ticketId).taskAssignee(takerId).singleResult();
		
		Map<String, String> actionMap = new HashMap<String, String>();
		actionMap.put("actionKey", task.getTaskDefinitionKey());
		actionMap.put("actionName", task.getName());

		return actionMap;
	}

	@Override
	public void operate(OperateVO operateVO) {
		String ticketId = operateVO.getTicketId();
		String takerId = operateVO.getTakerId();
		String comment = operateVO.getComment();
		String variable = operateVO.getVariable();
		String nextTakerIds = operateVO.getNextTakerIds();
		
		// 1. 获取任务
		Task task = taskService.createTaskQuery().processDefinitionKey(workflow)
				.taskCandidateOrAssigned(takerId)
				.processInstanceBusinessKey(ticketId)
				.singleResult();
		if (null == task) {
			log.error("不存在待处理的任务, assignee:{}, businessKey:{}", sysUserService.getById(takerId).getRealname(), ticketId);
			throw new RuntimeException("不存在待处理的任务");
		}
		
		String currentAct = task.getTaskDefinitionKey();
		Map<String, Object> variables = this.encaseVariables(currentAct, operateVO);
		
		if (!StringUtils.isEmpty(comment)) {
			taskService.addComment(task.getId(), task.getProcessInstanceId(), comment);
		}
		
		taskService.complete(task.getId(), variables);
		
		// 2. 更新数据库
		BisTicket bisTicket = this.getById(ticketId);
		bisTicket.setUpdateTime(new Date());
		String status = this.getUpdateStatus(currentAct, variable);
		if (!StringUtils.isEmpty(status)) {
			bisTicket.setStatus(status);
		}
		this.saveOrUpdate(bisTicket);
		
		// 3. 会签操作额外更新数据库
		// 发起会签操作时，新增会签记录
		if (currentAct.equals(TicketConstants.ACTION_MANAGER_REVIEW) 
				&& Boolean.parseBoolean(variable)) {
			List<Task> signTaskList = taskService.createTaskQuery().processDefinitionKey(workflow)
								.processInstanceId(task.getProcessInstanceId()).list();
			if (null == signTaskList || signTaskList.isEmpty()) {
				// TODO 没有会签任务
			}
			for (Task signTask : signTaskList) {
				SignRecord record = new SignRecord();
				record.setTicketId(ticketId);
				record.setTaskId(task.getId());
				record.setExecutionId(signTask.getExecutionId());
				record.setTakers(signTask.getAssignee());
				record.setStatus(SignStatusEnum.STARTED.getCode());
				record.setCreateTime(new Date());
				record.setUpdateTime(new Date());
				signRecordService.saveOrUpdate(record);
			}
			
		} else if (this.isSignAction(currentAct)) {
			// 执行会签任务时，更新会签记录状态
			String executionId = task.getExecutionId();
			LambdaQueryWrapper<SignRecord> queryWrapper = new LambdaQueryWrapper<SignRecord>();
			queryWrapper.eq(SignRecord::getExecutionId, executionId);
			SignRecord signRecord = signRecordService.getOne(queryWrapper);
			
			signRecord.setStatus(this.getSignStatus(currentAct, Boolean.parseBoolean(variable)));
			
			// 设置承办人
			if (!StringUtils.isEmpty(nextTakerIds)) {
				String old_takers = signRecord.getTakers();
				signRecord.setTakers(old_takers + "," + nextTakerIds);
			}
			
			signRecord.setUpdateTime(new Date());
			signRecordService.saveOrUpdate(signRecord);
		}
	}
	
	private String getSignStatus(String currentAct, boolean done) {
		String status = null;
		
		switch (currentAct) {
			case TicketConstants.ACITON_ASSIGN_TASK:
				status = done ? SignStatusEnum.COMPLETED.getCode() : SignStatusEnum.FIRST_SIGNING.getCode();
				break;
			case TicketConstants.ACTION_FIRST_SIGN:
				status = done ? SignStatusEnum.MANAGER_REVIEWING.getCode() : SignStatusEnum.SECOND_SIGNING.getCode();
				break;
			case TicketConstants.ACTION_SECOND_SIGN:
				status = done ? SignStatusEnum.FIRST_REVIEWING.getCode() : SignStatusEnum.THIRD_SIGNING.getCode();
				break;
			case TicketConstants.ACTION_THIRD_SIGN:
				status = SignStatusEnum.SECOND_REVIEWING.getCode();
				break;
			default:
				break;
		}
		
		return status;
	}
	
	private boolean isSignAction(String currentAct) {
		return TicketConstants.ACITON_ASSIGN_TASK.equals(currentAct)
				|| TicketConstants.ACTION_FIRST_SIGN.equals(currentAct)
				|| TicketConstants.ACTION_SECOND_SIGN.equals(currentAct)
				|| TicketConstants.ACTION_THIRD_SIGN.equals(currentAct);
	}
	
	private Map<String, Object> encaseVariables(String currentAct, OperateVO operateVO){
		String nextTakerIds = operateVO.getNextTakerIds();
		String variable = operateVO.getVariable();
		String takerId = operateVO.getTakerId();
		
		Map<String, Object> variables = new HashMap<String, Object>();
		String takerKey = this.getNextTakerVariableKey(currentAct);
		if (!StringUtils.isEmpty(takerKey)) {
			if (takerKey.equals(TicketConstants.PARAM_REVIEWER)) {
				variables.put(takerKey, getReviewer(takerId).getId());
			} else if (takerKey.equals(TicketConstants.PARAM_MANAGER)) {
				variables.put(takerKey, getManager(takerId).getId());
			} else if (takerKey.equals(TicketConstants.PARAM_ASSIGNEE_LIST) && !StringUtils.isEmpty(nextTakerIds)) {
				variables.put(takerKey, Arrays.asList(nextTakerIds.split(",")));
			} else {
				variables.put(takerKey, nextTakerIds);
			}
		}
		
		switch (currentAct) {
			case TicketConstants.ACTION_REVIEW:
			case TicketConstants.ACTION_MANAGER_REVIEW:
			case TicketConstants.ACTION_MANAGER_ADAPT:
				variables.put("pass", Boolean.parseBoolean(variable));
				break;
			case TicketConstants.ACITON_ASSIGN_TASK:
			case TicketConstants.ACTION_FIRST_SIGN:
			case TicketConstants.ACTION_SECOND_SIGN:
				variables.put("done", Boolean.parseBoolean(variable));
				break;
			case TicketConstants.ACTION_VP_APPROVE:
			case TicketConstants.ACTION_PRESIDENT_APPROVE:
			case TicketConstants.ACTION_CHARIMAN_APPROVE:
				variables.put("action", variable);	
				if (TicketApproveActionEnum.REQUEST.getAction().equals(variable)) {
					if (currentAct.equals(TicketConstants.ACTION_VP_APPROVE)) {
						variables.put("president", sysUserService.getPresident().getId());
					} else if (currentAct.equals(TicketConstants.ACTION_PRESIDENT_APPROVE)) {
						variables.put("chairman", sysUserService.getChairman().getId());
					}
				}
				break;
			default:
				break;
		}
		return variables;
	}
	
	private SysUser getReviewer(String drafterId) {
		List<SysUserDepart> list = sysUserDepartService.getDepartsOfUser(drafterId);
		String departId = list.get(0).getDepId();
		
		List<SysUser> reviewers = sysUserService.getUserByDeptAndRole(departId, TicketRoleEnum.REVIEWER.getCode());
		if (null == reviewers || reviewers.isEmpty()) {
			log.error("部门审核人缺失");
			throw new RuntimeException("部门审核人缺失");
		}
		SysUser reviewer = reviewers.get(0);
		
		return reviewer;
	}
	
	private SysUser getManager(String takerId) {
		List<SysUserDepart> list = sysUserDepartService.getDepartsOfUser(takerId);
		String departId = list.get(0).getDepId();
		
		List<SysUser> managers = sysUserService.getUserByDeptAndRole(departId, TicketRoleEnum.MANAGER.getCode());
		if (null == managers || managers.isEmpty()) {
			log.error("部门负责人缺失");
			throw new RuntimeException("部门负责人缺失");
		}
		SysUser manager = managers.get(0);
		
		return manager;
	}

	private String getUpdateStatus(String currentAct, String variable) {
		String status = null;
		
		switch (currentAct) {
			case TicketConstants.ACTION_SUBMIT:
				status = TickertStatusEnum.SUBMITTED.getCode();
				break;
			case TicketConstants.ACTION_REVIEW:
				if (Boolean.parseBoolean(variable)) {
					status = TickertStatusEnum.REVIEW_PASS.getCode();
				} else {
					status = TickertStatusEnum.REVIEW_FAIL.getCode();
				}
				break;
			case TicketConstants.ACTION_MANAGER_REVIEW:
				if (Boolean.parseBoolean(variable)) {
					status = TickertStatusEnum.COUNTERSIGNING.getCode();
				} else {
					status = TickertStatusEnum.REVIEW_FAIL.getCode();
				}
				break;
			case TicketConstants.ACITON_ASSIGN_TASK:
				if (Boolean.parseBoolean(variable)) {
					status = TickertStatusEnum.COUNTERSIGNED.getCode();
				} else {
					status = TickertStatusEnum.COUNTERSIGNING.getCode();
				}
				break;
			case TicketConstants.ACTION_FIRST_SIGN:
			case TicketConstants.ACTION_SECOND_SIGN:
			case TicketConstants.ACTION_THIRD_SIGN:
				status = TickertStatusEnum.COUNTERSIGNING.getCode();
				break;
			case TicketConstants.ACTION_MANAGER_ADAPT:
				if (Boolean.parseBoolean(variable)) {
					status = TickertStatusEnum.APPROVING.getCode();
				} else {
					status = TickertStatusEnum.ADAPTING.getCode();
				}
				break;
			case TicketConstants.ACTION_ADAPT:
				status = TickertStatusEnum.ADAPTED.getCode();
				break;
			case TicketConstants.ACTION_VP_APPROVE:
			case TicketConstants.ACTION_PRESIDENT_APPROVE:
			case TicketConstants.ACTION_CHARIMAN_APPROVE:
				if (TicketApproveActionEnum.APPROVE.getAction().equals(variable)) {
					status = TickertStatusEnum.APPROVED.getCode();
				} else if (TicketApproveActionEnum.REFUSE.getAction().equals(variable)) {
					status = TickertStatusEnum.REFUSED.getCode();
				} else if (TicketApproveActionEnum.REQUEST.getAction().equals(variable)) {
					status = TickertStatusEnum.APPROVING.getCode();
				} else if (TicketApproveActionEnum.FALLBACK.getAction().equals(variable)) {
					status = TickertStatusEnum.APPROVE_FAIL.getCode();
				}
				break;
			default:
				break;
		}
		
		return status;
	}

	
	@Override
	public List<SysDepart> getSignDeptList() {
		List<String> signDeparts = new ArrayList<String>() {
			{
				add("业务一部");
				add("业务二部");
				add("业务三部");
				add("业务四部");
				add("业务五部");
			}
		};
		LambdaQueryWrapper<SysDepart> queryDep = new LambdaQueryWrapper<SysDepart>();
		queryDep.in(SysDepart::getDepartName, signDeparts);
		queryDep.orderByAsc(SysDepart::getDepartName);
		return sysDepartService.list(queryDep);
	}

	@Override
	public String getSignDeptManagers(String departs) {
		List<String> managers = sysUserService.getUserIdsByDeptsAndRole(Arrays.asList(departs.split(","))
				, TicketRoleEnum.MANAGER.getCode());
		return String.join(",", managers);
	}

	@Override
	public List<SysUser> getValidSignTakerList(String ticketId, String takerId) {
		// 1. takerId ==> departId ==> users
		List<SysUserDepart> departsOfUser = sysUserDepartService.getDepartsOfUser(takerId);
		String departId = departsOfUser.get(0).getDepId();
		LambdaQueryWrapper<SysUserDepart> queryWrapper = new LambdaQueryWrapper<SysUserDepart>();
		queryWrapper.eq(SysUserDepart::getDepId, departId);
		List<SysUserDepart> userDeparts = sysUserDepartService.list(queryWrapper);
		
		// 2. ticketId + takerId ==> signRecord ==> assignees
		SignRecord signRecord = findByTicketIdAndTakerId(ticketId, takerId);
		if (null == signRecord) {
			return null;
		}
		List<String> takers = Arrays.asList(signRecord.getTakers().split(","));
		
		// 3. 
		List<SysUser> userList = new ArrayList<SysUser>();
		for (SysUserDepart userDepart : userDeparts) {
			if (!takers.contains(userDepart.getUserId())) {
				userList.add(sysUserService.getById(userDepart.getUserId()));
			}
		}
		
		return userList;
	}
	
	@Override
	public Boolean assignNeeded(String ticketId, String takerId) {
		SignRecord signRecord = findByTicketIdAndTakerId(ticketId, takerId);
		if (null == signRecord) {
			return true;
		}
		
		String status = signRecord.getStatus();
		
		return SignStatusEnum.STARTED.getCode().equals(status) 
				|| SignStatusEnum.FIRST_SIGNING.getCode().equals(status)
				|| SignStatusEnum.SECOND_SIGNING.getCode().equals(status);
	}
	
	private List<String> getExeclusive(String ticketId, String takerId) {
		List<String> execlusiveList = new ArrayList<String>();
		SignRecord signRecord = findByTicketIdAndTakerId(ticketId, takerId);
		if (null != signRecord) {
			String status = signRecord.getStatus();
			
			if (SignStatusEnum.STARTED.getCode().equals(status) 
					|| SignStatusEnum.FIRST_SIGNING.getCode().equals(status)
					|| SignStatusEnum.SECOND_SIGNING.getCode().equals(status)) {
				execlusiveList.add("true");
			} else if (SignStatusEnum.MANAGER_REVIEWING.getCode().equals(status)
					|| SignStatusEnum.FIRST_REVIEWING.getCode().equals(status)
					|| SignStatusEnum.SECOND_REVIEWING.getCode().equals(status)) {
				execlusiveList.add("false");
			}
		}
		
		return execlusiveList;
	}
	
	private SignRecord findByTicketIdAndTakerId(String ticketId, String takerId) {
		LambdaQueryWrapper<SignRecord> querySignRecord = new LambdaQueryWrapper<SignRecord>();
		querySignRecord.eq(SignRecord::getTicketId, ticketId);
		querySignRecord.like(SignRecord::getTakers, takerId);
		SignRecord signRecord = signRecordService.getOne(querySignRecord);
		
		return signRecord;
	}

	@Override
	public List<SysUser> getVPList() {
		LambdaQueryWrapper<SysRole> queryRole = new LambdaQueryWrapper<SysRole>();
		queryRole.eq(SysRole::getRoleCode, "VP");
		SysRole vpRole = sysRoleService.getOne(queryRole);
		
		LambdaQueryWrapper<SysUserRole> queryUserRole = new LambdaQueryWrapper<SysUserRole>();
		queryUserRole.eq(SysUserRole::getRoleId, vpRole.getId());
		List<SysUserRole> userRoleList = sysUserRoleService.list(queryUserRole);
		
		List<SysUser> userList = new ArrayList<SysUser>();
		if (null == userRoleList || userRoleList.isEmpty()) {
			return null;
		}
		for (SysUserRole userRole : userRoleList) {
			userList.add(sysUserService.getById(userRole.getUserId()));
		}
		
		return userList;
	}
	
}
