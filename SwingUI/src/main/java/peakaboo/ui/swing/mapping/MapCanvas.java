package peakaboo.ui.swing.mapping;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import peakaboo.common.PeakabooLog;
import peakaboo.controller.mapper.MappingController;
import peakaboo.controller.mapper.settings.AreaSelection;
import peakaboo.controller.mapper.settings.MapDisplayMode;
import peakaboo.controller.mapper.settings.MapScaleMode;
import peakaboo.controller.mapper.settings.MapViewSettings;
import peakaboo.controller.mapper.settings.PointsSelection;
import peakaboo.mapping.colours.OverlayColour;
import scidraw.drawing.DrawingRequest;
import scidraw.drawing.ViewTransform;
import scidraw.drawing.backends.Surface;
import scidraw.drawing.backends.Surface.CompositeModes;
import scidraw.drawing.common.Spectrums;
import scidraw.drawing.map.MapDrawing;
import scidraw.drawing.map.painters.FloodMapPainter;
import scidraw.drawing.map.painters.MapPainter;
import scidraw.drawing.map.painters.MapTechniqueFactory;
import scidraw.drawing.map.painters.RasterSpectrumMapPainter;
import scidraw.drawing.map.painters.SelectionMaskPainter;
import scidraw.drawing.map.painters.SpectrumMapPainter;
import scidraw.drawing.map.painters.axis.LegendCoordsAxisPainter;
import scidraw.drawing.map.painters.axis.SpectrumCoordsAxisPainter;
import scidraw.drawing.map.palettes.AbstractPalette;
import scidraw.drawing.map.palettes.OverlayPalette;
import scidraw.drawing.map.palettes.RatioPalette;
import scidraw.drawing.map.palettes.SaturationPalette;
import scidraw.drawing.map.palettes.ThermalScalePalette;
import scidraw.drawing.painters.axis.AxisPainter;
import scidraw.drawing.painters.axis.TitleAxisPainter;
import scidraw.swing.GraphicsPanel;
import scitypes.Bounds;
import scitypes.Coord;
import scitypes.Pair;
import scitypes.Ratios;
import scitypes.Spectrum;
import scitypes.SpectrumCalculations;


class MapCanvas extends GraphicsPanel
{

	private MappingController 		controller;
	private MapViewSettings			viewSettings;
	private DrawingRequest 		    dr;
	
	private MapDrawing		map;
	private SpectrumMapPainter contourMapPainter, ratioMapPainter, overlayMapPainterRed, overlayMapPainterGreen, overlayMapPainterBlue, overlayMapPainterYellow;

	private static final int	SPECTRUM_HEIGHT = 15;
	
	MapCanvas(MappingController controller)
	{
		this.controller = controller;
		this.viewSettings = controller.getSettings().getView();
		
		dr = new DrawingRequest();
		map = new MapDrawing(null, dr);
				
	}
	
	@Override
	protected void drawGraphics(Surface backend, boolean vector, Dimension size)
	{
		try {
			drawMap(backend, vector, size);
		} catch (Exception e) {
			PeakabooLog.get().log(Level.SEVERE, "Unable to draw map", e);
		}
	}

	@Override
	public float getUsedHeight()
	{
		return getUsedHeight(1f);
	}

	@Override
	public float getUsedWidth()
	{
		return getUsedWidth(1f);
	}

	@Override
	public float getUsedWidth(float zoom) {
		return map.calcTotalSize().x * zoom;
	}

	@Override
	public float getUsedHeight(float zoom) {
		return map.calcTotalSize().y * zoom;
	}
	
	
	
	
	
	

	public Coord<Integer> getMapCoordinateAtPoint(float x, float y, boolean allowOutOfBounds)
	{

		if (map == null) return null;
		return map.getMapCoordinateAtPoint(x, y, allowOutOfBounds);

	}


