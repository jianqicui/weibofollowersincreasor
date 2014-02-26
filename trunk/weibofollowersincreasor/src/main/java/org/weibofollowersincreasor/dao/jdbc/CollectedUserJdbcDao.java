package org.weibofollowersincreasor.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.weibofollowersincreasor.dao.CollectedUserDao;
import org.weibofollowersincreasor.dao.exception.DaoException;
import org.weibofollowersincreasor.entity.CollectedUser;

public class CollectedUserJdbcDao implements CollectedUserDao {

	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private RowMapper<CollectedUser> rowMapper = new CollectedUserRowMapper();

	private class CollectedUserRowMapper implements RowMapper<CollectedUser> {

		@Override
		public CollectedUser mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			CollectedUser collectedUser = new CollectedUser();

			try {
				collectedUser.setId(rs.getInt("id"));
				collectedUser.setUserId(rs.getString("user_id"));
				collectedUser.setUserName(rs.getString("user_name"));
			} catch (SQLException e) {
				throw e;
			}

			return collectedUser;
		}

	}

	private String getTableName(int categoryId, int typeId) {
		String tableName = "category" + categoryId + "_type" + typeId
				+ "_user_collected";

		return tableName;
	}

	@Override
	public List<CollectedUser> getCollectedUserList(int categoryId, int typeId)
			throws DaoException {
		String sql = "select id, user_id, user_name from "
				+ getTableName(categoryId, typeId);

		try {
			return jdbcTemplate.query(sql, rowMapper);
		} catch (Exception e) {
			throw new DaoException(e);
		}
	}

}
