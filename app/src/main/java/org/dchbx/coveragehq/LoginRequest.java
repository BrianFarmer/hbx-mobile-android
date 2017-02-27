package org.dchbx.coveragehq;

/**
 * Created by plast on 7/26/2016.
 */
public class LoginRequest {
    private String accountName;
    private String password;

    public LoginRequest(String accountName, String password){
        this.accountName = accountName;
        this.password = password;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getPassword() {
        return password;
    }
}
