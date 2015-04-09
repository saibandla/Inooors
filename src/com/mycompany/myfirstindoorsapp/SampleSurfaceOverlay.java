package com.mycompany.myfirstindoorsapp;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
 


import android.graphics.Path;

import com.customlbs.library.model.Zone;
import com.customlbs.model.ZonePoint;
import com.customlbs.shared.Coordinate;
import com.customlbs.surface.library.IndoorsSurfaceOverlay;
import com.customlbs.surface.library.IndoorsSurfaceOverlayUtil;
import com.customlbs.surface.library.IndoorsSurfaceOverlayUtil.CanvasCoordinate;
import com.customlbs.surface.library.SurfacePainterConfiguration;
import com.customlbs.surface.library.SurfaceState;
 
public class SampleSurfaceOverlay implements IndoorsSurfaceOverlay {
 
  private Paint paint;
  private Paint paint1;

  private  Zone zone;
   public SampleSurfaceOverlay(Zone zone) {
	// TODO Auto-generated constructor stub
	   this.zone=zone;
}
  @Override
  public void initialize(SurfacePainterConfiguration arg0) {
	  paint = new Paint();
	    paint.setColor(Color.YELLOW);
	   
	    paint1 = new Paint();
	    paint1.setColor(Color.RED);
	    paint1.setTextSize(30);

  }
ArrayList<Coordinate> zonepoints;
  @Override
  public void paint(Canvas canvas, SurfaceState state) {
    if (state.lastFloorLevelSelectedByLibrary == state.currentFloor.getLevel()) {
    	
      CanvasCoordinate coordinate = IndoorsSurfaceOverlayUtil
      .buildingCoordinateToCanvasAbsolute(state, state.getRoutingPath().get(state.getRoutingPath().size()-1).x,
    		  state.getRoutingPath().get(state.getRoutingPath().size()-1).y);
      canvas.drawCircle(coordinate.x, coordinate.y, 20, paint);
      if(zone!=null)
      {
    	  zonepoints=zone.getZonePoints();
    	  float[] a=new float[500];
    	  int i=0;
    	  for (Coordinate coordinate1 : zonepoints) {
			a[i]=coordinate1.x;
			a[i+1]=coordinate1.y;
			i+=2;
		}
    	  canvas.drawPoints(a, paint);
    	  canvas.drawText(zone.getName(),coordinate.x-20, coordinate.y-20, paint1);
      }
      zone=null;
    }
  }

  @Override
  public void destroy() {
  }
} 
