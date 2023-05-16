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
package it.govhub.govio.planner.batch.jobs;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDate;

import javax.persistence.EntityManager;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import it.govhub.govio.planner.batch.bean.CSVExpiration;
import it.govhub.govio.planner.batch.bean.CSVItem;
import it.govhub.govio.planner.batch.repository.ExpirationCIEFileRepository;
import it.govhub.govio.planner.batch.repository.GovioFileProducedRepository;
import it.govhub.govio.planner.batch.step.FileInsertTasklet;
import it.govhub.govio.planner.batch.step.LookForFileTasklet;
import it.govhub.govio.planner.batch.step.LookForLastDateTasklet;
import it.govhub.govio.planner.batch.step.NotifyItemProcessor;

@Configuration
@EnableBatchProcessing
public class GovioPlannerJob {
	@Value("${planner.ntfy.csv-dir}")
	private String notifyFile;

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
	
	public static final String PLANNERJOB= "PlannerJob";


	@Bean
	@StepScope
	@Qualifier("notifyItemReader")
	public FlatFileItemReader<CSVItem> notifyItemReader(@Value("#{jobExecutionContext[location]}") String filename) {
		FlatFileItemReader<CSVItem> itemReader = new FlatFileItemReader<>();
		  //Set input file location
		itemReader.setResource(new FileSystemResource(filename));
		  //Set number of lines to skips. Use it if file has header rows.
		itemReader.setLinesToSkip(1);
		  //Configure how each line will be parsed and mapped to different values
		itemReader.setStrict(false);
		DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
		tokenizer.setDelimiter(";");
		tokenizer.setNames(new String[] { "fullName", "birthdate", "address","identityCardNumber","dueDate","CAP","age","gender","releaseDate","taxCode","codPositionAnagrafica"});
		DefaultLineMapper<CSVItem> lineMapper = new DefaultLineMapper<>();
		lineMapper.setLineTokenizer(tokenizer);
		//Set values in CSVItem class
		BeanWrapperFieldSetMapper<CSVItem> setMapper = new BeanWrapperFieldSetMapper<>();
		setMapper.setTargetType(CSVItem.class);
		lineMapper.setFieldSetMapper(setMapper);
		itemReader.setLineMapper(lineMapper);
		return itemReader;
	}
	
// per personalizzare il nome degli header del csv
	public class MyFlatFileWriter extends FlatFileItemWriter<CSVExpiration> {

	    public MyFlatFileWriter (){
	        super.setHeaderCallback(new FlatFileHeaderCallback() {

				@Override
	            public void writeHeader(Writer writer) throws IOException {
					writer.write("tax_code;expedition_date;due_date;full_name;identity_card_number;release_date;forewarning");
	            }
	        });
	    }
	}

	/*
	 * 
	 * Scrittura del file csv contenente le nuove notifiche
	 * 	
	 */
	  @Bean
	  @StepScope
	  @Qualifier("notifyItemWriter")
	  public FlatFileItemWriter<CSVExpiration> notifyItemWriter() throws IOException
	  {
	    //Create writer instance
	    FlatFileItemWriter<CSVExpiration> filewriter = new MyFlatFileWriter();
	    
	    String filename = notifyFile+"CIE_EXPIRATION_"+LocalDate.now()+".csv";
	    File file = new File(filename);
	    if (!file.exists()) file.createNewFile();
	    if (!file.canWrite()) {
            throw new ItemStreamException("File is not writable: [" + file.getAbsolutePath() + "]");
	    }

	    //Set output file location
	    filewriter.setResource(new FileSystemResource(filename));
	    
	    //All job repetitions should "append" to same output file TODO TESTA
	    filewriter.setAppendAllowed(true);

	    //Name field values sequence based on object properties 
	    filewriter.setLineAggregator(new DelimitedLineAggregator<CSVExpiration>() {
	      {
	    	  // TODO: mettere ,
	        setDelimiter(";");
	        setFieldExtractor(new BeanWrapperFieldExtractor<CSVExpiration>() {
	          {
	            setNames(new String[] {
	            		"taxCode", "expeditionDate", "dueDate","fullName","identityCardNumber","releaseDate","forewarning"
	            		});
	          }
	        });
	      }
	    });
	    return filewriter;
	  }

	
	@Bean
	@StepScope
	@Qualifier("notifyItemProcessor")
	public ItemProcessor<CSVItem,CSVExpiration> notifyItemProcessor(
			@Value("#{jobExecutionContext[date]}") long date,
			@Value("#{jobExecutionContext[expeditionDate]}") long expeditionDate) {
	 		return new NotifyItemProcessor(date,expeditionDate);
	}

	@Bean
	@StepScope
	@Qualifier("dateTasklet")
	public Tasklet dateTasklet() {
        return new LookForLastDateTasklet();
	}
	
	@Bean
	@StepScope
	@Qualifier("fileTasklet")
	public Tasklet fileTasklet() {
        return new LookForFileTasklet();
	}


	@Bean
	@StepScope
	@Qualifier("insertTasklet")
	public Tasklet fileInsert() {
        return new FileInsertTasklet();
	}
	
	@Bean(name = "PlannerJob")
	public Job plannerJob(
			@Qualifier("notifyStep") Step notifyStep
			)  {
	    return jobs.get("plannerJob")
	  	   .start(lookForLastDateTasklet())
		   .next(lookForFileTasklet())
	      .next(notifyStep)
	      .next(fileInsertTasklet())
	      .build();
	}
	
	@Bean
	@Qualifier("notifyStep")
	public Step notifyStep(
			ItemStreamReader<CSVItem> notifyItemReader,
			ItemProcessor<CSVItem,CSVExpiration> notifyItemProcessor,
			ItemStreamWriter<CSVExpiration> notifyItemWriter)  {
	    return steps.get("notifyStep")
	    		.<CSVItem, CSVExpiration>chunk(10)
	    		.reader(notifyItemReader)
	    		.processor(notifyItemProcessor)
	    		.writer(notifyItemWriter)
	      .build();
	}
	
	@Bean
	@Qualifier("fileInsertTasklet")
	public Step fileInsertTasklet() {
		return steps.get("fileInsertTasklet")
				.tasklet(fileInsert())
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
}
