package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.ArrayList;


import java.util.List;
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
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.firstwebapp.filters.Logged;

import com.google.cloud.datastore.Entity;



@Path("/OP9")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ListUserLoggedResource {

	private static final String TRUE = "LOGGED";
	
	public ListUserLoggedResource() {}
	
	@GET
	@Path("/{userID}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response ListUsers(@PathParam("userID") String userID) {
		
		final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
		final Logger LOG = Logger.getLogger(RegisterResource.class.getName());		
		final Gson g = new Gson();;
		final Logged log = new Logged();
		
		LOG.fine("Attempt to list logged users: ");
		
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
		
			Query<Entity> query = Query.newEntityQueryBuilder()
					.setKind("User")
					.setFilter(PropertyFilter.hasAncestor(ancestorKey))
					.build();
			
			
			QueryResults<Entity> results = txn.run(query);
			
			List<String> entitysInfo = new ArrayList<String>();
			
			while (results.hasNext()) {
				Entity entity = results.next();
				if(entity.getString("user_logged").equals(TRUE)) {
					String entityID = entity.getKey().getName();
					String entityRole = entity.getString("user_role");		
					String info = "Info: " + entityID + " | " + entityRole;
					entitysInfo.add(info); 
				}
			}
			
			txn.commit();
			return Response.ok(g.toJson(entitysInfo)).build();
			
		}else{
			txn.rollback();
			return Response.status(Status.BAD_REQUEST).entity("User does not exists.").build();
		}
		
		
	}
	

}
