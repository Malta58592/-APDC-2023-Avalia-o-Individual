package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangePasswordData {
	
	public String userID;
	public String pwd, newPwd, confNewPwd;
	
	public ChangePasswordData(){}
	
	public ChangePasswordData(String userID, String pwd, String newPwd, String confNewPwd){
		this.userID = userID;
		this.pwd = pwd;
		this.newPwd = newPwd;
		this.confNewPwd = confNewPwd;
	}
}
