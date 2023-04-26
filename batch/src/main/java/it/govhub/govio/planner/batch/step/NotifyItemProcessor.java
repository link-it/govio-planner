package it.govhub.govio.planner.batch.step;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;

import it.govhub.govio.planner.batch.bean.CSVItem;
import it.govhub.govio.planner.batch.bean.CSVExpiration;


public class NotifyItemProcessor implements ItemProcessor<CSVItem, CSVExpiration> {
	@Value("${planner.ntfy.policy}")
	private String policy;
	@Value("${planner.ntfy.formatLastNotify}")
	private String formatLastNotify;
	@Value("${planner.ntfy.formatDueDate}")
	private String formatDueDate;
	@Value("${planner.ntfy.date-time}")
	private String dateTime;
	@Value("${planner.ntfy.dalay-days}")
	private long daysDelay;
	@Value("${planner.ntfy.dalay-time}")
	private long hoursDelay;
	

	
	private String dataUltimaNotifica;
	public NotifyItemProcessor(String dataUltimaNotifica){
		this.dataUltimaNotifica = dataUltimaNotifica;
	}
	
	@Override
	public CSVExpiration process(CSVItem item) throws Exception {
		dataUltimaNotifica = StringUtils.substringBefore(dataUltimaNotifica, 'T');
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatLastNotify);
		LocalDate lastMessage = LocalDate.parse(dataUltimaNotifica, formatter);
		  
		formatter = DateTimeFormatter.ofPattern(formatDueDate);
		LocalDate dueDate = LocalDate.parse(item.getDueDate(),formatter);

		String[] splits = policy.split(",");
		for (int i = splits.length-1; i>0; i--) {
			CSVExpiration res = compareDates(lastMessage, dueDate, Integer.valueOf(splits[i]),item);
			if (res != null) return res;
		  }
		return null;
	}
	
	private CSVExpiration compareDates(LocalDate lastMessage, LocalDate dueDate, int days, CSVItem item) {
		LocalTime expeditionHour = ((dateTime==null) ? LocalTime.NOON : LocalTime.parse(dateTime));
		// calcola la data e l'orario di spedizione, sommando ai valori di default e i parametri di configurazione
		LocalDateTime expeditionDate = LocalDateTime.of(LocalDate.now().plusDays(daysDelay), expeditionHour.plusHours(hoursDelay));
		if (
				(lastMessage.plusDays(days).compareTo(dueDate) <= 0)
				&& 
				(LocalDate.now().plusDays(days).compareTo(dueDate) >= 0)
				)
			return new CSVExpiration(item.getTaxCode(),expeditionDate,item.getDueDate(),item.getFullName(),item.getIdentityCardNumber(),item.getReleaseDate(),days);
		else return null;
	}
}
