package pt.unl.fct.di.apdc.firstwebapp.util;

public class ModifyAttributesData {
	
	public String userID;
	public String userToChangeID;
	public String attsToChange;
	
	public ModifyAttributesData() {}
	
	public ModifyAttributesData(String userID, String userToChangeID, String attsToChange) {
		this.userID = userID;
		this.userToChangeID = userToChangeID;
		this.attsToChange = attsToChange;
		
		
	}
}
