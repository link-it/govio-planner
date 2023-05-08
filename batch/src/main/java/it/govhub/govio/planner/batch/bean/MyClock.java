package it.govhub.govio.planner.batch.bean;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

@Component("it.govhub.govio.planner.batch.bean")
public class MyClock {
	public LocalDate now () {
		return LocalDate.now();
	}
}
