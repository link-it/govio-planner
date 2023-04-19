package it.govhub.govio.planner.api.test.controller.files;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.commons.io.IOUtils;
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

import it.govhub.govio.planner.api.Application;
import it.govhub.govio.planner.api.entity.ExpirationFileEntity;
import it.govhub.govio.planner.api.entity.GovioPlannerFileEntity;
import it.govhub.govio.planner.api.repository.ExpirationFileEntityRepository;
import it.govhub.govio.planner.api.repository.GovioPlannerFileEntityRepository;
import it.govhub.govio.planner.api.test.costanti.Costanti;
import it.govhub.govio.planner.api.test.utils.ExpirationFileUtils;
import it.govhub.govio.planner.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.commons.exception.InternalException;
import it.govhub.security.beans.GovhubPrincipal;
import it.govhub.security.services.GovhubUserDetailService;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di lettura csv tracciati")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
class Files_UC_7_GetGovIOFileContentTest {

	private static final String GOVIO_FILES_BASE_PATH = "/v1/govio-files";
	private static final String GOVIO_FILES_BASE_PATH_DETAIL_ID = GOVIO_FILES_BASE_PATH + "/{id}/content";

	@Value("${govio-planner.filerepository.path}")
	Path fileRepositoryPath;
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	@Autowired
	ExpirationFileEntityRepository expirationFileRepository;
	
	@Autowired
	GovioPlannerFileEntityRepository govioFileRepository;
	
	@Autowired
	private GovhubUserDetailService userDetailService;
	
	@Value("${govio-planner.id}")
	private String planId;

	@Value("${govio-planner.time-zone:Europe/Rome}")
	private String timeZone;
	
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
	
	// 1. getFileOK 
	@Test
	void UC_4_01_GetFileOk() throws Exception {
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
		int idFile = item1.getInt("id");
		
		result = this.mockMvc.perform(get(GOVIO_FILES_BASE_PATH_DETAIL_ID,idFile)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_OCTET_STREAM))
				.andExpect(status().isOk())
				.andReturn();
		
		String fileReceived = result.getResponse().getContentAsString(); 
		
		GovioPlannerFileEntity govioFileEntity = this.govioFileRepository.findById((long) idFile).get();
		
		Path path = govioFileEntity.getLocation();
		
		FileInputStream stream;
		try {
			stream = new FileInputStream(path.toFile());
		} catch (FileNotFoundException e) {
			throw new InternalException(e);
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IOUtils.copy(stream, baos);
		
		assertEquals(fileReceived, baos.toString());
	}
	
	// 2. getNotFound
	@Test
	void UC_4_02_GetFile_NotFound() throws Exception {
		int idFile = 10000;
		
		this.mockMvc.perform(get(GOVIO_FILES_BASE_PATH_DETAIL_ID,idFile)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/problem+json"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status", is(404)))
				.andExpect(jsonPath("$.title", is("Not Found")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
	
	// 3. getInvalidID
	@Test
	void UC_4_03_GetFile_InvalidID() throws Exception {
		String idFile = "XXX";
		
		this.mockMvc.perform(get(GOVIO_FILES_BASE_PATH_DETAIL_ID,idFile)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/problem+json"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
	
	// 4. getFile Fail file eliminato dal server 
	@Test
	void UC_4_04_GetFile_FileNotFoundOnFileSystem() throws Exception {
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
		int idFile = item1.getInt("id");
		
		GovioPlannerFileEntity govioFileEntity = this.govioFileRepository.findById((long) idFile).get();
		
		
		File contenutoFileDaEliminare = govioFileEntity.getLocation().toFile();
		if(contenutoFileDaEliminare.exists()) {
			contenutoFileDaEliminare.delete();
		}

		this.mockMvc.perform(get(GOVIO_FILES_BASE_PATH_DETAIL_ID,idFile)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/problem+json"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.status", is(500)))
				.andExpect(jsonPath("$.title", is("Internal Server Error")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
}
