package jp.co.nri.openapi.sample.faces;

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
import javax.transaction.UserTransaction;

import jp.co.nri.openapi.sample.common.ConstDef;
import jp.co.nri.openapi.sample.common.ServiceInvoker;
import jp.co.nri.openapi.sample.persistence.User;

@ManagedBean
public class OrderListBean extends ServiceInvoker {
	
	private List<Map<String, Object>> orderList;

	@Override
	protected long getUserId() {
		return (long)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(ConstDef.SK_USER_ID);
	}

	@Override
	protected Map<String, String> getAppParameters() {
		return new HashMap<>();
	}

	@Override
	protected String getReturnURL() {
		return FacesContext.getCurrentInstance().getExternalContext().getApplicationContextPath() + "/order_list.jsf";
	}
	
	@PostConstruct
	public void searchOrderList() throws IOException {
		try {
			System.out.println("searchOrderList");
			Map<String, Object> rst = invokeService("order_list", null);
			rst = (Map<String, Object>)rst.get("aplData");
			this.orderList = (List<Map<String, Object>>)rst.get("TradingList");
		} catch (OAuthRedirectException e) {
			FacesContext.getCurrentInstance().getExternalContext().redirect(e.transRedirectUrl());
			this.orderList = new ArrayList<>();
		}
	}
	
	public void logevent(String event) {
		System.out.println(event);
	}

	public List<Map<String, Object>> getOrderList() {
		return orderList;
	}

	public void setOrderList(List<Map<String, Object>> orderList) {
		this.orderList = orderList;
	}
}
