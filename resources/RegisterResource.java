package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;



import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;

import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData;

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Transaction;
import com.google.cloud.datastore.Entity;



@Path("/OP1")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RegisterResource {
	
	private static final String SU = "SU";
	private static final String GBO = "GBO";
	private static final String GA = "GA";
	private static final String GS= "GS";
	private static final String USER = "USER";
	
	private static final String INACTIVE = "INACTIVE";
	private static final String PUBLIC = "PUBLIC";
	private static final String EMPTY = "EMPTY";
	private static final String FALSE = "LOGOUT";
	
	public RegisterResource() {}
	
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response RegistUsersWithRole(RegisterData data) {
		
		final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
		final Logger LOG = Logger.getLogger(RegisterResource.class.getName());		
		
		LOG.fine("Attempt to register user: " + data.userID);
		
		//Checks input data
		if(!data.validRegistration()) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		
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
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User already exists.").build();
			}else {
				
				user = Entity.newBuilder(userKey)
					.set("user_email", data.email)
					.set("user_pwd", DigestUtils.sha512Hex(data.password))
					.set("user_name", data.userName)
					.set("user_state", INACTIVE)
					.set("user_perfil", PUBLIC)
					.set("user_telFix", EMPTY)
					.set("user_telMov", EMPTY)
					.set("user_work", EMPTY)
					.set("user_workLocal", EMPTY)
					.set("user_address", EMPTY)
					.set("user_nif", EMPTY)
					.set("user_photo", EMPTY)
					.set("user_role", USER)
					.set("user_logged", FALSE)
					.set("session_token", EMPTY)
					.build();
				txn.add(user);
				txn.commit();
				return Response.ok("User " + data.userID + " registered.").build();
			}
		}finally {
			if(txn.isActive())
				txn.rollback();
		}

	}
	
	
	
	@POST
	@Path("/{role}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response RegistUsers(RegisterData data, @PathParam("role") String role) {
		
		final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
		final Logger LOG = Logger.getLogger(RegisterResource.class.getName());		
		
		LOG.fine("Attempt to register user: " + data.userID);
		
		//Checks input data
		if(!data.validRegistration()) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		
		Transaction txn = datastore.newTransaction();
		
		String userRole = USER;
		
		 Key ancestorKey = datastore.newKeyFactory()
					.setKind("Project")
		            .newKey("project");
		 PathElement ancestorPath = PathElement.of(ancestorKey.getKind(), ancestorKey.getName());
		 
		 if(role.equals(SU) || role.equals(GS) || role.equals(GA) || role.equals(GBO) || role.equals(USER))
			 userRole = role;
		 else 
			 return Response.status(Status.BAD_REQUEST).entity("Can't register a user with that role.").build();
			 
		
		try {
			Key userKey = datastore.newKeyFactory()
					.setKind("User")
					.addAncestor(ancestorPath)
					.newKey(data.userID);
			
			Entity user = txn.get(userKey);
			
			if(user != null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User already exists.").build();
			}else {
				
				user = Entity.newBuilder(userKey)
					.set("user_email", data.email)
					.set("user_pwd", DigestUtils.sha512Hex(data.password))
					.set("user_name", data.userName)
					.set("user_state", INACTIVE)
					.set("user_perfil", PUBLIC)
					.set("user_telFix", EMPTY)
					.set("user_telMov", EMPTY)
					.set("user_work", EMPTY)
					.set("user_workLocal", EMPTY)
					.set("user_address", EMPTY)
					.set("user_nif", EMPTY)
					.set("user_photo", EMPTY)
					.set("user_role", userRole)
					.set("user_logged", FALSE)
					.set("session_token", EMPTY)
					.build();
				txn.add(user);
				txn.commit();
				return Response.ok("User " + data.userID + " registered.").build();
			}
		}finally {
			if(txn.isActive())
				txn.rollback();
		}

	}
	

}
