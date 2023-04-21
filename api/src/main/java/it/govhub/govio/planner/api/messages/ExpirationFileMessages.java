package it.govhub.govio.planner.api.messages;

import org.springframework.stereotype.Component;
import it.govhub.govregistry.commons.messages.RestEntityMessageBuilder;

@Component
public class ExpirationFileMessages extends RestEntityMessageBuilder{

	public ExpirationFileMessages() {
		super("ExpirationFile");
	}

}
