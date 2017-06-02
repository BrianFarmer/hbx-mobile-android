package org.dchbx.coveragehq;

class IvlBuildConfig2 extends EnrollConfigBase {

    private static ServerConfiguration serverConfiguration = null;

    public ServerConfiguration getServerConfiguration() {
        if (serverConfiguration != null){
            return serverConfiguration;
        }
        serverConfiguration = new ServerConfiguration();

        serverConfiguration.dataInfo = new ServerConfiguration.HostInfo();
        serverConfiguration.dataInfo.host = "enroll-mobile.dchbx.org";
        serverConfiguration.dataInfo.scheme = "https";
        serverConfiguration.dataInfo.port = 443;


        serverConfiguration.loginInfo = new ServerConfiguration.HostInfo();

        serverConfiguration.loginInfo.host = "hbx-mobile2-preprod.dchbx.org";
        serverConfiguration.loginInfo.scheme = "https";
        serverConfiguration.loginInfo.port = 443;

        /*
        serverConfiguration.loginInfo.host = "mobile.dcmic.org";
        serverConfiguration.loginInfo.scheme = "http";
        serverConfiguration.loginInfo.port = 3003;
        */


        //serverConfiguration.employerListPath = "api/v1/mobile_api/employers_list";
        serverConfiguration.loginPath = "login";
        serverConfiguration.endpointsPath = "endpoints";
        return serverConfiguration;
    }

    @Override
    public int getTimeoutCountdownSeconds() {
        return 30;
    }

    //
    // This is the number of seconds the have to pass before the user gets
    // a dialog telling them that the session is about to timeout.
    //
    @Override
    public int getSessionTimeoutSeconds() {
        return 14*60;
    }

    @Override
    public String getVersion() {
        return "preprod";
    }

    @Override
    public BrokerWorkerConfig.DataSource DataSource() {
        return BrokerWorkerConfig.DataSource.MobileServer;
    }

    public String getUrl() {
        return serverConfiguration.loginInfo.host;
    }
}
