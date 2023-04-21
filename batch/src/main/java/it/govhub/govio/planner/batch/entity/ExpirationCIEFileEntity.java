package it.govhub.govio.planner.batch.entity;


import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import java.nio.file.Path;
import javax.persistence.Convert;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "govio_planner_exp_files")
public class ExpirationCIEFileEntity {        public enum Status {CREATED, PROCESSING, PROCESSED}

@Id 
@SequenceGenerator(name="seq_govio_planner_files",sequenceName="seq_govio_planner_files", initialValue=1, allocationSize=1)
@GeneratedValue(strategy= GenerationType.SEQUENCE, generator="seq_govio_planner_files")
private Long id;

@Column(name = "name", nullable = false)
private String name;

@Column(name = "location",  length = 2048, nullable = false)
private String location;

@Column(name = "creation_date", nullable = false)
private OffsetDateTime creationDate;

}
