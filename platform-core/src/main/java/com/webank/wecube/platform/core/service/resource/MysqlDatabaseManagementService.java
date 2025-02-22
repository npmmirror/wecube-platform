package com.webank.wecube.platform.core.service.resource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.ApplicationProperties.ResourceProperties;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.entity.plugin.ResourceItem;
import com.webank.wecube.platform.core.utils.EncryptionUtils;

@Service
public class MysqlDatabaseManagementService implements ResourceItemService {
    private static final Logger log = LoggerFactory.getLogger(MysqlDatabaseManagementService.class);

    @Autowired
    private ResourceProperties resourceProperties;

    @Autowired
    private MysqlAccountManagementService mysqlAccountManagementService;

    public DriverManagerDataSource newMysqlDatasource(String host, String port, String username, String password) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl(
                "jdbc:mysql://" + host + ":" + port + "?characterEncoding=utf8&serverTimezone=UTC");
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    @Override
    public ResourceItem createItem(ResourceItem item) {
        DriverManagerDataSource dataSource = newDatasource(item);
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();) {
            if(doesItemExist(item)) {
                log.info("Such database schema already existed, name={}", item.getName());
            }else {
                statement.executeUpdate(String.format("CREATE SCHEMA %s", item.getName()));
            }
            
            
            mysqlAccountManagementService.createItem(item);
        } catch (SQLException e) {
            String errorMessage = String.format("Failed to create schema [%s], meet error [%s].", item.getName(),
                    e.getMessage());
            log.error(errorMessage, e);
            throw new WecubeCoreException("3244", errorMessage, item.getName(), e.getMessage());
        }
        return item;
    }

    private DriverManagerDataSource newDatasource(ResourceItem item) {
        String password = item.getResourceServer().getLoginPassword();

        password = EncryptionUtils.decryptAesPrefixedStringForcely(password,
                resourceProperties.getPasswordEncryptionSeed(), item.getResourceServer().getName());
        DriverManagerDataSource dataSource = newMysqlDatasource(item.getResourceServer().getHost(),
                item.getResourceServer().getPort(), item.getResourceServer().getLoginUsername(), password);
        log.info(String.format("Created new data source [host:%s,port:%s,username:%s]",
                item.getResourceServer().getHost(), item.getResourceServer().getPort(),
                item.getResourceServer().getLoginUsername()));
        return dataSource;
    }

    @Override
    public void deleteItem(ResourceItem item) {
        DriverManagerDataSource dataSource = newDatasource(item);
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();) {
            if (hasTables(connection, item.getName())) {
                throw new WecubeCoreException("3245",
                        String.format("Can not delete database [%s] : Database is not empty.", item.getName()),
                        item.getName());
            }
            mysqlAccountManagementService.deleteItem(item);
            statement.executeUpdate(String.format("DROP SCHEMA %s", item.getName()));
        } catch (SQLException e) {
            String errorMessage = String.format("Failed to delete schema [%s], meet error [%s].", item.getName(),
                    e.getMessage());
            log.error(errorMessage);
            throw new WecubeCoreException("3246", errorMessage, item.getName(), e.getMessage());
        }
    }

    @Override
    public boolean doesItemExist(ResourceItem item) {
        DriverManagerDataSource dataSource = newDatasource(item);

        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();) {
            int count = 0;
            String countSchemaSql = String
                    .format("SELECT count(1) FROM information_schema.SCHEMATA where SCHEMA_NAME='%s'", item.getName());

            try (ResultSet rs = statement.executeQuery(countSchemaSql);) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }

            return count > 0;

        } catch (SQLException e) {
            log.debug("Failed to check schema.", e);
            String errorMessage = String.format("Failed to check schema [%s], meet error [%s].", item.getName(),
                    e.getMessage());
            log.error(errorMessage);
            throw new WecubeCoreException(errorMessage);
        }
    }

    @Override
    public ResourceItem retrieveItem(ResourceItem item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResourceItem updateItem(ResourceItem item) {
        throw new UnsupportedOperationException();
    }

    private boolean hasTables(Connection connection, String dbName) {
        boolean hasTable = false;
        try (Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(String.format("SHOW TABLES FROM %s", dbName));) {
            resultSet.last();
            hasTable = resultSet.getRow() > 0;
        } catch (SQLException e) {
            String errorMessage = String.format("Failed to query tables, meet error [%s].", e.getMessage());
            log.error(errorMessage);
            throw new WecubeCoreException("3247", errorMessage, e.getMessage());
        }
        return hasTable;
    }
}
