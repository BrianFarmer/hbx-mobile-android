package org.dchbx.coveragehq;

/**
 * Created by plast on 12/20/2016.
 */
public class CoverageException extends Exception {
    private final String reason;

    public CoverageException(String reason) {
        this.reason = reason;
    }
}
