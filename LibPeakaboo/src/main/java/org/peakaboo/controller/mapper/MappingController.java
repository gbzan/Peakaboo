package org.peakaboo.controller.mapper;



import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.peakaboo.common.PeakabooLog;
import org.peakaboo.controller.mapper.dimensions.MapDimensionsController;
import org.peakaboo.controller.mapper.filtering.MapFilteringController;
import org.peakaboo.controller.mapper.fitting.MapFittingController;
import org.peakaboo.controller.mapper.rawdata.RawDataController;
import org.peakaboo.controller.mapper.selection.MapSelectionController;
import org.peakaboo.controller.mapper.settings.MapSettingsController;
import org.peakaboo.controller.plotter.PlotController;
import org.peakaboo.controller.plotter.SavedSession;
import org.peakaboo.curvefit.peak.transition.ITransitionSeries;
import org.peakaboo.datasource.model.internal.CroppedDataSource;
import org.peakaboo.datasource.model.internal.SelectionDataSource;
import org.peakaboo.display.map.MapRenderData;
import org.peakaboo.display.map.MapRenderSettings;
import org.peakaboo.display.map.MapScaleMode;
import org.peakaboo.display.map.Mapper;
import org.peakaboo.framework.cyclops.Coord;
import org.peakaboo.framework.cyclops.util.Mutable;
import org.peakaboo.framework.cyclops.visualization.SaveableSurface;
import org.peakaboo.framework.cyclops.visualization.SurfaceType;
import org.peakaboo.framework.eventful.EventfulType;
import org.peakaboo.framework.plural.Plural;
import org.peakaboo.framework.plural.executor.ExecutorSet;
import org.peakaboo.framework.plural.executor.eachindex.EachIndexExecutor;
import org.peakaboo.framework.plural.executor.eachindex.implementations.SimpleEachIndexExecutor;




public class MappingController extends EventfulType<String>
{
	
	private static final int SPECTRUM_HEIGHT = 15;

	public enum UpdateType
	{
		DATA_SIZE, DATA_OPTIONS, DATA, UI_OPTIONS, AREA_SELECTION, POINT_SELECTION, FILTER;
	}
	

	/*
	 * Generally, the flow from start to finish with these controllers is:
	 *  * RawDataController starts with the data from mapping
	 *  * MapDimensionsController lets the user specify the dimensions of the map
	 *  * MapFilteringController applies filters (which may change the size)
	 *  * MapFittingController determines how the maps are merged together (eg overlay)
	 *  * MapSettingsController determines how those merged maps are displayed
	 *  * MapSelectionController handles selection masks over the merged data
	 */
	public 	RawDataController		rawDataController;
	private MapDimensionsController dimensionsController;
	private MapFilteringController 	filteringController;
	private MapFittingController 	fittingController;
	private MapSettingsController	settingsController;
	private MapSelectionController	selectionController;
	
	
	private PlotController			plotcontroller;
	
	
	/**
	 * This constructor copies the user preferences from the map,
	 * and directly references the map data
	 * @param copy
	 * @param plotcontroller
	 */
	public MappingController(RawDataController rawDataController, PlotController plotcontroller)
	{
		this.plotcontroller = plotcontroller;
		
		this.rawDataController = rawDataController;
		this.rawDataController.addListener(this::updateListeners);
		
		this.filteringController = new MapFilteringController(this);
		this.filteringController.addListener(this::updateListeners);
		
		this.selectionController = new MapSelectionController(this);
		this.selectionController.addListener(this::updateListeners);
		
		this.settingsController = new MapSettingsController(this);		
		this.settingsController.addListener(this::updateListeners);		
		
		this.fittingController = new MapFittingController(this);
		this.fittingController.addListener(this::updateListeners);
		
		this.dimensionsController = new MapDimensionsController(this);	
		this.dimensionsController.addListener(this::updateListeners);
		
	}
	

	public MapSettingsController getSettings() {
		return settingsController;
	}
	
