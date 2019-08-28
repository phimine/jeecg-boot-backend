package org.jeecg.modules.ticket.vo;

import lombok.Data;

@Data
public class ApproveVO {

	private String ticketId;
	
	private String takerId;
	
	private String comment;
	
	private String action;
}
