package gov.dc.broker;

class BuildConfig2 {
    static String getString(){
        return "Woo Hoo!!!";
    }

    public static boolean isBrokerBuild() {
        return false;
    }

    public static int getDataSourceIndex (){
        return 0;
    }

    private static HbxSite.ServerSiteConfig serverSiteConfig = new HbxSite.ServerSiteConfig("https", "enroll-feature.dchbx.org", 443);
    private static GitSite site = new GitSite();

    public static HbxSite.ServerSiteConfig getServerSiteConfig() {
        return null;
    }

    public static Site getSite() {
        return site;
    }
}