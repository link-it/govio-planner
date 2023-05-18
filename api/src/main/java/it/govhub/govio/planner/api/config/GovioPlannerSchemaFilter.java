package it.govhub.govio.planner.api.config;

import org.hibernate.boot.model.relational.Namespace;
import org.hibernate.mapping.Table;
import org.hibernate.tool.schema.spi.SchemaFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GovioPlannerSchemaFilter implements SchemaFilter {
	
	public static final GovioPlannerSchemaFilter INSTANCE = new GovioPlannerSchemaFilter();
	
	private Logger log = LoggerFactory.getLogger(GovioPlannerSchemaFilter.class);

    @Override
    public boolean includeNamespace(Namespace namespace) {
        return true;
    }

    @Override
    public boolean includeTable(Table table) {
    	this.log.info("Should include table {} ?", table.getName());
    	return false;
    }

	@Override
	public boolean includeSequence(org.hibernate.boot.model.relational.Sequence sequence) {
		return true;
	}


}
