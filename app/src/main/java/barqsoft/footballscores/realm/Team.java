package barqsoft.footballscores.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by shetty on 03/02/16.
 */
public class Team extends RealmObject {

    @PrimaryKey
    private String id;
    private String crestUrl;
    private String localUrl;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCrestUrl() {
        return crestUrl;
    }

    public void setCrestUrl(String crestUrl) {
        this.crestUrl = crestUrl;
    }

    public String getLocalUrl() {
        return localUrl;
    }

    public void setLocalUrl(String localUrl) {
        this.localUrl = localUrl;
    }



    /*public Season getSeason() {
        return season;
    }*/
}