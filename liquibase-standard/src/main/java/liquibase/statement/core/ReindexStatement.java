package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class ReindexStatement extends AbstractSqlStatement {

    private liquibase.statement.core.DatabaseTableIdentifier databaseTableIdentifier = new liquibase.statement.core.DatabaseTableIdentifier(null, null, null);

    public ReindexStatement(String catalogName, String schemaName, String tableName) {
        this.databaseTableIdentifier.setCatalogName(catalogName);
		this.databaseTableIdentifier.setSchemaName(schemaName);
        this.databaseTableIdentifier.setTableName(tableName);
	}

    public String getCatalogName() {
        return databaseTableIdentifier.getCatalogName();
    }

    public String getSchemaName() {
        return databaseTableIdentifier.getSchemaName();
    }

    public String getTableName() {
        return databaseTableIdentifier.getTableName();
    }
}
