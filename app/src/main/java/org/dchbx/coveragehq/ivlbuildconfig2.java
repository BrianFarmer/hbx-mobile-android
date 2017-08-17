package org.dchbx.coveragehq;

import okhttp3.HttpUrl;

class IvlBuildConfig2 extends EnrollConfigBase {

    private static ServerConfiguration serverConfiguration = null;
    private String defaultLoginHost = "hbx-mobile2-preprod.dchbx.org";
    //private String defaultLoginHost = "hbx-mobile-preprod.dchbx.org";
    private String defaultLoginScheme = "https";
    private int defaultLoginPort = 443;

    public ServerConfiguration getServerConfiguration() {
        if (serverConfiguration != null){
            return serverConfiguration;
        }
        serverConfiguration = new ServerConfiguration();

        serverConfiguration.dataInfo = new ServerConfiguration.HostInfo();
        serverConfiguration.dataInfo.host = "enroll-mobile2.dchbx.org";
        serverConfiguration.dataInfo.scheme = "https";
        serverConfiguration.dataInfo.port = 443;


        serverConfiguration.loginInfo = new ServerConfiguration.HostInfo();

        serverConfiguration.loginInfo.host = defaultLoginHost;
        serverConfiguration.loginInfo.scheme = defaultLoginScheme;
        serverConfiguration.loginInfo.port = defaultLoginPort;


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
    public ServiceManager.DataSource DataSource() {
        return ServiceManager.DataSource.MobileServer;
    }

    public String getUrl() {
        if (serverConfiguration.currentMobileUrl != null){
            return serverConfiguration.currentMobileUrl;
        }

        HttpUrl build = new HttpUrl.Builder()
                .scheme(defaultLoginScheme)
                .host(defaultLoginHost)
                .addPathSegments(serverConfiguration.endpointsPath)
                .port(defaultLoginPort)
                .build();
        return build.toString();
    }

    public void setUrl(String mobileServerUrl) {
        HttpUrl httpUrl = HttpUrl.parse(mobileServerUrl);
        serverConfiguration.loginInfo.host = httpUrl.host();
        serverConfiguration.loginInfo.scheme = httpUrl.scheme();
        serverConfiguration.loginInfo.port = httpUrl.port();
        serverConfiguration.endpointsPath = httpUrl.encodedPath();
        serverConfiguration.currentMobileUrl = mobileServerUrl;
    }
}
