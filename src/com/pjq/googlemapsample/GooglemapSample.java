package com.pjq.googlemapsample;


import java.util.List;

import android.location.Address;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.content.Context;
import android.graphics.drawable.Drawable;
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


public class GooglemapSample extends MapActivity implements OnClickListener
{
	private static final int DEFAULT_ZOOM_LEVEL = 8;
	/** Called when the activity is first created. */
	private MapView			mapView;
	private Button			zoomInButton;
	private Button			zoomOutButton;
	private Button			streetButton;
	private Button			trafficButton;
	private Button			satelliteButton;
	private MapController	mapController;
	private GeoPoint		gp;
	private Address    address;    

	/**Vogellas code**********/
	private LocationManager locationManager;
	private List<Overlay> overlays;
	public static final int INSERT_ID = Menu.FIRST;
	public static final int CENTER_ID = Menu.FIRST + 1;
	public static final int SATELLITE_ID = Menu.FIRST + 2;
	public static final int MAP_ID = Menu.FIRST + 3;
	private Location location;
	/**End of Vogellas code**/	
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		/* ===================Initialize the map to display a street view with no location preference============== */
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);								///< bind the layout to the activity	
		mapView = (MapView) findViewById(R.id.mapView);
		zoomInButton = (Button) findViewById(R.id.zoomInButton);
		zoomOutButton = (Button) findViewById(R.id.zoomOutButton);
		streetButton = (Button) findViewById(R.id.streetButton);
		trafficButton = (Button) findViewById(R.id.trafficButton);
		satelliteButton = (Button) findViewById(R.id.satelliteButton);  	
		zoomInButton.setOnClickListener(this);
		zoomOutButton.setOnClickListener(this);
		streetButton.setOnClickListener(this);
		trafficButton.setOnClickListener(this);
		satelliteButton.setOnClickListener(this);
		mapController = mapView.getController();
		mapView.setVisibility(0);
		mapView.setStreetView(true);   ///<mapView.setSatellite(true);

		/* ===================Navigate to any geo point on the google maps by providting lat and long ============ */
		//Portland 2707.
		
		gp = new GeoPoint((int) (34.02 * 1000000), (int) (-118.28 * 1000000));
		mapController.animateTo(gp);
		mapController.setCenter(gp);
		mapController.setZoom(19);

		/* ===================Navigating to your current location on the map using Location services=============== */
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		//	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new GeoUpdateHandler());		
		
		
	} 

	@Override
	protected boolean isRouteDisplayed()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		int id = v.getId();

		switch (id)
		{
		case R.id.zoomInButton:
			mapController.setZoom(mapView.getZoomLevel() + 1);

			break;
		case R.id.zoomOutButton:
			mapController.setZoom(mapView.getZoomLevel() - 1);
			break;
		case R.id.streetButton:
			mapView.setStreetView(true);
			mapView.setSatellite(false);
			mapView.setTraffic(false);
			break;
		case R.id.trafficButton:
			mapView.setStreetView(false);
			mapView.setSatellite(false);
			mapView.setTraffic(true);
			break;
		case R.id.satelliteButton:
			mapView.setStreetView(false);
			mapView.setSatellite(true);
			mapView.setTraffic(false);
			break;

		default:
			break;
		}

	}
	private void satelliteMap(boolean satellite) {
		if (satellite) {
			mapView.setSatellite(true);
		} else {
			mapView.setSatellite(false);
		}
	}

	private void centerMap() {
		mapController.animateTo(getGeoPoint());
		// mapController.setCenter();
	}
	
	public GeoPoint getGeoPoint() {
		int lat = (int) (location.getLatitude() * 1E6);
		int lng = (int) (location.getLongitude() * 1E6);
		GeoPoint point = new GeoPoint(lat, lng);
		return point;

	}
	
	public class GeoUpdateHandler implements LocationListener {
		
		@Override
		public void onLocationChanged(Location newLocation) {
			System.out.println("Called");
			location = newLocation;
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}	
	
}