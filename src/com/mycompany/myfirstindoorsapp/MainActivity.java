package com.mycompany.myfirstindoorsapp;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.customlbs.coordinates.GeoCoordinate;
import com.customlbs.library.Indoors;
import com.customlbs.library.IndoorsException;
import com.customlbs.library.IndoorsFactory;
import com.customlbs.library.IndoorsLocationListener;
import com.customlbs.library.LocalizationParameters;
import com.customlbs.library.callbacks.IndoorsServiceCallback;
import com.customlbs.library.callbacks.RoutingCallback;
import com.customlbs.library.callbacks.ZoneCallback;
import com.customlbs.library.model.Building;
import com.customlbs.library.model.Floor;
import com.customlbs.library.model.Zone;
import com.customlbs.shared.Coordinate;
import com.customlbs.surface.library.IndoorsSurface;
import com.customlbs.surface.library.IndoorsSurface.OnSurfaceClickListener;
import com.customlbs.surface.library.IndoorsSurfaceFactory;
import com.customlbs.surface.library.IndoorsSurfaceFragment;
import com.customlbs.surface.library.ViewMode;

/**
 * Sample Android project, powered by indoo.rs :)
 *
 * @author indoo.rs | Philipp Koenig
 *
 */
public class MainActivity extends FragmentActivity implements IndoorsLocationListener, OnSurfaceClickListener {

	private IndoorsSurfaceFragment indoorsFragment;
	private Spinner zoneSpinner;
	Building currentBuilding;
	Coordinate userPosition;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		zoneSpinner=(Spinner)findViewById(R.id.zones_list);
		IndoorsFactory.Builder indoorsBuilder = new IndoorsFactory.Builder();
		IndoorsSurfaceFactory.Builder surfaceBuilder = new IndoorsSurfaceFactory.Builder();
		indoorsBuilder.setContext(this);
		// TODO: replace this with your API-key
		indoorsBuilder.setApiKey("0e414449-8674-4ef2-9cad-294c2c670af9");
		// TODO: replace 12345 with the id of the building you uploaded to
		// our cloud using the MMT
		indoorsBuilder.setBuildingId((long) 336853225);
				// callback for indoo.rs-events
		indoorsBuilder.setUserInteractionListener(this);

		surfaceBuilder.setIndoorsBuilder(indoorsBuilder);

		indoorsFragment = surfaceBuilder.build();
		indoorsFragment.setViewMode(ViewMode.HIGHLIGHT_CURRENT_ZONE);
		Log.e("Test",""+indoorsFragment.getCurrentZones());
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.add(R.id.container, indoorsFragment, "indoors");
		transaction.commit();
	}

	public void positionUpdated(Coordinate userPosition, int accuracy) {
		this.userPosition=userPosition;
		GeoCoordinate geoCoordinate = indoorsFragment.getCurrentUserGpsPosition();
		Toast.makeText(
			    this,
			    "User is located at " , Toast.LENGTH_SHORT).show();
		    indoorsFragment.updateSurface();
		if (geoCoordinate != null) {
			
			Toast.makeText(
			    this,
			    "User is located at " + geoCoordinate.getLatitude() + ","
			    + geoCoordinate.getLongitude(), Toast.LENGTH_SHORT).show();
		    indoorsFragment.updateSurface();

		}
	}

	public void buildingLoaded(Building building) {
		// indoo.rs SDK successfully loaded the building you requested and
		// calculates a position now
		currentBuilding=building;
indoorsFragment.getIndoorsSurface().registerOnSurfaceClickListener(this);
		Toast.makeText(
		    this,
		    "Building is located at " + building.getLatOrigin() / 1E6 + ","
		    + building.getLonOrigin() / 1E6, Toast.LENGTH_SHORT).show();
		;
		indoorsFragment.getIndoors().getZones(building, new ZoneCallback() {
			
			@Override
			public void setZones(ArrayList<Zone> zones) {
				// TODO Auto-generated method stub
				Log.e("On ZoneCallback", "The zone count"+zones.size());
				ArrayList<String> zonesStrings=new ArrayList<String>();

				for (Zone zone : zones) {
					zonesStrings.add(zone.getName());
				}
				ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getBaseContext(),
						android.R.layout.simple_spinner_item, zonesStrings);
					dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					zoneSpinner.setAdapter(dataAdapter);
			}
		});
		Log.e("Test",""+indoorsFragment.getCurrentZones().size());

	}

	public void onError(IndoorsException indoorsException) {
		Toast.makeText(this, indoorsException.getMessage(), Toast.LENGTH_LONG).show();
	}

	public void changedFloor(int floorLevel, String name) {
		// user changed the floor
		Floor currentFloor=currentBuilding.getFloorByLevel(floorLevel);
		
	}

	public void leftBuilding(Building building) {
		// user left the building
	}

	public void loadingBuilding(int progress) {
		// indoo.rs is still downloading or parsing the requested building
	}

	public void orientationUpdated(float orientation) {
		// user changed the direction he's heading to
	}

	public void enteredZones(List<Zone> zones) {
		// user entered one or more zones
		Log.e("On Entered Zone", "The zone count"+zones.size());
		
	}

	@Override
	public void onClick(Coordinate mapPoint) {
		// TODO Auto-generated method stub
		indoorsFragment.getIndoors().getRouteAToB(userPosition, mapPoint, new RoutingCallback() {
			
			@Override
			public void onError(IndoorsException indoorsException) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setRoute(ArrayList<Coordinate> path) {
				// TODO Auto-generated method stub
				 indoorsFragment.getSurfaceState().setRoutingPath(path, false);
				    indoorsFragment.updateSurface();
			}
		});
	}
	
}
