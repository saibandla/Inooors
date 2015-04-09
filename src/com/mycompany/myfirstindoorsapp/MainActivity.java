package com.mycompany.myfirstindoorsapp;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.customlbs.model.ZonePoint;
import com.customlbs.shared.Coordinate;
import com.customlbs.surface.library.DefaultSurfacePainterConfiguration;
import com.customlbs.surface.library.IndoorsSurface;
import com.customlbs.surface.library.IndoorsSurface.OnSurfaceClickListener;
import com.customlbs.surface.library.IndoorsSurfaceFactory;
import com.customlbs.surface.library.IndoorsSurfaceFragment;
import com.customlbs.surface.library.SurfacePainterConfiguration;
import com.customlbs.surface.library.ViewMode;

/**
 * Sample Android project, powered by indoo.rs :)
 *
 * @author indoo.rs | Philipp Koenig
 *
 */
public class MainActivity extends FragmentActivity implements IndoorsLocationListener, OnSurfaceClickListener, OnItemClickListener {

	private IndoorsSurfaceFragment indoorsFragment;
	private Spinner zoneSpinner;
	Building currentBuilding;
	Coordinate userPosition;
	AutoCompleteTextView textView;
	List<Zone> zonesList;
	Bitmap navigation;
	InputMethodManager imm;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		imm = (InputMethodManager)getSystemService(
			      Context.INPUT_METHOD_SERVICE);
		textView=(AutoCompleteTextView)findViewById(R.id.textView1);
		textView.setOnItemClickListener(this);
		navigation=BitmapFactory.decodeResource(getResources(), R.drawable.location);
		IndoorsFactory.Builder indoorsBuilder = new IndoorsFactory.Builder();
		IndoorsSurfaceFactory.Builder surfaceBuilder = new IndoorsSurfaceFactory.Builder();
		indoorsBuilder.setContext(this);
		// TODO: replace this with your API-key
		indoorsBuilder.setApiKey("0e414449-8674-4ef2-9cad-294c2c670af9");
		// TODO: replace 12345 with the id of the building you uploaded to
		// our cloud using the MMT
		indoorsBuilder.setBuildingId((long) 341623794);
				// callback for indoo.rs-events
		indoorsBuilder.setUserInteractionListener(this);
		SurfacePainterConfiguration configuration = DefaultSurfacePainterConfiguration.getConfiguration();
		configuration.setNavigationPoint(navigation);
		configuration.getUserPositionCircleInlinePaintConfiguration().setColor(Color.RED);
		configuration.getLargeCircleOutlinePaintConfiguration().setColor(Color.RED);  
		configuration.getRoutingPathPaintConfiguration().setColor(Color.RED);
		configuration.getRoutingPathPaintConfiguration().setStrokeWidth((float) 2.0);

		surfaceBuilder.setSurfacePainterConfiguration(configuration);
		surfaceBuilder.setSurfacePainterConfiguration(configuration);
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
		Toast.makeText(
			    this,
			    "User is located at " , Toast.LENGTH_SHORT).show();
		    indoorsFragment.updateSurface();
		if (indoorsFragment.getCurrentUserGpsPosition() != null) {
			
			Toast.makeText(
			    this,
			    "User is located at " + indoorsFragment.getCurrentUserGpsPosition().getLatitude() + ","
			    + indoorsFragment.getCurrentUserGpsPosition().getLongitude(), Toast.LENGTH_SHORT).show();
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
		reloatData();

//		indoorsFragment.getIndoors().getZones(building, new ZoneCallback() {
//			
//			@Override
//			public void setZones(ArrayList<Zone> zones) {
//				// TODO Auto-generated method stub
//				Log.e("On ZoneCallback", "The zone count"+zones.size());
//				zonesList=zones;
//				ArrayList<String> zonesStrings=new ArrayList<String>();
//				
//
//				for (Zone zone : zones) {
//					zonesStrings.add(zone.getName());
//					
//				}
//				ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getBaseContext(),
//						android.R.layout.simple_spinner_item, zonesStrings);
//					dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//					textView.setAdapter(dataAdapter);
//			}
//		});
		Log.e("Test",""+indoorsFragment.getCurrentZones().size());

	}

	public void reloatData()
	{
		zonesList=	indoorsFragment.getCurrentZones();
		ArrayList<String> zonesStrings=new ArrayList<String>();
		

		for (Zone zone : zonesList) {
			zonesStrings.add(zone.getName());
			
		}
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getBaseContext(),
				android.R.layout.simple_spinner_item, zonesStrings);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			textView.setAdapter(dataAdapter);
	}
	public void onError(IndoorsException indoorsException) {
		Toast.makeText(this, indoorsException.getMessage(), Toast.LENGTH_LONG).show();
	}

	public void changedFloor(int floorLevel, String name) {
		// user changed the floor
		indoorsFragment.updateSurface();
		reloatData();

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
		for (Zone  z : zonesList) {
			if(z.containsPosition(mapPoint))
			{
				drawRoute(mapPoint,z);
				return;
			}
		}
		drawRoute(mapPoint, null);
	}
	Coordinate selectedZoneCenter;
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
				Log.e("The selected index",""+id);
				imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);

		selectedZoneCenter=	getZoneCenter(position);
	}
	
	
	
	List<Coordinate> selectedZoneCoordinates;
	Zone seletedZone;
	private Coordinate getZoneCenter(int position) {
		for (Zone zoneObje :zonesList) {
			if(zoneObje.getName().equalsIgnoreCase(textView.getText().toString()))
			{
				seletedZone=zoneObje;
				selectedZoneCoordinates=	zoneObje.getZonePoints();
				int x=0,y=0,z=0;
				for (Coordinate coor : selectedZoneCoordinates) {
					x+=coor.x;
					y+=coor.y;
					z+=coor.z;
				}
				return new Coordinate(x/selectedZoneCoordinates.size(), y/selectedZoneCoordinates.size(), z/selectedZoneCoordinates.size());

			}
		}
		return null;
	}
	private void drawRoute( final Coordinate destination,final Zone seletedZone)
	{
indoorsFragment.getIndoors().getRouteAToB(userPosition, destination, new RoutingCallback() {
			
			@Override
			public void onError(IndoorsException indoorsException) {
				
				
				
			}
			
			@Override
			public void setRoute(ArrayList<Coordinate> path) {
				// TODO Auto-generated method stub
				 indoorsFragment.getSurfaceState().setRoutingPath(path, false);
				 
				 indoorsFragment.addOverlay(new SampleSurfaceOverlay(seletedZone));  
				 indoorsFragment.updateSurface();
				   
			}
		});
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.getRooute) {
			drawRoute(selectedZoneCenter,seletedZone);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
