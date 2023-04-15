package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;


import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;


import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.apdc.firstwebapp.filters.Logged;

import com.google.cloud.datastore.Entity;



@Path("/OP2")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RemoveResource {
	
	private static final String SU = "SU";
	private static final String GBO = "GBO";
	private static final String GA = "GA";
	private static final String GS= "GS";
	private static final String USER = "USER";
	
	public RemoveResource() {}
	
	@DELETE
	@Path("/{usersID}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response RemoveUser(@PathParam("usersID") String usersID) {
		
		String[] usersIDvec = usersID.split("\\$");
		String userID = usersIDvec[0];
		String userToRemoveID = usersIDvec[1];
		
		final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
		final Logger LOG = Logger.getLogger(RegisterResource.class.getName());	
		final Logged log = new Logged();
		
		LOG.fine("User " + userID + " attempt to remove user: " + userToRemoveID);
		
		Transaction txn = datastore.newTransaction();
		
		Key ancestorKey = datastore.newKeyFactory()
				.setKind("Project")
	            .newKey("project");
		PathElement ancestorPath = PathElement.of(ancestorKey.getKind(), ancestorKey.getName());
		
		Key userKey = datastore.newKeyFactory().setKind("User").addAncestor(ancestorPath).newKey(userID);
		Entity user = txn.get(userKey);
		
		
		try {
			Key userToRemoveKey = datastore.newKeyFactory().setKind("User").addAncestor(ancestorPath).newKey(userToRemoveID);
			Entity userToRemove = txn.get(userToRemoveKey);
			
			if(user == null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User " + userID + " does not exists.").build();
				
			}else if(userToRemove == null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User " + userToRemoveID + " does not exists.").build();	
				
			}else{
				
				if(!log.isLogged(user, datastore, txn)) {
					txn.rollback();
					return Response.status(Status.BAD_REQUEST).entity("User is not logged.").build();
				}
				
				String userRole = user.getString("user_role");
				String userToRemoveRole = userToRemove.getString("user_role");
			
				if(userRole.equals(SU)){
					txn.delete(userToRemoveKey);
					txn.commit();
					return Response.ok("User " + userToRemoveID + " removed.").build();
					
				}else if(userRole.equals(GS)) {
					if(userToRemoveRole.equals(GA) || userToRemoveRole.equals(GBO) || userToRemoveRole.equals(USER)) {
						txn.delete(userToRemoveKey);
						txn.commit();
						return Response.ok("User " + userToRemoveID + " removed.").build();
					}else
						return Response.status(Status.BAD_REQUEST).entity("User " + userID + " can't remove user " + userToRemoveID + ".").build();
					
				}else if(userRole.equals(GA)) {
					if(userToRemoveRole.equals(GBO) || userToRemoveRole.equals(USER)) {
						txn.delete(userToRemoveKey);
						txn.commit();
						return Response.ok("User " + userToRemoveID + " removed.").build();
					}else
						return Response.status(Status.BAD_REQUEST).entity("User " + userID + " can't remove user " + userToRemoveID + ".").build();
					
				}else if(userRole.equals(GBO)) {
					if(userToRemoveRole.equals(USER)) {
						txn.delete(userToRemoveKey);
						txn.commit();
						return Response.ok("User " + userToRemoveID + " removed.").build();
					}else
						return Response.status(Status.BAD_REQUEST).entity("User " + userID + " can't remove user " + userToRemoveID + ".").build();
					
				}else {
					if(userID.equals(userToRemoveID))
						return Response.ok("User " + userToRemoveID + " removed.").build();
					else
						return Response.status(Status.BAD_REQUEST).entity("User " + userID + " can't remove other users.").build();
				}
			
			}
		}finally {
			if(txn.isActive())
				txn.rollback();
		}

	}
	
}
