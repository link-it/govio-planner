package it.govhub.govio.planner.batch.step;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;

import it.govhub.govio.planner.batch.bean.CSVExpiration;

import it.govhub.govio.planner.batch.repository.*;


public class NotifyItemWriter implements ItemWriter<CSVExpiration> {
	@Value("${planner.ntfy.csv-dir}")
	private String notifyFile;

	private Logger logger = LoggerFactory.getLogger(NotifyItemWriter.class);

	GovioFileProducedRepository govioFileProducedRepository;
	@Override
	public void write(List<? extends CSVExpiration> items) throws Exception {
    	String file = notifyFile+"CIE_EXPIRATION_"+LocalDate.now()+".csv";
        File myObj = new File(file);
        myObj.createNewFile();
	    try {
	        FileWriter myWriter = new FileWriter(myObj);
			for (int i = 0; i<items.size(); i++) {
				CSVExpiration exp = items.get(i);
				myWriter.write(exp.getTaxCode()+";"+exp.getExpeditionDate()+";"+exp.getDueDate()+";"+exp.getDueDate()+";"+exp.getFullName()+";"+exp.getIdentityCardNumber()+";"+exp.getReleaseDate()+";"+exp.getForewarning()+'\n');
		      }
	    }catch (IOException e) {
			logger.error("Ricevuta IOException durante la scrittura del file {}",file);
	      }
	    }
}