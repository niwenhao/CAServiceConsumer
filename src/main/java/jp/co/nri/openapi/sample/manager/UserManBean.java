package jp.co.nri.openapi.sample.manager;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import jp.co.nri.openapi.sample.common.ConstDef;
import jp.co.nri.openapi.sample.persistence.Token;
import jp.co.nri.openapi.sample.persistence.User;

/**
 * ユーザメンテView
 * テンプレート：<a href="../../../../../../templates/user_man.txt">user_man.xhtml</a>
 */
@ManagedBean
public class UserManBean {

	@Resource
	UserTransaction ut;

	@PersistenceContext
	EntityManager em;

	/**
	 * ユーザ一覧を取得する。
	 * 
	 * @return ユーザ一覧
	 */
	public List<User> listUsers() {
		return em.createNamedQuery(User.LIST_ALL_USERS, User.class).getResultList();
	}

	/**
	 * 更新画面に遷移する。
	 * 
	 * @param id
	 *            ユーザのオブジェクトID
	 * @return 遷移先
	 * @throws Exception
	 */
	public String update(long id) throws Exception {
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstDef.SK_USER_ID, id);
		return "user_man_edit";
	}

	/**
	 * ユーザを削除する。
	 * 
	 * @param id
	 *            ユーザのオブジェクトID
	 * @return 遷移先
	 * @throws Exception
	 */
	public String delete(long id) throws Exception {
		ut.begin();

		User u = em.find(User.class, id);
		em.remove(u);
		ut.commit();

		return null;
	}

	/**
	 * システム混乱するときを考え、ユーザのすべてのトークンを削除する。
	 * 
	 * @param id	ユーザのオブジェクトID
	 * @return	遷移先。
	 */
	public String reset_token(long id) {
		try {
			ut.begin();

			User u = em.find(User.class, id);
			u.getTokens().clear();
			
			em.persist(u);

			ut.commit();
			return null;
		} catch (Exception e) {
			try {
				ut.rollback();
			} catch (Exception e1) {
				throw new RuntimeException(e1);
			}
			throw new RuntimeException(e);
		}
	}

	/**
	 * 追加画面に遷移する。
	 * 
	 * @return 遷移先
	 * @throws Exception
	 */
	public String append() throws Exception {
		Map<String, Object> map = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
		if (map.containsKey(ConstDef.SK_USER_ID))
			map.remove(ConstDef.SK_USER_ID);
		return "user_man_edit";

	}

}
