package org.jeecg.modules.ticket.enums;

public enum TicketTakerKeyEnum {
	REVIEWER("reviewer"),
	FIRST("firstTaker"),
	SECOND("secondTaker"),
	THIRD("thirdTaker"),
	
	;
	
	private String key;

	public String getKey() {
		return key;
	}

	private TicketTakerKeyEnum(String key) {
		this.key = key;
	}
	
}
