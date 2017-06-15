package jp.co.nri.openapi.sample.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

/**
 * OpenID connectサービスをアクセスするヘルパー
 *
 */
public interface OpenIdHelper extends JsonHelper, CommonFunction {

	/**
	 * @return	ランダム値をベースでストリングを生成する。
	 */
	default String randomGen() {
		return UUID.randomUUID().toString();
	}

	/**
	 * id tokenの署名を確認する。
	 * 
	 * @param cert	署名確認用鍵
	 * @param jwt	JWTそのもの
	 * @return	署名が確認できる場合、JWTの項目Mapを返す。署名エラーの場合、RuntimeExceptionをスローする。
	 */
	default Map<String, Object> parseIdToken(byte[] cert, String jwt) {
		Claims res = Jwts.parser().setSigningKey(cert).parseClaimsJws(jwt).getBody();
		return res;
	}

	/**
	 * OpenID connectのJWTのc_hashとat_hashをチェックするため、仕様が定義された手順で
	 * Hashを取得する。現行ではJWTのヘッダーを見ず、HS256を実装している。
	 * 参照：http://openid.net/specs/openid-connect-core-1_0.html#ImplicitIDToken
	 * @param code ハッシュを取るテキスト
	 * @return	ハッシュ
	 */
	default String base64UrlHelfSha256(String code) {
		byte[] codeBuf = code.getBytes(StandardCharsets.UTF_8);
		byte[] shaBuf = DigestUtils.sha256(codeBuf);
		byte[] helfShaBuf = new byte[shaBuf.length / 2];
		System.arraycopy(shaBuf, 0, helfShaBuf, 0, helfShaBuf.length);
		String rst = Base64.getUrlEncoder().encodeToString(helfShaBuf);
		return rst.replaceAll("=*$", "");
	}
	
	/**
	 * トークンリフレッシュ
	 * <p>
	 * 本メソッドはOpenID Connect仕様に従え、トークンリフレッシュを実装。
	 * トークンエンドポイントにアクセスして、更新したトークン情報を取得する。
	 * <b>しかし、このメソッドはトークン情報のチェックが行ってない。</b>
	 * </p>
	 * 
	 * @param tokenEP		トークンエンドポイント
	 * @param clientId		クライアントID
	 * @param clientSecret	クライアントシークレット
	 * @param scope			スコープ
	 * @param rt			リフレッシュトークン
	 * @return				取得トークン情報
	 */
	default Map<String, Object> takeRefreshToken(String tokenEP, String clientId, String clientSecret, String scope, String rt) {
		try {
			HttpClient httpClient = HttpClients.createDefault();

			HttpPost post = new HttpPost(tokenEP);
			// トークンを取得するためのパラメータを組み立て
			List<NameValuePair> paramList = new ArrayList<>();
			paramList.add(new BasicNameValuePair("grant_type", "refresh_token"));
			paramList.add(new BasicNameValuePair("refresh_token", rt));
			paramList.add(new BasicNameValuePair("scope", scope));
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

			// トークン関連情報をDBに保存。
			Map<String, Object> rst = json2Map(response.getEntity().getContent());
			return rst;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		} catch (ClientProtocolException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedOperationException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 認可コードでアクセストークンを取得する。
	 * <p>
	 * 本メソッドはOpenID Connect仕様に従え、認可コードでトークンを取得を実装。
	 * トークンエンドポイントにアクセスして、トークン情報を取得する。
	 * <b>しかし、このメソッドはトークン情報のチェックが行ってない。</b>
	 * </p>
	 * 
	 * @param tokenEndPointUrl	トークンエンドポイント
	 * @param authCode			認可コード
	 * @param redirectUrl		リダイレクトURL
	 * @param clientId			クライアントID
	 * @param clientSecret		クライアントシークレット
	 * @return					取得トークン情報
	 */
	default Map<String, Object> takeAccessToken(String tokenEndPointUrl, String authCode, String redirectUrl, String clientId,
			String clientSecret) {
		try {
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
			
			byte[] respBytes = dumpStream(response.getEntity().getContent());

			return json2Map(respBytes);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		} catch (ClientProtocolException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
