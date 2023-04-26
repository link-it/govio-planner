package it.govhub.govio.planner.api.test.controller.files;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import it.govhub.govio.planner.api.entity.ExpirationFileEntity.Status;
import it.govhub.govio.planner.api.test.costanti.Costanti;
import it.govhub.govio.planner.api.test.utils.MultipartUtils;
import it.govhub.govio.planner.api.test.utils.UserAuthProfilesUtils;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di caricamento dei files")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)

class File_UC_1_UploadExpirationFileTest {

	private static final String EXPIRATION_FILES_BASE_PATH = "/v1/expiration-files";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;

	// 1. Upload OK file csv per utenza admin 
//	@Test
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
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.filename", is(fileName)))
				.andExpect(jsonPath("$.plan_id", is(planId)))
				.andExpect(jsonPath("$.status", is(Status.CREATED.toString())))
				.andExpect(jsonPath("$.creation_date").exists())
				.andReturn();
	}
}
