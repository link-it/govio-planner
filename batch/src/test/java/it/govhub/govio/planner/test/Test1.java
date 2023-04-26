package it.govhub.govio.planner.test;

import static org.junit.Assert.assertThrows;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import it.govhub.govio.planner.batch.*;
import it.govhub.govio.planner.batch.entity.ExpirationCIEFileEntity;
import it.govhub.govio.planner.batch.entity.GovioFileProducedEntity;
import it.govhub.govio.planner.batch.entity.GovioFileProducedEntity.Status;
import it.govhub.govio.planner.batch.repository.ExpirationCIEFileRepository;
import it.govhub.govio.planner.batch.repository.GovioFileProducedRepository;

@SpringBootTest(classes = Application.class)
public class Test1 {
	@Value("${planner.ntfy.csv-dir}")
	private String notifyFile;
	
	@Value("${planner.exp.csv-dir}")
	private String expFile;

	private JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	@Qualifier(value = "PlannerJob")
	private Job job;

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private JobRepository jobRepository;
	
	@Autowired
	private ExpirationCIEFileRepository expirationCIEFileRepository;

	@Autowired
	private GovioFileProducedRepository govioFileProducedRepository;
	

	@BeforeEach
	void setUp(){
		String routePath = expFile;
		File f = new File(routePath);
		govioFileProducedRepository.deleteAll();
		expirationCIEFileRepository.deleteAll();
	}

	private void initializeJobLauncherTestUtils() throws Exception {
		this.jobLauncherTestUtils = new JobLauncherTestUtils();
		this.jobLauncherTestUtils.setJobLauncher(jobLauncher);
		this.jobLauncherTestUtils.setJobRepository(jobRepository);
		this.jobLauncherTestUtils.setJob(job);
	}
	/*
	@Test
	void testExpirationFileKO() throws Exception {
		//test senza ultimo file con le scadenze
		initializeJobLauncherTestUtils();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob();
		Assert.assertEquals("FAILED", jobExecution.getExitStatus().getExitCode());
	}
*/
	@Test
	void testDirectoryReadKO() throws Exception {
		// test che controlla il corretto funzionamento del batch in caso il file delle scadenze non sia leggibile
		initializeJobLauncherTestUtils();
		String routePath = expFile;
		File f = new File(routePath+"CIE_scadenza_2018_-_dic_2021_-_tracciato.csv");
		f.setReadable(false);
		ExpirationCIEFileEntity expirationCIEFileEntity = ExpirationCIEFileEntity.builder().creationDate(OffsetDateTime.now()).location(f.getAbsolutePath()).name("CSVTestNotifiche.csv").build();
		GovioFileProducedEntity govioFileProducedEntity = GovioFileProducedEntity.builder().creationDate(OffsetDateTime.parse("2007-12-03T10:15:30+01:00")).expirationFile(expirationCIEFileEntity).location("/etc/govio-planner/ntfy-files/CSVTestNotifiche.csv").size(null).status(Status.CREATED).build();
		expirationCIEFileRepository.save(expirationCIEFileEntity);
		govioFileProducedRepository.save(govioFileProducedEntity);
		JobExecution jobExecution = jobLauncherTestUtils.launchJob();
		Assert.assertEquals("FAILED", jobExecution.getExitStatus().getExitCode());
		f.setReadable(true);
	}
	
	@Test
	void testDirectoryWriteKO() throws Exception {
		// test che controlla il corretto funzionamento del batch in caso il file prodotto sia in una cartella non scrivibile
		initializeJobLauncherTestUtils();
		String routePath = expFile;
		File f = new File(routePath +"CIE_scadenza_2018_-_dic_2021_-_tracciato.csv");
		File fileScrittura = new File(notifyFile);
		fileScrittura.setReadOnly();
		ExpirationCIEFileEntity expirationCIEFileEntity = ExpirationCIEFileEntity.builder().creationDate(OffsetDateTime.now()).location(f.getAbsolutePath()).name("CSVTestNotifiche.csv").build();
		GovioFileProducedEntity govioFileProducedEntity =  GovioFileProducedEntity.builder().creationDate(OffsetDateTime.parse("2007-12-03T10:15:30+01:00")).expirationFile(expirationCIEFileEntity).location("/etc/govio-planner/ntfy-files/CSVTestNotifiche.csv").size(null).status(Status.CREATED).build();
		expirationCIEFileRepository.save(expirationCIEFileEntity);
		govioFileProducedRepository.save(govioFileProducedEntity);
		JobExecution jobExecution = jobLauncherTestUtils.launchJob();
		Assert.assertEquals("FAILED", jobExecution.getExitStatus().getExitCode());
		fileScrittura.setWritable(true);
	}

	@Test
	void testNewExpirationCSV() throws Exception {
		initializeJobLauncherTestUtils();
		// file delle scadenze caricato in /test/resources
		String routePath = expFile;
		File f = new File(routePath+"CIE_scadenza_2018_-_dic_2021_-_tracciato.csv");
		ExpirationCIEFileEntity expirationCIEFileEntity = ExpirationCIEFileEntity.builder().creationDate(OffsetDateTime.now()).location(f.getAbsolutePath()).name("CSVTestNotifiche.csv").build();
		GovioFileProducedEntity govioFileProducedEntity =  GovioFileProducedEntity.builder().creationDate(OffsetDateTime.parse("2007-12-03T10:15:30+01:00")).expirationFile(expirationCIEFileEntity).location("/etc/govio-planner/ntfy-files/CSVTestNotifiche.csv").size(null).status(Status.CREATED).build();
		expirationCIEFileRepository.save(expirationCIEFileEntity);
		govioFileProducedRepository.save(govioFileProducedEntity);
		JobExecution jobExecution = jobLauncherTestUtils.launchJob();
		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());

		String file = notifyFile+"CIE_EXPIRATION_"+LocalDate.now()+".csv";
		String expectedFilePath = notifyFile+"CIE_EXPIRATION_"+LocalDate.now()+".csv"; // vedere come fare questo
		File createdFile = new File(file);
		File expectedFile = new File(expectedFilePath);
		createdFile.compareTo(expectedFile);
		createdFile.delete();
	}
}