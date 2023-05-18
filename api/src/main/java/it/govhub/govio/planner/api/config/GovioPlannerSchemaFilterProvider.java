package it.govhub.govio.planner.api.config;

import org.hibernate.tool.schema.spi.SchemaFilter;
import org.hibernate.tool.schema.spi.SchemaFilterProvider;

public class GovioPlannerSchemaFilterProvider implements SchemaFilterProvider {

	@Override
	public SchemaFilter getCreateFilter() {
		return GovioPlannerSchemaFilter.INSTANCE;
	}

	@Override
	public SchemaFilter getDropFilter() {
		return GovioPlannerSchemaFilter.INSTANCE;
	}

	@Override
	public SchemaFilter getMigrateFilter() {
		return GovioPlannerSchemaFilter.INSTANCE;
	}

	@Override
	public SchemaFilter getValidateFilter() {
		return GovioPlannerSchemaFilter.INSTANCE;
	}

}
