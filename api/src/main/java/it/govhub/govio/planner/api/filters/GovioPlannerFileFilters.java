/*******************************************************************************
 *  GovIO Planner - Notification system Planner for AppIO
 *  
 *  Copyright (c) 2021-2023 Link.it srl (http://www.link.it).
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 3, as published by
 *  the Free Software Foundation.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *******************************************************************************/
package it.govhub.govio.planner.api.filters;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import it.govhub.govio.planner.api.entity.ExpirationFileEntity_;
import it.govhub.govio.planner.api.entity.GovioPlannerFileEntity;
import it.govhub.govio.planner.api.entity.GovioPlannerFileEntity_;

public class GovioPlannerFileFilters {

		private GovioPlannerFileFilters() {}
		
		public static Specification<GovioPlannerFileEntity> empty() {
			return (Root<GovioPlannerFileEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> null; 
		}

		
		public static Specification<GovioPlannerFileEntity>likeExpirationFilename(String q) {
			return (Root<GovioPlannerFileEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
				var expirationNamePath = root.get(GovioPlannerFileEntity_.expirationFile).get(ExpirationFileEntity_.name);
				return cb.like(cb.lower(expirationNamePath),	"%"+q.toLowerCase()+"%");
			};
		}

		
		public static Specification<GovioPlannerFileEntity> likeGovioFilename(String q) {
			return (Root<GovioPlannerFileEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
				var namePath = root.get(GovioPlannerFileEntity_.name);
				return cb.like(cb.lower(namePath),	"%"+q.toLowerCase()+"%");
			};
		}

		public static Specification<GovioPlannerFileEntity> byExpirationFileId(Long expirationFileId) {
			return (Root<GovioPlannerFileEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
				var expirationIdPath = root.get(GovioPlannerFileEntity_.expirationFile).get(ExpirationFileEntity_.id);
				return cb.equal(expirationIdPath, expirationFileId);
			};
		}

}
