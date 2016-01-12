package nextbuspns_d.polytech.unice.fr.nextbuspls;

public class LocationManager {
    private static LocationManager instance = null;

    private LocationManager() {

    }

    public static synchronized LocationManager getInstance() {
        if (instance == null) {
            instance = new LocationManager();
        }
        return instance;
    }
}