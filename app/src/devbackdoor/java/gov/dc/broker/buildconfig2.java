package gov.dc.broker;

class BuildConfig2 {
    public static boolean isBrokerBuild() {
        return false;
    }

    public static int getDataSourceIndex (){
        return 5 ;
    }

    private static HbxSite.ServerSiteConfig serverSiteConfig = new HbxSite.ServerSiteConfig("http", "54.82.163.155", 3000);
    private static BackdoorSite site = new BackdoorSite(serverSiteConfig);
    public static HbxSite.ServerSiteConfig getServerSiteConfig() {
        return serverSiteConfig;
    }

    public static Site getSite() {
        return site;
    }
}