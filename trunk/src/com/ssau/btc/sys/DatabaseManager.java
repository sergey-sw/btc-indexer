package com.ssau.btc.sys;

import com.intelli.ray.core.ManagedComponent;
import com.ssau.btc.model.IndexSnapshot;
import com.ssau.btc.utils.DateUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Author: Sergey42
 * Date: 03.03.14 21:33
 */
@ManagedComponent(name = DatabaseAPI.NAME)
public class DatabaseManager implements DatabaseAPI {

    private Connection connection;

    public DatabaseManager() throws Exception {
        Class.forName("org.postgresql.Driver");

        try {
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/btc", "root", "root");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<IndexSnapshot> getDailyIndexes() {
        List<IndexSnapshot> indexSnapshots = new ArrayList<>();
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "select index_date, index_value from daily_index order by index_date");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Date date = resultSet.getDate("index_date");
                double value = resultSet.getDouble("index_value");

                indexSnapshots.add(new IndexSnapshot(date, value));
            }
        } catch (SQLException e) {
            ExceptionHandler.handle(e);
        }

        return indexSnapshots;
    }

    @Override
    public void storeDailyIndexes(List<IndexSnapshot> indexSnapshots) {
        StringBuilder query = new StringBuilder();
        query.append("insert into daily_index values ");

        Iterator<IndexSnapshot> iterator = indexSnapshots.iterator();
        while (iterator.hasNext()) {
            IndexSnapshot indexSnapshot = iterator.next();
            query.append("(").append(DateUtils.formatSQL(indexSnapshot.date)).append(",")
                    .append(indexSnapshot.value).append(")");
            if (iterator.hasNext()) {
                query.append(",");
            }
        }

        try {
            PreparedStatement statement = connection.prepareStatement(query.toString());
            statement.execute();
        } catch (SQLException e) {
            ExceptionHandler.handle(e);
        }
    }

    @Override
    public void removeDailyIndexes(List<java.util.Date> dates) {
        if (dates.isEmpty()) {
            return;
        }

        StringBuilder query = new StringBuilder("delete from daily_index where index_date in (");

        Iterator<java.util.Date> iterator = dates.iterator();
        while (iterator.hasNext()) {
            query.append(DateUtils.formatSQL(iterator.next()));

            if (iterator.hasNext()) {
                query.append(",");
            }
        }
        query.append(")");

        try {
            PreparedStatement statement = connection.prepareStatement(query.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            ExceptionHandler.handle(e);
        }
    }

    @Override
    public String getConfig(String name) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    String.format("select config_value from sys_config where name = '%s'", name));
            ResultSet resultSet = statement.executeQuery();

            return resultSet.next() ? resultSet.getString("config_value") : null;
        } catch (SQLException e) {
            ExceptionHandler.handle(e);
            return null;
        }
    }

    @Override
    public void writeConfig(String name, String value) {
        String config = getConfig(name);

        if (config != null && !config.isEmpty()) {
            try {
                PreparedStatement statement = connection.prepareStatement(
                        String.format("update sys_config set config_value = '%s' where name = '%s'", value, name));
                statement.executeUpdate();
            } catch (SQLException e) {
                ExceptionHandler.handle(e);
            }
        } else {
            try {
                PreparedStatement statement = connection.prepareStatement(
                        "insert into sys_config values ('" + name + "','" + value + "')");
                statement.execute();
            } catch (SQLException e) {
                ExceptionHandler.handle(e);
            }
        }
    }

    @Override
    public void testSettings() throws Exception {
        Class.forName("org.postgresql.Driver");

        try {
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/btc", "root", "root");
        } catch (SQLException e) {
            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
            return;
        }

        if (connection != null) {
            System.out.println("You made it, take control your database now!");
        } else {
            System.out.println("Failed to make connection!");
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (connection != null) {
            connection.close();
        }
    }
}
