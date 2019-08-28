package org.jeecg.modules.ticket.vo;

import lombok.Data;

@Data
public class CounterSignVO {
	
	private String ticketId;
	
	private String takerId;
	
	private String nextTakerId;
	
	private Boolean done = Boolean.FALSE;
	
	private String comment;

}
