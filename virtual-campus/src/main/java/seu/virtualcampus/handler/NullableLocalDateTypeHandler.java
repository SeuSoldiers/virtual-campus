package seu.virtualcampus.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * MyBatis TypeHandler for nullable {@link LocalDate}.
 * <p>
 * This handler converts between {@link LocalDate} objects in Java and
 * string representations (e.g., "YYYY-MM-DD") in the database, correctly handling null values.
 */
@MappedTypes(LocalDate.class)
public class NullableLocalDateTypeHandler extends BaseTypeHandler<LocalDate> {

    /**
     * Sets a non-null {@link LocalDate} parameter on a {@link PreparedStatement}.
     * The date is stored as a string.
     *
     * @param ps        the prepared statement
     * @param i         the parameter index
     * @param parameter the parameter value
     * @param jdbcType  the JDBC type
     * @throws SQLException if a database access error occurs
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, LocalDate parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.toString()); // 存储为字符串
    }

    /**
     * Gets a nullable {@link LocalDate} result from a {@link ResultSet} by column name.
     *
     * @param rs         the result set
     * @param columnName the column name
     * @return the {@link LocalDate} object, or null
     * @throws SQLException if a database access error occurs
     */
    @Override
    public LocalDate getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String dateStr = rs.getString(columnName); // 读取为字符串
        return dateStr != null ? LocalDate.parse(dateStr) : null;
    }

    /**
     * Gets a nullable {@link LocalDate} result from a {@link ResultSet} by column index.
     *
     * @param rs          the result set
     * @param columnIndex the column index
     * @return the {@link LocalDate} object, or null
     * @throws SQLException if a database access error occurs
     */
    @Override
    public LocalDate getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String dateStr = rs.getString(columnIndex);
        return dateStr != null ? LocalDate.parse(dateStr) : null;
    }

    /**
     * Gets a nullable {@link LocalDate} result from a {@link CallableStatement} by column index.
     *
     * @param cs          the callable statement
     * @param columnIndex the column index
     * @return the {@link LocalDate} object, or null
     * @throws SQLException if a database access error occurs
     */
    @Override
    public LocalDate getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String dateStr = cs.getString(columnIndex);
        return dateStr != null ? LocalDate.parse(dateStr) : null;
    }
}