package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.apdc.firstwebapp.filters.Logged;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;

@Path("/OP8")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LogOutResource {
	
	private static final String FALSE = "LOGOUT";
	private static final String EMPTY = "EMPTY";
	
	public LogOutResource() {}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response LogOut(LoginData data) {
		
		final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
		final Logger LOG = Logger.getLogger(RegisterResource.class.getName());	
		final Logged log = new Logged();
		
		LOG.fine("Attempt to logOut user : " + data.userID + ".");
		
		Transaction txn = datastore.newTransaction();
		
		 Key ancestorKey = datastore.newKeyFactory()
					.setKind("Project")
		            .newKey("project");
		 PathElement ancestorPath = PathElement.of(ancestorKey.getKind(), ancestorKey.getName());
		 
		 try {
		
		 Key userKey = datastore.newKeyFactory()
					.setKind("User")
					.addAncestor(ancestorPath)
					.newKey(data.userID);
			
			Entity user = txn.get(userKey);
			
			
			if(user != null) {
				
				if(!log.isLogged(user, datastore, txn)) {
					txn.rollback();
					return Response.status(Status.BAD_REQUEST).entity("User is already logged out.").build();
				}
				
				String oldTokenId = user.getString("session_token");
				Key oldTokenKey = datastore.newKeyFactory()
						.setKind("Token")
						.addAncestor(ancestorPath)
						.newKey(oldTokenId);
				txn.delete(oldTokenKey);
				
				user = Entity.newBuilder(user)
						.set("user_logged", FALSE)
						.set("session_token", EMPTY)
						.build();
				
				txn.update(user);
				txn.commit();
				return Response.ok("User " + data.userID + " logout.").build();
			}else {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User does not exist.").build();
			}
		 }
		finally {
			if(txn.isActive())
				txn.rollback();
		}

	}
	
}