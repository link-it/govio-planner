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
