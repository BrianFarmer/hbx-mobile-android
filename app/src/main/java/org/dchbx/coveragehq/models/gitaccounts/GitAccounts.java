package org.dchbx.coveragehq.models.gitaccounts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

/**
 * Created by plast on 1/24/2017.
 */

public class GitAccounts {
    public static class NamedAccountInfo {
        public String name;
        public AccountInfo accountInfo;
    }
    public ArrayList<NamedAccountInfo> accountInfoList;
    public Map<String, AccountInfo> accountInfoMap;

    public void setAccountInfo(Map<String, AccountInfo> accountInfoMap){
        this.accountInfoMap = accountInfoMap;
        this.accountInfoList = new ArrayList<>();
        for (Map.Entry<String, AccountInfo> accountInfoEntry: accountInfoMap.entrySet()) {
            NamedAccountInfo namedAccountInfo = new NamedAccountInfo();
            namedAccountInfo.accountInfo = accountInfoEntry.getValue();
            namedAccountInfo.name = accountInfoEntry.getKey();
            accountInfoList.add(namedAccountInfo);
        }
        Collections.sort(accountInfoList, new Comparator<NamedAccountInfo>() {
            @Override
            public int compare(NamedAccountInfo namedAccountInfo, NamedAccountInfo t1) {
                return namedAccountInfo.name.compareToIgnoreCase(t1.name);
            }
        });
    }
}
