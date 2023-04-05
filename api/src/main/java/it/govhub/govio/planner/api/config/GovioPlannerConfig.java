package it.govhub.govio.planner.api.config;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.govhub.govregistry.commons.config.ApplicationConfig;

@Component
public class GovioPlannerConfig  implements ApplicationConfig {
	
	@Value("${application-id:govio-planner}")
	private String applicationId;
	
	public static final Set<String> DEFAULT_READ_ORGANIZATION_ROLES = Set.of(
				GovioPlannerRoles.GOVIOPLANNER_OPERATOR
			);
	
	public static final Set<String> DEFAULT_READ_SERVICE_ROLES = Set.of(
			GovioPlannerRoles.GOVIOPLANNER_OPERATOR
			);

	
	@Override
	public String getApplicationId() {
		return this.applicationId;
	}

	@Override
	public Set<String> getReadServiceRoles() {
		return DEFAULT_READ_ORGANIZATION_ROLES;
	}

	@Override
	public Set<String> getReadOrganizationRoles() {
		return DEFAULT_READ_SERVICE_ROLES;
	}

}