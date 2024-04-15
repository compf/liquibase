package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.DeleteStatement;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Table;

import static liquibase.util.SqlUtil.replacePredicatePlaceholders;

public class DeleteGenerator extends AbstractSqlGenerator<DeleteStatement> {

    @Override
    public ValidationErrors validate(DeleteStatement deleteStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", deleteStatement.databaseTableIdentifier.getGetTableName()());
        if ((deleteStatement.getWhereParameters() != null) && !deleteStatement.getWhereParameters().isEmpty() &&
            (deleteStatement.getWhere() == null)) {
            validationErrors.addError("whereParams set but no whereClause");
        }
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(DeleteStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder sql = new StringBuilder("DELETE FROM ")
            .append(database.escapeTableName(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()(), statement.databaseTableIdentifier.getGetTableName()()));

        if (statement.getWhere() != null) {
            sql.append(" WHERE ").append(replacePredicatePlaceholders(database, statement.getWhere(), statement.getWhereColumnNames(), statement.getWhereParameters()));
        }

        return new Sql[] { new UnparsedSql(sql.toString(), getAffectedTable(statement)) };
    }

    protected Relation getAffectedTable(DeleteStatement statement) {
        return new Table().setName(statement.databaseTableIdentifier.getGetTableName()()).setSchema(statement.databaseTableIdentifier.getGetCatalogName()(), statement.databaseTableIdentifier.getGetSchemaName()());
    }
}
