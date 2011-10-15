package blob.mylocation;



import java.util.Iterator;
import java.util.List;

import android.location.Address;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.R.integer;
import android.app.Activity;


import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;



/**
 * @author andrejsstepanovs@gmail.com
 *
 * Retrieve accurate location from GPS or network services. 
 * 
 *
 * Class usage example:
 * 
 * public void onCreate(Bundle savedInstanceState) {
 *      ...
 *      my_location = new MyLocation();
 *      my_location.init(main.this, locationResult);
 * }
 * 
 * 
 * public LocationResult locationResult = new LocationResult(){
 *      @Override
 *      public void gotLocation(final Location location){
 *          // do something
 *          location.getLongitude();
 *          location.getLatitude();
 *      }
 *  };
 */
class MyLocation{

    /**
     * If GPS is enabled. 
     * Use minimal connected satellites count.
     */
    private static final int min_gps_sat_count = 5;

    /**
     * Iteration step time.
     */
    private static final int iteration_timeout_step = 500;

    LocationResult locationResult;
    private Location bestLocation = null;
    private Handler handler = new Handler();
    private LocationManager myLocationManager; 
    public Context context;

    private boolean gps_enabled = false;

    private int counts    = 0;
    private int sat_count = 0;

    private Runnable showTime = new Runnable() {

         public void run() {
            boolean stop = false;
            counts++;
            //System.println("counts=" + counts);

            //if timeout (1 min) exceeded, stop tying
            if(counts > 120){
                stop = true;
            }

            //update last best location
            bestLocation = getLocation(context);

            //if location is not ready or don`t exists, try again
            if(bestLocation == null){
                //System.println("BestLocation not ready, continue to wait");
                handler.postDelayed(this, iteration_timeout_step);
            }else{
                //if best location is known, calculate if we need to continue to look for better location
                //if gps is enabled and min satellites count has not been connected or min check count is smaller then 4 (2 sec)  
                if(stop == false && !needToStop()){
                    //System.println("Connected " + sat_count + " sattelites. continue waiting..");
                    handler.postDelayed(this, iteration_timeout_step);
                }else{
                    //System.println("#########################################");
                    //System.println("BestLocation finded return result to main. sat_count=" + sat_count);
                    //System.println("#########################################");

                    // removing all updates and listeners
                    myLocationManager.removeUpdates(gpsLocationListener);
                    myLocationManager.removeUpdates(networkLocationListener);    
                    myLocationManager.removeGpsStatusListener(gpsStatusListener);
                    sat_count = 0;

                    // send best location to locationResult
                    locationResult.gotLocation(bestLocation);
                }
            }
         }
    };

    /**
     * Determine if continue to try to find best location
     */
    private Boolean needToStop(){
        if(gps_enabled){
            if(counts <= 4){
                return false;
            }
            if(sat_count < min_gps_sat_count){
                //if 20-25 sec and 3 satellites found then stop
                if(counts >= 40 && sat_count >= 3){
                    return true;
                }
                return false;
            }
        }
        return true;
    }

    /**
     * Best location abstract result class
     */
    public static abstract class LocationResult{
         public abstract void gotLocation(Location location);
     }

    /**
     * Initialize starting values and starting best location listeners
     * 
     * @param Context ctx
     * @param LocationResult result
     */
    public void init(Context ctx, LocationResult result){
        context = ctx;
        locationResult = result;

        myLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        gps_enabled = (Boolean) myLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        bestLocation = null;
        counts = 0;

        // turning on location updates
        myLocationManager.requestLocationUpdates("network", 0, 0, networkLocationListener);
        myLocationManager.requestLocationUpdates("gps", 0, 0, gpsLocationListener);
        myLocationManager.addGpsStatusListener(gpsStatusListener);

        // starting best location finder loop
        handler.postDelayed(showTime, iteration_timeout_step);
    }

    /**
     * GpsStatus listener. OnChainged counts connected satellites count.
     */
    public final GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) {

             if(event == GpsStatus.GPS_EVENT_SATELLITE_STATUS){
                try {
                    // Check number of satellites in list to determine fix state
                     GpsStatus status = myLocationManager.getGpsStatus(null);
                     Iterable<GpsSatellite>satellites = status.getSatellites();

                     sat_count = 0;

                     Iterator<GpsSatellite>satI = satellites.iterator();
                     while(satI.hasNext()) {
                         GpsSatellite satellite = satI.next();
                         //System.println("Satellite: snr=" + satellite.getSnr() + ", elevation=" + satellite.getElevation());                         
                         sat_count++;
                     }
                } catch (Exception e) {
                    e.printStackTrace();
                    sat_count = min_gps_sat_count + 1;
                }

                 //System.println("#### sat_count = " + sat_count);
             }
         }
    };

    /**
     * Gps location listener.
     */
    public final LocationListener gpsLocationListener = new LocationListener(){
        @Override
         public void onLocationChanged(Location location){

        }
         public void onProviderDisabled(String provider){}
         public void onProviderEnabled(String provider){}
         public void onStatusChanged(String provider, int status, Bundle extras){}
    }; 

    /**
     * Network location listener.
     */
    public final LocationListener networkLocationListener = new LocationListener(){
        @Override
         public void onLocationChanged(Location location){

        }
         public void onProviderDisabled(String provider){}
         public void onProviderEnabled(String provider){}
         public void onStatusChanged(String provider, int status, Bundle extras){}
    }; 


    /**
     * Returns best location using LocationManager.getBestProvider()
     * 
     * @param context
     * @return Location|null
     */
    public static Location getLocation(Context context){
        //System.println("getLocation()");

        // fetch last known location and update it
        try {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
             criteria.setAltitudeRequired(false);
             criteria.setBearingRequired(false);
             criteria.setCostAllowed(true);
             String strLocationProvider = lm.getBestProvider(criteria, true);

             //System.println("strLocationProvider=" + strLocationProvider);
             Location location = lm.getLastKnownLocation(strLocationProvider);
             if(location != null){
                return location;
             }
             return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}