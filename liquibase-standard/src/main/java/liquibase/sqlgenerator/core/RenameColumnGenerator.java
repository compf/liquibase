package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.DatabaseException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.RenameColumnStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import liquibase.util.StringUtil;

public class RenameColumnGenerator extends AbstractSqlGenerator<RenameColumnStatement> {

    @Override
    public boolean supports(RenameColumnStatement statement, Database database) {
        if(database instanceof  SQLiteDatabase) {
            try {
                if(database.getDatabaseMajorVersion() <= 3 && database.getDatabaseMinorVersion() < 25) {
                    return false;
                }
            }
            catch (DatabaseException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    @Override
    public ValidationErrors validate(RenameColumnStatement renameColumnStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", renameColumnStatement.databaseTableIdentifier.getGetTableName()());
        validationErrors.checkRequiredField("oldColumnName", renameColumnStatement.getOldColumnName());
        validationErrors.checkRequiredField("newColumnName", renameColumnStatement.getNewColumnName());

        if (database instanceof MySQLDatabase) {
            validationErrors.checkRequiredField("columnDataType", StringUtil.trimToNull(renameColumnStatement.getColumnDataType()));
        }

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(RenameColumnStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String sql;
        if (database instanceof MSSQLDatabase) {
        	// do no escape the new column name. Otherwise it produce "exec sp_rename '[dbo].[person].[usernae]', '[username]'"
            sql = "exec sp_rename '" + database.escapeTableName(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()(), statement.databaseTableIdentifier.getGetTableName()()) + "." + database.escapeColumnName(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()(), statement.databaseTableIdentifier.getGetTableName()(), statement.getOldColumnName()) + "', '" + statement.getNewColumnName() + "', 'COLUMN'";
        } else if (database instanceof MySQLDatabase) {
            sql ="ALTER TABLE " + database.escapeTableName(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()(), statement.databaseTableIdentifier.getGetTableName()()) + " CHANGE " + database.escapeColumnName(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()(), statement.databaseTableIdentifier.getGetTableName()(), statement.getOldColumnName()) + " " + database.escapeColumnName(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()(), statement.databaseTableIdentifier.getGetTableName()(), statement.getNewColumnName()) + " " + DataTypeFactory.getInstance().fromDescription(statement.getColumnDataType(), database).toDatabaseDataType(database);
        } else if (database instanceof SybaseDatabase) {
            sql = "exec sp_rename '" + statement.databaseTableIdentifier.getGetTableName()() + "." + statement.getOldColumnName() + "', '" + statement.getNewColumnName() + "'";
        } else if ((database instanceof HsqlDatabase) || (database instanceof H2Database)) {
            sql ="ALTER TABLE " + database.escapeTableName(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()(), statement.databaseTableIdentifier.getGetTableName()()) + " ALTER COLUMN " + database.escapeColumnName(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()(), statement.databaseTableIdentifier.getGetTableName()(), statement.getOldColumnName()) + " RENAME TO " + database.escapeColumnName(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()(), statement.databaseTableIdentifier.getGetTableName()(), statement.getNewColumnName());
        } else if (database instanceof FirebirdDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()(), statement.databaseTableIdentifier.getGetTableName()()) + " ALTER COLUMN " + database.escapeColumnName(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()(), statement.databaseTableIdentifier.getGetTableName()(), statement.getOldColumnName()) + " TO " + database.escapeColumnName(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()(), statement.databaseTableIdentifier.getGetTableName()(), statement.getNewColumnName());
        } else if (
                // supported in Derby from version 10.3.1.4 (see "http://issues.apache.org/jira/browse/DERBY-1490")
                (database instanceof DerbyDatabase)
                || (database instanceof InformixDatabase)) {
          sql = "RENAME COLUMN " + database.escapeTableName(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()(), statement.databaseTableIdentifier.getGetTableName()()) + "." + database.escapeColumnName(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()(), statement.databaseTableIdentifier.getGetTableName()(), statement.getOldColumnName()) + " TO " + database.escapeColumnName(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()(), statement.databaseTableIdentifier.getGetTableName()(), statement.getNewColumnName());
        } else if (database instanceof SybaseASADatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()(), statement.databaseTableIdentifier.getGetTableName()()) + " RENAME " + database.escapeColumnName(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()(), statement.databaseTableIdentifier.getGetTableName()(), statement.getOldColumnName()) + " TO " + database.escapeColumnName(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()(), statement.databaseTableIdentifier.getGetTableName()(), statement.getNewColumnName());
        } else if (database instanceof AbstractDb2Database) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()(), statement.databaseTableIdentifier.getGetTableName()()) + " RENAME COLUMN " + database.escapeColumnName(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()(), statement.databaseTableIdentifier.getGetTableName()(), statement.getOldColumnName()) + " TO " + database.escapeColumnName(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()(), statement.databaseTableIdentifier.getGetTableName()(), statement.getNewColumnName());
        } else {
            sql = "ALTER TABLE " + database.escapeTableName(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()(), statement.databaseTableIdentifier.getGetTableName()()) + " RENAME COLUMN " + database.escapeColumnName(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()(), statement.databaseTableIdentifier.getGetTableName()(), statement.getOldColumnName()) + " TO " + database.escapeColumnName(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()(), statement.databaseTableIdentifier.getGetTableName()(), statement.getNewColumnName());
        }

        if((database instanceof MySQLDatabase) && (statement.getRemarks() != null)) {
            sql += " COMMENT '" + statement.getRemarks() +"' ";
        }

        return new Sql[] {
                new UnparsedSql(sql, getAffectedOldColumn(statement), getAffectedNewColumn(statement))
        };
    }

    protected Column getAffectedOldColumn(RenameColumnStatement statement) {
        return new Column().setName(statement.getOldColumnName()).setRelation(new Table().setName(statement.databaseTableIdentifier.getGetTableName()()).setSchema(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()()));
    }

    protected Column getAffectedNewColumn(RenameColumnStatement statement) {
        return new Column().setName(statement.getNewColumnName()).setRelation(new Table().setName(statement.databaseTableIdentifier.getGetTableName()()).setSchema(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()()));
    }
}
