package com.jaspercloud.mybatis.support.plus;

import com.jaspercloud.mybatis.support.plus.annotation.SelectKey;

import java.util.ArrayList;
import java.util.List;

public class TableInfo {

    private String tableName;
    private TableColumn keyColumn;
    private List<TableColumn> columns = new ArrayList<>();

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public TableColumn getKeyColumn() {
        return keyColumn;
    }

    public void setKeyColumn(TableColumn keyColumn) {
        this.keyColumn = keyColumn;
    }

    public void addColumn(TableColumn column) {
        columns.add(column);
    }

    public List<TableColumn> getColumns() {
        return columns;
    }

    public static class TableColumn {

        private String columnName;
        private String propertyName;
        private Class<?> type;
        private SelectKey selectKey;

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public void setPropertyName(String propertyName) {
            this.propertyName = propertyName;
        }

        public Class<?> getType() {
            return type;
        }

        public void setType(Class<?> type) {
            this.type = type;
        }

        public SelectKey getSelectKey() {
            return selectKey;
        }

        public void setSelectKey(SelectKey selectKey) {
            this.selectKey = selectKey;
        }

        public TableColumn() {
        }

        public TableColumn(String columnName, String propertyName, Class<?> type) {
            this.columnName = columnName;
            this.propertyName = propertyName;
            this.type = type;
        }
    }
}
