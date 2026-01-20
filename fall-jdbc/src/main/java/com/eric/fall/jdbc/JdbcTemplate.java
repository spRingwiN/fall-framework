package com.eric.fall.jdbc;

import com.eric.fall.exception.DataAccessException;
import com.eric.fall.jdbc.tx.TransactionUtils;
import jakarta.annotation.Nullable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Number queryForNumber(String sql, Object... args) throws DataAccessException {
        return queryForObject(sql, NumberRowMapper.INSTANCE, args);
    }

    @SuppressWarnings("unchecked")
    public <T> T queryForObject(String sql, Class<T> clazz, Object... args) throws DataAccessException {
        if (clazz == String.class) {
            return (T) queryForObject(sql, StringRowMapper.INSTANCE, args);
        }
        if (clazz == Boolean.class || clazz == boolean.class) {
            return (T) queryForObject(sql, BooleanRowMapper.INSTANCE, args);
        }
        if (Number.class.isAssignableFrom(clazz) || clazz.isPrimitive()) {
            return (T) queryForObject(sql, NumberRowMapper.INSTANCE, args);
        }
        return queryForObject(sql, new BeanRowMapper<>(clazz), args);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException {
        return execute(preparedStatementCreator(sql, args),
                // preparedStatementCallback
                (PreparedStatement ps) -> {
                    T t = null;
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            if (t == null) {
                                t = rowMapper.mapRow(rs, rs.getRow());
                            } else {
                                throw new DataAccessException("More than one row returned");
                            }
                        }
                    }
                    if (t == null) {
                        throw new DataAccessException("No row returned");
                    }
                    return t;
                });
    }

    public <T> List<T> queryForList(String sql, Class<T> clazz, Object... args) throws DataAccessException {
        return queryForList(sql, new BeanRowMapper<>(clazz), args);
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(preparedStatementCreator(sql, args),
                // PreparedStatementCallback
                (PreparedStatement ps) -> {
                   List<T> list = new ArrayList<>();
                   try (ResultSet rs = ps.executeQuery()) {
                       while (rs.next()) {
                           list.add(rowMapper.mapRow(rs, rs.getRow()));
                       }
                   }
                   return list;
                }
        );
    }

    public Number updateAndReturnGeneratedKey(String sql, Object... args) throws DataAccessException {
        return execute(
                // preparedStatementCreator
                (Connection con) -> {
                    var ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
                    bindArgs(ps, args);
                    return ps;
                },
                // preparedStatementCallback
                (PreparedStatement ps) -> {
                    int n = ps.executeUpdate();
                    if (n == 0) {
                        throw new DataAccessException("0 rows inserted");
                    }
                    if (n > 1) {
                        throw new DataAccessException("More than one row inserted");
                    }
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        while (keys.next()) {
                            return (Number) keys.getObject(1);
                        }
                    }
                    throw new DataAccessException("Should not reach here");
                }
        );
    }

    public int update(String sql, Object... args) throws DataAccessException {
        return execute(preparedStatementCreator(sql, args), (PreparedStatement ps) -> {
            return ps.executeUpdate();
        });
    }

    public <T> T execute(PreparedStatementCreator psc, PreparedStatementCallback<T> action) {
        return execute((Connection conn) -> {
            try (PreparedStatement ps = psc.createPreparedStatement(conn)) {
                return action.doInPreparedStatement(ps);
            }
        });
    }

    public <T> T execute(ConnectionCallback<T> action) throws DataAccessException {
        // 尝试获取当前事务连接
        Connection current = TransactionUtils.getCurrentConnection();
        if (current != null) {
            try {
                return action.doInConnection(current);
            } catch (SQLException e) {
                throw new DataAccessException(e);
            }
        }
        // 获取新连接
        try (Connection newConn = dataSource.getConnection()) {
            final boolean autoCommit = newConn.getAutoCommit();
            if (!autoCommit) {
                newConn.setAutoCommit(true);
            }
            T result = action.doInConnection(newConn);
            if (!autoCommit) {
                newConn.setAutoCommit(false);
            }
            return result;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private PreparedStatementCreator preparedStatementCreator(String sql, Object... args) {
        return (Connection con) -> {
            var ps = con.prepareStatement(sql);
            bindArgs(ps, args);
            return ps;
        };
    }

    private void bindArgs(PreparedStatement ps, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            ps.setObject(i + 1, args[i]);
        }
    }

}

class StringRowMapper implements RowMapper<String> {

    static StringRowMapper INSTANCE = new StringRowMapper();

    @Nullable
    @Override
    public String mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getString(1);
    }
}

class BooleanRowMapper implements RowMapper<Boolean> {

    static BooleanRowMapper INSTANCE = new BooleanRowMapper();

    @Nullable
    @Override
    public Boolean mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getBoolean(1);
    }
}

class NumberRowMapper implements RowMapper<Number> {

    static NumberRowMapper INSTANCE = new NumberRowMapper();

    @Nullable
    @Override
    public Number mapRow(ResultSet rs, int rowNum) throws SQLException {
        return (Number) rs.getObject(1);
    }
}













