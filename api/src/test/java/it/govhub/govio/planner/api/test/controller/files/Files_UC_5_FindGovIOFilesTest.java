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
package it.govhub.govio.planner.api.test.controller.files;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import it.govhub.govio.planner.api.Application;
import it.govhub.govio.planner.api.entity.ExpirationFileEntity;
import it.govhub.govio.planner.api.entity.GovioPlannerFileEntity;
import it.govhub.govio.planner.api.repository.ExpirationFileEntityRepository;
import it.govhub.govio.planner.api.repository.GovioPlannerFileEntityRepository;
import it.govhub.govio.planner.api.test.costanti.Costanti;
import it.govhub.govio.planner.api.test.utils.ExpirationFileUtils;
import it.govhub.govio.planner.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.security.beans.GovhubPrincipal;
import it.govhub.security.services.GovhubUserDetailService;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di lettura dei govio-files")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)

class Files_UC_5_FindGovIOFilesTest {

	private static final String GOVIO_FILES_BASE_PATH = "/v1/govio-files";

	@Value("${govio-planner.filerepository.path}")
	Path fileRepositoryPath;

	@Autowired
	ExpirationFileEntityRepository expirationFileRepository;
	
	@Autowired
	GovioPlannerFileEntityRepository govioFileRepository;

	@Autowired
	private GovhubUserDetailService userDetailService;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;

	@Value("${govio-planner.id}")
	private String planId;

	@BeforeEach
	void setUp() throws Exception{
		govioFileRepository.deleteAll();
		expirationFileRepository.deleteAll();

		UserEntity user = ((GovhubPrincipal) this.userDetailService.loadUserByUsername("user_govio_sender")).getUser();

		ExpirationFileEntity expirationFileEntity1 = ExpirationFileUtils.buildFile(this.fileRepositoryPath, "exp01", user, this.planId);
		expirationFileRepository.save(expirationFileEntity1);
		ExpirationFileEntity expirationFileEntity2 = ExpirationFileUtils.buildFile(this.fileRepositoryPath, "exp02", user, this.planId);
		expirationFileRepository.save(expirationFileEntity2);
		
		GovioPlannerFileEntity govioFileEntity1 = ExpirationFileUtils.buildGovIOFile(this.fileRepositoryPath, "01", expirationFileEntity1);
		govioFileRepository.save(govioFileEntity1);
		GovioPlannerFileEntity govioFileEntity2 = ExpirationFileUtils.buildGovIOFile(this.fileRepositoryPath, "02", expirationFileEntity2);
		govioFileRepository.save(govioFileEntity2);
		
	}
	
	private ExpirationFileEntity getExpirationFileEntity(String filename) {
		List<ExpirationFileEntity> findAll = this.expirationFileRepository.findAll();
		
		return findAll.stream().filter(f -> f.getName().equals(filename)).collect(Collectors.toList()).get(0);
	}

	// 1. findAllOK senza filtri 
	@Test
	void UC_5_01_FindAllOk() throws Exception {
		MvcResult result = this.mockMvc.perform(get(GOVIO_FILES_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();

		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(2, page.getInt("total"));

		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(2, items.size());


		JsonObject item1 = items.getJsonObject(0);
		JsonObject item2 = items.getJsonObject(1);

		assertEquals("02.csv", item1.getString("filename"));
		assertEquals("01.csv", item2.getString("filename"));
	}

	// 02. findAllOk filtro ordinamento per creation_date asc
	@Test
	void UC_5_02_FindAllOk_SortAsc() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_ASC);

		MvcResult result = this.mockMvc.perform(get(GOVIO_FILES_BASE_PATH).params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();

		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(2, page.getInt("total"));

		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(2, items.size());


		JsonObject item1 = items.getJsonObject(0);
		JsonObject item2 = items.getJsonObject(1);

		assertEquals("01.csv", item1.getString("filename"));
		assertEquals("02.csv", item2.getString("filename"));
	}

	@Test
	void UC_5_03_FindAllOk_Limit() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_LIMIT, "1");

		MvcResult result = this.mockMvc.perform(get(GOVIO_FILES_BASE_PATH).params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();

		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(1, page.getInt("limit"));
		assertEquals(2, page.getInt("total"));

		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(1, items.size());

	}

	@Test
	void UC_5_04_FindAllOk_InvalidLimit() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_LIMIT, "XXX");

		this.mockMvc.perform(get(GOVIO_FILES_BASE_PATH).params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.status", is(400)))
		.andExpect(jsonPath("$.title", is("Bad Request")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
		.andReturn();
	}

	@Test
	void UC_5_05_FindAllOk_Offset() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_OFFSET, "1");

		MvcResult result = this.mockMvc.perform(get(GOVIO_FILES_BASE_PATH).params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();

		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(2, page.getInt("total"));

		// Controlli sugli items
		//		JsonArray items = userList.getJsonArray("items");
		//		assertEquals(7, items.size());
	}

	@Test
	void UC_5_06_FindAllOk_InvalidOffset() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_OFFSET, "XXX");

		this.mockMvc.perform(get(GOVIO_FILES_BASE_PATH).params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.status", is(400)))
		.andExpect(jsonPath("$.title", is("Bad Request")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
		.andReturn();
	}

	@Test
	void UC_5_07_FindAllOk_Q() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_Q, "exp01");

		MvcResult result = this.mockMvc.perform(get(GOVIO_FILES_BASE_PATH).params(params)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();

		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(1, page.getInt("total"));

		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(1, items.size());


		JsonObject item1 = items.getJsonObject(0);

		assertEquals("01.csv", item1.getString("filename"));
	}
	
	@Test
	void UC_5_08_FindAllOk_Q() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_Q, "02");

		MvcResult result = this.mockMvc.perform(get(GOVIO_FILES_BASE_PATH).params(params)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();

		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(1, page.getInt("total"));

		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(1, items.size());


		JsonObject item1 = items.getJsonObject(0);

		assertEquals("02.csv", item1.getString("filename"));
	}

	@Test
	void UC_5_09_FindAllOk_ExpirationFileID() throws Exception {
		ExpirationFileEntity expirationFileEntity = getExpirationFileEntity("exp01.csv");
		int expFileId = expirationFileEntity.getId().intValue();
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_EXPIRATION_FILE_ID, expFileId + "");

		MvcResult result = this.mockMvc.perform(get(GOVIO_FILES_BASE_PATH).params(params)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();

		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(1, page.getInt("total"));

		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(1, items.size());

		JsonObject item1 = items.getJsonObject(0);

		assertEquals("01.csv", item1.getString("filename"));
		assertEquals(expFileId, item1.getInt("expiration_file_id"));

	}

}
