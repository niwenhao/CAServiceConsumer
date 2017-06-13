package jp.co.nri.openapi.sample.manager;

import javax.faces.bean.*;
import javax.faces.context.*;
import javax.transaction.UserTransaction;
import javax.persistence.*;
import javax.persistence.criteria.*;
import javax.annotation.Resource;

import java.util.List;
import java.util.Map;

import jp.co.nri.openapi.sample.common.*;
import jp.co.nri.openapi.sample.persistence.Client;

/**
 * クライアントメンテナンスBean
 * テンプレート：<a href="../../../../../../templates/client_man.txt">client_man.xhtml</a>
 */
@ManagedBean
public class ClientManBean {

	@Resource
	UserTransaction ut;

	@PersistenceContext
	EntityManager em;

	/**
	 * クライアント一覧を提供する。
	 * 
	 * @return	クライアント一覧
	 */
	public List<Client> listClients() {
		System.out.println("listClients");
		CriteriaQuery<Client> query = em.getCriteriaBuilder().createQuery(Client.class);
		Root<Client> root = query.from(Client.class);
		return em.createQuery(query.select(root)).getResultList();
	}

	/**
	 * サービスメンテ画面に遷移。
	 * 
	 * @param id	クライアントオブジェクトID
	 * @return	遷移先
	 */
	public String apiMan(long id) {
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstDef.SK_CLIENT_ID, id);
		return "service_man";
	}

	/**
	 * クライアント更新画面に遷移。
	 * 
	 * @param id	クライアントオブジェクトID
	 * @return	遷移先
	 * @throws Exception
	 */
	public String update(long id) throws Exception {
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstDef.SK_CLIENT_ID, id);
		return "client_man_edit";
	}

	/**
	 * クライアントを削除する。
	 * 
	 * @param id	クライアントオブジェクトID
	 * @return	遷移先
	 * @throws Exception
	 */
	public String delete(long id) throws Exception {
		ut.begin();

		Client u = em.find(Client.class, Long.valueOf(id));
		em.remove(u);
		ut.commit();
		return null;
	}

	/**
	 * クライアント追加画面に遷移。
	 * 
	 * @return	遷移先
	 * @throws Exception
	 */
	public String append() throws Exception {
		Map<String, Object> map = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
		if (map.containsKey(ConstDef.SK_CLIENT_ID))
			map.remove(ConstDef.SK_CLIENT_ID);
		return "client_man_edit";
	}
}
