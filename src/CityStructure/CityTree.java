package CityStructure;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentSkipListSet;

public class CityTree
        extends ConcurrentSkipListSet<City>
        implements Serializable {
    public LocalDateTime authDateTime;

    public void setAuthDateTime(LocalDateTime authDateTime) {
        this.authDateTime = authDateTime;
    }

    public LocalDateTime getAuthDateTime(){
        return this.authDateTime;
    }
}
