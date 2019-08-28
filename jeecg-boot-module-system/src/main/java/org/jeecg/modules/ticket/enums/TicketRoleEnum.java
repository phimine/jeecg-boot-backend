package org.jeecg.modules.ticket.enums;

public enum TicketRoleEnum {
	DRAFTER("drafter", "起草人"),
	REVIEWER("reviewer", "审核人"),
	TAKER("taker", "承办人"),
	MANAGER("manager", "负责人"),
	VP("VP", "副总裁"),
	PRESIDENT("president", "总裁"),
	CHAIRMAN("chairman", "董事长"),
	ADMIN("admin", "管理员"),
	;
	private String code;
	
	private String name;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private TicketRoleEnum(String code, String name) {
		this.code = code;
		this.name = name;
	}

}
