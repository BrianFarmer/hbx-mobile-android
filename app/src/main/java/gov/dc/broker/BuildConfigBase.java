package gov.dc.broker;

import org.joda.time.Duration;

/**
 * Created by plast on 12/20/2016.
 */

public class BuildConfigBase {
    private static int cacheTimeoutSeconds = 120;

    public static Duration getCacheTimeout() {
        return Duration.standardSeconds(cacheTimeoutSeconds);
    }
}
