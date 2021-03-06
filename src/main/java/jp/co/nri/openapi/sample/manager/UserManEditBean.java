package jp.co.nri.openapi.sample.manager;

import javax.annotation.Resource;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import jp.co.nri.openapi.sample.common.ConstDef;
import jp.co.nri.openapi.sample.persistence.User;

/**
 * ユーザ編集View
 * テンプレート：<a href="../../../../../../templates/user_man_edit.txt">user_man_edit.xhtml</a>
 */
@ManagedBean
public class UserManEditBean {

	@Resource
	UserTransaction ut;

	@PersistenceContext
	EntityManager em;

	private User data = new User();
	
	/**
	 * @return	編集対象USER
	 */
	public User getData() {
		return data;
	}

	/**
	 * @param data	編集対象USER
	 */
	public void setData(User data) {
		this.data = data;
	}

	/**
	 * 編集対象のユーザデータを初期化。
	 * 
	 * その後JSFによって、更新される。
	 * @param event
	 * @throws AbortProcessingException
	 */
	public void preRenderView(ComponentSystemEvent event) throws AbortProcessingException {
		Long id = (Long) FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
				.get(ConstDef.SK_USER_ID);
		System.out.println(String.format("preRenderView(%d)", id));

		if (null == id) {
			this.data = new User();
		} else {
			this.data = em.find(User.class, id);
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

			User d = em.merge(data);

			em.persist(d);

			ut.commit();
		} catch (Exception e) {
			ut.rollback();
		}

		return "user_man";
	}

	/**
	 * 追加ボタンが押下され、入力データをDBに保存する。
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
		return "user_man";
	}
}
