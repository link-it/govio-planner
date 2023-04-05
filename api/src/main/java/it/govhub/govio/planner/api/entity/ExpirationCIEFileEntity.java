package it.govhub.govio.planner.api.entity;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.commons.utils.JpaPathConverter;
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
@Table(name = "govio_planner_files")
public class ExpirationCIEFileEntity {
	
	public enum Status {CREATED, PROCESSING, PROCESSED}
	
	@Id 
	@SequenceGenerator(name="seq_govio_planner_files",sequenceName="seq_govio_planner_files", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy= GenerationType.SEQUENCE, generator="seq_govio_planner_files")
	private Long id;
	
	@ManyToOne
 	@JoinColumn(name = "id_govauth_user", nullable=false, foreignKey = @ForeignKey(name = "GovioFile_GovhubUser"))
	private UserEntity uploaderUser;
	
	@Column(name = "name", nullable = false)
	private String name;
	
	@Convert(converter = JpaPathConverter.class)
	@Column(name = "location",  length = 2048, nullable = false)
	private Path location;
	
	@Column(name = "creation_date", nullable = false)
	private OffsetDateTime creationDate;
	
	@Column(name = "processing_date")
	private OffsetDateTime processingDate;
	
	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private Status status;
	
	@Column(name = "size")
	private Long size;

	@OneToMany(mappedBy = "expirationFile")
	@Builder.Default
	private Set<GovioFileProducedEntity> govioFiles = new HashSet<>();

}
