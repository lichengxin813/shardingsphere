/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.executor;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.core.executor.sql.execute.SQLExecuteCallback;
import io.shardingsphere.core.transaction.TransactionTypeHolder;
import io.shardingsphere.transaction.manager.base.executor.SagaSQLExeucteCallback;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

/**
 * Sql Execute callback factory.
 *
 * @author yangyi
 */
public final class SQLExecuteCallbackFactory {
    
    /**
     * Get update SQLExecuteCallBack for PreparedStatementExecutor.
     *
     * @param databaseType      types of database
     * @param sqlType           types of sql
     * @param isExceptionThrown is exception thrown
     * @param dataMap           data map
     * @return update SQLExecuteCallBack
     */
    public static SQLExecuteCallback<Integer> getPreparedUpdateSQLExecuteCallback(final DatabaseType databaseType, final SQLType sqlType, final boolean isExceptionThrown, final Map<String, Object> dataMap) {
        if (isSagaTransaction(sqlType)) {
            return getSagaUpdateSQLExecuteCallback(databaseType, sqlType, isExceptionThrown, dataMap);
        }
        return new SQLExecuteCallback<Integer>(databaseType, sqlType, isExceptionThrown, dataMap) {
            @Override
            protected Integer executeSQL(final StatementExecuteUnit statementExecuteUnit) throws SQLException {
                return ((PreparedStatement) statementExecuteUnit.getStatement()).executeUpdate();
            }
        };
    }
    
    /**
     * Get single SQLExecuteCallBack for PreparedStatementExecutor.
     *
     * @param databaseType      types of database
     * @param sqlType           types of sql
     * @param isExceptionThrown is exception thrown
     * @param dataMap           data map
     * @return single SQLExecuteCallBack
     */
    public static SQLExecuteCallback<Boolean> getPreparedSQLExecuteCallback(final DatabaseType databaseType, final SQLType sqlType, final boolean isExceptionThrown, final Map<String, Object> dataMap) {
        if (isSagaTransaction(sqlType)) {
            return getSagaSQLExecuteCallback(databaseType, sqlType, isExceptionThrown, dataMap);
        }
        return new SQLExecuteCallback<Boolean>(databaseType, sqlType, isExceptionThrown, dataMap) {
            
            @Override
            protected Boolean executeSQL(final StatementExecuteUnit statementExecuteUnit) throws SQLException {
                return ((PreparedStatement) statementExecuteUnit.getStatement()).execute();
            }
        };
    }
    
    private static SQLExecuteCallback<Integer> getSagaUpdateSQLExecuteCallback(final DatabaseType databaseType, final SQLType sqlType, final boolean isExceptionThrown, final Map<String, Object> dataMap) {
        return new SagaSQLExeucteCallback<Integer>(databaseType, sqlType, isExceptionThrown, dataMap) {
            @Override
            protected Integer executeResult() {
                return 0;
            }
        };
    }
    
    private static SQLExecuteCallback<Boolean> getSagaSQLExecuteCallback(final DatabaseType databaseType, final SQLType sqlType, final boolean isExceptionThrown, final Map<String, Object> dataMap) {
        return new SagaSQLExeucteCallback<Boolean>(databaseType, sqlType, isExceptionThrown, dataMap) {
            @Override
            protected Boolean executeResult() {
                return false;
            }
        };
    }
    
    private static boolean isSagaTransaction(final SQLType sqlType) {
        return SQLType.DML.equals(sqlType) && TransactionType.BASE.equals(TransactionTypeHolder.get());
    }
    
}
