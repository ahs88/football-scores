package barqsoft.footballscores.realm;

import io.realm.Realm;
import io.realm.RealmObject;

/**
 * Created by shetty on 05/02/16.
 */
public class Season extends RealmObject{

    private boolean dataLoaded;
    private String seasonId;

    public boolean isDataLoaded() {
        return dataLoaded;
    }

    public void setDataLoaded(boolean dataLoaded) {
        this.dataLoaded = dataLoaded;
    }

    public String getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(String seasonId) {
        this.seasonId = seasonId;
    }
}
