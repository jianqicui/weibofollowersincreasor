package org.weibofollowersincreasor.handler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.weibofollowersincreasor.handler.exception.HandlerException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class WeiboHandler {

	private ObjectMapper objectMapper;

	public void initialize() {
		objectMapper = new ObjectMapper();
	}

	private JsonNode getJsonNode(String result) throws HandlerException {
		int beginIndex = result.indexOf("(");
		int endIndex = result.lastIndexOf(")");

		JsonNode jsonNode;

		try {
			jsonNode = objectMapper.readTree(result.substring(beginIndex + 1,
					endIndex));
		} catch (JsonProcessingException e) {
			throw new HandlerException(e);
		} catch (IOException e) {
			throw new HandlerException(e);
		}

		return jsonNode;
	}

	private String get(HttpClient httpClient, String url)
			throws HandlerException {
		String result;

		HttpGet get = new HttpGet(url);

		try {
			HttpResponse response = httpClient.execute(get);

			int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode == HttpStatus.SC_OK) {
				result = EntityUtils.toString(response.getEntity(), "UTF-8");
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

		return result;
	}

	private String crossDomain(HttpClient httpClient) throws HandlerException {
		StringBuilder url = new StringBuilder();

		url.append("http://login.sina.com.cn/sso/crossdomain.php");
		url.append("?");
		url.append("scriptId");
		url.append("=");
		url.append("ssoCrossDomainScriptId");
		url.append("&");
		url.append("callback");
		url.append("=");
		url.append("sinaSSOController.crossDomainCallBack");
		url.append("&");
		url.append("action");
		url.append("=");
		url.append("login");
		url.append("&");
		url.append("domain");
		url.append("=");
		url.append("sina.com.cn");
		url.append("&");
		url.append("sr");
		url.append("=");
		url.append("1440*900");
		url.append("&");
		url.append("client");
		url.append("=");
		url.append("ssologin.js(v1.4.13)");

		String result = get(httpClient, url.toString());

		return result;
	}

	private String loginWeiboQuery(String result) throws HandlerException {
		JsonNode jsonNode = getJsonNode(result);

		String arrUrl = ((ArrayNode) jsonNode.get("arrURL")).get(0).asText();

		String query;

		try {
			URI uri = new URI(arrUrl);

			query = uri.getQuery();
		} catch (URISyntaxException e) {
			throw new HandlerException(e);
		}

		return query;
	}

	private String loginWeibo(HttpClient httpClient, String query)
			throws HandlerException {
		StringBuilder url = new StringBuilder();

		url.append("http://www.weibo.com/sso/login.php");
		url.append("?");
		url.append(query);
		url.append("&");
		url.append("callback");
		url.append("=");
		url.append("sinaSSOController.doCrossDomainCallBack");
		url.append("&");
		url.append("scriptId");
		url.append("=");
		url.append("ssoscript0");
		url.append("&");
		url.append("client");
		url.append("=");
		url.append("ssologin.js(v1.4.13)");

		String result = get(httpClient, url.toString());

		return result;
	}

	public void refresh(HttpClient httpClient) throws HandlerException {
		// crossDomain
		String result = crossDomain(httpClient);

		// loginWeiboQuery
		String query = loginWeiboQuery(result);

		// loginWeibo
		result = loginWeibo(httpClient, query);

		if (!result.startsWith("sinaSSOController.doCrossDomainCallBack")) {
			query = loginWeiboQuery(result);

			loginWeibo(httpClient, query);
		}
	}

	private byte[] post(HttpClient httpClient, HttpPost post)
			throws HandlerException {
		byte[] result;

		try {
			HttpResponse response = httpClient.execute(post);

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
			post.releaseConnection();
		}

		return result;
	}

	private void analyzeResult(byte[] result) throws HandlerException {
		JsonNode jsonNode;

		try {
			jsonNode = objectMapper.readTree(result);
		} catch (JsonProcessingException e) {
			throw new HandlerException(e);
		} catch (IOException e) {
			throw new HandlerException(e);
		}

		String code = jsonNode.get("code").asText();

		if (!"100000".equals(code)) {
			throw new HandlerException(code);
		}
	}

	public boolean isNormal(HttpClient httpClient, String userId)
			throws HandlerException {
		String result = null;

		HttpGet get = new HttpGet("http://www.weibo.com/u/" + userId);

		try {
			HttpResponse response = httpClient.execute(get);

			int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode == HttpStatus.SC_OK) {
				result = EntityUtils.toString(response.getEntity(), "UTF-8");
			}
		} catch (ClientProtocolException e) {
			throw new HandlerException(e);
		} catch (IOException e) {
			throw new HandlerException(e);
		} finally {
			get.releaseConnection();
		}

		Document doc = Jsoup.parse(result);

		Elements divs = doc.select(".page_error");

		boolean normalUser = divs.isEmpty();

		return normalUser;
	}

	public void follow(HttpClient httpClient, String userId)
			throws HandlerException {
		HttpPost post = new HttpPost("http://www.weibo.com/aj/f/followed");

		post.addHeader(HttpHeaders.REFERER, "http://www.weibo.com/" + userId);

		List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>();

		nameValuePairList.add(new BasicNameValuePair("uid", userId));

		try {
			post.setEntity(new UrlEncodedFormEntity(nameValuePairList));
		} catch (UnsupportedEncodingException e) {
			throw new HandlerException(e);
		}

		byte[] result = post(httpClient, post);

		analyzeResult(result);
	}

	public void unfollow(HttpClient httpClient, String userId)
			throws HandlerException {
		HttpPost post = new HttpPost("http://www.weibo.com/aj/f/unfollow");

		post.addHeader(HttpHeaders.REFERER, "http://www.weibo.com/" + userId);

		List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>();

		nameValuePairList.add(new BasicNameValuePair("uid", userId));

		try {
			post.setEntity(new UrlEncodedFormEntity(nameValuePairList));
		} catch (UnsupportedEncodingException e) {
			throw new HandlerException(e);
		}

		byte[] result = post(httpClient, post);

		analyzeResult(result);
	}

}