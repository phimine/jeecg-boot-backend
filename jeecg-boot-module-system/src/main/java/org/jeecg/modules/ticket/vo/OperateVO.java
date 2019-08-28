package org.jeecg.modules.ticket.vo;

import lombok.Data;

@Data
public class OperateVO {
	
	private String ticketId;
	
	private String takerId;
	
	private String nextTakerIds;
	
	private String variable;
	
	private String comment;

}