	public MapFilteringController getFiltering() {
		return filteringController;
	}

	public MapSelectionController getSelection() {
		return selectionController;
	}
	
	public MapDimensionsController getUserDimensions() {
		return dimensionsController;
	}

	public MapFittingController getFitting() {
		return fittingController;
	}
	
	public CroppedDataSource getDataSourceForSubset(Coord<Integer> cstart, Coord<Integer> cend)
	{
		return plotcontroller.data().getDataSourceForSubset(getUserDimensions().getUserDataWidth(), getUserDimensions().getUserDataHeight(), cstart, cend);
	}

	public SelectionDataSource getDataSourceForSubset(List<Integer> points)
	{
		return plotcontroller.data().getDataSourceForSubset(points);
	}
	
	
	public SavedSession getPlotSavedSettings() {
		return plotcontroller.getSavedSettings();
	}
	
	
	public MapRenderSettings getRenderSettings() {
		MapRenderSettings settings = new MapRenderSettings();
		settings.userDataWidth = this.getUserDimensions().getUserDataWidth(); 
		settings.userDataHeight = this.getUserDimensions().getUserDataHeight();
		settings.filteredDataWidth = this.getFiltering().getFilteredDataWidth();
		settings.filteredDataHeight = this.getFiltering().getFilteredDataHeight();
		
		settings.showDatasetTitle = this.getSettings().getShowDatasetTitle();
		settings.datasetTitle = this.rawDataController.getDatasetTitle();
		settings.showScaleBar = this.getSettings().getShowScaleBar();
		settings.showMapTitle = this.getSettings().getShowTitle();
		settings.mapTitle = this.getFitting().mapLongTitle();
		
		settings.scalemode = this.getFitting().getMapScaleMode();
		settings.monochrome = this.getSettings().getMonochrome();
		settings.contours = this.getSettings().getContours();
		settings.contourSteps = this.getSettings().getSpectrumSteps();
		
		settings.mode = this.getFitting().getMapDisplayMode();
				
		settings.drawCoord = this.getSettings().getShowCoords();
		settings.coordLoXLoY = this.getSettings().getLoXLoYCoord();
		settings.coordHiXLoY = this.getSettings().getHiXLoYCoord();
		settings.coordLoXHiY = this.getSettings().getLoXHiYCoord();
		settings.coordHiXHiY = this.getSettings().getHiXHiYCoord();
		settings.physicalUnits = this.rawDataController.getRealDimensionUnits();
		settings.physicalCoord = this.rawDataController.getRealDimensions() != null;
		
		settings.showSpectrum = this.getSettings().getShowSpectrum();
		settings.spectrumHeight = SPECTRUM_HEIGHT;
		
		settings.calibrationProfile = this.getFitting().getCalibrationProfile();
		settings.selectedPoints = this.getSelection().getPoints();
			
		
		
		switch (settings.mode) {
		case COMPOSITE:
			settings.spectrumTitle = "Intensity (counts)";
			break;
		case OVERLAY:
			settings.spectrumTitle = "Colour" +
					(this.getFitting().getMapScaleMode() == MapScaleMode.RELATIVE ? " - Colours scaled independently" : "");
			break;
		case RATIO:
			settings.spectrumTitle = "Intensity (ratio)" + (this.getFitting().getMapScaleMode() == MapScaleMode.RELATIVE ? " - sides scaled independently" : "");
			break;
		}
		
		String filterActions = filteringController.getActionDescription();
		if (filterActions != null) {
			settings.spectrumTitle += " - " + filterActions;
		}
		
		return settings;
		
	}
	
	public MapRenderData getMapRenderData() {
		
		MapRenderData data = new MapRenderData();
		
		switch (getFitting().getMapDisplayMode()) {
		case COMPOSITE:
			data.compositeData = this.getFitting().getCompositeMapData();
			break;
		case OVERLAY:
			data.overlayData = this.getFitting().getOverlayMapData();
			break;
		case RATIO:
			data.ratioData = this.getFitting().getRatioMapData();
			break;
		}
		data.maxIntensity = this.getFitting().sumAllTransitionSeriesMaps().max();
		
		return data;
		
	}


