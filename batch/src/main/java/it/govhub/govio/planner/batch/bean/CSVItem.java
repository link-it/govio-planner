package it.govhub.govio.planner.batch.bean;


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
public class CSVItem {
	String fullName;
	String date;
	String address;
	String identityCardNumber;
	String dueDate;
	String CAP;
	String age;
	String gender;
	String releaseDate;
	String taxCode;
	String codPositionAnagrafica;
}
