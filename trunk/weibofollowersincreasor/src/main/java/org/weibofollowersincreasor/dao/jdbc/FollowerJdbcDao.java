package org.weibofollowersincreasor.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.weibofollowersincreasor.dao.FollowerDao;
import org.weibofollowersincreasor.dao.exception.DaoException;
import org.weibofollowersincreasor.entity.Follower;
import org.weibofollowersincreasor.entity.FollowerPhase;

public class FollowerJdbcDao implements FollowerDao {

	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private String getTableName(int categoryId, int typeId,
			FollowerPhase followerPhase) {
		String tableName = "category" + categoryId + "_type" + typeId
				+ "_follower_" + followerPhase;

		return tableName;
	}

	private RowMapper<Follower> rowMapper = new FollowerRowMapper();

	private class FollowerRowMapper implements RowMapper<Follower> {

		@Override
		public Follower mapRow(ResultSet rs, int rowNum) throws SQLException {
			Follower follower = new Follower();

			try {
				follower.setId(rs.getInt("id"));
				follower.setUserId(rs.getString("user_id"));
			} catch (SQLException e) {
				throw e;
			}

			return follower;
		}

	}

	@Override
	public void addFollower(int categoryId, int typeId,
			FollowerPhase followerPhase, Follower follower) throws DaoException {
		String sql = "insert into "
				+ getTableName(categoryId, typeId, followerPhase)
				+ " (user_id) values (?)";

		try {
			jdbcTemplate.update(sql, follower.getUserId());
		} catch (Exception e) {
			throw new DaoException(e);
		}
	}

	@Override
	public List<Follower> getFollowerList(int categoryId, int typeId,
			FollowerPhase followerPhase, int index, int size)
			throws DaoException {
		String sql = "select id, user_id from "
				+ getTableName(categoryId, typeId, followerPhase) + " limit "
				+ index + ", " + size;

		try {
			return jdbcTemplate.query(sql, rowMapper);
		} catch (Exception e) {
			throw new DaoException(e);
		}
	}

	@Override
	public boolean isSameFollowerExisting(int categoryId, int typeId,
			FollowerPhase followerPhase, Follower follower) throws DaoException {
		String sql = "select count(*) from "
				+ getTableName(categoryId, typeId, followerPhase)
				+ " where user_id = ?";

		int size;

		try {
			size = jdbcTemplate.queryForObject(sql,
					new String[] { follower.getUserId() }, Integer.class);
		} catch (Exception e) {
			throw new DaoException(e);
		}

		return size > 0;
	}

	@Override
	public void deleteFollower(int categoryId, int typeId,
			FollowerPhase followerPhase, int id) throws DaoException {
		String sql = "delete from "
				+ getTableName(categoryId, typeId, followerPhase)
				+ " where id = ?";

		try {
			jdbcTemplate.update(sql, id);
		} catch (Exception e) {
			throw new DaoException(e);
		}
	}

}
