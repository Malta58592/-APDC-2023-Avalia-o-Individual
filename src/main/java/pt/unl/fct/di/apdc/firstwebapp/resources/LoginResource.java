package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.UUID;


import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
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
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;

@Path("/OP6")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {
	
	private static final String TRUE = "LOGGED";
	
	private static final long EXPIRATION_TIME = 1000*60*60*2; //2h
	
	public LoginResource() {}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response Login(LoginData data) {
		
		final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
		final Logger LOG = Logger.getLogger(RegisterResource.class.getName());	
		final Logged log = new Logged();
		
		
		LOG.fine("Attempt to login user : " + data.userID + ".");
		
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
			
			if(user != null && user.getString("user_pwd").equals( DigestUtils.sha512Hex(data.pwd))) {
				
				if(log.isLogged(user, datastore, txn)) {
					txn.rollback();
					return Response.status(Status.BAD_REQUEST).entity("User already logged.").build();
				}
				
				String tokenID = UUID.randomUUID().toString();
				Key tokenKey = datastore.newKeyFactory()
						.setKind("Token")
						.addAncestor(ancestorPath)
						.newKey(tokenID);
				Entity token = txn.get(tokenKey);
				
				token = Entity.newBuilder(tokenKey)
						.set("user_id",user.getKey().getName())
						.set("user_role",user.getString("user_role"))
						.set("valid_from",System.currentTimeMillis())
						.set("valid_to",System.currentTimeMillis() + EXPIRATION_TIME)
						.build();
				txn.add(token);
				
				user = Entity.newBuilder(user)
						.set("user_logged", TRUE)
						.set("session_token", tokenID)
						.build();
				txn.update(user);
				txn.commit();
				return Response.ok("User " + data.userID + " login is complete.\n"+
				"OP2 - Remove User\n"+"OP3 - List Users\n"+"OP4 - Modify Users\n"+"OP5 - Change Password\n"
				+"OP7 - Token Info\n"+"OP8 - LogOut\n").build();
				
			}else {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Failed to login.").build();
			}
		 }
		finally {
			if(txn.isActive())
				txn.rollback();
		}

	}
	
}