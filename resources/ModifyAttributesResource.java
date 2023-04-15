package pt.unl.fct.di.apdc.firstwebapp.resources;

import javax.ws.rs.Consumes;
import java.util.logging.Logger;
import javax.ws.rs.PUT;
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
import pt.unl.fct.di.apdc.firstwebapp.util.ModifyAttributesData;


@Path("/OP4")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ModifyAttributesResource {
	
	private static final String SU = "SU";
	private static final String GBO = "GBO";
	private static final String GS= "GS";
	private static final String USER = "USER";
	
	public ModifyAttributesResource() {}
	
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response ModifyAttributes(ModifyAttributesData data) {
		
		final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();	
		final Logger LOG = Logger.getLogger(RegisterResource.class.getName());	
		final Logged log = new Logged();
		
		LOG.fine("Attempt to modify user: " + data.userToChangeID);
		
		Transaction txn = datastore.newTransaction();
		
		Key ancestorKey = datastore.newKeyFactory()
				.setKind("Project")
	            .newKey("project");
		PathElement ancestorPath = PathElement.of(ancestorKey.getKind(), ancestorKey.getName());
		
		Key userKey = datastore.newKeyFactory().setKind("User").addAncestor(ancestorPath).newKey(data.userID);
		Entity user = txn.get(userKey);
		
		Key userToChangeKey = datastore.newKeyFactory().setKind("User").addAncestor(ancestorPath).newKey(data.userToChangeID);
		Entity userToChange = txn.get(userToChangeKey);
		
		String[] atts = data.attsToChange.split("!");
		
		try {
			
			if(user == null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User " + data.userID + " does not exists.").build();
				
			} else if(userToChange == null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User " + data.userID + " does not exists.").build();
				
			} else {
				
				if(!log.isLogged(user, datastore, txn)) {
					txn.rollback();
					return Response.status(Status.BAD_REQUEST).entity("User is not logged.").build();
				}
				
				String userRole = user.getString("user_role");
				String userToChangeRole = userToChange.getString("user_role");
				
				if(userRole.equals(USER) && data.userID.equals(data.userToChangeID)) {
					
						for (String att:atts) {
							String[] keysAndAtts = att.split("=");
							String attToChange = keysAndAtts[0];
							String value = keysAndAtts[1];
							
							if(!value.equals(data.userToChangeID) && !attToChange.equals("user_name") && !attToChange.equals("user_email")
									&& !attToChange.equals("user_state") && !attToChange.equals("user_role")) {
								
								userToChange = Entity.newBuilder(userToChange)
								.set(attToChange, value)
								.build();
								txn.update(userToChange);
							}else {
								txn.rollback();
								return Response.status(Status.BAD_REQUEST).entity("This user can't change this attribute.").build();
							}

					}
						txn.commit();
						return Response.ok("User " + data.userToChangeID + " was changed.").build();
					
					
				}else if(userRole.equals(GBO)) {
					
					if(userToChangeRole.equals(USER)) {
						for (String att:atts) {
							String[] keysAndAtts = att.split("=");
							String attToChange = keysAndAtts[0];
							String value = keysAndAtts[1];
							
							if(!value.equals(data.userToChangeID)) {
								
								userToChange = Entity.newBuilder(userToChange)
								.set(attToChange, value)
								.build();
								txn.update(userToChange);
							}else {
								txn.rollback();
								return Response.status(Status.BAD_REQUEST).entity("Users can't change id's.").build();
							}

					}
						txn.commit();
						return Response.ok("User " + data.userToChangeID + " was changed.").build();
					}else {
						txn.rollback();
						return Response.status(Status.BAD_REQUEST).entity("User " + data.userID + " can't change user " + data.userToChangeID + ".").build();
					}
					
				}else if(userRole.equals(GS)) {
					
					if(userToChangeRole.equals(GBO) || userToChangeRole.equals(USER)) {
						for (String att:atts) {
							String[] keysAndAtts = att.split("=");
							String attToChange = keysAndAtts[0];
							String value = keysAndAtts[1];
							
							if(!value.equals(data.userToChangeID)) {	
								userToChange = Entity.newBuilder(userToChange)
								.set(attToChange, value)
								.build();
								txn.update(userToChange);
							}else {
								txn.rollback();
								return Response.status(Status.BAD_REQUEST).entity("Users can't change id's.").build();
							}						
							
						}
						txn.commit();
						return Response.ok("User " + data.userToChangeID + " was changed.").build();
					}else {
						txn.rollback();
						return Response.status(Status.BAD_REQUEST).entity("User " + data.userID + " can't change user " + data.userToChangeID + ".").build();
					}
					
				}else if(userRole.equals(SU)) {
					
					if(userToChangeRole.equals(GS) || userToChangeRole.equals(GBO) || userToChangeRole.equals(USER)) {
						for (String att:atts) {
							String[] keysAndAtts = att.split("=");
							String attToChange = keysAndAtts[0];
							String value = keysAndAtts[1];
							
							if(!value.equals(data.userToChangeID)) {							
								userToChange = Entity.newBuilder(userToChange)
								.set(attToChange, value)
								.build();
								txn.update(userToChange);
							}else {
								txn.rollback();
								return Response.status(Status.BAD_REQUEST).entity("Users can't change id's.").build();
							}						
						}
						txn.commit();
						return Response.ok("User " + data.userToChangeID + " was changed.").build();
					}else {
						txn.rollback();
						return Response.status(Status.BAD_REQUEST).entity("User " + data.userID + " can't change user " + data.userToChangeID + ".").build();
					}
							
				}else {
					txn.rollback();
					return Response.status(Status.BAD_REQUEST).entity("User " + data.userID + " can't change user other users.").build();
				}
				
			}
		}finally{
			if(txn.isActive())
				txn.rollback();
		}
		
	}

}
