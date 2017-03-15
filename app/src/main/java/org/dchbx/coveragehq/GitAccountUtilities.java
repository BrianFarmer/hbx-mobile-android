package org.dchbx.coveragehq;

import org.dchbx.coveragehq.models.gitaccounts.AccountInfo;
import org.dchbx.coveragehq.models.gitaccounts.GitAccounts;

import java.util.ArrayList;

/**
 * Created by plast on 3/13/2017.
 */

public class GitAccountUtilities {

    public static GitAccounts.NamedAccountInfo getAccountInfo(GitAccounts gitAccounts, int accountId) {
        return gitAccounts.accountInfoList.get(accountId);
    }

    public static int getAccountType(AccountInfo accountInfo) {
        if (accountInfo.brokerEndpoint != null){
            return org.dchbx.coveragehq.AccountInfo.Broker;
        }
        if (accountInfo.employerDetailsEndpointPath != null){
            return org.dchbx.coveragehq.AccountInfo.Employer;
        }
        return org.dchbx.coveragehq.AccountInfo.Employee;
    }

    public static ArrayList<String> getAccountNames(GitAccounts gitAccounts) {
        ArrayList<String> names = new ArrayList<>();
        for (GitAccounts.NamedAccountInfo namedAccountInfo : gitAccounts.accountInfoList) {
            names.add(namedAccountInfo.name);
        }
        return names;
    }
}
