package it.govhub.govio.planner.api.filters;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import it.govhub.govio.planner.api.entity.GovioPlannerFileEntity;

public class GovioPlannerFileFilters {

		private GovioPlannerFileFilters() {}
		
		public static Specification<GovioPlannerFileEntity> empty() {
			return (Root<GovioPlannerFileEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> null; 
		}

}
