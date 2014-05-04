package org.weibofollowersincreasor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.List;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.RandomUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class WeiboLoginTest {

	private ObjectMapper objectMapper;
	
	private DefaultHttpClient defaultHttpClient;

	public WeiboLoginTest() {
		objectMapper = new ObjectMapper();
		
		defaultHttpClient = new DefaultHttpClient();
	}

	private JsonNode getJsonNode(String result) {
		int beginIndex = result.indexOf("(");
		int endIndex = result.lastIndexOf(")");

		JsonNode jsonNode = null;

		try {
			jsonNode = objectMapper.readTree(result.substring(beginIndex + 1,
					endIndex));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return jsonNode;
	}

	private String rsaCrypt(String modeHex, String exponentHex, String message) {
		String result = null;

		try {
			KeyFactory factory = KeyFactory.getInstance("RSA");

			BigInteger m = new BigInteger(modeHex, 16);
			BigInteger e = new BigInteger(exponentHex, 16);
			RSAPublicKeySpec spec = new RSAPublicKeySpec(m, e);
			RSAPublicKey pub = (RSAPublicKey) factory.generatePublic(spec);
			Cipher enc = Cipher.getInstance("RSA");
			enc.init(Cipher.ENCRYPT_MODE, pub);

			byte[] encryptedContentKey = enc.doFinal(message.getBytes());

			result = new String(Hex.encodeHex(encryptedContentKey));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}

		return result;
	}

	private byte[] get(String url) {
		byte[] result = null;

		HttpGet get = new HttpGet(url);

		try {
			HttpResponse response = defaultHttpClient.execute(get);

			int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode == HttpStatus.SC_OK) {
				result = EntityUtils.toByteArray(response.getEntity());
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			get.releaseConnection();
		}

		return result;
	}

	private byte[] prelogin(String su) {
		StringBuilder url = new StringBuilder();

		url.append("http://login.sina.com.cn/sso/prelogin.php");
		url.append("?");
		url.append("entry");
		url.append("=");
		url.append("general");
		url.append("&");
		url.append("callback");
		url.append("=");
		url.append("sinaSSOController.preloginCallBack");
		url.append("&");
		url.append("su");
		url.append("=");
		url.append(su);
		url.append("&");
		url.append("rsakt");
		url.append("=");
		url.append("mod");
		url.append("&");
		url.append("client");
		url.append("=");
		url.append("ssologin.js(v1.4.13)");

		byte[] result = get(url.toString());

		return result;
	}

	private byte[] loginSina(String su, String servertime, String nonce,
			String rsakv, String sp, int prelt) {
		StringBuilder url = new StringBuilder();

		url.append("https://login.sina.com.cn/sso/login.php");
		url.append("?");
		url.append("entry");
		url.append("=");
		url.append("general");
		url.append("&");
		url.append("gateway");
		url.append("=");
		url.append("1");
		url.append("&");
		url.append("from");
		url.append("=");
		url.append("");
		url.append("&");
		url.append("savestate");
		url.append("=");
		url.append("14");
		url.append("&");
		url.append("useticket");
		url.append("=");
		url.append("1");
		url.append("&");
		url.append("pagerefer");
		url.append("=");
		url.append("");
		url.append("&");
		url.append("su");
		url.append("=");
		url.append(su);
		url.append("&");
		url.append("service");
		url.append("=");
		url.append("miniblog");
		url.append("&");
		url.append("servertime");
		url.append("=");
		url.append(servertime);
		url.append("&");
		url.append("nonce");
		url.append("=");
		url.append(nonce);
		url.append("&");
		url.append("pwencode");
		url.append("=");
		url.append("rsa2");
		url.append("&");
		url.append("rsakv");
		url.append("=");
		url.append(rsakv);
		url.append("&");
		url.append("sp");
		url.append("=");
		url.append(sp);
		url.append("&sr");
		url.append("=");
		url.append("1440*900");
		url.append("&");
		url.append("encoding");
		url.append("=");
		url.append("UTF-8");
		url.append("&");
		url.append("callback");
		url.append("=");
		url.append("sinaSSOController.loginCallBack");
		url.append("&");
		url.append("cdult");
		url.append("=");
		url.append("2");
		url.append("&");
		url.append("domain");
		url.append("=");
		url.append("weibo.com");
		url.append("&");
		url.append("prelt");
		url.append("=");
		url.append(prelt);
		url.append("&");
		url.append("returntype");
		url.append("=");
		url.append("TEXT");
		url.append("&");
		url.append("client");
		url.append("=");
		url.append("ssologin.js(v1.4.13)");

		byte[] result = get(url.toString());

		return result;
	}

	private byte[] loginWeibo(String ticket) {
		StringBuilder url = new StringBuilder();

		url.append("http://www.weibo.com/sso/login.php");
		url.append("?");
		url.append("callback");
		url.append("=");
		url.append("sinaSSOController.callbackLoginStatus");
		url.append("&");
		url.append("ticket");
		url.append("=");
		url.append(ticket);
		url.append("&");
		url.append("client");
		url.append("=");
		url.append("ssologin.js(v1.4.13)");

		byte[] result = get(url.toString());

		return result;
	}

	private byte[] pin(String pcid) {
		StringBuilder url = new StringBuilder();

		url.append("http://login.sina.com.cn/cgi/pin.php");
		url.append("?");
		url.append("p");
		url.append("=");
		url.append(pcid);

		byte[] result = get(url.toString());

		return result;
	}

	private byte[] validatePinCode(String pcid, String pinCode) {
		StringBuilder url = new StringBuilder();

		url.append("http://login.sina.com.cn/sso/validatepincode.php");
		url.append("?");
		url.append("entry");
		url.append("=");
		url.append("miniblog");
		url.append("&");
		url.append("pincodeid");
		url.append("=");
		url.append(pcid);
		url.append("&");
		url.append("pincode");
		url.append("=");
		url.append(pinCode);
		url.append("&");
		url.append("_t");
		url.append("=");
		url.append("1");

		byte[] result = get(url.toString());

		return result;
	}

	private byte[] crossDomain() {
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

		byte[] result = get(url.toString());

		return result;
	}

	private byte[] loginWeibo2(String query) {
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

		byte[] result = get(url.toString());

		return result;
	}

	private void login(String username, String password, String pinCodeFile) {
		byte[] result;

		JsonNode jsonNode;
		
		String su = null;

		try {
			su = Base64.encodeBase64String(URLEncoder.encode(username, "UTF-8")
					.getBytes());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		// prelogin
		result = prelogin(su);
		String preloginResult = null;

		try {
			preloginResult = new String(result, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		System.out.println(String
				.format("Prelogin result = %s", preloginResult));

		jsonNode = getJsonNode(preloginResult);

		String servertime = jsonNode.get("servertime").asText();
		String pcid = jsonNode.get("pcid").asText();
		String nonce = jsonNode.get("nonce").asText();
		String pubkey = jsonNode.get("pubkey").asText();
		String rsakv = jsonNode.get("rsakv").asText();
		int prelt = 100 + RandomUtils.nextInt(0, 900);

		String sp = rsaCrypt(pubkey, "10001", servertime + "\t" + nonce + "\n"
				+ password);

		// loginSina
		result = loginSina(su, servertime, nonce, rsakv, sp, prelt);
		String loginSinaResult = null;

		try {
			loginSinaResult = new String(result, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		System.out.println(String.format("LoginSina result = %s",
				loginSinaResult));

		jsonNode = getJsonNode(loginSinaResult);

		String ticket = jsonNode.get("ticket").asText();

		// loginWeibo
		result = loginWeibo(ticket);
		String loginWeiboResult = null;

		try {
			loginWeiboResult = new String(result, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		System.out.println(String.format("LoginWeibo result = %s",
				loginWeiboResult));

		// pin
		result = pin(pcid);

		FileOutputStream fileOutputStream = null;

		try {
			fileOutputStream = new FileOutputStream(pinCodeFile);

			fileOutputStream.write(result);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fileOutputStream != null) {
					fileOutputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Scanner scanner = new Scanner(System.in);

		System.out.println("Input pin code: ");
		String pinCode = scanner.nextLine();

		// validatePinCode
		result = validatePinCode(pcid, pinCode);
		String validatePinCodeResult = null;

		try {
			validatePinCodeResult = new String(result, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		System.out.println(String.format("ValidatePinCode result = %s",
				validatePinCodeResult));

		// crossDomain
		result = crossDomain();
		String crossDomainResult = null;

		try {
			crossDomainResult = new String(result, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		System.out.println(String.format("CrossDomain result = %s",
				crossDomainResult));

		jsonNode = getJsonNode(crossDomainResult);

		String arrUrl = ((ArrayNode) jsonNode.get("arrURL")).get(0).asText();

		String query = null;

		try {
			URI uri = new URI(arrUrl);

			query = uri.getQuery();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		// loginWeibo2
		result = loginWeibo2(query);
		String loginWeibo2Result = null;

		try {
			loginWeibo2Result = new String(result, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		System.out.println(String.format("LoginWeibo2 result = %s",
				loginWeibo2Result));
	}

	private void saveCookies(String cookiesFile) {
		CookieStore cookieStore = defaultHttpClient.getCookieStore();

		List<Cookie> cookieList = cookieStore.getCookies();

		File file = new File(cookiesFile);

		try {
			objectMapper.writeValue(file, cookieList);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);

		System.out.println("Input username: ");
		String username = scanner.nextLine();

		System.out.println("Input password: ");
		String password = scanner.nextLine();

		System.out.println("Input pin code file: ");
		String pinCodeFile = scanner.nextLine();

		System.out.println("Input cookies file: ");
		String cookiesFile = scanner.nextLine();

		WeiboLoginTest weiboLoginTest = new WeiboLoginTest();
		weiboLoginTest.login(username, password, pinCodeFile);
		weiboLoginTest.saveCookies(cookiesFile);
	}

}
