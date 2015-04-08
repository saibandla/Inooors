package com.mycompany.myfirstindoorsapp;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
 

import com.customlbs.shared.Coordinate;
import com.customlbs.surface.library.IndoorsSurfaceOverlay;
import com.customlbs.surface.library.IndoorsSurfaceOverlayUtil;
import com.customlbs.surface.library.IndoorsSurfaceOverlayUtil.CanvasCoordinate;
import com.customlbs.surface.library.SurfacePainterConfiguration;
import com.customlbs.surface.library.SurfaceState;
 
public class SampleSurfaceOverlay implements IndoorsSurfaceOverlay {
 
  private Paint paint;

  Coordinate coornate1;
   
  @Override
  public void initialize(SurfacePainterConfiguration arg0) {
   
  }

  @Override
  public void paint(Canvas canvas, SurfaceState state) {
    if (state.lastFloorLevelSelectedByLibrary == state.currentFloor.getLevel()) {
    	 paint = new Paint();
    	    paint.setColor(Color.YELLOW);
      CanvasCoordinate coordinate = IndoorsSurfaceOverlayUtil
      .buildingCoordinateToCanvasAbsolute(state, state.getRoutingPath().get(state.getRoutingPath().size()-1).x,
    		  state.getRoutingPath().get(state.getRoutingPath().size()-1).y);

      canvas.drawCircle(coordinate.x, coordinate.y, 20, paint);
    }
  }

  @Override
  public void destroy() {
  }
} 
