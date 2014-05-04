package org.weibofollowersincreasor.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.weibofollowersincreasor.dao.CollectingUserDao;
import org.weibofollowersincreasor.dao.exception.DaoException;
import org.weibofollowersincreasor.entity.CollectingUser;

public class CollectingUserJdbcDao implements CollectingUserDao {

	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private RowMapper<CollectingUser> rowMapper = new CollectingUserRowMapper();

	private class CollectingUserRowMapper implements RowMapper<CollectingUser> {

		@Override
		public CollectingUser mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			CollectingUser collectingUser = new CollectingUser();

			try {
				collectingUser.setId(rs.getInt("id"));
				collectingUser.setUserId(rs.getString("user_id"));
				collectingUser.setUserName(rs.getString("user_name"));
			} catch (SQLException e) {
				throw e;
			}

			return collectingUser;
		}

	}

	@Override
	public List<CollectingUser> getCollectingUserList() throws DaoException {
		String sql = "select id, user_id, user_name from user_collecting order by id";

		try {
			return jdbcTemplate.query(sql, rowMapper);
		} catch (Exception e) {
			throw new DaoException(e);
		}
	}

}
