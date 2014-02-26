package org.weibofollowersincreasor.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.weibofollowersincreasor.dao.ActiveUserDao;
import org.weibofollowersincreasor.dao.exception.DaoException;
import org.weibofollowersincreasor.entity.ActiveUser;
import org.weibofollowersincreasor.entity.ActiveUserPhase;

public class ActiveUserJdbcDao implements ActiveUserDao {

	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private RowMapper<ActiveUser> rowMapper = new ActiveUserRowMapper();

	private class ActiveUserRowMapper implements RowMapper<ActiveUser> {

		@Override
		public ActiveUser mapRow(ResultSet rs, int rowNum) throws SQLException {
			ActiveUser activeUser = new ActiveUser();

			try {
				activeUser.setId(rs.getInt("id"));
				activeUser.setCookies(rs.getBytes("cookies"));
			} catch (SQLException e) {
				throw e;
			}

			return activeUser;
		}

	}

	private String getTableName(ActiveUserPhase activeUserPhase) {
		return "user_" + activeUserPhase;
	}

	@Override
	public ActiveUser getActiveUser(ActiveUserPhase activeUserPhase)
			throws DaoException {
		String sql = "select id, cookies from " + getTableName(activeUserPhase);

		try {
			return jdbcTemplate.queryForObject(sql, rowMapper);
		} catch (Exception e) {
			throw new DaoException(e);
		}
	}

	@Override
	public void updateActiveUser(ActiveUserPhase activeUserPhase,
			ActiveUser activeUser) throws DaoException {
		String sql = "update " + getTableName(activeUserPhase)
				+ " set cookies = ?, created_timestamp = ?";

		try {
			jdbcTemplate.update(sql, activeUser.getCookies(), new Date());
		} catch (Exception e) {
			throw new DaoException(e);
		}
	}

	private String getTableName(int categoryId, int typeId,
			ActiveUserPhase activeUserPhase) {
		return "category" + categoryId + "_type" + typeId + "_user_"
				+ activeUserPhase;
	}

	@Override
	public List<ActiveUser> getActiveUserList(int categoryId, int typeId,
			ActiveUserPhase activeUserPhase) throws DaoException {
		String sql = "select id, cookies from "
				+ getTableName(categoryId, typeId, activeUserPhase);

		try {
			return jdbcTemplate.query(sql, rowMapper);
		} catch (Exception e) {
			throw new DaoException(e);
		}
	}

	@Override
	public void updateActiveUserList(int categoryId, int typeId,
			ActiveUserPhase activeUserPhase, List<ActiveUser> activeUserList)
			throws DaoException {
		for (ActiveUser activeUser : activeUserList) {
			String sql = "update "
					+ getTableName(categoryId, typeId, activeUserPhase)
					+ " set cookies = ?, created_timestamp = ? where id = ?";

			try {
				jdbcTemplate.update(sql, activeUser.getCookies(), new Date(),
						activeUser.getId());
			} catch (Exception e) {
				throw new DaoException(e);
			}
		}
	}

}
