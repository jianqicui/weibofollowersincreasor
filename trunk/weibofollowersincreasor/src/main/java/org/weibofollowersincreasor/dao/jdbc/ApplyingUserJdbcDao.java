package org.weibofollowersincreasor.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.weibofollowersincreasor.dao.ApplyingUserDao;
import org.weibofollowersincreasor.dao.exception.DaoException;
import org.weibofollowersincreasor.entity.ApplyingUser;

public class ApplyingUserJdbcDao implements ApplyingUserDao {

	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private RowMapper<ApplyingUser> rowMapper = new ApplyingUserRowMapper();

	private class ApplyingUserRowMapper implements RowMapper<ApplyingUser> {

		@Override
		public ApplyingUser mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			ApplyingUser applyingUser = new ApplyingUser();

			try {
				applyingUser.setId(rs.getInt("id"));
				applyingUser.setCookies(rs.getBytes("cookies"));
				applyingUser.setFollowingIndex(rs.getInt("following_index"));
			} catch (SQLException e) {
				throw e;
			}

			return applyingUser;
		}

	}

	private String getTableName(int categoryId, int typeId) {
		return "category" + categoryId + "_type" + typeId + "_user_applying";
	}

	@Override
	public List<ApplyingUser> getApplyingUserList(int categoryId, int typeId)
			throws DaoException {
		String sql = "select id, cookies, following_index from "
				+ getTableName(categoryId, typeId) + " order by id";

		try {
			return jdbcTemplate.query(sql, rowMapper);
		} catch (Exception e) {
			throw new DaoException(e);
		}
	}

	@Override
	public void updateApplyingUserList(int categoryId, int typeId,
			List<ApplyingUser> applyingUserList) throws DaoException {
		for (ApplyingUser applyingUser : applyingUserList) {
			String sql = "update "
					+ getTableName(categoryId, typeId)
					+ " set cookies = ?, following_index = ?, created_timestamp = ? where id = ?";

			try {
				jdbcTemplate.update(sql, applyingUser.getCookies(),
						applyingUser.getFollowingIndex(), new Date(),
						applyingUser.getId());
			} catch (Exception e) {
				throw new DaoException(e);
			}
		}
	}

}
