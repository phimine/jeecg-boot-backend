package org.jeecg.modules.ticket.vo;

import lombok.Data;

@Data
public class Outcome {
	
	private String name;
	
	private String conditionKey;
	
	private String conditionValue;
	
	public static Outcome defaOutcome() {
		Outcome outcome = new Outcome();
		outcome.setName("提交");
		return outcome;
	}
}
