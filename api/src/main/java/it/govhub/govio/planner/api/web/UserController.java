/*
 * GovHub - Application suite for Public Administration
 *
 * Copyright (c) 2023-2024 Link.it srl (https://www.link.it).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3, as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.govhub.govio.planner.api.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;

import it.govhub.govregistry.commons.api.beans.User;
import it.govhub.govregistry.commons.api.beans.UserList;
import it.govhub.govregistry.commons.api.beans.UserOrdering;
import it.govhub.govregistry.commons.config.V1RestController;
import it.govhub.govregistry.readops.api.spec.UserApi;
import it.govhub.govregistry.readops.api.web.ReadUserController;

@V1RestController
public class UserController implements UserApi {
	
	@Autowired
	ReadUserController readUserController;

	@Override
	public ResponseEntity<UserList> listUsers(UserOrdering sort, Direction sortDirection, Integer limit, Long offset,	String q, Boolean enabled) {
		return this.readUserController.listUsers(sort, sortDirection, limit, offset, q, enabled);
	}

	@Override
	public ResponseEntity<User> readUser(Long id) {
		return this.readUserController.readUser(id);
	}

}
