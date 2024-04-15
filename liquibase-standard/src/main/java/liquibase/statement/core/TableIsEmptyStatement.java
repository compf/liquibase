package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TableIsEmptyStatement extends AbstractSqlStatement {

    private liquibase.statement.core.DatabaseTableIdentifier databaseTableIdentifier = new liquibase.statement.core.DatabaseTableIdentifier(null, null, null);
}
