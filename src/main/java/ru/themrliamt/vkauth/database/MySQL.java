package ru.themrliamt.vkauth.database;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.*;

public class MySQL {

    final ExecutorService QUERY_EXECUTOR = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder()
                    .setNameFormat("database-thread #%s")
                    .setDaemon(true)
                    .build());

    private Connection connection;

    public MySQL(String host, String user, String password, String database) {

        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUser(user);
        dataSource.setPassword(password);
        dataSource.setServerName(host);
        dataSource.setDatabaseName(database);
        dataSource.setAutoReconnect(true);
        dataSource.setPort(3306);
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public <T> T executeQuery(String sql, ResponseHandler<ResultSet, T> handler, Object... objects) {
        Callable<T> callable = () -> {
            try (PreparedStatement preparedStatement = createStatement(sql, PreparedStatement.NO_GENERATED_KEYS, objects)) {
                ResultSet rs = preparedStatement.executeQuery();
                return handler.handleResponse(rs);
            }
        };

        Future<T> future = asyncTask(callable);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }

    private PreparedStatement createStatement(String query, int generatedKeys, Object... objects) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(query, generatedKeys);

        if (objects != null) {
            for (int i = 0; i < objects.length; i++) {
                ps.setObject(i + 1, objects[i]);
            }
        }
        return ps;
    }

    private <T> Future<T> asyncTask(Callable<T> callable) {
        return QUERY_EXECUTOR.submit(callable);
    }
}
