package org.jeecg.modules.ticket.vo;

import java.util.List;

import lombok.Data;

@Data
public class ManagerReviewVO {

	private String ticketId;
	
	private String managerId;
	
	private Boolean pass;
	
	private String comment;
	
	private List<String> departList;
}