	public Coord<Integer> getPointForMapCoordinate(Coord<Integer> coord)
	{
		if (map == null) return null;

		Coord<Bounds<Float>> borders = map.calcAxisBorders();
		float topOffset, leftOffset;
		topOffset = borders.y.start;
		leftOffset = borders.x.start;

		Coord<Float> mapSize = map.calcMapSize();
		int locX, locY;
		locX = (int) (leftOffset + (((float) coord.x / (float) viewSettings.getDataWidth()) * mapSize.x) - (MapDrawing
			.calcInterpolatedCellSize(mapSize.x, mapSize.y, dr) * 0.5));
		locY = (int) (topOffset + (((float) coord.y / (float) viewSettings.getDataHeight()) * mapSize.y) - (MapDrawing
			.calcInterpolatedCellSize(mapSize.x, mapSize.y, dr) * 0.5));

		return new Coord<Integer>(locX, locY);
	}

	
	
	
	
	
	

	/**
	 * Drawing logic for the composite view
	 * @param backend surface to draw to
	 * @param vector is this a vector-based backend
	 * @param spectrumSteps how many steps should our legeng spectrum have
	 */
	private void drawBackendComposite(Surface backend, boolean vector, int spectrumSteps)
	{
		
		AbstractPalette palette 			=		new ThermalScalePalette(spectrumSteps, viewSettings.getMonochrome());
		AxisPainter spectrumCoordPainter 	= 		null;
		List<AbstractPalette> paletteList	=		new ArrayList<AbstractPalette>();
		List<AxisPainter> axisPainters 		= 		new ArrayList<AxisPainter>();
		
		
		Spectrum data = controller.getSettings().getMapFittings().getCompositeMapData();
		
		dr.uninterpolatedWidth = viewSettings.getDataWidth();
		dr.uninterpolatedHeight = viewSettings.getDataHeight();
		dr.dataWidth = viewSettings.getInterpolatedWidth();
		dr.dataHeight = viewSettings.getInterpolatedHeight();
		dr.viewTransform = controller.getSettings().getMapFittings().isLogView() ? ViewTransform.LOG : ViewTransform.LINEAR;

		
		if (controller.getSettings().getMapFittings().getMapScaleMode() == MapScaleMode.RELATIVE)
		{
			dr.maxYIntensity = data.max();
		}
		else
		{
			dr.maxYIntensity = controller.getSettings().getMapFittings().sumAllTransitionSeriesMaps().max();
		}

		
		palette = new ThermalScalePalette(spectrumSteps, viewSettings.getMonochrome());

		

		if (viewSettings.getShowDatasetTitle())
		{
			axisPainters.add(new TitleAxisPainter(1.0f, null, null, controller.mapsController.getDatasetTitle(), null));
		}

		if (viewSettings.getShowTitle())
		{
			String mapTitle = "";

			if (controller.getSettings().getMapFittings().getVisibleTransitionSeries().size() > 1)
			{
				mapTitle = "Composite of " + controller.getSettings().getMapFittings().mapLongTitle();
			}
			else
			{
				mapTitle = "Map of " + controller.getSettings().getMapFittings().mapLongTitle();
			}
			
			axisPainters.add(new TitleAxisPainter(1.0f, null, null, null, mapTitle));
		}
		
		
		spectrumCoordPainter = new SpectrumCoordsAxisPainter
		(

			viewSettings.getDrawCoords(),
			controller.mapsController.getBottomLeftCoord(),
			controller.mapsController.getBottomRightCoord(),
			controller.mapsController.getTopLeftCoord(),
			controller.mapsController.getTopRightCoord(),
			controller.mapsController.getRealDimensionUnits(),

			viewSettings.getShowSpectrum(),
			SPECTRUM_HEIGHT,
			spectrumSteps,
			paletteList,

			controller.mapsController.getRealDimensions() != null,
			(controller.getSettings().getMapFittings().isLogView() ? "Log Scale Intensity (counts)" : "Intensity (counts)")
		);
		axisPainters.add(spectrumCoordPainter);

		
		boolean oldVector = dr.drawToVectorSurface;
		dr.drawToVectorSurface = vector;

		map.setContext(backend);
		map.setAxisPainters(axisPainters);
		map.setDrawingRequest(dr);


		paletteList.add(palette);
		
		List<MapPainter> mapPainters = new ArrayList<MapPainter>();
		if (contourMapPainter == null) {
			contourMapPainter = MapTechniqueFactory.getTechnique(paletteList, data, viewSettings.getContours(), spectrumSteps); 
		} else {
			/*Spectrum modData = SpectrumCalculations.gridYReverse(
					data, 
					new GridPerspective<Float>(dr.dataWidth, dr.dataHeight, 0f));*/
			contourMapPainter.setData(data);
			contourMapPainter.setPalettes(paletteList);
		}
		mapPainters.add(contourMapPainter);
		
		
		
		//There should only ever be one selection active at a time
		AreaSelection areaSelection = controller.getSettings().getAreaSelection();
		if (areaSelection.hasSelection()) {
			mapPainters.add(new SelectionMaskPainter(Color.white, areaSelection.getPoints(), viewSettings.getDataWidth(), viewSettings.getDataHeight()));
		}
		
		PointsSelection pointsSelection = controller.getSettings().getPointsSelection();
		if (pointsSelection.hasSelection()) {
			mapPainters.add(new SelectionMaskPainter(Color.white, pointsSelection.getPoints(), viewSettings.getDataWidth(), viewSettings.getDataHeight()));
		}
		
		
		
		map.setPainters(mapPainters);
		map.draw();

		dr.drawToVectorSurface = oldVector;

	}
	
	
	/**
	 * Drawing logic for the ratio view
	 * @param backend surface to draw to
	 * @param vector is this a vector-based backend
	 * @param spectrumSteps how many steps should our legeng spectrum have
	 */
	private void drawBackendRatio(Surface backend, boolean vector, int spectrumSteps)
	{
		AxisPainter spectrumCoordPainter 	= 		null;
		List<AbstractPalette> paletteList	=		new ArrayList<AbstractPalette>();
		List<AxisPainter> axisPainters 		= 		new ArrayList<AxisPainter>();
		
		Pair<Spectrum, Spectrum> ratiodata = controller.getSettings().getMapFittings().getRatioMapData();
		
		dr.uninterpolatedWidth = viewSettings.getDataWidth();
		dr.uninterpolatedHeight = viewSettings.getDataHeight();
		dr.dataWidth = viewSettings.getInterpolatedWidth();
		dr.dataHeight = viewSettings.getInterpolatedHeight();
		//LOG view not supported
		dr.viewTransform = ViewTransform.LINEAR;
		
		//create a unique list of the represented sides of the ratio from the set of visible TransitionSeries
		List<Integer> ratioSideValues = controller.getSettings().getMapFittings().getVisibleTransitionSeries().stream().map(ts -> controller.getSettings().getMapFittings().getRatioSide(ts)).distinct().collect(toList());
		
		
		//this is a valid ratio if there is at least 1 visible TS for each side
		boolean validRatio = (ratioSideValues.contains(1) && ratioSideValues.contains(2));

		
		//how many steps/markings will we display on the spectrum
		float steps = (float) Math.ceil(SpectrumCalculations.abs(ratiodata.first).max());
		dr.maxYIntensity = steps;
		
		
		
		//if this is a valid ratio, make a real colour palette -- otherwise, just a black palette
		if (validRatio)
		{
			paletteList.add(new RatioPalette(spectrumSteps, viewSettings.getMonochrome()));
		}
		
		
		
		//generate a list of markers to be drawn along the spectrum to indicate the ratio at those points
		List<Pair<Float, String>> spectrumMarkers = new ArrayList<Pair<Float, String>>();

		int increment = 1;
		if (steps > 8) increment = (int) Math.ceil(steps / 8);

		if (validRatio)
		{
			for (int i = -(int) steps; i <= (int) steps; i += increment)
			{
				float percent = 0.5f + 0.5f * (i / steps);				
				spectrumMarkers.add(new Pair<Float, String>(percent, Ratios.fromFloat(i, true)));
			}
		}
		
		
		

		
		//if we're showing a dataset title, add a title axis painter to put a title on the top
		if (viewSettings.getShowDatasetTitle())
		{
			axisPainters.add(new TitleAxisPainter(1.0f, null, null, controller.mapsController.getDatasetTitle(), null));
		}

		//if we're map title, add a title axis painter to put a title on the bottom
		if (viewSettings.getShowTitle())
		{
			String mapTitle = "";
			mapTitle = controller.getSettings().getMapFittings().mapLongTitle();
			axisPainters.add(new TitleAxisPainter(1.0f, null, null, null, mapTitle));
		}
		

		//create a new coordinate/axis painter using the values in the model
		spectrumCoordPainter = new SpectrumCoordsAxisPainter
		(
			viewSettings.getDrawCoords(),
			controller.mapsController.getBottomLeftCoord(),
			controller.mapsController.getBottomRightCoord(),
			controller.mapsController.getTopLeftCoord(),
			controller.mapsController.getTopRightCoord(),
			controller.mapsController.getRealDimensionUnits(),

			viewSettings.getShowSpectrum(),
			SPECTRUM_HEIGHT,
			spectrumSteps,
			paletteList,

			controller.mapsController.getRealDimensions() != null,
			"Intensity (ratio)" + (controller.getSettings().getMapFittings().getMapScaleMode() == MapScaleMode.RELATIVE ? " - Ratio sides scaled independently" : ""),
			1,
			controller.getSettings().getMapFittings().getMapDisplayMode() == MapDisplayMode.RATIO,
			spectrumMarkers
		);
		axisPainters.add(spectrumCoordPainter);

		
		boolean oldVector = dr.drawToVectorSurface;
		dr.drawToVectorSurface = vector;

		map.setContext(backend);
		map.setAxisPainters(axisPainters);
		map.setDrawingRequest(dr);


		
		List<MapPainter> mapPainters = new ArrayList<MapPainter>();
		if (ratioMapPainter == null) {
			ratioMapPainter = MapTechniqueFactory.getTechnique(paletteList, ratiodata.first, viewSettings.getContours(), spectrumSteps); 
		} else {
			ratioMapPainter.setData(ratiodata.first);
			ratioMapPainter.setPalettes(paletteList);
		}
		mapPainters.add(ratioMapPainter);
		
		

				
		
		Spectrum invalidPoints = ratiodata.second;
		final float datamax = dr.maxYIntensity;
		
		
		invalidPoints.map_i((Float value) -> {
			if (value == 1f) return datamax;
			return 0f;
		});
		

		MapPainter invalidPainter = MapTechniqueFactory.getTechnique(new SaturationPalette(Color.gray, new Color(0,0,0,0)), invalidPoints, false, 0);
		mapPainters.add(invalidPainter);
		
		
		//There should only ever be one selection active at a time
		AreaSelection areaSelection = controller.getSettings().getAreaSelection();
		if (areaSelection.hasSelection())
		{
			mapPainters.add(new SelectionMaskPainter(Color.white, areaSelection.getPoints(), viewSettings.getDataWidth(), viewSettings.getDataHeight()));
		}
		PointsSelection pointsSelection = controller.getSettings().getPointsSelection();
		if (pointsSelection.hasSelection()) {
			mapPainters.add(new SelectionMaskPainter(Color.white, pointsSelection.getPoints(), viewSettings.getDataWidth(), viewSettings.getDataHeight()));
		}
		
		map.setPainters(mapPainters);
		map.draw();

		
		dr.drawToVectorSurface = oldVector;
		
	}
	
