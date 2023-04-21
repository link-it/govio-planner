package it.govhub.govio.planner.batch;

import java.time.LocalDate;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;


@SpringBootApplication(scanBasePackages={"it.govhub.govio.planner"})
@EnableScheduling
public class Application extends SpringBootServletInitializer {
	
	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	@Qualifier("PlannerJob") 
	private Job plannerJob;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	@Scheduled(cron = "0 0 */1 * * *", zone = "Europe/Berlin")
	public void lookForExpiringJob() throws Exception {
		JobParameters params = new JobParametersBuilder()
				.addString("GovioPlannerJobID", LocalDate.now().toString())
				.toJobParameters();
		jobLauncher.run(plannerJob, params);
	}
}