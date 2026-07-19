package com.framework.core;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConfig {
    private String driver;
    private String url;
    private String user;
    private String password;

    public DatabaseConfig(String driver, String url, String user, String password) {
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public Connection getConnection() throws Exception {
        Class.forName(driver);
        return DriverManager.getConnection(url, user, password);
    }

    public String getDriver() {
        return driver;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}