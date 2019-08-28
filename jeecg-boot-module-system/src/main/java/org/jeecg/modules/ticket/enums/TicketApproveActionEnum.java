package org.jeecg.modules.ticket.enums;

public enum TicketApproveActionEnum {
	REQUEST("request", "请示上级"),
	APPROVE("approve", "批准"),
	REFUSE("refuse", "拒绝"),
	FALLBACK("fallback", "退回")
	;
	
	private String action;
	
	private String msg;

	public String getAction() {
		return action;
	}

	public String getMsg() {
		return msg;
	}

	private TicketApproveActionEnum(String action, String msg) {
		this.action = action;
		this.msg = msg;
	}

}
