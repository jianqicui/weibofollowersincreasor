package org.weibofollowersincreasor.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
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

	private Map<String, Integer> getStatusSizeMapByRange(HttpClient httpClient,
			List<String> userIdList) throws HandlerException {
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
		url.append(StringUtils.join(userIdList, ","));

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

		Map<String, Integer> statusSizeMap = new HashMap<String, Integer>();

		for (JsonNode jsonNode : arrayNode) {
			JsonNode idJsonNode = jsonNode.get("id");
			JsonNode statusSizeJsonNode = jsonNode.get("statuses_count");

			if (idJsonNode != null && statusSizeJsonNode != null) {
				String userId = idJsonNode.asText();
				int statusSize = statusSizeJsonNode.asInt();

				statusSizeMap.put(userId, statusSize);
			} else {
				throw new HandlerException("GetStatusSize failed");
			}
		}

		return statusSizeMap;
	}

	public Map<String, Integer> getStatusSizeMap(HttpClient httpClient,
			List<String> userIdList) throws HandlerException {
		List<List<String>> userIdListList = new ArrayList<List<String>>();

		int count = 100;
		int size = (userIdList.size() / count) + (userIdList.size() % count == 0 ? 0 : 1);

		int fromIndex;
		int toIndex;

		for (int i = 0; i < size; i++) {
			fromIndex = i * count;

			if (i != size - 1) {
				toIndex = (i + 1) * count;
			} else {
				toIndex = userIdList.size();
			}

			userIdListList.add(userIdList.subList(fromIndex, toIndex));
		}

		Map<String, Integer> statusSizeMap = new HashMap<String, Integer>();

		for (List<String> vUserIdList : userIdListList) {
			statusSizeMap.putAll(getStatusSizeMapByRange(httpClient,
					vUserIdList));
		}

		return statusSizeMap;
	}

}
