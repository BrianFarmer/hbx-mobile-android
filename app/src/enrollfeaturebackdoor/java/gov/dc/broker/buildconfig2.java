package gov.dc.broker;

class BuildConfig2 {
    static String getString(){
        return "Woo Hoo!!!";
    }

    public static boolean isBrokerBuild() {
        return false;
    }

    public static int getDataSourceIndex (){
        return 5 ;
    }



    private static HbxSite.ServerSiteConfig serverSiteConfig = new HbxSite.ServerSiteConfig("https", "enroll-feature.dchbx.org", 443);
    private static BackdoorSite site = new BackdoorSite(serverSiteConfig);

    public static HbxSite.ServerSiteConfig getServerSiteConfig() {
        return serverSiteConfig;
    }

    public static Site getSite() {
        return site;
    }

    public  ConnectionHandler getConnectionHandler(){
        return null;
    }
}