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
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import it.govhub.govio.planner.api.entity.ExpirationFileEntity;
import it.govhub.govio.planner.api.entity.ExpirationFileEntity_;
import it.govhub.govio.planner.api.entity.GovioPlannerFileEntity;
import it.govhub.govio.planner.api.entity.GovioPlannerFileEntity_;

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

		public static Specification<ExpirationFileEntity> byGovioFilename(String name) {
			return (Root<ExpirationFileEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
				Path<String> fileNameNode = root.join(ExpirationFileEntity_.govioFiles).get(GovioPlannerFileEntity_.name);
				return cb.like(cb.lower(fileNameNode), "%"+name.toLowerCase()+"%");
			};
		}
		
		

}
