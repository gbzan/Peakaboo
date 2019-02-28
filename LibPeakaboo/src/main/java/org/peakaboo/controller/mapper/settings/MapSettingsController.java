package org.peakaboo.controller.mapper.settings;

import java.io.File;

import org.peakaboo.controller.mapper.MappingController;
import org.peakaboo.controller.mapper.MappingController.UpdateType;

import cyclops.Bounds;
import cyclops.Coord;
import eventful.EventfulType;

/**
 * Settings for a map view which relate to presentation settings unrelated to
 * the maps themselves.
 * 
 * @author NAS
 */
public class MapSettingsController extends EventfulType<String>
{

	//SOURCE DATA
	private MappingController mapController;
	
	
	//SETTINGS
	private boolean	drawCoordinates		= true;
	private boolean	drawSpectrum		= true;
	private boolean	drawTitle			= true;
	private boolean drawScaleBar		= true;
	private boolean	drawDataSetTitle	= false;
	
	private int		spectrumSteps		= 15;
	private boolean	contour				= false;
	private boolean	monochrome			= false;

	private float	zoom				= 1f;

	public File		savePictureFolder 	= null;
	public File		dataSourceFolder 	= null;
	
		
	public MapSettingsController(MappingController mapController)
	{
		setMappingController(mapController);
	}
	
	
	public void setMappingController(MappingController controller) {
		this.mapController = controller;
	}


	// contours
	public void setContours(boolean contours)
	{
		this.contour = contours;
				
		updateListeners(UpdateType.UI_OPTIONS.toString());
	}


	public boolean getContours()
	{
		return this.contour;
	}

	
	//zoom
	public float getZoom() {
		return zoom;
	}


	public void setZoom(float zoom) {
		this.zoom = zoom;
		updateListeners(UpdateType.UI_OPTIONS.toString());
	}
	

	// spectrum
	public void setSpectrumSteps(int steps)
	{
		if (steps > 25) steps = 25;
		if (steps > 0)
		{
			this.spectrumSteps = steps;
		}
		updateListeners(UpdateType.UI_OPTIONS.toString());
	}


	public int getSpectrumSteps()
	{
		return this.spectrumSteps;
	}


	public void setMonochrome(boolean mono)
	{
		this.monochrome = mono;
		updateListeners(UpdateType.UI_OPTIONS.toString());
	}


	public boolean getMonochrome()
	{
		return this.monochrome;
	}

	

	public void setShowSpectrum(boolean show)
	{
		this.drawSpectrum = show;
		updateListeners(UpdateType.UI_OPTIONS.toString());
	}


	public boolean getShowSpectrum()
	{
		return this.drawSpectrum;
	}


	public void setShowScaleBar(boolean show) {
		this.drawScaleBar = show;
		updateListeners(UpdateType.UI_OPTIONS.toString());
	}
	
	public boolean getShowScaleBar() {
		return this.drawScaleBar;
	}
	
	public void setShowTitle(boolean show)
	{
		this.drawTitle = show;
		updateListeners(UpdateType.UI_OPTIONS.toString());
	}


	public boolean getShowTitle()
	{
		return this.drawTitle;
	}


	public void setShowDatasetTitle(boolean show)
	{
		this.drawDataSetTitle = show;
		updateListeners(UpdateType.UI_OPTIONS.toString());
	}


	public boolean getShowDatasetTitle()
	{
		return this.drawDataSetTitle;
	}


	public void setShowCoords(boolean show)
	{
		this.drawCoordinates = show;
		updateListeners(UpdateType.UI_OPTIONS.toString());
	}


	public boolean getShowCoords()
	{
		return this.drawCoordinates;
	}
	

	public Coord<Number> getLoXLoYCoord() {
		Coord<Bounds<Number>> realDims = mapController.getFiltering().getRealDimensions();
		if (realDims != null) {
			return new Coord<Number>(realDims.x.start, realDims.y.end);
		} else {
			return new Coord<Number>(1, mapController.getFiltering().getFilteredDataHeight());
		}
	}
	public Coord<Number> getHiXLoYCoord()
	{
		Coord<Bounds<Number>> realDims = mapController.getFiltering().getRealDimensions();
		if (realDims != null) {
			return new Coord<Number>( realDims.x.end, 		realDims.y.end);
		} else {
			return new Coord<Number>(mapController.getFiltering().getFilteredDataWidth(), mapController.getFiltering().getFilteredDataHeight());
		}
	}
	public Coord<Number> getLoXHiYCoord()
	{
		Coord<Bounds<Number>> realDims = mapController.getFiltering().getRealDimensions();
		if (realDims != null) {
			return new Coord<Number>( realDims.x.start,	realDims.y.start);
		} else {
			return new Coord<Number>(1, 1);
		}
	}
	public Coord<Number> getHiXHiYCoord()
	{
		Coord<Bounds<Number>> realDims = mapController.getFiltering().getRealDimensions();
		if (realDims != null) {
			return new Coord<Number>( realDims.x.end,		realDims.y.start);
		} else {
			return new Coord<Number>(mapController.getFiltering().getFilteredDataWidth(), 1);
		}
	}







	

	
	
}