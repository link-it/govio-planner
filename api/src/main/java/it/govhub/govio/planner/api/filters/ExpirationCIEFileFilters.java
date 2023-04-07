package it.govhub.govio.planner.api.filters;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import it.govhub.govio.planner.api.entity.ExpirationCIEFileEntity;

public class ExpirationCIEFileFilters {

		private ExpirationCIEFileFilters() {}
		
		public static Specification<ExpirationCIEFileEntity> empty() {
			return (Root<ExpirationCIEFileEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> null; 
		}
		
		

}
