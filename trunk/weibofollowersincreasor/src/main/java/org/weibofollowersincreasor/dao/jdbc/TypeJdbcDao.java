package org.weibofollowersincreasor.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.weibofollowersincreasor.dao.TypeDao;
import org.weibofollowersincreasor.dao.exception.DaoException;
import org.weibofollowersincreasor.entity.Type;

public class TypeJdbcDao implements TypeDao {

	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private RowMapper<Type> rowMapper = new TypeRowMapper();

	private class TypeRowMapper implements RowMapper<Type> {

		@Override
		public Type mapRow(ResultSet rs, int rowNum) throws SQLException {
			Type type = new Type();

			try {
				type.setTypeId(rs.getInt("type_id"));
				type.setTypeName(rs.getString("type_name"));
			} catch (SQLException e) {
				throw e;
			}

			return type;
		}

	}

	@Override
	public List<Type> getTypeList(int categoryId) throws DaoException {
		String tableName = "category" + categoryId + "_type";

		String sql = "select type_id, type_name from " + tableName;

		try {
			return jdbcTemplate.query(sql, rowMapper);
		} catch (Exception e) {
			throw new DaoException(e);
		}
	}

}
