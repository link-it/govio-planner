/*
 * GovHub - Application suite for Public Administration
 *
 * Copyright (c) 2023-2024 Link.it srl (https://www.link.it).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3, as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.govhub.govio.planner.test.batch;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

import it.govhub.govio.planner.batch.*;
import it.govhub.govio.planner.batch.bean.*;
import it.govhub.govio.planner.batch.entity.ExpirationCIEFileEntity;
import it.govhub.govio.planner.batch.entity.GovioFileProducedEntity;
import it.govhub.govio.planner.batch.entity.GovioFileProducedEntity.Status;
import it.govhub.govio.planner.batch.jobs.GovioPlannerJob;
import it.govhub.govio.planner.batch.repository.ExpirationCIEFileRepository;
import it.govhub.govio.planner.batch.repository.GovioFileProducedRepository;
import it.govhub.govio.planner.batch.service.*;

@ActiveProfiles("test")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(classes = Application.class)
 class TestBatch {
	@Value("${planner.ntfy.csv-dir}")
	private Path govioFilePath;
	
	@Value("${planner.exp.csv-dir}")
	private Path expirationPath;
	
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	@Qualifier(value = GovioPlannerJob.PLANNERJOB)
	private Job job;

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private JobRepository jobRepository;
	
	@Autowired
	private ExpirationCIEFileRepository expirationCIEFileRepository;

	@Autowired
	private GovioFileProducedRepository govioFileProducedRepository;
	
	// giorno successivo alla data in cui è girato il batch l'ultima volta nei test
	private final static LocalDate LOCAL_DATE = LocalDate.of(2023, 05, 04);
	
    @MockBean
	private MyClock clock;

	@Autowired
	GovioPlannerBatchService govioBatchService;
	
	// data in cui è girato l'ultima volta il batch che viene usata nei test
	OffsetDateTime lastDate = OffsetDateTime.parse("2023-05-03T10:15:30+02:00");
	
	@BeforeEach
	void setUp(){
		govioFileProducedRepository.deleteAll();
		expirationCIEFileRepository.deleteAll();
		// mock del metodo OffsetDateTime.now() in modo da far restituire il giorno 05/05/2023 alle ore 12:00
		Mockito
		.when(clock.now())
		.thenReturn(OffsetDateTime.of(LOCAL_DATE,LocalTime.of(2,0), ZoneOffset.UTC));
		}
	

	private void initializeJobLauncherTestUtils() throws Exception {
		this.jobLauncherTestUtils = new JobLauncherTestUtils();
		this.jobLauncherTestUtils.setJobLauncher(jobLauncher);
		this.jobLauncherTestUtils.setJobRepository(jobRepository);
		this.jobLauncherTestUtils.setJob(job);
	}
	
	// funzioni di utility dei test per la creazione dei csv di input e output

	public String escapeSpecialCharacters(String data) {
	    String escapedData = data.replaceAll("\\R", " ");
	    if (data.contains(",") || data.contains("\"") || data.contains("'")) {
	        data = data.replace("\"", "\"\"");
	        escapedData = "\"" + data + "\"";
	    }
	    return escapedData;
	}

	@Test
	void testExpirationFileKO() throws Exception {
		initializeJobLauncherTestUtils();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob();
		Assert.assertEquals("FAILED", jobExecution.getExitStatus().getExitCode());
	}

	// testa il corretto funzionamento del batch in caso il file delle scadenze non sia presente sul file system
	@Test
	void testDirectoryReadKO() throws Exception {
		initializeJobLauncherTestUtils();
		Path fileAssentePath = expirationPath.resolve("file_assente.csv");
		Path location = expirationPath.resolve("CSVTestNotifiche.csv");
		ExpirationCIEFileEntity expirationCIEFileEntity = ExpirationCIEFileEntity.builder()
				.creationDate(clock.now())
				.location(fileAssentePath)
				.name("file_assente.csv")
				.build();
		GovioFileProducedEntity govioFileProducedEntity = GovioFileProducedEntity.builder()
				.creationDate(lastDate)
				.expirationFile(expirationCIEFileEntity)
				.location(location)
				.status(Status.SCHEDULED)
				.messageCount(0L).name("test")
				.size(0L)
				.build();
		expirationCIEFileRepository.save(expirationCIEFileEntity);
		govioFileProducedRepository.save(govioFileProducedEntity);
		JobExecution jobExecution = jobLauncherTestUtils.launchJob();
		Assert.assertEquals("FAILED", jobExecution.getExitStatus().getExitCode());
	}
	// testa il corretto funzionamento del batch in caso ci sia un errore sintattico in una riga del csv. Il batch salta la riga e continua l'eseguzione dopo aver loggato un warning
	// il CSV prodotto ha una sola riga
	@Test
	void testNewExpirationCSV_ErrorInCSVOK() throws Exception   {
		File createdFile=null;
		File expectedFile=null;
				File inputFile = null;
		try {
			initializeJobLauncherTestUtils();
		// File CSV composto da più righe e in cui la prima è l'unica senza errori sintattici
			inputFile = expirationPath.resolve("CIE_scadenza_tracciato.csv").toFile();
			TestUtility utility = new TestUtility(clock);
			utility.createErrorInCSVOKInput(inputFile);
		ExpirationCIEFileEntity expirationCIEFileEntity = ExpirationCIEFileEntity
				.builder()
				.creationDate(clock.now())
				.location(inputFile.toPath())
				.name("CIE_scadenza_tracciato.csv")
				.build();
		GovioFileProducedEntity govioFileProducedEntity = GovioFileProducedEntity
				.builder()
				.creationDate(lastDate)
				.expirationFile(expirationCIEFileEntity)
				.location(inputFile.toPath())
				.status(Status.SCHEDULED)
				.messageCount(0L)
				.name("test")
				.size(0L)
				.build();
		expirationCIEFileRepository.save(expirationCIEFileEntity);
		govioFileProducedRepository.save(govioFileProducedEntity);
		JobExecution jobExecution = jobLauncherTestUtils.launchJob();
		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
		// copio il template del file delle notifiche nella directory  
		
		
		// file delle notifiche creato in govioFilePath dal batch	
		createdFile = govioFilePath.resolve(jobExecution.getExecutionContext().get("destFilename").toString()).toFile();
		expectedFile = govioFilePath.resolve("testCSV.csv").toFile();
		utility.createErrorInCSVOKOutput(expectedFile);
		
		byte[] buff = Files.readAllBytes(expectedFile.toPath());
		boolean isFirstLine = true;
        String expeditionDate = null;
		String s = new String(buff, Charset.defaultCharset());
        BufferedReader br = new BufferedReader(new FileReader(createdFile));
            String line;
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                String[] columns = line.split(",");
                expeditionDate = columns[1];
        		// sostituisco il placeholder del valore dell'expedition date con il valore del file prodotto dal test
        		s = s.replaceAll(Pattern.quote("$expedition_date"), expeditionDate);
        		Files.write(expectedFile.toPath(), s.getBytes());
            }
            br.close();
		assertEquals(true,FileUtils.contentEquals(expectedFile,createdFile));
		} finally {
					if (inputFile!=null) inputFile.delete();
			if (createdFile!=null) createdFile.delete();
			if (expectedFile!=null) expectedFile.delete();
		}
	}

	@Test
	void testNewExpirationCSVOK() throws Exception  {
		File createdFile=null;
		File expectedFile=null;
		File inputFile = null;
		try {
		// fase di inizializzazione del test
		initializeJobLauncherTestUtils();
		
		// file di input del test
		Path inputFilePath = expirationPath.resolve("CIE_scadenza_tracciato.csv");
		inputFile= inputFilePath.toFile();
		TestUtility utility = new TestUtility(clock);
		utility.createCSVOKInput(inputFile);
		ExpirationCIEFileEntity expirationCIEFileEntity = ExpirationCIEFileEntity.builder()
				.creationDate(clock.now())
				.location(inputFilePath)
				.name("CIE_scadenza_tracciato.csv")
				.build();
		GovioFileProducedEntity govioFileProducedEntity = GovioFileProducedEntity.builder()
				.creationDate(lastDate)
				.expirationFile(expirationCIEFileEntity)
				.location(inputFile.toPath())
				.status(Status.SCHEDULED)
				.messageCount(0L)
				.name("test")
				.size(0L)
				.build();
		expirationCIEFileRepository.save(expirationCIEFileEntity);
		govioFileProducedRepository.save(govioFileProducedEntity);

		// viene eseguito il batch e controllato il successo
		JobExecution jobExecution = jobLauncherTestUtils.launchJob();
		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
		
		// file delle notifiche creato in govioFilePath dal batch	
		createdFile = govioFilePath.resolve(jobExecution.getExecutionContext().get("destFilename").toString()).toFile();
		expectedFile = govioFilePath.resolve("testCSV.csv").toFile();
		utility.createCSVOKOutput(expectedFile);

		
		byte[] buff = Files.readAllBytes( expectedFile.toPath());
		boolean isFirstLine = true;
        String expeditionDate = null;
		String s = new String(buff, Charset.defaultCharset());
        BufferedReader br = new BufferedReader(new FileReader(createdFile));
            String line;
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                String[] columns = line.split(",");
                expeditionDate = columns[1];
        		// sostituisco il placeholder del valore dell'expedition date con il valore del file prodotto dal test
        		s = s.replaceAll(Pattern.quote("$expedition_date"), expeditionDate);
        		Files.write(expectedFile.toPath(), s.getBytes());
            }
        br.close();
		assertEquals(true,FileUtils.contentEquals(expectedFile,createdFile));
		} finally {
			if (inputFile!=null) inputFile.delete();
			if (createdFile!=null) createdFile.delete();
			if (expectedFile!=null) expectedFile.delete();
		}
	}
	
	
	@Test
	void testNewExpirationCSV_VuotoOK() throws Exception  {
		File createdFile=null;
		File expectedFile=null;
		File inputFile = null;
		try {
			initializeJobLauncherTestUtils();
			// file di input del test
			Path inputFilePath = expirationPath.resolve("CIE_scadenza_tracciato.csv");
			inputFile= inputFilePath.toFile();
			TestUtility utility = new TestUtility(clock);
			utility.createNewExpirationCSV_VuotoOKInput(inputFile);
			ExpirationCIEFileEntity expirationCIEFileEntity = ExpirationCIEFileEntity.builder()
					.creationDate(clock.now())
					.location(inputFilePath)
					.name("CIE_scadenza_tracciato.csv")
					.build();
			GovioFileProducedEntity govioFileProducedEntity = GovioFileProducedEntity.builder()
					.creationDate(lastDate)
					.expirationFile(expirationCIEFileEntity)
					.location(inputFile.toPath())
					.status(Status.SCHEDULED)
					.messageCount(0L)
					.name("test")
					.size(0L)
					.build();
			expirationCIEFileRepository.save(expirationCIEFileEntity);
			govioFileProducedRepository.save(govioFileProducedEntity);
		JobExecution jobExecution = jobLauncherTestUtils.launchJob();
		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());

		// file delle notifiche creato in govioFilePath dal batch	
		createdFile = govioFilePath.resolve(jobExecution.getExecutionContext().get("destFilename").toString()).toFile();
		expectedFile = govioFilePath.resolve("testCSV.csv").toFile();
		utility.createNewExpirationCSV_VuotoOKOutput(expectedFile);
		
		assertEquals(true,FileUtils.contentEquals(expectedFile,createdFile));
		} finally {
			if (inputFile!=null) inputFile.delete();
			if (createdFile!=null) createdFile.delete();
			if (expectedFile!=null) expectedFile.delete();
		}
	}

}