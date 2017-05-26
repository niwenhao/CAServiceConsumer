package jp.co.nri.openapi.sample.faces;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.faces.bean.ManagedBean;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import javax.xml.stream.events.Characters;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import jp.co.nri.openapi.sample.common.JsonHelper;
import jp.co.nri.openapi.sample.common.ServiceInvoker;
import jp.co.nri.openapi.sample.persistence.Client;
import jp.co.nri.openapi.sample.persistence.Token;
import jp.co.nri.openapi.sample.persistence.User;

/**
 * 認証コードを受け取って、TOKEN取得処理View。
 * テンプレート：<a href="../../../../../../templates/authorize_code.txt">authorize_code.xhtml</a>
 */
@ManagedBean
public class AuthorizeCodeBean implements JsonHelper {
	@Resource
	UserTransaction ut;

	@PersistenceContext
	EntityManager em;

	String authCode;

	String stateEncoded;

	String returnUrl;

	List<String[]> followParams;

	/**
	 * 認証コードとステータスでトークンを取得し、DBに保存する。
	 * 
	 * @throws Exception
	 */
	public void performTakeToken() throws Exception {
		//ステータスをデーコードする
		Map<String, Object> state = json2Map(
				URLDecoder.decode(stateEncoded, StandardCharsets.UTF_8.name()).getBytes(StandardCharsets.UTF_8));
		returnUrl = (String) state.get(ServiceInvoker.RETURN_URL);
		followParams = new ArrayList<>();
		((Map<String, Object>) state.get(ServiceInvoker.FOLLOW_PARAMETERS)).forEach((k, v) -> {
			String[] ent = { k, (String) v };
			followParams.add(ent);
		});

		long userId = ((BigDecimal) state.get(ServiceInvoker.USER_ID)).longValue();
		long clientId = ((BigDecimal) state.get(ServiceInvoker.CLIENT_ID)).longValue();
		try {
			ut.begin();
			//トークンと関連するデータを取得しておく
			User user = em.find(User.class, userId);
			Client client = em.find(Client.class, clientId);

			HttpClient httpClient = HttpClients.createDefault();

			HttpPost post = new HttpPost(client.getTokenUrl());
			//トークンを取得するためのパラメータを組み立て
			List<NameValuePair> paramList = new ArrayList<>();
			paramList.add(new BasicNameValuePair("grant_type", "authorization_code"));
			paramList.add(new BasicNameValuePair("code", this.authCode));
			paramList.add(new BasicNameValuePair("redirect_uri", client.getRequestUrl()));
			paramList.add(new BasicNameValuePair("client_id", client.getIdent()));
			paramList.add(new BasicNameValuePair("client_secret", client.getSecret()));
			post.setEntity(new UrlEncodedFormEntity(paramList));

			//トークンエンドポイントにアクセス。
			HttpResponse response = httpClient.execute(post);

			//ステータスチェック
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException(
						"Status code: " + response.getStatusLine().getStatusCode() + "\n" + response.toString());
			}

			//コンテンツタイプチェック
			if (response.getEntity().getContentType().getValue().replaceAll("^.*application/json.*$", "")
					.length() != 0) {
				throw new RuntimeException("Content-type: " + response.getEntity().getContentType().getValue() + "\n"
						+ response.toString());
			}

			//トークン関連情報をDBに保存。
			Map<String, Object> rst = json2Map(response.getEntity().getContent());

			Token token = new Token();
			token.setAccessToken((String) rst.get("access_token"));
			token.setRefreshToken((String) rst.get("refresh_token"));
			token.setTimeLimit(new Date(System.currentTimeMillis() + ((BigDecimal) rst.get("expires_in")).longValue()*1000));

			token.setUser(user);
			token.setClient(client);

			em.persist(token);

			ut.commit();
		} catch (Exception e) {
			ut.rollback();
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return	認証コード（パラメータ受け取り用）
	 */
	public String getAuthCode() {
		return authCode;
	}

	/**
	 * @param authCode	認証コード（パラメータ受け取り用）
	 */
	public void setAuthCode(String authCode) {
		this.authCode = authCode;
	}

	/**
	 * @return	エンコードしたステータス（パラメータ受け取り用）
	 */
	public String getStateEncoded() {
		return stateEncoded;
	}

	/**
	 * @param stateEncoded	エンコードしたステータス（パラメータ受け取り用）
	 */
	public void setStateEncoded(String stateEncoded) {
		this.stateEncoded = stateEncoded;
	}

	/**
	 * @return	APPに戻るURL
	 */
	public String getReturnUrl() {
		return returnUrl;
	}

	/**
	 * @param returnUrl	APPに戻るURL
	 */
	public void setReturnUrl(String returnUrl) {
		this.returnUrl = returnUrl;
	}

	/**
	 * @return	APPに転送するパラメータ
	 */
	public List<String[]> getFollowParams() {
		return followParams;
	}

	/**
	 * @param followParams	APPに転送するパラメータ
	 */
	public void setFollowParams(List<String[]> followParams) {
		this.followParams = followParams;
	}

}
