package it.govhub.govio.planner.api.filters;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import it.govhub.govio.planner.api.entity.ExpirationFileEntity;

public class ExpirationFileFilters {

		private ExpirationFileFilters() {}
		
		public static Specification<ExpirationFileEntity> empty() {
			return (Root<ExpirationFileEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> null; 
		}

		public static Specification<ExpirationFileEntity> byName(String string) {
			// TODO Auto-generated method stub
			return null;
		}

		public static Specification<ExpirationFileEntity> byGovioFilename(String string) {
			// TODO Auto-generated method stub
			return null;
		}
		
		

}
