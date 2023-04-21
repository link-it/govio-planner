package it.govhub.govio.planner.batch.entity;

import java.nio.file.Path;
import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

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
@Table(name = "govio_planner_ntfy_files")
public class GovioFileProducedEntity {

        public enum Status {CREATED, PROCESSING, PROCESSED}
        
        @Id 
        @SequenceGenerator(name="seq_govio_planner_govio_files",sequenceName="seq_govio_planner_govio_files", initialValue=1, allocationSize=1)
        @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="seq_govio_planner_govio_files")
        private Long id;
        
        @Column(name = "creation_date", nullable = false)
        private OffsetDateTime creationDate;
        	
        @Column(name = "status", nullable = false)
        @Enumerated(EnumType.STRING)
        private Status status;
        
        @Column(name = "location", nullable = false)
        private String location;

        @Column(name = "size")
        private Long size;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "id_govio_planner_file", nullable = false, foreignKey = @ForeignKey(name = "GovioFileProducedEntity_GovioPlannerFile"))
        private ExpirationCIEFileEntity expirationFile;
}
