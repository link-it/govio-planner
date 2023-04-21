package it.govhub.govio.planner.batch.repository;

import java.time.OffsetDateTime;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import it.govhub.govio.planner.batch.entity.GovioFileProducedEntity;

public interface GovioFileProducedRepository extends JpaRepositoryImplementation<GovioFileProducedEntity,Long>{

	@Query(value = "SELECT a.creationDate from GovioFileProducedEntity a WHERE (SELECT MAX(b.creationDate) from GovioFileProducedEntity b) = a.creationDate")
	public OffsetDateTime lastDateNotifyFile();
}
