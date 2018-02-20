package controller;

import java.awt.Color;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import model.NamedValue;
import model.TaxonRank;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import common.base.Logger;

/**
 * Creates charts from database contents.
 * 
 * <p>Creates the following pie charts:
 * <ul>
 * <li>Number of taxons by rank
 * <li>Number of pictures by location
 * <li>Number of pictures by month
 * </ul>
 * 
 * <p>Charts are created as png files in the chartsPath directory
 * using JFreeCharts.
 * 
 * @author nicz
 *
 */
public class ChartExporter {
	
	private static final Logger log = new Logger("ChartExporter");
	
	private static final int width = 800;
	private static final int height = 600;
	
	/** The directory for saving chart images */
	private static final String chartsPath = Controller.exportPath;
	
	
	/**
	 * @return a list of existing chart files
	 */
	public List<File> getChartFiles() {
		File dirCharts = new File(chartsPath);
		File[] charts = dirCharts.listFiles(new FilenameFilter() { 
			public boolean accept(File dir, String filename) { 
				return filename.endsWith(".png"); 
			}
		});
		return Arrays.asList(charts);
	}
	
	/**
	 * Creates all charts.
	 */
	public void exportCharts() {
		createRanksPieChart();
		createLocationsPieChart();
		createMonthsPieChart();
	}

	/**
	 * Creates a pie chart for the number of taxons by rank.
	 */
	public void createRanksPieChart() {
		Vector<NamedValue> vecDbValues = DataAccess.getInstance().getGroupedCount("Taxon", "taxRank", null);
		log.info("Got " + vecDbValues.size() + " rank counts");
		
		Vector<NamedValue> vecValues = new Vector<>();
		for (NamedValue nv : vecDbValues) {
			TaxonRank rank = TaxonRank.valueOf(nv.getName());
			vecValues.add(new NamedValue(nv.getValue(), rank.getGuiName()));
		}
		
		createPieChart("Nombre de taxons par rang", "ranks", vecValues, 12);
	}
	
	/**
	 * Creates a pie chart for the number of pictures by location.
	 */
	public void createLocationsPieChart() {
		Vector<NamedValue> vecValues = DataAccess.getInstance().getGroupedCount("Picture", "picLocation", null);
		log.info("Got " + vecValues.size() + " location counts");
		createPieChart("Nombre de photos par lieu", "locations", vecValues, 8);
	}
	
	/**
	 * Creates a pie chart for the number of pictures by month.
	 */
	public void createMonthsPieChart() {
		Vector<NamedValue> vecDbValues = DataAccess.getInstance().getGroupedCount("Picture", 
				"month(picShotAt)", "month(picShotAt)");
		log.info("Got " + vecDbValues.size() + " month counts");
		
		Vector<NamedValue> vecValues = new Vector<>();
		for (NamedValue nv : vecDbValues) {
			int iMonth = Integer.valueOf(nv.getName());
			vecValues.add(new NamedValue(nv.getValue(), DatabaseTools.monthNames[iMonth-1]));
		}
		
		createPieChart("Nombre de photos par mois", "picsByMonth", vecValues, 12);
	}
	
	
	private void createPieChart(String title, String filename, Vector<NamedValue> vecValues,
			int maxSectors) {
		DefaultPieDataset dataset = new DefaultPieDataset();
		
		Vector<NamedValue> vecMaxedValues = vecValues;
		if (vecValues.size() > maxSectors) {
			vecMaxedValues = new Vector<>();
			int sum = 0;
			String descr = "Autres";
			for (int k=0; k<vecValues.size(); k++) {
				NamedValue nv = vecValues.get(k);
				if (k < maxSectors) {
					vecMaxedValues.add(nv);
				} else {
					sum += nv.getValue();
				}
			}
			vecMaxedValues.add(new NamedValue(sum, descr));
		}
		
		for (NamedValue nv : vecMaxedValues) {
			String descr = nv.getName() + " (" + nv.getValue() + ")";
			dataset.setValue(descr, nv.getValue());
		}
		JFreeChart chart = createChart(dataset, title);
		saveChart(chart, filename);
	}
	
	/**
	 * Create a JFreeChart form the given pie dataset.
	 * 
	 * @param dataset the data to plot in a chart
	 * @param plotTitle the plot's title
	 */
	private JFreeChart createChart(PieDataset dataset, String plotTitle) {
		JFreeChart chart = ChartFactory.createPieChart(
				plotTitle, dataset, true, false, false );

		PiePlot plot = (PiePlot) chart.getPlot();
		plot.setNoDataMessage("No data available");
		plot.setCircular(true);
		plot.setLabelGap(0.02);
		plot.setBackgroundPaint(Color.WHITE);

		return chart;
	}

	/**
	 * Save a {@link JFreeChart} as a PNG file under the given name.
	 * @param chart the chart to save
	 * @param name the file name, without extension
	 */
	private void saveChart(JFreeChart chart, String name) {
		String fileName = chartsPath + name + ".png";
		log.info("Saving chart " + chart.getTitle().getText() + " as " + fileName);
		try {
			ChartUtilities.saveChartAsPNG(new File(fileName), chart, width, height);
		} catch (IOException e) {
			log.error("Failed to save chart: " + e.getMessage());
		}
	}

	/** the singleton instance */
	private static ChartExporter _instance = null;
	
	/** Gets the singleton instance. */
	public static ChartExporter getInstance() {
		if (_instance == null)
			_instance = new ChartExporter();
		return _instance;
	}
	
	/** Private singleton constructor */
	private ChartExporter() {
	}

}
