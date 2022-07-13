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
package org.obeonetwork.siriusweb.db.connector.influxdb;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

import java.util.ArrayList;
import java.util.List;

public class InfluxDBServices {

    private String dbAddress;

    private String dbOrg;

    private String dbToken;

    private InfluxDBClient influxDBClient;

    private String bucketName;

    private List<FluxQueryPart> queryParts;

    public InfluxDBServices() {
        this.dbAddress = System.getenv("DB_CONNECTOR_INFLUXDB_URL"); //$NON-NLS-1$
        this.dbOrg = System.getenv("DB_CONNECTOR_INFLUXDB_ORG"); //$NON-NLS-1$
        this.dbToken = System.getenv("DB_CONNECTOR_INFLUXDB_TOKEN"); //$NON-NLS-1$
        this.influxDBClient = null;
        this.bucketName = null;
        this.queryParts = new ArrayList<>();
    }

    public InfluxDBServices bucket(Object object, String bucketName) {
        this.bucketName = bucketName;
        this.influxDBClient = InfluxDBClientFactory.create(this.dbAddress, this.dbToken.toCharArray(), this.dbOrg, this.bucketName);
        return this;
    }

    public InfluxDBServices aggregateWindow(InfluxDBServices object, String aggregateWindowExpression) {
        this.queryParts.add(new FluxQueryPart("aggregateWindow", aggregateWindowExpression));
        return this;
    }

    public InfluxDBServices cumulativeSum(InfluxDBServices object) {
        this.queryParts.add(new FluxQueryPart("cumulativeSum", null));
        return this;
    }

    public InfluxDBServices derivative(InfluxDBServices object, String derivativeExpression) {
        this.queryParts.add(new FluxQueryPart("derivative", derivativeExpression));
        return this;
    }

    public InfluxDBServices keep(InfluxDBServices object, String keepExpression) {
        this.queryParts.add(new FluxQueryPart("keep", keepExpression));
        return this;
    }

    public InfluxDBServices fill(InfluxDBServices object, String fillExpression) {
        this.queryParts.add(new FluxQueryPart("fill", fillExpression));
        return this;
    }

    public InfluxDBServices fluxFilter(InfluxDBServices object, String filterExpression) {
        this.queryParts.add(new FluxQueryPart("filter", filterExpression));
        return this;
    }

    public InfluxDBServices group(InfluxDBServices object, String groupExpression) {
        this.queryParts.add(new FluxQueryPart("group", groupExpression));
        return this;
    }

    public InfluxDBServices group(InfluxDBServices object) {
        this.queryParts.add(new FluxQueryPart("group", null));
        return this;
    }

    public InfluxDBServices histogram(InfluxDBServices object, String histogramExpression) {
        this.queryParts.add(new FluxQueryPart("histogram", histogramExpression));
        return this;
    }

    public InfluxDBServices increase(InfluxDBServices object) {
        this.queryParts.add(new FluxQueryPart("increase", null));
        return this;
    }

    public InfluxDBServices limit(InfluxDBServices object, String limitExpression) {
        this.queryParts.add(new FluxQueryPart("limit", limitExpression));
        return this;
    }

    public InfluxDBServices map(InfluxDBServices object, String mapExpression) {
        this.queryParts.add(new FluxQueryPart("map", mapExpression));
        return this;
    }

    public InfluxDBServices median(InfluxDBServices object) {
        this.queryParts.add(new FluxQueryPart("median", null));
        return this;
    }

    public InfluxDBServices movingAverage(InfluxDBServices object, String movingAverageExpression) {
        this.queryParts.add(new FluxQueryPart("movingAverage", movingAverageExpression));
        return this;
    }

    public InfluxDBServices pivot(InfluxDBServices object, String pivotExpression) {
        this.queryParts.add(new FluxQueryPart("pivot", pivotExpression));
        return this;
    }

    public InfluxDBServices quantile(InfluxDBServices object, String quantileExpression) {
        this.queryParts.add(new FluxQueryPart("quantile", quantileExpression));
        return this;
    }

    public InfluxDBServices range(InfluxDBServices object, String rangeExpression) {
        this.queryParts.add(new FluxQueryPart("range", rangeExpression));
        return this;
    }

    public InfluxDBServices sort(InfluxDBServices object, String sortExpression) {
        this.queryParts.add(new FluxQueryPart("sort", sortExpression));
        return this;
    }

    public InfluxDBServices timedMovingAverage(InfluxDBServices object, String timedMovingAverageExpression) {
        this.queryParts.add(new FluxQueryPart("timedMovingAverage", timedMovingAverageExpression));
        return this;
    }

    public InfluxDBServices fluxFirst(InfluxDBServices object) {
        this.queryParts.add(new FluxQueryPart("first", null));
        return this;
    }

    public InfluxDBServices fluxLast(InfluxDBServices object) {
        this.queryParts.add(new FluxQueryPart("last", null));
        return this;
    }

    public List<Object> yield(InfluxDBServices object, String column) {
        List<Object> objects = new ArrayList<>();
        QueryApi queryApi = this.influxDBClient.getQueryApi();
        String query = influxQueryBuilder().toString();
        List<FluxTable> tables = queryApi.query(query);
        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {
                objects.add(fluxRecord.getValueByKey(column));
            }
        }
        this.queryParts.clear();
        closeConnection();
        return objects;
    }

    private StringBuilder influxQueryBuilder() {
        StringBuilder fluxQuery = new StringBuilder();
        fluxQuery.append("from(bucket:\"").append(this.bucketName).append("\")"); //$NON-NLS-1$ //$NON-NLS-2$

        this.queryParts.forEach(queryPart -> {
            fluxQuery.append(" |> ").append(queryPart.queryPartName).append("("); //$NON-NLS-1$ //$NON-NLS-2$
            if (queryPart.expression != null) {
                fluxQuery.append(queryPart.expression);
            }
            fluxQuery.append(")"); //$NON-NLS-1$
        });

        return fluxQuery;
    }

    private void closeConnection() {
        if (this.influxDBClient != null) {
            this.influxDBClient.close();
        }
    }

    class FluxQueryPart {
        private String queryPartName;

        private String expression;

        public FluxQueryPart(String queryPartName, String expression) {
            this.queryPartName = queryPartName;
            this.expression = expression;
        }

        public String getQueryPartName() {
            return this.queryPartName;
        }

        public String getExpression() {
            return this.expression;
        }
    }

}
