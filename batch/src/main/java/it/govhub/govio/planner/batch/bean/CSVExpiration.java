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
	String tax_code;
	String expedition_date;
	String due_date;
    String full_name;
    String identity_card_number;
    String release_date;
    String forewarning;
}