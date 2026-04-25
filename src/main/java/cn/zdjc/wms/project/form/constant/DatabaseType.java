package cn.zdjc.wms.project.form.constant;

public enum DatabaseType {
    MYSQL("MySQL", "com.mysql.cj.jdbc.Driver", "mysql"),
    SQLSERVER("SQL Server", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "sqlserver"),
    ORACLE("Oracle", "oracle.jdbc.OracleDriver", "oracle");

    private final String name;
    private final String driverClass;
    private final String vendor;

    DatabaseType(String name, String driverClass, String vendor) {
        this.name = name;
        this.driverClass = driverClass;
        this.vendor = vendor;
    }

    public String getName() { return name; }
    public String getDriverClass() { return driverClass; }
    public String getVendor() { return vendor; }
}
