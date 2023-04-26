package it.govhub.govio.planner.api.test.utils;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import it.govhub.security.services.GovhubUserDetailService;

@Component
public class UserAuthProfilesUtils {
	
	@Autowired
	private GovhubUserDetailService userDetailService;

	@Transactional
	public RequestPostProcessor utenzaAdmin() {
		return utenzaPrincipal("amministratore");
	}

	public RequestPostProcessor utenzaGovIOSender() {
	    return utenzaPrincipal("user_govio_sender"); 
	}

	public RequestPostProcessor utenzaOspite() {
	    return utenzaPrincipal("ospite"); 
	}
	
	public RequestPostProcessor utenzaAutorizzataSI() {
	    return utenzaPrincipal("user_govio_sender_si"); 
	}

	public RequestPostProcessor utenzaPrincipal(String principal) {
		return user(this.userDetailService.loadUserByUsername(principal));
	}

}
