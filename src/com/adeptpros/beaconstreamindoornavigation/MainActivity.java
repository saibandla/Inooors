package com.adeptpros.beaconstreamindoornavigation;

import java.util.ArrayList;
import java.util.List;

import android.app.SearchManager;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.customlbs.library.IndoorsException;
import com.customlbs.library.IndoorsFactory;
import com.customlbs.library.IndoorsLocationListener;
import com.customlbs.library.callbacks.RoutingCallback;
import com.customlbs.library.callbacks.ZoneCallback;
import com.customlbs.library.model.Building;
import com.customlbs.library.model.Zone;
import com.customlbs.shared.Coordinate;
import com.customlbs.surface.library.DefaultSurfacePainterConfiguration;
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
public class MainActivity extends FragmentActivity implements
		IndoorsLocationListener, OnSurfaceClickListener, OnItemClickListener {

	private IndoorsSurfaceFragment indoorsFragment;
	Building currentBuilding;
	Coordinate userPosition;
	ListView dataListView;
	List<Zone> zonesList;
	Bitmap navigation;
	InputMethodManager imm;
	SearchView searchView;
	List<String> filteredData;
	ArrayList<String> zonesStrings;
	ImageView splashImaage;
	Coordinate selectedZoneCenter;
	List<Coordinate> selectedZoneCoordinates;
	Zone seletedZone;
	private SampleSurfaceOverlay prevOverlay;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		dataListView = (ListView) findViewById(R.id.listView1);
		splashImaage = (ImageView) findViewById(R.id.splashImage);
		filteredData = new ArrayList<String>();
		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		dataListView.setOnItemClickListener(this);
		navigation = BitmapFactory.decodeResource(getResources(),
				R.drawable.location);
		IndoorsFactory.Builder indoorsBuilder = new IndoorsFactory.Builder();
		IndoorsSurfaceFactory.Builder surfaceBuilder = new IndoorsSurfaceFactory.Builder();
		indoorsBuilder.setContext(this);
		getActionBar().hide();
		indoorsBuilder.setApiKey("65be3735-84bc-45b8-acb3-e40e95c67e42");
		// TODO: replace 12345 with the id of the building you uploaded to
		// our cloud using the MMT
		indoorsBuilder.setBuildingId((long) 339082730);
		// callback for indoo.rs-events
		indoorsBuilder.setUserInteractionListener(this);
		SurfacePainterConfiguration configuration = DefaultSurfacePainterConfiguration
				.getConfiguration();
		configuration.setNavigationPoint(navigation);
		configuration.getUserPositionCircleInlinePaintConfiguration().setColor(
				Color.RED);
		configuration.getLargeCircleOutlinePaintConfiguration().setColor(
				Color.RED);
		configuration.getRoutingPathPaintConfiguration().setColor(Color.RED);
		configuration.getRoutingPathPaintConfiguration().setStrokeWidth(
				(float) 2.0);

		surfaceBuilder.setSurfacePainterConfiguration(configuration);
		surfaceBuilder.setSurfacePainterConfiguration(configuration);
		surfaceBuilder.setIndoorsBuilder(indoorsBuilder);

		indoorsFragment = surfaceBuilder.build();
		indoorsFragment.setViewMode(ViewMode.HIGHLIGHT_CURRENT_ZONE);

		Log.e("Test", "" + indoorsFragment.getCurrentZones());
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		transaction.add(R.id.container, indoorsFragment, "indoors");
		transaction.commit();

	}

	//CallBack
	public void positionUpdated(Coordinate userPosition, int accuracy) {
		this.userPosition = userPosition;
		Toast.makeText(this, "User is located at ", Toast.LENGTH_SHORT).show();
		indoorsFragment.updateSurface();
		if (indoorsFragment.getCurrentUserGpsPosition() != null) {
			indoorsFragment.updateSurface();
		}
	}

	//CallBack
	public void buildingLoaded(Building building) {
		// indoo.rs SDK successfully loaded the building you requested and
		// calculates a position now
		currentBuilding = building;
		indoorsFragment.getIndoorsSurface()
				.registerOnSurfaceClickListener(this);
		reloatData();
		splashImaage.setVisibility(View.GONE);
		getActionBar().show();
		Log.e("Test", "" + indoorsFragment.getCurrentZones().size());

	}

	//CallBack
	public void onError(IndoorsException indoorsException) {
		Toast.makeText(this, indoorsException.getMessage(), Toast.LENGTH_LONG)
				.show();
	}

	//CallBack
	public void changedFloor(int floorLevel, String name) {
		// user changed the floor
		indoorsFragment.getSurfaceState().setRoutingPath(null);
		indoorsFragment.removeOverlay(prevOverlay);
		indoorsFragment.updateSurface();
		reloatData();
	}

	//CallBack
	public void leftBuilding(Building building) {
		// user left the building
	}

	//CallBack
	public void loadingBuilding(int progress) {
		// indoo.rs is still downloading or parsing the requested building
	}

	//CallBack
	public void orientationUpdated(float orientation) {
		// user changed the direction he's heading to
	}

	//CallBack
	public void enteredZones(List<Zone> zones) {
		// user entered one or more zones
		Log.e("On Entered Zone", "The zone count" + zones.size());
	}

	@Override
	public void onClick(Coordinate mapPoint) {
		for (Zone z : zonesList) {
			if (z.containsPosition(mapPoint)) {
				drawRoute(mapPoint, z);
				return;
			}
		}
		drawRoute(mapPoint, null);
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Log.e("The selected index", "" + id);
		TextView txt = (TextView) view.findViewById(android.R.id.text1);

		searchView.setQuery(txt.getText(), false);

		selectedZoneCenter = getZoneCenter(position);
		searchView.setIconified(false);
		drawRoute(selectedZoneCenter, seletedZone);
		searchView.clearFocus();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		searchView = (SearchView) menu.findItem(R.id.action_search)
				.getActionView();
		searchView.setSearchableInfo(searchManager
				.getSearchableInfo(getComponentName()));
		searchView.setIconifiedByDefault(false); // Do not iconify the widget;
		searchView.setQueryHint(getResources().getString(R.string.search_hint));
		searchView.setOnQueryTextListener(new OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String arg0) {
				imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				
				return true;
			}

			@Override
			public boolean onQueryTextChange(String arg0) {
				filterSearch(arg0);
				return true;
			}
		});
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
	
	// Methods Defined for Utility
	private void reloatData() {
		indoorsFragment.getIndoors().getZones(currentBuilding,
				new ZoneCallback() {

					@Override
					public void setZones(ArrayList<Zone> zones) {
						zonesList = zones;
						zonesStrings = new ArrayList<String>();

						for (Zone zone : zones) {
							if (zone.getFloorLevel() == indoorsFragment
									.getCurrentFloor().getLevel())
								zonesStrings.add(zone.getName());

						}
						ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(
								getBaseContext(),
								android.R.layout.simple_spinner_item,
								zonesStrings);

						dataListView.setAdapter(dataAdapter);
					}
				});
	}

	private Coordinate getZoneCenter(int position) {
		for (Zone zoneObje : zonesList) {
			if (zoneObje.getName().equalsIgnoreCase(
					searchView.getQuery().toString())) {
				seletedZone = zoneObje;
				selectedZoneCoordinates = zoneObje.getZonePoints();
				int x = 0, y = 0, z = 0;
				for (Coordinate coor : selectedZoneCoordinates) {
					x += coor.x;
					y += coor.y;
					z += coor.z;
				}
				return new Coordinate(x / selectedZoneCoordinates.size(), y
						/ selectedZoneCoordinates.size(), z
						/ selectedZoneCoordinates.size());

			}
		}
		return null;
	}


	private void drawRoute(final Coordinate destination, final Zone seletedZone) {
		if (userPosition == null) {
			Toast.makeText(getBaseContext(),
					"The User Location is not Defined Yet", Toast.LENGTH_LONG)
					.show();
			return;
		}
		if (destination == null) {
			Toast.makeText(getBaseContext(), "Server is Loaading the way points, Please try Again!",
					Toast.LENGTH_LONG).show();
			return;
		}
		
		indoorsFragment.getIndoors().getRouteAToB(userPosition, destination,
				new RoutingCallback() {

					@Override
					public void onError(IndoorsException indoorsException) {

					}

					@Override
					public void setRoute(ArrayList<Coordinate> path) {
						// TODO Auto-generated method stub
						indoorsFragment.getSurfaceState().setRoutingPath(path,
								false);
						prevOverlay = new SampleSurfaceOverlay(seletedZone);
						indoorsFragment.addOverlay(prevOverlay);
						indoorsFragment.updateSurface();

					}
				});
	}

	
	private void filterSearch(String searchString) {
		if (searchString.length() > 0) {
			filteredData.clear();
			for (String obj : zonesStrings) {
				if (obj.toLowerCase().contains(searchString.toLowerCase())) {
					filteredData.add(obj);
				}
			}

			dataListView.setAdapter(new ArrayAdapter<String>(getBaseContext(),
					android.R.layout.simple_list_item_1, filteredData));
		} else {
			dataListView.setAdapter(new ArrayAdapter<String>(getBaseContext(),
					android.R.layout.simple_list_item_1, zonesStrings));

		}

	}

	
}
