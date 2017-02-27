package org.dchbx.coveragehq;

class AccountInfo {
    public static final int Broker = 0;
    public static final int Employer = 1;
    public static final int Employee = 2;

    public static String sessionId;

    public String accountName;
    public String password;
    public Boolean rememberMe;
    public String securityQuestion;
    public String securityAnswer;
    public String location;
    public int userType;
}