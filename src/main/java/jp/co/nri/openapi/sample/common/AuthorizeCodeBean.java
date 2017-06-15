package jp.co.nri.openapi.sample.common;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpSession;
import javax.transaction.UserTransaction;

import org.apache.http.HttpResponse;

import io.jsonwebtoken.Claims;
import jp.co.nri.openapi.sample.persistence.Client;
import jp.co.nri.openapi.sample.persistence.Token;
import jp.co.nri.openapi.sample.persistence.User;

/**
 * 認証コードを受け取って、TOKEN取得処理View。
 * テンプレート：<a href="../../../../../../templates/authorize_code.txt">authorize_code.xhtml</a>
 */
@ManagedBean
public class AuthorizeCodeBean implements JsonHelper, OpenIdHelper, CommonFunction {
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
		HttpSession session = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(false);
		//STATEをチェックする。
		String origState = (String)session.getAttribute(ConstDef.SK_STATE_VALUE);
		if (! origState.equals(stateEncoded)) {
			throw new RuntimeException("state validation failed. ");
		}
		session.removeAttribute(ConstDef.SK_STATE_VALUE);
		// ステータスをデーコードする
		Map<String, Object> forwardValue = (Map)session.getAttribute(ConstDef.SK_FORWARD_VALUE);
		returnUrl = (String) forwardValue.get(ServiceInvoker.RETURN_URL);
		followParams = new ArrayList<>();
		((Map<String, Object>) forwardValue.get(ServiceInvoker.FOLLOW_PARAMETERS)).forEach((k, v) -> {
			String[] ent = { k, (String) v };
			followParams.add(ent);
		});

		long userId = (long) forwardValue.get(ServiceInvoker.USER_ID);
		long clientId = (long) forwardValue.get(ServiceInvoker.CLIENT_ID);

		String nonce = (String) session.getAttribute(ConstDef.SK_NONCE_VALUE);
		try {
			ut.begin();
			// トークンと関連するデータを取得しておく
			User user = em.find(User.class, userId);
			Client client = em.find(Client.class, clientId);

			HttpResponse response = takeAccessToken(client.getTokenUrl(), authCode, client.getRequestUrl(),
					client.getIdent(), client.getSecret());

			// トークン関連情報をDBに保存。
			Map<String, Object> rst = json2Map(duplicateInputStream(System.out, response.getEntity().getContent()));

			String idToken = (String) rst.get("id_token");
			Claims claims = this.parseIdToken(client.getSecret().getBytes("UTF-8"), idToken);
			String o;
			String c;
			
			o = nonce;
			c = (String) claims.get("nonce");
			if (!o.equals(c)) {
				throw new RuntimeException(String.format("nonce validation failed. o=%s, c=%s", o, c));
			}


			o = base64UrlHelfSha256(this.authCode);
			c = (String) claims.get("c_hash");
			if (!o.equals(c)) {
				throw new RuntimeException(String.format("authcode validation failed. o=%s, c=%s", o, c));
			}

			Token token = new Token();
			token.setAccessToken((String) rst.get("access_token"));
			token.setRefreshToken((String) rst.get("refresh_token"));
			token.setTimeLimit(
					new Date(System.currentTimeMillis() + ((BigDecimal) rst.get("expires_in")).longValue() * 1000));

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
