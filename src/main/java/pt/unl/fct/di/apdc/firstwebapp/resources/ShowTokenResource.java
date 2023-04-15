package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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

@Path("/OP7")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ShowTokenResource {

	public ShowTokenResource() {}
	
	@GET
	@Path("/{userID}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response tokenInfo(@PathParam("userID") String userID) {
		
		final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
		final Logger LOG = Logger.getLogger(RegisterResource.class.getName());	
		final Logged log = new Logged();
		
		LOG.fine("Attempt to see session token info from user : " + userID + ".");
		
		Transaction txn = datastore.newTransaction();
		
		 Key ancestorKey = datastore.newKeyFactory()
					.setKind("Project")
		            .newKey("project");
		 PathElement ancestorPath = PathElement.of(ancestorKey.getKind(), ancestorKey.getName());
		 
		 try {
			
			Key userKey = datastore.newKeyFactory()
						.setKind("User")
						.addAncestor(ancestorPath)
						.newKey(userID);	
			Entity user = txn.get(userKey);
			
			if(user != null && log.isLogged(user, datastore, txn)) {
				String tokenID = user.getString("session_token");
				Key tokenKey = datastore.newKeyFactory()
						.setKind("Token")
						.addAncestor(ancestorPath)
						.newKey(tokenID);	
				Entity token = txn.get(tokenKey);
				LOG.info("FLASH: " + token);
				
				long creaTime = token.getLong("valid_from");
				long expirTime = token.getLong("valid_to");
				
				txn.commit();
				return Response.ok("Token id: " + tokenID + "\nToken user: " + userID + "\nUser role: " 
				+ user.getString("user_role") + "\nCreation time: " + creaTime + "\nExpiration time: " + expirTime).build();
				
			} else {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User is not logged.").build();
				
			}
			
		 }
		finally{
			if(txn.isActive())
				txn.rollback();
		}
	}
	
}