	/**
	 * Drawing logic for the overlay view
	 * @param backend surface to draw to
	 * @param vector is this a vector-based backend
	 * @param spectrumSteps how many steps should our legeng spectrum have
	 */
	private void drawBackendOverlay(Surface backend, boolean vector, int spectrumSteps)
	{
		AxisPainter spectrumCoordPainter 	= 		null;
		List<AxisPainter> axisPainters 		= 		new ArrayList<AxisPainter>();
		
		Map<OverlayColour, Spectrum> data = controller.getSettings().getMapFittings().getOverlayMapData();
		
		
		dr.uninterpolatedWidth = viewSettings.getDataWidth();
		dr.uninterpolatedHeight = viewSettings.getDataHeight();
		dr.dataWidth = viewSettings.getInterpolatedWidth();
		dr.dataHeight = viewSettings.getInterpolatedHeight();
		dr.viewTransform = controller.getSettings().getMapFittings().isLogView() ? ViewTransform.LOG : ViewTransform.LINEAR;
		
		
		Float redMax = 0f, greenMax = 0f, blueMax = 0f, yellowMax=0f;
		
		Spectrum redSpectrum = data.get(OverlayColour.RED);
		Spectrum greenSpectrum = data.get(OverlayColour.GREEN);
		Spectrum blueSpectrum = data.get(OverlayColour.BLUE);
		Spectrum yellowSpectrum = data.get(OverlayColour.YELLOW);
		
		
		if (redSpectrum != null ) redMax = redSpectrum.max();
		if (greenSpectrum != null ) greenMax = greenSpectrum.max();
		if (blueSpectrum != null ) blueMax = blueSpectrum.max();
		if (yellowSpectrum != null ) yellowMax = yellowSpectrum.max();
		
		
		dr.maxYIntensity = Math.max(Math.max(redMax, yellowMax), Math.max(greenMax, blueMax));


		spectrumCoordPainter = new LegendCoordsAxisPainter(

			viewSettings.getDrawCoords(),
			controller.mapsController.getBottomLeftCoord(),
			controller.mapsController.getBottomRightCoord(),
			controller.mapsController.getTopLeftCoord(),
			controller.mapsController.getTopRightCoord(),
			controller.mapsController.getRealDimensionUnits(),

			viewSettings.getShowSpectrum(),
			SPECTRUM_HEIGHT,

			controller.mapsController.getRealDimensions() != null,
			"Colour" +
					(controller.getSettings().getMapFittings().isLogView() ? " (Log Scale)" : "") + 
					(controller.getSettings().getMapFittings().getMapScaleMode() == MapScaleMode.RELATIVE ? " - Colours scaled independently" : ""),

			// create a list of color,string pairs for the legend by mapping the list of transitionseries per
			// colour and filter for empty strings

			// input list - get a unique list of colours in use

			controller.getSettings().getMapFittings().getOverlayColourValues().stream()
				.distinct()
				.map(ocolour -> new Pair<Color, String>(ocolour.toColor(),					//convert the color objects into color,string pairs (ie color/element list)	
						controller.getSettings().getMapFittings().getOverlayColourKeys().stream()						//grab a list of all TSs from the TS->Colour	
							.filter(ts -> controller.getSettings().getMapFittings().getOverlayColour(ts) == ocolour)	//filter for the right color
							.map(ts -> ts.toElementString())								//get element string
							.collect(joining(", "))											//comma separated list
						)
				)
				.filter(
						//filter for empty strings
						element -> !(element.second.length() == 0)
				).collect(toList())
			
		);

			

		if (viewSettings.getShowDatasetTitle())
		{
			axisPainters.add(new TitleAxisPainter(1.0f, null, null, controller.mapsController.getDatasetTitle(), null));
		}

		if (viewSettings.getShowTitle())
		{
			String mapTitle = "";
			mapTitle = "Overlay of " + controller.getSettings().getMapFittings().mapLongTitle();
			axisPainters.add(new TitleAxisPainter(1.0f, null, null, null, mapTitle));
		}

		axisPainters.add(spectrumCoordPainter);

		boolean oldVector = dr.drawToVectorSurface;
		dr.drawToVectorSurface = vector;

		map.setContext(backend);
		map.setAxisPainters(axisPainters);
		map.setDrawingRequest(dr);

	

		// create a list of map painters, one for each of the maps we want to show
		List<MapPainter> painters = new ArrayList<MapPainter>();
		
		if (redSpectrum != null){
			if (overlayMapPainterRed == null) {
				overlayMapPainterRed = new RasterSpectrumMapPainter(new OverlayPalette(spectrumSteps, OverlayColour.RED.toColor()), redSpectrum);
				overlayMapPainterRed.setCompositeMode(CompositeModes.ADD);
			}
			overlayMapPainterRed.setData(redSpectrum);
			overlayMapPainterRed.setPalette(new OverlayPalette(spectrumSteps, OverlayColour.RED.toColor()));
			painters.add(overlayMapPainterRed);
		}
			
		if (greenSpectrum != null) {
			if (overlayMapPainterGreen == null) {
				overlayMapPainterGreen = new RasterSpectrumMapPainter(new OverlayPalette(spectrumSteps, OverlayColour.GREEN.toColor()), greenSpectrum);
				overlayMapPainterGreen.setCompositeMode(CompositeModes.ADD);
			}
			overlayMapPainterGreen.setData(greenSpectrum);
			overlayMapPainterGreen.setPalette(new OverlayPalette(spectrumSteps, OverlayColour.GREEN.toColor()));
			painters.add(overlayMapPainterGreen);
		}
		
		if (blueSpectrum != null) {
			if (overlayMapPainterBlue == null) {
				overlayMapPainterBlue = new RasterSpectrumMapPainter(new OverlayPalette(spectrumSteps, OverlayColour.BLUE.toColor()), blueSpectrum);
				overlayMapPainterBlue.setCompositeMode(CompositeModes.ADD);
			}
			overlayMapPainterBlue.setData(blueSpectrum);
			overlayMapPainterBlue.setPalette(new OverlayPalette(spectrumSteps, OverlayColour.BLUE.toColor()));
			painters.add(overlayMapPainterBlue);
		}
		
		if (yellowSpectrum != null) {
			if (overlayMapPainterYellow == null) {
				overlayMapPainterYellow = new RasterSpectrumMapPainter(new OverlayPalette(spectrumSteps, OverlayColour.YELLOW.toColor()), yellowSpectrum);
				overlayMapPainterYellow.setCompositeMode(CompositeModes.ADD);
			}
			overlayMapPainterYellow.setData(yellowSpectrum);
			overlayMapPainterYellow.setPalette(new OverlayPalette(spectrumSteps, OverlayColour.YELLOW.toColor()));
			painters.add(overlayMapPainterYellow);
		}
		
		//need to paint the background black first
		painters.add(
				0, 
				new FloodMapPainter(Color.black)
		);
		
		
		
		//There should only ever be one selection active at a time
		AreaSelection selection = controller.getSettings().getAreaSelection();
		if (selection.hasSelection()) {
			painters.add(new SelectionMaskPainter(Color.white, selection.getPoints(), viewSettings.getDataWidth(), viewSettings.getDataHeight()));
		}
		
		PointsSelection pointsSelection = controller.getSettings().getPointsSelection();
		if (pointsSelection.hasSelection()) {
			painters.add(new SelectionMaskPainter(Color.white, pointsSelection.getPoints(), viewSettings.getDataWidth(), viewSettings.getDataHeight()));
		}

		
		
		// set the new data
		map.setPainters(painters);
		map.draw();


		dr.drawToVectorSurface = oldVector;
		
	}
	
	
	public void updateCanvasSize()
	{
			
		//Width
		double parentWidth = 1.0;
		if (this.getParent() != null)
		{
			parentWidth = this.getParent().getWidth();
		}

		int newWidth = (int) (parentWidth * viewSettings.getZoom());
		if (newWidth < parentWidth) newWidth = (int) parentWidth;

		
		
		//Height
		double parentHeight = 1.0;
		if (this.getParent() != null)
		{
			parentHeight = this.getParent().getHeight();
		}

		int newHeight = (int) (parentHeight * viewSettings.getZoom());
		if (newHeight < parentHeight) newHeight = (int) parentHeight;
		
		
		//Generate new sizes
		Rectangle oldView = this.getVisibleRect();
		Dimension oldSize = getPreferredSize();
		Dimension newSize = new Dimension(newWidth, newHeight);
		Rectangle newView = new Rectangle(oldView);
		

		//Ratio of new size to old one.
		float dx = (float)newSize.width / (float)oldSize.width;
		float dy = (float)newSize.height / (float)oldSize.height;

		//Scale view by size ratio
		newView.x = (int) (oldView.x * dx);
		newView.y = (int) (oldView.y * dy);

		//Set new size and update
		this.setPreferredSize(newSize);
		this.revalidate();
		this.scrollRectToVisible(newView);
		
		


	}

	
	
