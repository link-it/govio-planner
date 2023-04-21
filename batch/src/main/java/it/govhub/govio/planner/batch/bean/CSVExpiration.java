package it.govhub.govio.planner.batch.bean;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class CSVExpiration {
	String taxCode;
	LocalDateTime expeditionDate;
	String dueDate;
    String fullName;
    String identityCardNumber;
    String releaseDate;
    int forewarning;
}
