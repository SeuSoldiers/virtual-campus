// LocalDateTimeTypeHandler.java
package seu.virtualcampus.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * MyBatis TypeHandler for {@link LocalDateTime}.
 * <p>
 * This handler converts between {@link LocalDateTime} objects in Java and
 * string representations (e.g., "yyyy-MM-dd HH:mm:ss") in the database.
 */
@MappedTypes(LocalDateTime.class)
public class LocalDateTimeTypeHandler extends BaseTypeHandler<LocalDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Sets a non-null {@link LocalDateTime} parameter on a {@link PreparedStatement}.
     *
     * @param ps        the prepared statement
     * @param i         the parameter index
     * @param parameter the parameter value
     * @param jdbcType  the JDBC type
     * @throws SQLException if a database access error occurs
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, LocalDateTime parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.format(FORMATTER));
    }

    /**
     * Gets a nullable {@link LocalDateTime} result from a {@link ResultSet} by column name.
     *
     * @param rs         the result set
     * @param columnName the column name
     * @return the {@link LocalDateTime} object, or null
     * @throws SQLException if a database access error occurs
     */
    @Override
    public LocalDateTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : LocalDateTime.parse(value, FORMATTER);
    }

    /**
     * Gets a nullable {@link LocalDateTime} result from a {@link ResultSet} by column index.
     *
     * @param rs          the result set
     * @param columnIndex the column index
     * @return the {@link LocalDateTime} object, or null
     * @throws SQLException if a database access error occurs
     */
    @Override
    public LocalDateTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : LocalDateTime.parse(value, FORMATTER);
    }

    /**
     * Gets a nullable {@link LocalDateTime} result from a {@link CallableStatement} by column index.
     *
     * @param cs          the callable statement
     * @param columnIndex the column index
     * @return the {@link LocalDateTime} object, or null
     * @throws SQLException if a database access error occurs
     */
    @Override
    public LocalDateTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : LocalDateTime.parse(value, FORMATTER);
    }
}