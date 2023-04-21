package it.govhub.govio.planner.batch.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govhub.govio.planner.batch.entity.ExpirationCIEFileEntity;

public interface ExpirationCIEFileRepository extends JpaRepositoryImplementation<ExpirationCIEFileEntity,Long>{

	@Query(value = "SELECT a from ExpirationCIEFileEntity a WHERE (SELECT MAX(b.creationDate) from ExpirationCIEFileEntity b) = a.creationDate")
	public ExpirationCIEFileEntity lastExpirationFile();
}
