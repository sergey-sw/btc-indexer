package com.ssau.btc.sys;

import com.intelli.ray.core.ManagedComponent;
import com.ssau.btc.model.IndexSnapshot;
import com.ssau.btc.utils.DateUtils;
import com.ssau.btc.utils.MathUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Author: Sergey42
 * Date: 03.03.14 21:33
 */
@ManagedComponent(name = DatabaseAPI.NAME)
public class DatabaseManager implements DatabaseAPI {

    public static final String DRIVER = "org.postgresql.Driver";
    private Connection connection;

    public DatabaseManager() {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            GuiExceptionHandler.handleCritical("Database driver not found : " + DRIVER);
        }

        String url = Config.getDbUrl();
        String user = Config.getDbUser();
        String pass = Config.getDbPass();

        try {
            connection = DriverManager.getConnection(url, user, pass);
        } catch (SQLException e) {
            GuiExceptionHandler.handleDbError(e);
        }
    }

    @Override
    public List<IndexSnapshot> getDailyIndexes() {
        List<IndexSnapshot> indexSnapshots = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT index_date, index_value FROM daily_index ORDER BY index_date")) {
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

        try (PreparedStatement statement = connection.prepareStatement(query.toString())) {
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

        try (PreparedStatement statement = connection.prepareStatement(query.toString())) {
            statement.executeUpdate();
        } catch (SQLException e) {
            ExceptionHandler.handle(e);
        }
    }

    @Override
    public boolean storeTotalBtc(Map<java.util.Date, Integer> values) {
        if (values.isEmpty()) {
            return false;
        }

        StringBuilder query = new StringBuilder("insert into total_btc values ");
        Iterator<Map.Entry<java.util.Date, Integer>> iterator = values.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<java.util.Date, Integer> entry = iterator.next();
            query.append("(").append(DateUtils.formatSQL(entry.getKey())).append(",").append(entry.getValue()).append(")");

            if (iterator.hasNext()) {
                query.append(",");
            }
        }

        try (PreparedStatement statement = connection.prepareStatement(query.toString())) {
            statement.execute();
            return true;
        } catch (SQLException e) {
            ExceptionHandler.handle(e);
            return false;
        }
    }

    @Override
    public java.util.Date getLastDateInTotalBtc() {
        String query = "SELECT max(date_) FROM total_btc";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                java.util.Date date = resultSet.getDate(1);
                return date;
            } else {
                throw new RuntimeException("No info about total_btc");
            }
        } catch (SQLException e) {
            throw new RuntimeException("No info about total_btc");
        }
    }

    @Override
    public void storeSingleTotalBtc(java.util.Date date, Integer value) {
        String getQuery = "select * from total_btc where date_ = " + DateUtils.formatSQL(date);
        try (PreparedStatement statement = connection.prepareStatement(getQuery)) {
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                String query = String.format("insert into total_btc values (%s," + value + ")", DateUtils.formatSQL(date));
                try {
                    PreparedStatement statement1 = connection.prepareStatement(query);
                    statement1.execute();
                } catch (SQLException e) {
                    ExceptionHandler.handle(e);
                }
            }
        } catch (SQLException e) {
            ExceptionHandler.handle(e);
        }
    }

    @Override
    public double[] loadTotalBtcByPeriod(java.util.Date date1, java.util.Date date2) {
        String query = String.format("select value_ from total_btc where date_ >= %s and date_ <= %s",
                DateUtils.formatSQL(date1), DateUtils.formatSQL(date2));
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            ResultSet resultSet = statement.executeQuery();
            List<Double> doubles = new ArrayList<>();
            while (resultSet.next()) {
                doubles.add(resultSet.getDouble(1));
            }
            return MathUtils.convertDoubles(doubles);
        } catch (SQLException e) {
            throw new RuntimeException(String.format("Failed to select total btc for period %s-%s", date1, date2));
        }
    }

    @Override
    public String getConfig(String name) {
        try (PreparedStatement statement = connection.prepareStatement(
                String.format("select config_value from sys_config where name = '%s'", name))) {
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
            try (PreparedStatement statement = connection.prepareStatement(
                    String.format("update sys_config set config_value = '%s' where name = '%s'", value, name))) {
                statement.executeUpdate();
            } catch (SQLException e) {
                ExceptionHandler.handle(e);
            }
        } else {
            try (PreparedStatement statement = connection.prepareStatement(
                    "insert into sys_config values ('" + name + "','" + value + "')")) {
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