	protected void drawMap(Surface context, boolean vector, Dimension size)
	{
		
		context.rectangle(0, 0, (float)size.getWidth(), (float)size.getHeight());
		context.setSource(Color.white);
		context.fill();
		

		// Map Dimensions
		int originalWidth = viewSettings.getDataWidth();
		int originalHeight = viewSettings.getDataHeight();

		dr.dataHeight = viewSettings.getDataHeight();
		dr.dataWidth = viewSettings.getDataWidth();
		dr.imageWidth = (float)size.getWidth();
		dr.imageHeight = (float)size.getHeight();
		
		map.setContext(context);
		
		
		if (controller.mapsController.getRealDimensions() != null)
		{

			Coord<Bounds<Number>> realDims = controller.mapsController.getRealDimensions();
			
			controller.mapsController.setMapCoords(
					new Coord<Number>( realDims.x.start, 	realDims.y.end),
					new Coord<Number>( realDims.x.end, 		realDims.y.end), 
					new Coord<Number>( realDims.x.start,	realDims.y.start), 
					new Coord<Number>( realDims.x.end,		realDims.y.start) 
					
					
				);

		}
		else
		{

			controller.mapsController.setMapCoords(
					new Coord<Number>(1, originalHeight),
					new Coord<Number>(originalWidth, originalHeight),
					new Coord<Number>(1, 1), 
					new Coord<Number>(originalWidth, 1)					
				);
		}
		
		
		final int spectrumSteps = (viewSettings.getContours()) ? viewSettings.getSpectrumSteps() : Spectrums.DEFAULT_STEPS;
		
		switch (controller.getSettings().getMapFittings().getMapDisplayMode())
		{
			case COMPOSITE:
				drawBackendComposite(context, vector, spectrumSteps);
				break;
				
			case OVERLAY:
				drawBackendOverlay(context, vector, spectrumSteps);
				break;
				
			case RATIO:
				drawBackendRatio(context, vector, spectrumSteps);
				break;
				
		}
		
		return;
		
	}
	
	
	public void setNeedsRedraw()
	{
		map.needsMapRepaint();
		
		if (contourMapPainter != null)			contourMapPainter.clearBuffer();
		if (ratioMapPainter != null) 			ratioMapPainter.clearBuffer();
		if (overlayMapPainterBlue != null) 		overlayMapPainterBlue.clearBuffer();
		if (overlayMapPainterGreen != null)		overlayMapPainterGreen.clearBuffer();
		if (overlayMapPainterRed != null) 		overlayMapPainterRed.clearBuffer();
		if (overlayMapPainterYellow != null) 	overlayMapPainterYellow.clearBuffer();
	}


	
}
