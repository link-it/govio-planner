package it.govhub.govio.planner.api.filters;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import it.govhub.govio.planner.api.entity.ExpirationFileEntity;
import it.govhub.govio.planner.api.entity.ExpirationFileEntity_;

public class ExpirationFileFilters {

		private ExpirationFileFilters() {}
		
		public static Specification<ExpirationFileEntity> empty() {
			return (Root<ExpirationFileEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> null; 
		}

		public static Specification<ExpirationFileEntity> byName(String name) {
			return (Root<ExpirationFileEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
				cb.like(
						cb.lower(root.get(ExpirationFileEntity_.name)),
						"%"+name.toLowerCase()+"%");
		}

		public static Specification<ExpirationFileEntity> byGovioFilename(String string) {
			// TODO Auto-generated method stub
			return null;
		}
		
		

}
