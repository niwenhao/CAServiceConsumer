package jp.co.nri.openapi.sample.faces;

import javax.annotation.Resource;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import jp.co.nri.openapi.sample.common.ConstDef;
import jp.co.nri.openapi.sample.persistence.Client;

/**
 * クライアント編集View
 * テンプレート：<a href="../../../../../../templates/client_man_edit.txt">client_man_edit.xhtml</a>
 */
@ManagedBean
public class ClientManEditBean {

	@Resource
	UserTransaction ut;

	@PersistenceContext
	EntityManager em;

	private Client data = new Client();
	
	/**
	 * @param data 編集対象クライアント
	 */
	public void setData(Client data) {
		this.data = data;
	}

	/**
	 * @return	編集対象クライアント
	 */
	public Client getData() {
		return data;
	}

	/**
	 * 画面構成する前の処理。データを設定する。
	 * @param event	イベント
	 * @throws AbortProcessingException
	 */
	public void preRenderView(ComponentSystemEvent event) throws AbortProcessingException {
		Long id = (Long) FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
				.get(ConstDef.SK_CLIENT_ID);
		System.out.println(String.format("preRenderView(%s)", event.toString()));

		if (null == id) {
			this.data = new Client();
		} else {
			this.data = em.find(Client.class, id);
		}
	}

	/**
	 * 更新ボタンを押下した場合の処理、入力データを保存する。
	 * @return	遷移先
	 * @throws Exception
	 */
	public String update() throws Exception {
		ut.begin();

		try {
			Client d = em.merge(data);

			em.persist(d);

			ut.commit();
		} catch (Exception e) {
			ut.rollback();
		}

		return "client_man";
	}

	/**
	 * 追加ボタンを押下した場合の処理、入力データの保存。
	 * 
	 * @return	遷移先
	 * @throws Exception
	 */
	public String append() throws Exception {
		ut.begin();

		try {

			em.persist(data);

			ut.commit();
		} catch (Exception e) {
			ut.rollback();
		}
		return "client_man";
	}
}
