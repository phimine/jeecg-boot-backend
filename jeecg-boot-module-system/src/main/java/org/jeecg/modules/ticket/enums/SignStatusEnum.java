package org.jeecg.modules.ticket.enums;

public enum SignStatusEnum {
	STARTED("00", "发起会签"),
	FIRST_SIGNING("10", "第一承办人会签中"),
	SECOND_SIGNING("20", "第二承办人会签中"),
	THIRD_SIGNING("30", "第三承办人会签中"),
	SECOND_REVIEWING("21", "第二承办人汇总中"),
	FIRST_REVIEWING("11", "第一承办人汇总中"),
	MANAGER_REVIEWING("01", "负责人汇总"),
	COMPLETED("40", "会签结束"),
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
	private SignStatusEnum(String code, String message) {
		this.code = code;
		this.message = message;
	}	
}
