package com.yupi.yucodemotherbackend.ai.model.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * AI 相应消息
 */
@EqualsAndHashCode
@Data
@NoArgsConstructor
public class AiResponseMessage extends StreamMessage {

	private String data;

	public AiResponseMessage(String data) {
		super(StreamMessageTypeEnum.AI_RESPONSE.getValue());
		this.data = data;
	}
}
