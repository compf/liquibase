package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.DropDefaultValueStatement;

public class DropDefaultValueGeneratorSnowflake extends DropDefaultValueGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(DropDefaultValueStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public Sql[] generateSql(DropDefaultValueStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[]{
                new UnparsedSql(
                        "ALTER TABLE "
                                + database.escapeTableName(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()(), statement.databaseTableIdentifier.getGetTableName()())
                                + " ALTER COLUMN "
                                + database.escapeColumnName(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()(),
                                statement.databaseTableIdentifier.getGetTableName()(), statement.getColumnName())
                                + " DROP DEFAULT")};
    }

}
