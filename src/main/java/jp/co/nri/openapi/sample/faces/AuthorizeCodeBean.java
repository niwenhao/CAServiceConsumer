package jp.co.nri.openapi.sample.faces;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.faces.bean.ManagedBean;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import javax.xml.stream.events.Characters;

import jp.co.nri.openapi.sample.common.JsonHelper;
import jp.co.nri.openapi.sample.common.ServiceInvoker;

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
	
	public void performTakeToken() throws Exception {
		Map<String, Object> state = json2Map(URLDecoder.decode(stateEncoded, StandardCharsets.UTF_8.name()).getBytes(StandardCharsets.UTF_8));
		returnUrl = (String)state.get(ServiceInvoker.RETURN_URL);
		followParams = new ArrayList<>();
		((Map<String, Object>)state.get(ServiceInvoker.FOLLOW_PARAMETERS)).forEach((k, v) -> {
			String[] ent = { k, (String)v };
			followParams.add(ent);
		});
		
	}

	public String getAuthCode() {
		return authCode;
	}
	public void setAuthCode(String authCode) {
		this.authCode = authCode;
	}
	public String getStateEncoded() {
		return stateEncoded;
	}
	public void setStateEncoded(String stateEncoded) {
		this.stateEncoded = stateEncoded;
	}
	public String getReturnUrl() {
		return returnUrl;
	}
	public void setReturnUrl(String returnUrl) {
		this.returnUrl = returnUrl;
	}
	public List<String[]> getFollowParams() {
		return followParams;
	}
	public void setFollowParams(List<String[]> followParams) {
		this.followParams = followParams;
	}
	
	
}
