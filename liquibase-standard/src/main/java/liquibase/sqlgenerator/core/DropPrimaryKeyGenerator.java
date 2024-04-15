package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.DropPrimaryKeyStatement;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;

public class DropPrimaryKeyGenerator extends AbstractSqlGenerator<DropPrimaryKeyStatement> {

    @Override
    public boolean supports(DropPrimaryKeyStatement statement, Database database) {
        return (!(database instanceof SQLiteDatabase));
    }

    @Override
    public ValidationErrors validate(DropPrimaryKeyStatement dropPrimaryKeyStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", dropPrimaryKeyStatement.databaseTableIdentifier.getGetTableName()());

        if ((database instanceof FirebirdDatabase) || (database instanceof InformixDatabase) || (database instanceof
            SybaseDatabase)) {
            validationErrors.checkRequiredField("constraintName", dropPrimaryKeyStatement.getConstraintName());
        }

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(DropPrimaryKeyStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String sql;
        if (database instanceof MSSQLDatabase) {
            String escapedTableName = database.escapeTableName(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()(), statement.databaseTableIdentifier.getGetTableName()());
            if (statement.getConstraintName() == null) {
                sql =
                        "DECLARE @sql [nvarchar](MAX)\r\n" +
                        "SELECT @sql = N'ALTER TABLE " + database.escapeStringForDatabase(escapedTableName) + " DROP CONSTRAINT ' + QUOTENAME([kc].[name]) " +
                        "FROM [sys].[key_constraints] AS [kc] " +
                        "WHERE [kc].[parent_object_id] = OBJECT_ID(N'" + database.escapeStringForDatabase(escapedTableName) +  "') " +
                        "AND [kc].[type] = 'PK'\r\n" +
                        "EXEC sp_executesql @sql";
            } else {
                sql = "ALTER TABLE " + escapedTableName + " DROP CONSTRAINT " + database.escapeConstraintName(statement.getConstraintName());
            }
        } else if (database instanceof PostgresDatabase) {
			if (statement.getConstraintName() == null) {
				String schemaName = (statement.databaseTableIdentifier.getGetSchemaName()() != null) ? statement.databaseTableIdentifier.getGetSchemaName()() : database
                    .getDefaultSchemaName();
				schemaName = database.correctObjectName(schemaName, Schema.class);
				String tableName = database.correctObjectName(statement.databaseTableIdentifier.getGetTableName()(), Table.class);

				sql = String.format(""
						+ "DO $$ DECLARE constraint_name varchar;\n"
						+ "BEGIN\n"
						+ "  SELECT tc.CONSTRAINT_NAME into strict constraint_name\n"
						+ "    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc\n"
						+ "    WHERE CONSTRAINT_TYPE = 'PRIMARY KEY'\n"
						+ "      AND TABLE_NAME = '%2$s' AND TABLE_SCHEMA = '%1$s';\n"
						+ "    EXECUTE 'alter table %3$s.%4$s drop constraint \"' || constraint_name || '\"';\n"
						+ "END $$;"
						, schemaName, tableName
						, database.escapeObjectName(schemaName, Schema.class), database.escapeObjectName(tableName, Table.class));
			} else {
				sql = "ALTER TABLE " + database.escapeTableName(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()(), statement.databaseTableIdentifier.getGetTableName()()) + " DROP CONSTRAINT " + database.escapeConstraintName(statement.getConstraintName());
			}
        } else if (database instanceof FirebirdDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()(), statement.databaseTableIdentifier.getGetTableName()()) + " DROP CONSTRAINT "+database.escapeConstraintName(statement.getConstraintName());
        } else if (database instanceof OracleDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()(), statement.databaseTableIdentifier.getGetTableName()()) + " DROP PRIMARY KEY";
            if ((statement.getDropIndex() == null) || statement.getDropIndex()) {
                sql += " DROP INDEX";
            } else {
                sql += " KEEP INDEX";
            }
        } else if (database instanceof InformixDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()(), statement.databaseTableIdentifier.getGetTableName()()) + " DROP CONSTRAINT " + database.escapeConstraintName(statement.getConstraintName());
        } else if (database instanceof SybaseDatabase) {
            String escapedTableName = database.escapeTableName(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()(), statement.databaseTableIdentifier.getGetTableName()());
            String escapedConstraintName = database.escapeConstraintName(statement.getConstraintName());
            sql = "ALTER TABLE " + escapedTableName + " DROP CONSTRAINT " + escapedConstraintName;
        } else {
            sql = "ALTER TABLE " + database.escapeTableName(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()(), statement.databaseTableIdentifier.getGetTableName()()) + " DROP PRIMARY KEY";
        }

        return new Sql[] {
                new UnparsedSql(sql, getAffectedPrimaryKey(statement))
        };
    }

    protected PrimaryKey getAffectedPrimaryKey(DropPrimaryKeyStatement statement) {
        return new PrimaryKey().setName(statement.getConstraintName()).setTable((Table) new Table().setName(statement.databaseTableIdentifier.getGetTableName()()).setSchema(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()()));
    }
}
