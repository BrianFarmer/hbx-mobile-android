package org.dchbx.coveragehq;

class BuildConfig2 extends EnrollConfigBase {
    private static ServerConfiguration serverConfiguration = null;

    public static ServerConfiguration getServerConfiguration() {
        if (serverConfiguration != null){
            return serverConfiguration;
        }
        serverConfiguration = new ServerConfiguration();

        serverConfiguration.dataInfo = new ServerConfiguration.HostInfo();
        serverConfiguration.dataInfo.host = "enroll-mobile.dchbx.org";
        serverConfiguration.dataInfo.scheme = "https";
        serverConfiguration.dataInfo.port = 443;

        serverConfiguration.loginInfo = new ServerConfiguration.HostInfo();
        serverConfiguration.loginInfo.host = "hbx-mobile.dchbx.org";
        serverConfiguration.loginInfo.scheme = "https";
        serverConfiguration.loginInfo.port = 443;


        //serverConfiguration.employerListPath = "api/v1/mobile_api/employers_list";
        serverConfiguration.loginPath = "login";
        return serverConfiguration;
    }

    public static int getTimeoutCountdownSeconds() {
        return 30;
    }

    //
    // This is the number of seconds the have to pass before the user gets
    // a dialog telling them that the session is about to timeout.
    //
    public static int getSessionTimeoutSeconds() {
        return 14*60;
    }

    public static String getVersion() {
        return null;
    }
}
