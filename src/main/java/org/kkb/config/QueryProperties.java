package org.kkb.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class QueryProperties {
    @Value("${mysql.insert.query}")
    private String insertQuery;

    public String getInsertQuery() { return insertQuery; }
    public void setInsertQuery(String insertQuery) { this.insertQuery = insertQuery; }
}
