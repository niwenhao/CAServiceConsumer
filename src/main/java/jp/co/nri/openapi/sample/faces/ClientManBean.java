package jp.co.nri.openapi.sample.faces;

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

@ManagedBean
public class ClientManBean {

	@Resource
	UserTransaction ut;

	@PersistenceContext
	EntityManager em;

	public List<Client> listClients() {
		CriteriaQuery<Client> query = em.getCriteriaBuilder().createQuery(Client.class);
		Root<Client> root = query.from(Client.class);
		return em.createQuery(query.select(root)).getResultList();
	}

	public String apiMan(long id) {
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstDef.SK_CLIENT_ID, id);
		return "service_man";
	}

	public String update(long id) throws Exception {
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ConstDef.SK_CLIENT_ID, id);
		return "client_man_edit";
	}

	public String delete(long id) throws Exception {
		ut.begin();

		Client u = em.find(Client.class, Long.valueOf(id));
		em.remove(u);
		ut.commit();
		return null;
	}

	public String append() throws Exception {
		Map<String, Object> map = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
		if (map.containsKey(ConstDef.SK_CLIENT_ID))
			map.remove(ConstDef.SK_CLIENT_ID);
		return "client_man_edit";
	}
}
