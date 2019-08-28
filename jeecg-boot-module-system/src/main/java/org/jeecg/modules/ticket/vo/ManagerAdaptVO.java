package org.jeecg.modules.ticket.vo;

import lombok.Data;

@Data
public class ManagerAdaptVO {

	private String ticketId;
	
	private String managerId;
	
	private String vpId;
	
	private Boolean pass;
	
	private String comment;
}
