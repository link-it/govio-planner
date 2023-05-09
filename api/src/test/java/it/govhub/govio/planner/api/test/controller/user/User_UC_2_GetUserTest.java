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
package it.govhub.govio.planner.api.test.controller.user;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import it.govhub.govio.planner.api.Application;
import it.govhub.govio.planner.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.security.beans.GovhubPrincipal;
import it.govhub.security.services.GovhubUserDetailService;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di lettura user")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)

class User_UC_2_GetUserTest {

	private static final String USERS_BASE_PATH = "/v1/users";
	private static final String USERS_BASE_PATH_DETAIL_ID = USERS_BASE_PATH + "/{id}";

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private GovhubUserDetailService userDetailService;
	
	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	private UserEntity leggiUtenteDB(String nome) {
		GovhubPrincipal findAll = (GovhubPrincipal) this.userDetailService.loadUserByUsername(nome);
		return findAll.getUser();
	}
	
	@Test
	void UC_5_01_GetUserOk() throws Exception {
		UserEntity userEntity = leggiUtenteDB("ospite");
		
		long idService1 = userEntity.getId();
		
		MvcResult result = this.mockMvc.perform(get(USERS_BASE_PATH_DETAIL_ID,idService1)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject item = reader.readObject();
		
		assertEquals(userEntity.getEnabled(), Boolean.parseBoolean(item.get("enabled").toString()));
		assertEquals(userEntity.getFullName(), item.getString("full_name"));
		assertEquals(userEntity.getPrincipal(), item.getString("principal"));
		
	}
	
	@Test
	void UC_5_02_GetUser_NotFound() throws Exception {
		int idService1 = 10000;
		
		this.mockMvc.perform(get(USERS_BASE_PATH_DETAIL_ID,idService1)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status", is(404)))
				.andExpect(jsonPath("$.title", is("Not Found")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
	
	@Test	
	void UC_5_03_GetUser_InvalidId() throws Exception {
		String idService1 = "XXX";
		
		this.mockMvc.perform(get(USERS_BASE_PATH_DETAIL_ID,idService1)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
}
