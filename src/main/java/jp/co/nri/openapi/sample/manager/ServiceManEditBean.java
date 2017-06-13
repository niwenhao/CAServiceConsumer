package jp.co.nri.openapi.sample.manager;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import jp.co.nri.openapi.sample.common.ConstDef;
import jp.co.nri.openapi.sample.persistence.Client;
import jp.co.nri.openapi.sample.persistence.Service;

/**
 * サービス編集View
 * テンプレート：<a href="../../../../../../templates/service_man_edit.txt">service_man_edit.xhtml</a>
 */
@ManagedBean
public class ServiceManEditBean {

	@Resource
	UserTransaction ut;

	@PersistenceContext
	EntityManager em;

	private Service data = new Service();
	
	/**
	 * @return	編集データ
	 */
	public Service getData() {
		return data;
	}

	/**
	 * @param data	編集データ
	 */
	public void setData(Service data) {
		this.data = data;
	}

	/**
	 * データの初期値を設定する。
	 * その後、JSFフレームによって、いろいろ同期が入る。
	 */
	@PostConstruct
	public void preRenderView() {
		Long id = (Long) FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
				.get(ConstDef.SK_SERVICE_ID);
		System.out.println(String.format("preRenderView(%d)", id));

		if (null == id) {
			this.data = new Service();
		} else {
			this.data = em.find(Service.class, id);
		}
	}

	/**
	 * 更新ボタンが押下され、入力データをDBに保存する。
	 * @return	遷移先
	 * @throws Exception
	 */
	public String update() throws Exception {
		ut.begin();

		try {

			Service d = em.merge(data);

			em.persist(d);

			ut.commit();
		} catch (Exception e) {
			ut.rollback();
		}

		return "service_man";
	}

	/**
	 * 追加ボタンが押下され、入力データをDBに保存する。
	 * 
	 * @return	遷移先
	 * @throws Exception
	 */
	public String append() throws Exception {
		ut.begin();
		
		try {
			long clientId = (Long)FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(ConstDef.SK_CLIENT_ID);
			
			Client client = em.find(Client.class, clientId);
			
			data.setClient(client);

			em.persist(data);

			ut.commit();
		} catch (Exception e) {
			ut.rollback();
		}
		return "service_man";
	}
}
