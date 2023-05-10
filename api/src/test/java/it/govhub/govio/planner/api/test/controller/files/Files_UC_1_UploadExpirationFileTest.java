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

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import it.govhub.govio.planner.api.Application;
import it.govhub.govio.planner.api.entity.ExpirationFileEntity;
import it.govhub.govio.planner.api.repository.ExpirationFileEntityRepository;
import it.govhub.govio.planner.api.test.costanti.Costanti;
import it.govhub.govio.planner.api.test.utils.MultipartUtils;
import it.govhub.govio.planner.api.test.utils.UserAuthProfilesUtils;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di caricamento dei files")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)

class Files_UC_1_UploadExpirationFileTest {

	private static final String EXPIRATION_FILES_BASE_PATH = "/v1/expiration-files";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;

	@Autowired
	ExpirationFileEntityRepository fileRepository;

//	private DateTimeFormatter dt = DateTimeFormatter.ISO_DATE_TIME;

	@Value("${govio-planner.time-zone:Europe/Rome}")
	private String timeZone;

	// 1. Upload OK file csv per utenza admin 
	@Test
	void UC_1_01_UploadCsvFileOk_Utenza_Admin() throws Exception {
		String fileName = "csv-test-UC101";
		byte[] content = FileUtils.readFileToByteArray(new ClassPathResource("csv-test").getFile());
		String boundary = MultipartUtils.generateBoundary();

		String planId = Costanti.PARAMETRO_PLAN_ID_BARI_CIE_EXP;
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.PARAMETRO_PLAN_ID, planId);

		MvcResult result = this.mockMvc.perform(
				multipart(EXPIRATION_FILES_BASE_PATH)
				.content(MultipartUtils.createFileContent(content, boundary,  Costanti.TEXT_CSV_CONTENT_TYPE, fileName))
				.params(params)
				.contentType("multipart/form-data; boundary=" + boundary)
				.characterEncoding("UTF-8")
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.filename", endsWith(fileName)))
				.andExpect(jsonPath("$.plan_id", is(planId)))
				.andExpect(jsonPath("$.uploader_id").isNumber())
				.andExpect(jsonPath("$.creation_date").exists())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject item = reader.readObject();
		int idFile = item.getInt("id");

		ExpirationFileEntity expirationFileEntity = this.fileRepository.findById((long) idFile).get();

		assertEquals(item.getInt("id"), expirationFileEntity.getId());
		assertEquals(item.getInt("uploader_id"), expirationFileEntity.getUploaderUser().getId());
		assertEquals(item.getString("plan_id"), expirationFileEntity.getPlanId());
		//assertEquals(item.getString("creation_date"), dt.format(expirationFileEntity.getCreationDate()));
	}

	// 1. Upload Fail utenza non autorizzata 
	@Test
	void UC_1_02_UploadCsvFileOk_Utenza_Admin() throws Exception {
		String fileName = "csv-test-UC102";
		byte[] content = FileUtils.readFileToByteArray(new ClassPathResource("csv-test").getFile());
		String boundary = MultipartUtils.generateBoundary();

		String planId = Costanti.PARAMETRO_PLAN_ID_BARI_CIE_EXP;
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.PARAMETRO_PLAN_ID, planId);

		this.mockMvc.perform(
				multipart(EXPIRATION_FILES_BASE_PATH)
				.content(MultipartUtils.createFileContent(content, boundary,  Costanti.TEXT_CSV_CONTENT_TYPE, fileName))
				.params(params)
				.contentType("multipart/form-data; boundary=" + boundary)
				.characterEncoding("UTF-8")
				.with(this.userAuthProfilesUtils.utenzaOspite())
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status", is(401)))
				.andExpect(jsonPath("$.title", is("Unauthorized")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
}
