package com.eric.fall.jdbc;

import com.eric.fall.io.PropertyResolver;

import java.util.Properties;

public class JdbcTestBase {

    public static final String CREATE_USER = "create table f_users (id int primary key auto_increment, name varchar(255) NOT NULL, age int);";

    public static final String CREATE_ADDRESS = "create table f_addresses ( id int primary key auto_increment,    userId int not null ,    address varchar(255) not null ,    zip int);";

    public static final String INSERT_USER = "INSERT INTO F_USERS (name, age) VALUES (?, ?);";

    public static final String INSERT_ADDRESS = "INSERT INTO F_ADDRESSES (userId, address, zip) VALUES (?, ?, ?);";

    public static final String UPDATE_USER = "UPDATE F_USERS SET NAME = ?, AGE = ? WHERE ID = ?;";

    public static final String UPDATE_ADDRESS = "UPDATE F_ADDRESSES SET ADDRESS = ?, ZIP = ? WHERE ID = ?";

    public static final String DELETE_USER = "DELETE FROM F_USERS WHERE ID = ?;";

    public static final String DELETE_ADDRESS_BY_USERID = "DELETE FROM F_ADDRESSES WHERE USERID = ?";

    public static final String SELECT_USER = "SELECT * FROM F_USERS WHERE ID = ?";

    public static final String SELECT_USER_NAME = "SELECT NAME FROM F_USERS WHERE ID = ?";

    public static final String SELECT_USER_AGE = "SELECT AGE FROM F_USERS WHERE ID = ?";

    public static final String SELECT_ADDRESS_BY_USERID = "SELECT * FROM F_ADDRESSES WHERE USERID = ?";

    public PropertyResolver createPropertyResolver() {
        var ps = new Properties();
        ps.put("fall.datasource.url", "jdbc:mysql://127.0.0.1:3306/crm?zeroDateTimeBehavior=CONVERT_TO_NULL&useSSL=true&serverTimezone=GMT%2B8&rewriteBatchedStatements=true&allowMultiQueries=true");
        ps.put("fall.datasource.username", "crm_test");
        ps.put("fall.datasource.password", "1syh|9t");
        ps.put("fall.datasource.driver-class-name", "com.mysql.cj.jdbc.Driver");
        var pr = new PropertyResolver(ps);
        return pr;
    }

}
