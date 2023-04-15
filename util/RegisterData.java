package pt.unl.fct.di.apdc.firstwebapp.util;

public class RegisterData {
	
	public String userID, email, userName, password, confPassword;
	
	public RegisterData() {}
	
	public RegisterData(String userID, String email, String userName,
				String password, String confPassword) {
		this.userID = userID;
		this.email = email;
		this.userName = userName;
		this.password = password;
		this.confPassword = confPassword;
	}
	
	public boolean validRegistration() {
		return password.length()>= 8 && email.contains("@") && password.equals(confPassword);
	}

}
