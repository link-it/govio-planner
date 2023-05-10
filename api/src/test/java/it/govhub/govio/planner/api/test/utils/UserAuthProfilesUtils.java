/*******************************************************************************
 *  GovIO Planner - Notification system Planner for AppIO
 *  
 *  Copyright (c) 2021-2023 Link.it srl (http://www.link.it).
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 3, as published by
 *  the Free Software Foundation.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *******************************************************************************/
package it.govhub.govio.planner.api.test.utils;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import it.govhub.security.beans.GovhubPrincipal;
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

	public Long getIdUtenzaPrincipal(String principal) {
		return ((GovhubPrincipal) this.userDetailService.loadUserByUsername(principal)).getUser().getId();
	}
}
