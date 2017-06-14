package jp.co.nri.openapi.sample.common;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jp.co.nri.openapi.sample.persistence.Client;
import jp.co.nri.openapi.sample.persistence.Token;

public interface OpenIdHelper extends JsonHelper {
	default String randomGen() {
		return UUID.randomUUID().toString();
	}

	default Claims parseIdToken(byte[] cert, String jwt) {
		Claims res = Jwts.parser().setSigningKey(cert).parseClaimsJws(jwt).getBody();
		return res;
	}

	default String base64UrlHelfSha256(String code) {
		byte[] codeBuf = code.getBytes(StandardCharsets.UTF_8);
		byte[] shaBuf = DigestUtils.sha256(codeBuf);
		byte[] helfShaBuf = new byte[shaBuf.length / 2];
		System.arraycopy(shaBuf, 0, helfShaBuf, 0, helfShaBuf.length);
		String rst = Base64.getEncoder().encodeToString(helfShaBuf);
		return rst.replaceAll("\\+", "-").replaceAll("/", "_").replaceAll("=*$", "");
	}

	default Map<String, Object> takeRefreshToken(Client client, Token token) throws Exception {
		HttpClient httpClient = HttpClients.createDefault();

		HttpPost post = new HttpPost(client.getTokenUrl());
		// トークンを取得するためのパラメータを組み立て
		List<NameValuePair> paramList = new ArrayList<>();
		paramList.add(new BasicNameValuePair("grant_type", "refresh_token"));
		paramList.add(new BasicNameValuePair("refresh_token", token.getRefreshToken()));
		paramList.add(new BasicNameValuePair("scope", client.getScope()));
		paramList.add(new BasicNameValuePair("client_id", client.getIdent()));
		paramList.add(new BasicNameValuePair("client_secret", client.getSecret()));
		post.setEntity(new UrlEncodedFormEntity(paramList));

		// トークンエンドポイントにアクセス。
		HttpResponse response = httpClient.execute(post);

		// ステータスチェック
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new RuntimeException(
					"Status code: " + response.getStatusLine().getStatusCode() + "\n" + response.toString());
		}

		// コンテンツタイプチェック
		if (response.getEntity().getContentType().getValue().replaceAll("^.*application/json.*$", "").length() != 0) {
			throw new RuntimeException(
					"Content-type: " + response.getEntity().getContentType().getValue() + "\n" + response.toString());
		}

		// トークン関連情報をDBに保存。
		Map<String, Object> rst = json2Map(response.getEntity().getContent());
		return rst;
	}

	/**
	 * @param client
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	default HttpResponse takeAccessToken(String tokenEndPointUrl, String authCode, String redirectUrl, String clientId,
			String clientSecret) throws Exception {
		HttpClient httpClient = HttpClients.createDefault();

		HttpPost post = new HttpPost(tokenEndPointUrl);
		// トークンを取得するためのパラメータを組み立て
		List<NameValuePair> paramList = new ArrayList<>();
		paramList.add(new BasicNameValuePair("grant_type", "authorization_code"));
		paramList.add(new BasicNameValuePair("code", authCode));
		paramList.add(new BasicNameValuePair("redirect_uri", redirectUrl));
		paramList.add(new BasicNameValuePair("client_id", clientId));
		paramList.add(new BasicNameValuePair("client_secret", clientSecret));
		post.setEntity(new UrlEncodedFormEntity(paramList));

		// トークンエンドポイントにアクセス。
		HttpResponse response = httpClient.execute(post);

		// ステータスチェック
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new RuntimeException(
					"Status code: " + response.getStatusLine().getStatusCode() + "\n" + response.toString());
		}

		// コンテンツタイプチェック
		if (response.getEntity().getContentType().getValue().replaceAll("^.*application/json.*$", "").length() != 0) {
			throw new RuntimeException(
					"Content-type: " + response.getEntity().getContentType().getValue() + "\n" + response.toString());
		}
		return response;
	}

}
