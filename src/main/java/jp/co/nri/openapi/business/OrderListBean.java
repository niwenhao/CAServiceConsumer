package jp.co.nri.openapi.business;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.ComponentSystemEvent;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpSession;
import javax.transaction.UserTransaction;

import jp.co.nri.openapi.sample.common.ConstDef;
import jp.co.nri.openapi.sample.common.ServiceInvoker;
import jp.co.nri.openapi.sample.persistence.User;

/**
 * 注文一覧View
 * テンプレート：<a href="../../../../../../templates/order_list.txt">order_list.xhtml</a>
 */
@ManagedBean
public class OrderListBean extends ServiceInvoker {
	
	/* (non-Javadoc)
	 * @see jp.co.nri.openapi.sample.common.ServiceInvoker#getUserId()
	 */
	@Override
	protected long getUserId() {
		return (long)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(ConstDef.SK_USER_ID);
	}

	/* (non-Javadoc)
	 * @see jp.co.nri.openapi.sample.common.ServiceInvoker#getAppParameters()
	 */
	@Override
	protected Map<String, String> getAppParameters() {
		return new HashMap<>();
	}

	/* (non-Javadoc)
	 * @see jp.co.nri.openapi.sample.common.ServiceInvoker#getReturnURL()
	 */
	@Override
	protected String getReturnURL() {
		return FacesContext.getCurrentInstance().getExternalContext().getApplicationContextPath() + "/order_list.jsf";
	}

	/* (non-Javadoc)
	 * @see jp.co.nri.openapi.sample.common.ServiceInvoker#getSession()
	 */
	@Override
	protected HttpSession getSession() {
		HttpSession session = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(true);
		return session;
	}
	
	/**
	 * @return	注文一覧をサービスから取得する。
	 * @throws IOException
	 */
	public List<Map<String, Object>> searchOrderList() throws IOException {
		try {
			System.out.println("searchOrderList");
			Map<String, Object> rst = invokeService("order_list", null);
			rst = (Map<String, Object>)rst.get("aplData");
			return (List<Map<String, Object>>)rst.get("TradingList");
		} catch (OAuthRedirectException e) {
			FacesContext.getCurrentInstance().getExternalContext().redirect(e.transRedirectUrl());
			return new ArrayList<>();
		}
	}
}
