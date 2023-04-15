package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.apdc.firstwebapp.filters.Logged;
import pt.unl.fct.di.apdc.firstwebapp.util.ChangePasswordData;

@Path("/OP5")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ChangePasswordResource {
	
	public ChangePasswordResource() {}
	
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response ChangePassword(ChangePasswordData data){
		
		final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
		final Logger LOG = Logger.getLogger(RegisterResource.class.getName());		
		final Logged log = new Logged();
		
		LOG.fine("Attempt to change user: " + data.userID + " password");
		
		Transaction txn = datastore.newTransaction();
		
		Key ancestorKey = datastore.newKeyFactory()
				.setKind("Project")
	            .newKey("project");
		PathElement ancestorPath = PathElement.of(ancestorKey.getKind(), ancestorKey.getName());
		
		Key userKey = datastore.newKeyFactory().setKind("User").addAncestor(ancestorPath).newKey(data.userID);
		Entity user = txn.get(userKey);
		
		try {
			
			if(user != null) {
				
				if(!log.isLogged(user, datastore, txn)) {
					txn.rollback();
					return Response.status(Status.BAD_REQUEST).entity("User is not logged.").build();
				}
				
				if(!user.getString("user_pwd").equals( DigestUtils.sha512Hex(data.pwd))) {
					txn.rollback();
					return Response.status(Status.BAD_REQUEST).entity("Can't change another user password.").build();
					
				} else {
					if(data.newPwd.equals(data.confNewPwd)) {
						user = Entity.newBuilder(user)
								.set("user_pwd", DigestUtils.sha512Hex(data.newPwd))
								.build();
						txn.update(user);
					}
					
					txn.commit();
					return Response.ok("User " + data.userID + " password was changed.").build();
				}
			} else {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User does not exist.").build();
			}
		}
		finally{	
			if(txn.isActive())
			txn.rollback();
		}
		
	}

}
