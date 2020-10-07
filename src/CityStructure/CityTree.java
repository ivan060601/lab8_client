package CityStructure;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentSkipListSet;

public class CityTree
        extends ConcurrentSkipListSet<City>
        implements Serializable {
    final static long serialVersionUID = 3L;
    public LocalDateTime authDateTime;

    public void setAuthDateTime(LocalDateTime authDateTime) {
        this.authDateTime = authDateTime;
    }

    public LocalDateTime getAuthDateTime(){
        return this.authDateTime;
    }

    public City getCityByID(Long id){
        for (City c : this){
            if (id.equals(Long.valueOf(c.getId()))){
                return c;
            }
        }
        return null;
    }
}
