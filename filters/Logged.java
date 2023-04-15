package pt.unl.fct.di.apdc.firstwebapp.filters;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Transaction;

public class Logged {
	private static final String TRUE = "LOGGED";
	
	public Logged() {}
	
	public boolean isLogged(Entity user, Datastore datastore, Transaction txn) {
		 Key ancestorKey = datastore.newKeyFactory()
					.setKind("Project")
		            .newKey("project");
		 PathElement ancestorPath = PathElement.of(ancestorKey.getKind(), ancestorKey.getName());
		
		 String tokenID = user.getString("session_token");
		 Key tokenKey = datastore.newKeyFactory()
				.setKind("Token")
				.addAncestor(ancestorPath)
				.newKey(tokenID);
		 Entity token = txn.get(tokenKey);
		
		return user.getString("user_logged").equals(TRUE) && token.getLong("valid_to") > System.currentTimeMillis();
	}

}