	public ExecutorSet<Void> writeArchive(OutputStream fos, SurfaceType format, int width, int height, Supplier<SaveableSurface> surfaceFactory) throws IOException {
		//we make a copy of the controller to prevent spamming the UI with changes as we generate map after map
		MappingController exportController = new MappingController(this.rawDataController, this.plotcontroller);
		new SavedMapSession().storeFrom(this).loadInto(exportController);
		return writeArchive(exportController, fos, format, width, height, surfaceFactory);
	}
	
	private static ExecutorSet<Void> writeArchive(MappingController controller, OutputStream fos, SurfaceType format, int width, int height, Supplier<SaveableSurface> surfaceFactory) throws IOException {
		final ZipOutputStream zos = new ZipOutputStream(fos);
		
		List<ITransitionSeries> tss = controller.getFitting().getAllTransitionSeries();
		/*
		 * Little different here than in most places. Saving an image usually just
		 * re-uses the GUI component and has it render to a different backend. This
		 * time, we do it manually so that we can avoid spamming the UI with
		 * events/redraws. We also only draw composite maps here, since drawing
		 * per-element overlays/ratios doesn't make a lot of sense.
		 */
		Coord<Integer> size = new Coord<>(width, height);
		Mutable<Exception> exceptionBox = new Mutable<>();
		
		EachIndexExecutor executor = new SimpleEachIndexExecutor(tss.size(), index -> {
			ITransitionSeries ts = tss.get(index);
			
			Mapper mapper = new Mapper();
			MapRenderData data = new MapRenderData();
			data.compositeData = controller.getFitting().getCompositeMapData(Optional.of(ts));
			data.maxIntensity = controller.getFitting().sumAllTransitionSeriesMaps().max();
			
			controller.getFitting().setAllTransitionSeriesVisibility(false);
			controller.getFitting().setTransitionSeriesVisibility(ts, true);
			MapRenderSettings settings = controller.getRenderSettings();
			
			//image extension
			String ext = "";
			switch (format) {
			case PDF: 
				ext = "pdf";
				break;
			case RASTER:
				ext = "png";
				break;
			case VECTOR:
				ext = "svg";
				break;			
			}
			try {
				ZipEntry entry = new ZipEntry(ts.toString() + "." + ext);
				zos.putNextEntry(entry);
				SaveableSurface context = surfaceFactory.get();
				mapper.draw(data, settings, context, size);
				context.write(zos);
				zos.closeEntry();
				
				//csv
				entry = new ZipEntry(ts.toString() + ".csv");
				zos.putNextEntry(entry);
				controller.writeCSV(zos);
				zos.closeEntry();
			} catch (IOException exception) {
				exceptionBox.set(exception);
			}
		});
		executor.setName("Generating Maps");
		
		Function<Void, Void> after = nothing -> {
			
			try {
				ZipEntry sessionEntry = new ZipEntry("session.peakaboo");
				zos.putNextEntry(sessionEntry);
				zos.write(controller.getPlotSavedSettings().serialize().getBytes());
				zos.closeEntry();
			} catch (IOException e) {
				exceptionBox.set(e);
			} finally {
				try {
					zos.close();
				} catch (IOException e) {
					exceptionBox.set(e);
				}
			}
			
			if (exceptionBox.get() != null) {
				PeakabooLog.get().log(Level.SEVERE, "Failed to generate archive", exceptionBox.get());
			}
			
			return null;
		};
		
		return Plural.build("Writing Archive", executor, () -> {}, after);

		
	}
	
	public void writeCSV(OutputStream os) throws IOException {
		os.write(getFitting().mapAsCSV().getBytes());
	}
	

}
