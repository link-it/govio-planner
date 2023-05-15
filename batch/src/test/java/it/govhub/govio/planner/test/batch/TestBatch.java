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

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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
@SpringBootTest(classes = Application.class)
 class TestBatch {
	@Value("${planner.ntfy.csv-dir}")
	private String notifyFile;
	@Value("${planner.exp.csv-dir}")
	private String expFile;
	@Value("${planner.ntfy.date-time}")
	private String dateTimeDelay;
	


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
	
	//Mock clock bean
    @MockBean
	private MyClock clock;

	@Autowired
	GovioPlannerBatchService govioBatchService;
	
	// data in cui è girato l'ultima volta il batch che viene usata nei test
	OffsetDateTime lastDate = OffsetDateTime.parse("2023-05-04T10:15:30+01:00");
	
	@BeforeEach
	void setUp(){
		govioFileProducedRepository.deleteAll();
		expirationCIEFileRepository.deleteAll();
		// mock del metodo LocalDate.now() in modo da far restituire il giorno 05/05/2023
		Mockito
		.when(clock.now())
		.thenReturn(LOCAL_DATE);
	}

	private void initializeJobLauncherTestUtils() throws Exception {
		this.jobLauncherTestUtils = new JobLauncherTestUtils();
		this.jobLauncherTestUtils.setJobLauncher(jobLauncher);
		this.jobLauncherTestUtils.setJobRepository(jobRepository);
		this.jobLauncherTestUtils.setJob(job);
	}
	
	//test con file dele scadenze non presente sul database
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
		String routePath = expFile;
		File f = new File(routePath+"file_assente.csv");
		Path location = Path.of("/etc/govio-planner/ntfy-files/CSVTestNotifiche.csv");
		ExpirationCIEFileEntity expirationCIEFileEntity = ExpirationCIEFileEntity.builder().creationDate(OffsetDateTime.now()).location(f.getAbsolutePath()).name("CSVTestNotifiche.csv").build();
		GovioFileProducedEntity govioFileProducedEntity = GovioFileProducedEntity.builder().creationDate(lastDate).expirationFile(expirationCIEFileEntity).location(location).status(Status.SCHEDULED).build();
		expirationCIEFileRepository.save(expirationCIEFileEntity);
		govioFileProducedRepository.save(govioFileProducedEntity);
		JobExecution jobExecution = jobLauncherTestUtils.launchJob();
		Assert.assertEquals("FAILED", jobExecution.getExitStatus().getExitCode());
	}

	// testa il corretto funzionamento del batch in caso ci sia un errore sintattico in una riga del csv. Il batch salta la riga e continua l'eseguzione dopo aver loggato un warning
	// il template del CSV di output ha una sola riga, il file di input con le scadenze ne ha due, la prima è quella contenente l'errore che verrà saltata
	// il CSV prodotto ha una sola riga
	@Test
	void testNewExpirationCSV_ErrorInCSVOK() throws Exception   {
		File createdFile=null;
		File expectedFile=null;
		try {
			initializeJobLauncherTestUtils();
		// file delle scadenze caricato in /test/resources
		String routePath = expFile;
		// File CSV composto da due righe e in cui la seconda ha il nome sintatticamente errato
		File f = new File(routePath+"CIE_scadenza_KO.csv");
		ExpirationCIEFileEntity expirationCIEFileEntity = ExpirationCIEFileEntity.builder().creationDate(OffsetDateTime.now()).location(f.getAbsolutePath()).name("CSVTestNotifiche.csv").build();
		Path location = Path.of("/etc/govio-planner/ntfy-files/CSVTestNotifiche.csv");
		GovioFileProducedEntity govioFileProducedEntity =  GovioFileProducedEntity.builder().creationDate(lastDate).expirationFile(expirationCIEFileEntity).location(location).status(Status.SCHEDULED).build();
		expirationCIEFileRepository.save(expirationCIEFileEntity);
		govioFileProducedRepository.save(govioFileProducedEntity);
		JobExecution jobExecution = jobLauncherTestUtils.launchJob();
		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
		// file delle notifiche creato in /test/resources 
		String fileCreatedPath = notifyFile+"CIE_EXPIRATION_"+LocalDate.now()+".csv";
		String expectedFilePath = notifyFile+"CIE_EXPIRATION_TEMPLATE2.csv";
		createdFile = new File(fileCreatedPath);
		// template del file su che mi aspetto di produrre caricato in /test/resources.
		// lo copio momentaneamente, così da non modificare il template da usare per questo test successivamente
	    File template = new File(expectedFilePath);
	    expectedFile = new File(notifyFile+"CIE_EXPIRATION.csv");
   	    FileUtils.copyFile(template,expectedFile);
		byte[] buff = Files.readAllBytes(expectedFile.toPath());
		// calcolo dell'expedition date
		LocalDateTime expeditionDate;
		// se sono passate le 12:00 metto data di spedizione a domani a mezzogiorno, sennò oggi a mezzogiorno
		LocalTime noon = LocalTime.NOON;
		LocalDate today = LocalDate.now(ZoneId.of("Europe/Berlin"));
		expeditionDate = LocalDateTime.of(today, noon);
		if (LocalTime.now().getHour()>12) expeditionDate = expeditionDate.plusDays(1);
		// in caso il parametro dateTimeDelay non sia vuoto, viene settato un ritardo alla spedizione del messaggio
		if (!dateTimeDelay.isEmpty()) expeditionDate.plusHours(Long.valueOf(dateTimeDelay));
		// sostituisco il placeholder del valore dell'expedition date con il valore corretto essendo l'expedition date calcolata in base alla data corrente
		String s = new String(buff, Charset.defaultCharset());
		s = s.replaceAll(Pattern.quote("$expedition_date"), expeditionDate.toString());
		Files.write(expectedFile.toPath(), s.getBytes());
		assertEquals(true,FileUtils.contentEquals(expectedFile,createdFile));
		} finally {
			if (createdFile!=null) createdFile.delete();
			if (expectedFile!=null) expectedFile.delete();
		}
	}

	
	@Test
	void testNewExpirationCSVOK() throws Exception  {
		File createdFile=null;
		File expectedFile=null;
		try {
			initializeJobLauncherTestUtils();
		// file delle scadenze caricato in /test/resources
		String routePath = expFile;
		File f = new File(routePath+"CIE_scadenza_tracciato.csv");
		ExpirationCIEFileEntity expirationCIEFileEntity = ExpirationCIEFileEntity.builder().creationDate(OffsetDateTime.now()).location(f.getAbsolutePath()).name("CSVTestNotifiche.csv").build();
		Path location = Path.of("/etc/govio-planner/ntfy-files/CSVTestNotifiche.csv");
		GovioFileProducedEntity govioFileProducedEntity =  GovioFileProducedEntity.builder().creationDate(lastDate).expirationFile(expirationCIEFileEntity).location(location).status(Status.SCHEDULED).build();
		expirationCIEFileRepository.save(expirationCIEFileEntity);
		govioFileProducedRepository.save(govioFileProducedEntity);
		JobExecution jobExecution = jobLauncherTestUtils.launchJob();
		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
		// file delle notifiche creato in /test/resources 
		String fileCreatedPath = notifyFile+"CIE_EXPIRATION_"+LocalDate.now()+".csv";
		String expectedFilePath = notifyFile+"CIE_EXPIRATION_TEMPLATE.csv";
		createdFile = new File(fileCreatedPath);
		// template del file su che mi aspetto di produrre caricato in /test/resources
		// lo copio, così da non modificare il template da usare per i test
	    File template = new File(expectedFilePath);
	    expectedFile = new File(notifyFile+"CIE_EXPIRATION.csv");
   	    FileUtils.copyFile(template,expectedFile);
		byte[] buff = Files.readAllBytes(expectedFile.toPath());
		// calcolo dell'expedition date
		LocalDateTime expeditionDate;
		// se sono passate le 12:00 metto data di spedizione a domani a mezzogiorno, sennò oggi a mezzogiorno
		LocalTime noon = LocalTime.NOON;
		LocalDate today = LocalDate.now(ZoneId.of("Europe/Berlin"));
		expeditionDate = LocalDateTime.of(today, noon);
		if (LocalTime.now().getHour()>12) expeditionDate = expeditionDate.plusDays(1);
		// in caso il parametro dateTimeDelay non sia vuoto, viene settato un ritardo alla spedizione del messaggio
		if (!dateTimeDelay.isEmpty()) expeditionDate.plusHours(Long.valueOf(dateTimeDelay));
		// sostituisco il placeholder del valore dell'expedition date con il valore corretto
		String s = new String(buff, Charset.defaultCharset());
		s = s.replaceAll(Pattern.quote("$expedition_date"), expeditionDate.toString());
		Files.write(expectedFile.toPath(), s.getBytes());
		assertEquals(true,FileUtils.contentEquals(expectedFile,createdFile));
		} finally {
			if (createdFile!=null) createdFile.delete();
			if (expectedFile!=null) expectedFile.delete();
		}
	}
	
	

	@Test
	void testNewExpirationCSV_VuotoOK() throws Exception  {
		File createdFile=null;
		File expectedFile=null;
		try {
			initializeJobLauncherTestUtils();
		// file delle scadenze caricato in /test/resources
		String routePath = expFile;
		File f = new File(routePath+"CIE_scadenza_InputVOID.csv");
		f.createNewFile();
		ExpirationCIEFileEntity expirationCIEFileEntity = ExpirationCIEFileEntity.builder().creationDate(OffsetDateTime.now()).location(f.getAbsolutePath()).name("CSVTestNotifiche.csv").build();
		Path location = Path.of("/etc/govio-planner/ntfy-files/CSVTestNotifiche.csv");
		GovioFileProducedEntity govioFileProducedEntity =  GovioFileProducedEntity.builder().creationDate(lastDate).expirationFile(expirationCIEFileEntity).location(location).status(Status.SCHEDULED).build();
		expirationCIEFileRepository.save(expirationCIEFileEntity);
		govioFileProducedRepository.save(govioFileProducedEntity);
		JobExecution jobExecution = jobLauncherTestUtils.launchJob();
		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
		// file delle notifiche creato in /test/resources 
		String fileCreatedPath = notifyFile+"CIE_EXPIRATION_"+LocalDate.now()+".csv";
		String expectedFilePath = notifyFile+"CIE_EXPIRATION_VOID.csv";
		createdFile = new File(fileCreatedPath);
	    expectedFile = new File(expectedFilePath);
		expectedFile.createNewFile();
		assertEquals(true,FileUtils.contentEquals(expectedFile,createdFile));
		} finally {
			if (createdFile!=null) createdFile.delete();
		}
	}
	
}