package org.jeecg.modules.ticket.service;

import java.util.List;
import java.util.Map;

import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.ticket.entity.BisTicket;
import org.jeecg.modules.ticket.vo.ApproveVO;
import org.jeecg.modules.ticket.vo.CounterSignVO;
import org.jeecg.modules.ticket.vo.ManagerAdaptVO;
import org.jeecg.modules.ticket.vo.ManagerReviewVO;
import org.jeecg.modules.ticket.vo.OperateVO;
import org.jeecg.modules.ticket.vo.Outcome;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 业务请示
 * @Author: jeecg-boot
 * @Date:   2019-08-19
 * @Version: V1.0
 */
public interface IBisTicketService extends IService<BisTicket> {
	
	void draft(BisTicket bisTicket);
	
	void submit(BisTicket bisTicket);
	
	void review(String ticketId, String reviewerId, Boolean pass, String comment);

	void managerReview(ManagerReviewVO managerReviewVO);

	@Deprecated
	void assignSign(String ticketId, String managerId, String takerId);

	void sign(CounterSignVO counterSignVO);

	void managerAdapt(ManagerAdaptVO managerAdaptVO);

	void drafterAdapt(BisTicket bisTicket);

	void approve(ApproveVO approveVO);

	List<Outcome> findOutComeListByTaskId(String ticketId, String takerId);

	List<BisTicket> findTaskListByUserId(String userId);

	Map<String, String> getCurrentAction(String ticketId, String takerId);

	void operate(OperateVO operateVO);

	List<SysDepart> getSignDeptList();

	String getSignDeptManagers(String departs);

	List<SysUser> getValidSignTakerList(String ticketId, String takerId);

	Boolean assignNeeded(String ticketId, String takerId);

	List<SysUser> getVPList();
	
}
