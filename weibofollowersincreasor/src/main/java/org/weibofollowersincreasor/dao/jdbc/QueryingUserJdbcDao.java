package org.weibofollowersincreasor.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.weibofollowersincreasor.dao.QueryingUserDao;
import org.weibofollowersincreasor.dao.exception.DaoException;
import org.weibofollowersincreasor.entity.QueryingUser;

public class QueryingUserJdbcDao implements QueryingUserDao {

	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private RowMapper<QueryingUser> rowMapper = new QueryingUserRowMapper();

	private class QueryingUserRowMapper implements RowMapper<QueryingUser> {

		@Override
		public QueryingUser mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			QueryingUser queryingUser = new QueryingUser();

			try {
				queryingUser.setId(rs.getInt("id"));
				queryingUser.setCookies(rs.getBytes("cookies"));
			} catch (SQLException e) {
				throw e;
			}

			return queryingUser;
		}

	}

	@Override
	public QueryingUser getQueryingUser() throws DaoException {
		String sql = "select id, cookies from user_querying order by id";

		try {
			return jdbcTemplate.queryForObject(sql, rowMapper);
		} catch (Exception e) {
			throw new DaoException(e);
		}
	}

	@Override
	public void updateQueryingUser(QueryingUser queryingUser)
			throws DaoException {
		String sql = "update user_querying set cookies = ?, created_timestamp = ?";

		try {
			jdbcTemplate.update(sql, queryingUser.getCookies(), new Date());
		} catch (Exception e) {
			throw new DaoException(e);
		}
	}

}
