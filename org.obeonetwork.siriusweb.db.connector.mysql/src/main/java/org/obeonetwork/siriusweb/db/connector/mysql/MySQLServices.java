/*******************************************************************************
 * Copyright (c) 2022 CEA.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.obeonetwork.siriusweb.db.connector.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySQLServices {

    private final Logger logger = LoggerFactory.getLogger(MySQLServices.class);

    private String dbAddress;

    private String dbUser;

    private String dbPwd;

    private Connection connnection;

    private String selectExpression;

    private String fromExpression;

    private String whereExpression;

    private String orderByExpression;

    private String groupByExpression;

    private String joinExpression;

    private boolean leftJoin;

    private boolean rightJoin;

    private boolean innerJoin;

    private boolean fullJoin;

    private String onExpression;

    public MySQLServices() {
        this.dbAddress = System.getProperty("db.connector.mysql.url"); //$NON-NLS-1$
        this.dbUser = System.getProperty("db.connector.mysql.user"); //$NON-NLS-1$
        this.dbPwd = System.getProperty("db.connector.mysql.password"); //$NON-NLS-1$
        this.connnection = null;
        this.selectExpression = null;
        this.fromExpression = null;
        this.whereExpression = null;
        this.orderByExpression = null;
        this.groupByExpression = null;
        this.joinExpression = null;
        this.onExpression = null;
    }

    public MySQLServices mySQL(Object object, String dbName) {
        try {
            Class.forName("com.mysql.jdbc.Driver"); //$NON-NLS-1$
            this.connnection = DriverManager.getConnection("jdbc:mysql://" + this.dbAddress + "/" + dbName, this.dbUser, this.dbPwd); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (SQLException | IllegalArgumentException | SecurityException | ClassNotFoundException e) {
            this.logger.error(e.getMessage());
            closeConnection();
        }
        return this;
    }

    public MySQLServices sqlSelect(MySQLServices s, String selectExpression) {
        this.selectExpression = selectExpression;
        return this;
    }

    public MySQLServices from(MySQLServices s, String fromExpression) {
        this.fromExpression = fromExpression;
        return this;
    }

    public MySQLServices where(MySQLServices s, String whereExpression) {
        this.whereExpression = whereExpression;
        return this;
    }

    public MySQLServices orderBy(MySQLServices s, String orderByExpression) {
        this.orderByExpression = orderByExpression;
        return this;
    }

    public MySQLServices groupBy(MySQLServices s, String groupByExpression) {
        this.groupByExpression = groupByExpression;
        return this;
    }

    public MySQLServices join(MySQLServices s, String joinExpression) {
        this.joinExpression = joinExpression;
        return this;
    }

    public MySQLServices leftJoin(MySQLServices s, String joinExpression) {
        this.joinExpression = joinExpression;
        this.leftJoin = true;
        return this;
    }

    public MySQLServices rightJoin(MySQLServices s, String joinExpression) {
        this.joinExpression = joinExpression;
        this.rightJoin = true;
        return this;
    }

    public MySQLServices innerJoin(MySQLServices s, String joinExpression) {
        this.joinExpression = joinExpression;
        this.innerJoin = true;
        return this;
    }

    public MySQLServices fullJoin(MySQLServices s, String joinExpression) {
        this.joinExpression = joinExpression;
        this.fullJoin = true;
        return this;
    }

    public MySQLServices on(MySQLServices s, String onExpression) {
        this.onExpression = onExpression;
        return this;
    }

    public List<Object> fetch(MySQLServices s, int columnIndex) {
        Statement statement = null;
        ResultSet resultSet = null;
        List<Object> objects = new ArrayList<>();
        try {
            statement = this.connnection.createStatement();
            resultSet = statement.executeQuery(sqlQueryBuilder().toString());
            if (resultSet != null) {
                while (resultSet.next()) {
                    objects.add(resultSet.getObject(columnIndex));
                }
            }
        } catch (SQLException | IllegalArgumentException | SecurityException e) {
            this.logger.error(e.getMessage());
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                this.logger.error(e.getMessage());
            }
        }
        closeConnection();
        return objects;
    }

    public List<Object> fetch(MySQLServices s, String columnName) {
        Statement statement = null;
        ResultSet resultSet = null;
        List<Object> objects = new ArrayList<>();
        try {
            statement = this.connnection.createStatement();
            resultSet = statement.executeQuery(sqlQueryBuilder().toString());
            if (resultSet != null) {
                while (resultSet.next()) {
                    objects.add(resultSet.getObject(columnName));
                }
            }
        } catch (SQLException | IllegalArgumentException | SecurityException e) {
            this.logger.error(e.getMessage());
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                this.logger.error(e.getMessage());
            }
        }
        closeConnection();
        return objects;
    }

    public List<Object> fetch(MySQLServices s) {
        return fetch(s, 1);
    }

    private StringBuilder sqlQueryBuilder() {
        StringBuilder sqlQuery = new StringBuilder();
        if (this.selectExpression != null && !this.selectExpression.isBlank()) {
            sqlQuery.append("SELECT ").append(this.selectExpression); //$NON-NLS-1$
        }
        if (this.fromExpression != null && !this.fromExpression.isBlank()) {
            sqlQuery.append(" FROM ").append(this.fromExpression); //$NON-NLS-1$
        }
        if (this.whereExpression != null && !this.whereExpression.isBlank()) {
            sqlQuery.append(" WHERE ").append(this.whereExpression); //$NON-NLS-1$
        }
        if (this.joinExpression != null && !this.joinExpression.isBlank() && this.onExpression != null && !this.onExpression.isBlank()) {
            if (this.leftJoin) {
                sqlQuery.append(" LEFT "); //$NON-NLS-1$
            } else if (this.rightJoin) {
                sqlQuery.append(" RIGHT "); //$NON-NLS-1$
            } else if (this.innerJoin) {
                sqlQuery.append(" INNER "); //$NON-NLS-1$
            } else if (this.fullJoin) {
                sqlQuery.append(" FULL "); //$NON-NLS-1$
            }
            sqlQuery.append(" JOIN ").append(this.joinExpression).append(" ON ").append(this.onExpression); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (this.orderByExpression != null && !this.orderByExpression.isBlank()) {
            sqlQuery.append(" ORDER BY ").append(this.orderByExpression); //$NON-NLS-1$
        }
        if (this.groupByExpression != null && !this.groupByExpression.isBlank()) {
            sqlQuery.append(" GROUP BY ").append(this.groupByExpression); //$NON-NLS-1$
        }
        sqlQuery.append(";"); //$NON-NLS-1$
        return sqlQuery;
    }

    private void closeConnection() {
        try {
            if (this.connnection != null) {
                this.connnection.close();
            }
        } catch (SQLException e) {
            this.logger.error(e.getMessage());
        }
    }
}
