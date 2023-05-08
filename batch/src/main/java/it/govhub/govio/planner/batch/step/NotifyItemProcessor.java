package it.govhub.govio.planner.batch.step;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.govhub.govio.planner.batch.bean.CSVItem;
import it.govhub.govio.planner.batch.bean.CSVExpiration;
import it.govhub.govio.planner.batch.bean.MyClock;

/*
 * Classe che processa gli elementi del file csv, selezionando le righe la cui data di scadenza rientra nella finestra di preavviso specificata nel file di properties.
 * Viene controllata dal preavviso più recente al più remoto. Se una entry risulta sintatticamente non valida, viene ignorata e viene loggato un messaggio di warning.
 * I formati delle date sono configurabili nelle application properties 
 */
public class NotifyItemProcessor implements ItemProcessor<CSVItem, CSVExpiration> {
	@Value("${planner.ntfy.policy}")
	private String policy;
	@Value("${planner.ntfy.formatLastNotify}")
	private String formatLastNotify;
	@Value("${planner.ntfy.formatDueDate}")
	private String formatDueDate;
	@Value("${planner.ntfy.formatExpeditionDate}")
	private String formatExpeditionDate;

	private Logger logger = LoggerFactory.getLogger(NotifyItemProcessor.class);

	private String dataUltimaNotifica;
	private String expeditionDate;
	
	public NotifyItemProcessor(String dataUltimaNotifica,String expeditionDate){
		this.dataUltimaNotifica = dataUltimaNotifica;
		this.expeditionDate = expeditionDate;
	}

	@Autowired
	MyClock myClock;
	
	@Override
	public CSVExpiration process(CSVItem item) {
		// controlla che i valori della riga del csv siano tutti sintatticamente validi in caso contrario salta la riga
		if (item.validate(item) == false) return null;
		logger.info("Riga: {} validata con successo",item);

		dataUltimaNotifica = StringUtils.substringBefore(dataUltimaNotifica, 'T');

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatLastNotify);
		LocalDate lastMessage = LocalDate.parse(dataUltimaNotifica, formatter);
		formatter = DateTimeFormatter.ofPattern(formatDueDate);
		LocalDate dueDate = LocalDate.parse(item.getDueDate(),formatter);
		formatter = DateTimeFormatter.ofPattern(formatExpeditionDate);
		LocalDateTime expDate = LocalDateTime.parse(expeditionDate);

		String[] splits = policy.split(",");
		for (int i = splits.length-1; i>=0; i--) {
			CSVExpiration res = compareDates(lastMessage, expDate, dueDate, Integer.valueOf(splits[i]),item);
			if (res != null) {
				logger.info("Riga: {} aggiunta alle righe da inserire nel CSV",item);
				return res;
			}
		  }
		logger.info("Riga: {} saltata perchè la scadenza non rientra nelle finestre di preavviso",item);
		return null;
	}
	
	private CSVExpiration compareDates(LocalDate lastMessage,LocalDateTime expeditionDate, LocalDate dueDate, int days, CSVItem item) {
		if (
				(lastMessage.plusDays(days).compareTo(dueDate) <= 0)
				&&
				(myClock.now().plusDays(days).compareTo(dueDate) >= 0)
				)
			return new CSVExpiration(item.getTaxCode(),expeditionDate.toString(),item.getDueDate(),item.getFullName(),item.getIdentityCardNumber(),item.getReleaseDate(),Integer.toString(days));
		return null;
	}
	
}

