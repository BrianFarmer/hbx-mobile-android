package gov.dc.broker;

import org.joda.time.DateTime;

public class CachedData<T> {
    public DateTime time;
    public T storedData;

    public CachedData(T data, DateTime time){
        this.time = time;
        this.storedData = data;
    }
}
