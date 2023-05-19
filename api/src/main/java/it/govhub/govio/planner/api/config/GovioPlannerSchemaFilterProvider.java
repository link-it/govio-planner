package it.govhub.govio.planner.api.config;

import org.hibernate.tool.schema.spi.SchemaFilter;
import org.hibernate.tool.schema.spi.SchemaFilterProvider;


/**
 * TODO: Questo mettilo in commons, dove verranno escluse tutte le sue tabelle, il planner e govio nell'application.properties
 * 
 * lo useranno così: spring.jpa.properties.hibernate.hbm2ddl.schema_filter_provider=it.govhub.govio.planner.api.config.GovioPlannerSchemaFilterProvider
 * ma con il package di commons
 * @author froggo
 *
 */
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
