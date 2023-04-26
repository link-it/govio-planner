package it.govhub.govio.planner.batch.config;


import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import it.govhub.govio.planner.batch.repository.*;
import it.govhub.govio.planner.batch.step.*;
import it.govhub.govio.planner.batch.bean.*;


@Configuration
@EnableBatchProcessing
public class GovioPlannerConfig {

	@Autowired
	protected JobBuilderFactory jobs;
	
	@Autowired
	protected StepBuilderFactory steps;

	@Autowired
	protected EntityManager entityManager;

	@Autowired
	GovioFileProducedRepository govioFileProducedRepository;
	
	@Autowired
	ExpirationCIEFileRepository expirationCIEFileRepository;

	@Bean
	@StepScope
	@Qualifier("notifyItemReader")
	public ItemStreamReader<CSVItem> notifyItemReader(@Value("#{jobExecutionContext[location]}") String filename) {
		FlatFileItemReader<CSVItem> itemReader = new FlatFileItemReader<CSVItem>();
		  //Set input file location
		itemReader.setResource(new FileSystemResource(filename));
		  //Set number of lines to skips. Use it if file has header rows.
		itemReader.setLinesToSkip(1);
		  //Configure how each line will be parsed and mapped to different values
		itemReader.setStrict(false);
		DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
		tokenizer.setDelimiter(";");
		tokenizer.setNames(new String[] { "fullName", "date", "address","identityCardNumber","dueDate","CAP","age","gender","releaseDate","taxCode","codPositionAnagrafica"});
		DefaultLineMapper<CSVItem> lineMapper = new DefaultLineMapper<CSVItem>();
		lineMapper.setLineTokenizer(tokenizer);
		//Set values in CSVItem class
		BeanWrapperFieldSetMapper<CSVItem> setMapper = new BeanWrapperFieldSetMapper<CSVItem>();
		setMapper.setTargetType(CSVItem.class);
		lineMapper.setFieldSetMapper(setMapper);
		itemReader.setLineMapper(lineMapper);
		return itemReader;
	}
	
	@Bean
	@StepScope
	@Qualifier("notifyItemProcessor")
	public ItemProcessor<CSVItem,CSVExpiration> notifyItemProcessor(@Value("#{jobExecutionContext[date]}") String date) {
		ItemProcessor<CSVItem, CSVExpiration> notifyItemProcessor = new NotifyItemProcessor(date);
		return notifyItemProcessor;
	}

	@Bean
	@StepScope
	@Qualifier("notifyItemWriter")
	public ItemWriter<CSVExpiration> notifyItemWriter() {
		ItemWriter<CSVExpiration> itemWriter = new NotifyItemWriter();
        return itemWriter;
	}

	@Bean
	@StepScope
	@Qualifier("dateTasklet")
	public Tasklet dateTasklet() {
		LookForLastDateTasklet lookForLastDateTasklet = new LookForLastDateTasklet();
        return lookForLastDateTasklet;
	}
	
	@Bean
	@StepScope
	@Qualifier("fileTasklet")
	public Tasklet fileTasklet() {
		LookForFileTasklet fileTasklet = new LookForFileTasklet();
        return fileTasklet;
	}

	@Bean
	@StepScope
	@Qualifier("saveTasklet")
	public Tasklet saveTasklet(@Value("#{jobExecutionContext[idExp]}") Long idExpFile) {
		NotifyRecordWriterTasklet saveTasklet = new NotifyRecordWriterTasklet(idExpFile);
        return saveTasklet;
	}

	@Bean(name = "PlannerJob")
	public Job plannerJob(
			@Qualifier("notifyItemReader") Step notifyStep,
			@Qualifier("recordSaveTasklet") Step recordSaveTasklet
			)  {
	    return jobs.get("plannerJob")
	  	   .start(lookForLastDateTasklet())
		   .next(lookForFileTasklet())
	      .next(notifyStep)
	      .next(recordSaveTasklet)
	    .build();
	}
	
	@Bean
	@Qualifier("notifyItemReader")
	public Step notifyStep(
			ItemStreamReader<CSVItem> notifyItemReader,
			ItemProcessor<CSVItem,CSVExpiration> notifyItemProcessor,
			ItemWriter<CSVExpiration> notifyItemWriter)  {
	    return steps.get("notifyStep")
	    		.<CSVItem, CSVExpiration>chunk(10)
	    		.reader(notifyItemReader)
	    		.processor(notifyItemProcessor)
	    		.writer(notifyItemWriter)
	      .build();
	}

	@Bean
	@Qualifier("lookForLastDateTasklet")
	public Step lookForLastDateTasklet() {
		return steps.get("lookForLastDateTasklet")
				.tasklet(dateTasklet())
				.build();
	}
	
	@Bean
	@Qualifier("lookForFileTasklet")
	public Step lookForFileTasklet() {
		return steps.get("lookForFileTasklet")
				.tasklet(fileTasklet())
				.build();
	}
	@Bean
	@Qualifier("recordSaveTasklet")
	public Step recordSaveTasklet(Tasklet saveTasklet) {
		return steps.get("recordSaveTasklet")
				.tasklet(saveTasklet)
				.build();
	}

}
