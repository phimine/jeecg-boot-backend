package org.jeecg.modules.ticket.enums;

public enum TickertStatusEnum {
	DRAFTED("0", "已起草"),
	SUBMITTED("1", "已提交"),
	REVIEWING("2", "提交审核中"),
	REVIEW_FAIL("21", "审核退回"),
	REVIEW_PASS("22", "审核通过"),
	COUNTERSIGNING("3", "会签中"),
	COUNTERSIGNED("4", "会签完成"),
	ADAPTING("5", "等待调整"),
	ADAPTED("52", "已调整"),
	APPROVING("6", "批阅中"),
	APPROVE_FAIL("61", "退回调整"),
	APPROVED("7", "已批准"),
	REFUSED("8", "已拒绝"),
	;
	
	private String code;
	private String message;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	private TickertStatusEnum(String code, String message) {
		this.code = code;
		this.message = message;
	}	
}
