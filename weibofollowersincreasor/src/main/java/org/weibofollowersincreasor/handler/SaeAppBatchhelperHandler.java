package org.weibofollowersincreasor.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.weibofollowersincreasor.entity.Follower;
import org.weibofollowersincreasor.entity.Followers;
import org.weibofollowersincreasor.handler.exception.HandlerException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class SaeAppBatchhelperHandler {

	private ObjectMapper objectMapper;

	public void initialize() {
		objectMapper = new ObjectMapper();
	}

	public void authorize(HttpClient httpClient) throws HandlerException {
		String url = "https://api.weibo.com/oauth2/authorize?client_id=3144078080&redirect_uri=http%3A%2F%2Fbatchhelper.sinaapp.com%2F&response_type=code";

		HttpGet get = new HttpGet(url);

		try {
			httpClient.execute(get);
		} catch (ClientProtocolException e) {
			throw new HandlerException(e);
		} catch (IOException e) {
			throw new HandlerException(e);
		} finally {
			get.releaseConnection();
		}
	}

	public Followers getFollowersByUserName(HttpClient httpClient,
			String userName, int cursor, int size) throws HandlerException {
		byte[] result;

		StringBuilder url = new StringBuilder();
		url.append("http://batchhelper.sinaapp.com/action.php");
		url.append("?");
		url.append("action");
		url.append("=");
		url.append("queryFollowersIds");
		url.append("&");
		url.append("userName");
		url.append("=");
		url.append(userName);
		url.append("&");
		url.append("cursor");
		url.append("=");
		url.append(cursor);
		url.append("&");
		url.append("count");
		url.append("=");
		url.append(size);

		HttpGet get = new HttpGet(url.toString());

		try {
			HttpResponse response = httpClient.execute(get);

			int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode == HttpStatus.SC_OK) {
				result = EntityUtils.toByteArray(response.getEntity());
			} else {
				throw new HandlerException(String.valueOf(statusCode));
			}
		} catch (ClientProtocolException e) {
			throw new HandlerException(e);
		} catch (IOException e) {
			throw new HandlerException(e);
		} finally {
			get.releaseConnection();
		}

		JsonNode jsonNode;

		try {
			jsonNode = objectMapper.readTree(result);
		} catch (JsonProcessingException e) {
			throw new HandlerException(e);
		} catch (IOException e) {
			throw new HandlerException(e);
		}

		Followers followers = new Followers();

		JsonNode previousCursorJsonNode = jsonNode.get("previous_cursor");

		if (previousCursorJsonNode != null) {
			int previousCursor = previousCursorJsonNode.asInt();

			followers.setPreviousCursor(previousCursor);
		} else {
			throw new HandlerException("GetFollowerIdsByUserName failed");
		}

		JsonNode nextCursorJsonNode = jsonNode.get("next_cursor");

		if (nextCursorJsonNode != null) {
			int nextCursor = nextCursorJsonNode.asInt();

			followers.setNextCursor(nextCursor);
		} else {
			throw new HandlerException("GetFollowerIdsByUserName failed");
		}

		ArrayNode userIdsArrayNode = (ArrayNode) jsonNode.get("ids");

		if (userIdsArrayNode != null) {
			List<Follower> followerList = new ArrayList<Follower>();

			for (int i = 0; i < userIdsArrayNode.size(); i++) {
				String userId = userIdsArrayNode.get(i).asText();

				Follower follower = new Follower();
				follower.setUserId(userId);

				followerList.add(follower);
			}

			followers.setFollowerList(followerList);
		} else {
			throw new HandlerException("GetFollowerIdsByUserName failed");
		}

		return followers;
	}

	public int getStatusSize(HttpClient httpClient, String userId)
			throws HandlerException {
		byte[] result;

		StringBuilder url = new StringBuilder();
		url.append("http://batchhelper.sinaapp.com/action.php");
		url.append("?");
		url.append("action");
		url.append("=");
		url.append("queryUsersCounts");
		url.append("&");
		url.append("userIds");
		url.append("=");
		url.append(userId);

		HttpGet get = new HttpGet(url.toString());

		try {
			HttpResponse response = httpClient.execute(get);

			int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode == HttpStatus.SC_OK) {
				result = EntityUtils.toByteArray(response.getEntity());
			} else {
				throw new HandlerException(String.valueOf(statusCode));
			}
		} catch (ClientProtocolException e) {
			throw new HandlerException(e);
		} catch (IOException e) {
			throw new HandlerException(e);
		} finally {
			get.releaseConnection();
		}

		ArrayNode arrayNode;

		try {
			arrayNode = (ArrayNode) objectMapper.readTree(result);
		} catch (JsonProcessingException e) {
			throw new HandlerException(e);
		} catch (IOException e) {
			throw new HandlerException(e);
		}

		if (arrayNode.size() == 0) {
			throw new HandlerException("GetStatusSize failed");
		}

		JsonNode jsonNode = arrayNode.get(0);

		JsonNode statusSizeJsonNode = jsonNode.get("statuses_count");

		if (statusSizeJsonNode != null) {
			return statusSizeJsonNode.asInt();
		} else {
			throw new HandlerException("GetStatusSize failed");
		}
	}

}
