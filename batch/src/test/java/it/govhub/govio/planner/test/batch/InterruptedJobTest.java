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
package it.govhub.govio.planner.test.batch;

import static org.junit.Assert.assertThrows;

import java.io.File;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import it.govhub.govio.planner.batch.Application;
import it.govhub.govio.planner.batch.bean.MyClock;
import it.govhub.govio.planner.batch.entity.ExpirationCIEFileEntity;
import it.govhub.govio.planner.batch.entity.GovioFileProducedEntity;
import it.govhub.govio.planner.batch.entity.GovioFileProducedEntity.Status;
import it.govhub.govio.planner.batch.jobs.GovioPlannerJob;
import it.govhub.govio.planner.batch.repository.ExpirationCIEFileRepository;
import it.govhub.govio.planner.batch.repository.GovioFileProducedRepository;
import it.govhub.govio.planner.batch.service.GovioPlannerBatchService;
import it.govhub.govio.planner.test.config.JobOperatorConfig;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@AutoConfigureMockMvc
@Import({JobOperatorConfig.class})
@TestInstance(Lifecycle.PER_CLASS)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(classes = Application.class)
public class InterruptedJobTest {
	@Value("${planner.ntfy.csv-dir}")
	private Path govioFilePath;
	@Value("${planner.exp.csv-dir}")
	private Path expirationPath;
	
	@Autowired
	private GovioPlannerBatchService govioBatchService;
	
	@Autowired
	private ExpirationCIEFileRepository expirationCIEFileRepository;

	@Autowired
	private GovioFileProducedRepository govioFileProducedRepository;
	
    @MockBean
	private MyClock clock;
	
	Logger log = LoggerFactory.getLogger(InterruptedJobTest.class);
	
	@BeforeEach
	void setUp(){
		// mock del metodo LocalDate.now() in modo da far restituire sempre il giorno 05/05/2023
		govioFileProducedRepository.deleteAll();
		expirationCIEFileRepository.deleteAll();
	}
	

	// test per verificare che il batch nel caso giri una seconda volta nello stesso giorno, dopo aver completato con successo, non facendo nulla
	@Test
	void testSecondRunDoesNothingOK() throws Exception {
		File createdFile = null;
		File inputFile = null;
		try {
			Mockito
			.when(clock.now())
			.thenReturn(OffsetDateTime.of(2023, 05, 29, 0, 0, 0, 0, ZoneOffset.UTC));
			TestUtility utility = new TestUtility(clock);
			inputFile = expirationPath.resolve("CIE_scadenza_tracciato.csv").toFile();
			utility.createCSVOKInput(inputFile);
			ExpirationCIEFileEntity expirationCIEFileEntity = ExpirationCIEFileEntity.builder()
					.creationDate(clock.now())
					.location(expirationPath)
					.name("CIE_scadenza_tracciato.csv")
					.build();
			GovioFileProducedEntity govioFileProducedEntity =  GovioFileProducedEntity.builder()
					.creationDate(OffsetDateTime.parse("2007-12-03T10:15:30+01:00"))
					.expirationFile(expirationCIEFileEntity)
					.location(inputFile.toPath())
					.status(Status.SCHEDULED)
					.messageCount(0L)
					.size(0L)
					.name("test")
					.build();	
			expirationCIEFileRepository.save(expirationCIEFileEntity);
			govioFileProducedRepository.save(govioFileProducedEntity);

			JobExecution jobExecution = govioBatchService.runPlannerJob();
			Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
			createdFile = govioFilePath.resolve(jobExecution.getExecutionContext().get("destFilename").toString()).toFile();

			assertThrows(org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException.class, () -> {
				govioBatchService.runPlannerJob();
			});
		} finally {
			if (inputFile!=null) inputFile.delete();
			if (createdFile!=null) createdFile.delete();
		}
	}
	// test che verifica il corretto funzionamento del batch ad una seconda iterazione dopo essere fallito la prima volta per errore, e dopo che tale errore sia risolto
	@Test
	void testNewCSVFirstKOThenOK() throws Exception {
		Mockito
		.when(clock.now())
		.thenReturn(OffsetDateTime.of(2023, 05, 30, 0, 0, 0, 0, ZoneOffset.UTC));
		TestUtility utility = new TestUtility(clock);

		JobExecution brokenExecution = govioBatchService.runPlannerJob();
		if (brokenExecution != null) {
			this.log.info("Il Job [{}] è rimasto in stato {}", GovioPlannerJob.PLANNERJOB, brokenExecution.getStatus());
			Assert.assertTrue(BatchStatus.FAILED == brokenExecution.getStatus());
		}
		File inputFile = null;
		File createdFile=null;
		try {
			inputFile = expirationPath.resolve("CIE_scadenza_tracciato.csv").toFile();
			
			utility.createCSVOKInput(inputFile);
			ExpirationCIEFileEntity expirationCIEFileEntity = ExpirationCIEFileEntity.builder()
					.creationDate(clock.now())
					.location(expirationPath)
					.name("CIE_scadenza_tracciato.csv")
					.build();
			GovioFileProducedEntity govioFileProducedEntity =  GovioFileProducedEntity.builder()
					.creationDate(OffsetDateTime.parse("2007-12-03T10:15:30+01:00"))
					.expirationFile(expirationCIEFileEntity)
					.location(inputFile.toPath())
					.status(Status.SCHEDULED)
					.messageCount(0L)
					.size(0L)
					.name("test")
					.build();	
			expirationCIEFileRepository.save(expirationCIEFileEntity);
			govioFileProducedRepository.save(govioFileProducedEntity);
		// Rilancio l'esecuzione
		JobExecution jobExecution = govioBatchService.runPlannerJob();
		Assert.assertEquals("COMPLETED", jobExecution.getStatus().toString());

		createdFile = govioFilePath.resolve(jobExecution.getExecutionContext().get("destFilename").toString()).toFile();
		Assert.assertNotNull(createdFile);
		} finally {
			if (inputFile!=null) inputFile.delete();
			if (createdFile!=null) createdFile.delete();
		}
	}
}
