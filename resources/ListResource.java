package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.ArrayList;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.cloud.datastore.Transaction;
import com.google.cloud.datastore.Value;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.firstwebapp.filters.Logged;

import com.google.cloud.datastore.Entity;



@Path("/OP3")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ListResource {

	private static final String SU = "SU";
	private static final String GBO = "GBO";
	private static final String GA = "GA";
	private static final String GS= "GS";
	private static final String USER = "USER";
	
	private static final String ACTIVE = "ACTIVE";
	private static final String PUBLIC = "PUBLIC";
	private static final String EMPTY = "EMPTY";
	
	public ListResource() {}
	
	@GET
	@Path("/{userID}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response ListUsers(@PathParam("userID") String userID) {
		
		final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
		final Logger LOG = Logger.getLogger(RegisterResource.class.getName());		
		final Gson g = new Gson();;
		final Logged log = new Logged();
		
		LOG.fine("Attempt to list users: ");
		
		Transaction txn = datastore.newTransaction();
		
		Key ancestorKey = datastore.newKeyFactory()
				.setKind("Project")
	            .newKey("project");
		PathElement ancestorPath = PathElement.of(ancestorKey.getKind(), ancestorKey.getName());
		
		
		Key userKey = datastore.newKeyFactory().setKind("User").addAncestor(ancestorPath).newKey(userID);
		Entity user = txn.get(userKey);
		
		if (user != null) {
			
			if(!log.isLogged(user, datastore, txn)) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User is not logged.").build();
			}
			
		    String role = user.getString("user_role");
		
			Query<Entity> query = Query.newEntityQueryBuilder()
					.setKind("User")
					.setFilter(PropertyFilter.hasAncestor(ancestorKey))
					.build();
			
			
			QueryResults<Entity> results = txn.run(query);
			
			List<String> entitysInfo = new ArrayList<String>();
			
			if(role.equals(USER)) { 
				while (results.hasNext()) {
					Entity entity = results.next();
					if(entity.getString("user_role").equals(USER) && entity.getString("user_state").equals(ACTIVE) && entity.getString("user_perfil").equals(PUBLIC)) {
						String entityID = entity.getKey().getName();
						String entityEmail = entity.getString("user_email");		
						String entityName = entity.getString("user_name");
						String info = "Info: " + entityID + " | " + entityEmail + " | " + entityName;
						entitysInfo.add(info); 
					}
				}
				
			} else if(role.equals(GBO)) {
				while (results.hasNext()) {
					Entity entity = results.next();
					if(entity.getString("user_role").equals(USER)) {
						String entityID = entity.getKey().getName();
					    Map<String, Value<?>> properties = entity.getProperties();
					    String entityInfo = "Info: " + entityID;
					    for (Map.Entry<String, Value<?>> entry : properties.entrySet()) {
					    	String propertyName = entry.getKey();
					        Value<?> propertyValue = entry.getValue();
					        if (!propertyValue.get().equals(EMPTY) && !propertyName.equals("user_pwd") &&
					        		!propertyName.equals("session_token")) {
					        	String propertyValueString = (String)propertyValue.get();
					        	entityInfo +=  " | " + propertyValueString;	
					        }
					    }
					    entitysInfo.add(entityInfo); 
					}
				}
				
			} else if(role.equals(GS)) {
				while (results.hasNext()) {
					Entity entity = results.next();
					if(entity.getString("user_role").equals(USER) || entity.getString("user_role").equals(GBO)
							||  entity.getString("user_role").equals(GA)) {
						String entityID = entity.getKey().getName();
					    Map<String, Value<?>> properties = entity.getProperties();
					    String entityInfo = "Info: " + entityID;
					    for (Map.Entry<String, Value<?>> entry : properties.entrySet()) {
					    	String propertyName = entry.getKey();
					        Value<?> propertyValue = entry.getValue();
					        if (!propertyValue.get().equals(EMPTY) && !propertyName.equals("user_pwd") &&
					        		!propertyName.equals("session_token")) {
					        	String propertyValueString = (String)propertyValue.get();
					        	entityInfo +=  " | " + propertyValueString;	
					        }
					    }
					    entitysInfo.add(entityInfo); 
					}
				}
				
			} else if(role.equals(SU)) {
				while (results.hasNext()) {
				    Entity entity = results.next();
				    String entityID = entity.getKey().getName();
				    Map<String, Value<?>> properties = entity.getProperties();
				    String entityInfo = "Info: " + entityID;
				    for (Map.Entry<String, Value<?>> entry : properties.entrySet()) {
				    	String propertyName = entry.getKey();
				        Value<?> propertyValue = entry.getValue();
				        if (!propertyValue.get().equals(EMPTY) && !propertyName.equals("user_pwd") &&
				        		!propertyName.equals("session_token")) {
				        	String propertyValueString = (String)propertyValue.get();
				        	entityInfo +=  " | " + propertyValueString;	
				        }
				    }
				    entitysInfo.add(entityInfo.toString());
				}

				
			}else{
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("This user can't see the list of users.").build();
			}
			
			txn.commit();
			return Response.ok(g.toJson(entitysInfo)).build();
			
		}else{
			txn.rollback();
			return Response.status(Status.BAD_REQUEST).entity("User does not exists.").build();
		}
		
		
	}
	

}
