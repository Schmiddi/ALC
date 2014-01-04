
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class GeneratesPlot {

	/**
	 * Creates a sample dataset.
	 * 
	 * @return a sample dataset.
	 */

	private static XYDataset createDataset(List <List<Double>> myList) {
		final XYSeries series = new XYSeries("Third");
		for(List<Double> tempList : myList){
			series.add(tempList.get(0),tempList.get(1));
		}

		final XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(series);

		return dataset;

	}

	/**
	 * Creates a chart.
	 * 
	 * @param dataset
	 *            the data for the chart.
	 * 
	 * @return a chart.
	 */
	private static JFreeChart createChart(final XYDataset dataset, String title) {

		// create the chart...
		final JFreeChart chart = ChartFactory.createXYLineChart(
				title, // chart title
				"Threshold", // x axis label
				"UAR", // y axis label
				dataset, // data
				PlotOrientation.VERTICAL, 
				false, // no legend
				true, // tooltips
				false // urls
				);

		chart.setBackgroundPaint(Color.white);

		// get a reference to the plot for further customisation...
		final XYPlot plot = chart.getXYPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);

		// change the auto tick unit selection to integer units only...
		// X-Axis
		NumberAxis domain = (NumberAxis) plot.getDomainAxis();
		domain.setRange(0.00, 0.01);
		domain.setTickUnit(new NumberTickUnit(0.001));
		domain.setVerticalTickLabels(false);

		// Y-Axis
		NumberAxis range = (NumberAxis) plot.getRangeAxis();
		range.setRange(0.4, 1.0);
		range.setTickUnit(new NumberTickUnit(0.1));
		range.setVerticalTickLabels(false); // Horizontal Alignment

		return chart;
	}

	public static void create(List <List<Double>> myList, String outputFolder, String filename) {

		// Get the Data
		final XYDataset dataset = createDataset(myList);
		final JFreeChart chart = createChart(dataset,filename);

		// Size: Width, Height
		BufferedImage objBufferedImage = chart.createBufferedImage(1000, 600);

		ByteArrayOutputStream bas = new ByteArrayOutputStream();
		try {
			ImageIO.write(objBufferedImage, "png", bas); // File Type
			byte[] byteArray = bas.toByteArray();
			InputStream in = new ByteArrayInputStream(byteArray);
			BufferedImage image = ImageIO.read(in);
			File outputfile = new File(outputFolder + filename+".png"); // File Name
			ImageIO.write(image, "png", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private static XYDataset createDatasetLC(List <List<Double>> myList) {
		final XYSeries series_train = new XYSeries("Train");
		final XYSeries series_cross_validation = new XYSeries("Cross-Validation");
		for(List<Double> tempList : myList){
								//			percentage of test instances, 		FMeasure
			series_train.add(					tempList.get(6),				tempList.get(2));
			series_cross_validation.add(		tempList.get(6), 				tempList.get(4));
		}

		final XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(series_train);
		dataset.addSeries(series_cross_validation);
		return dataset;

	}
	
	private static JFreeChart createChartLC(final XYDataset dataset, String title) {

		// create the chart...
		final JFreeChart chart = ChartFactory.createXYLineChart(
				title, // chart title
				"m (in %)- training set size", // x axis label
				"FScore", // y axis label
				dataset, // data
				PlotOrientation.VERTICAL, 
				false, // no legend
				true, // tooltips
				false // urls
				);

		chart.setBackgroundPaint(Color.white);

		// get a reference to the plot for further customisation...
		final XYPlot plot = chart.getXYPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);

		// change the auto tick unit selection to integer units only...
		// X-Axis
		NumberAxis domain = (NumberAxis) plot.getDomainAxis();
		domain.setRange(0.00, 0.01);
		domain.setTickUnit(new NumberTickUnit(0.001));
		domain.setVerticalTickLabels(false);

		// Y-Axis
		NumberAxis range = (NumberAxis) plot.getRangeAxis();
		range.setRange(0.4, 1.0);
		range.setTickUnit(new NumberTickUnit(0.1));
		range.setVerticalTickLabels(false); // Horizontal Alignment

		return chart;
	}
	
	public static void printLearningCurve(List <List<Double>> myList, String outputFolder, String filename) {

		// Get the Data
		final XYDataset dataset = createDatasetLC(myList);
		final JFreeChart chart = createChart(dataset,filename);

		// Size: Width, Height
		BufferedImage objBufferedImage = chart.createBufferedImage(1000, 600);

		ByteArrayOutputStream bas = new ByteArrayOutputStream();
		try {
			ImageIO.write(objBufferedImage, "png", bas); // File Type
			byte[] byteArray = bas.toByteArray();
			InputStream in = new ByteArrayInputStream(byteArray);
			BufferedImage image = ImageIO.read(in);
			File outputfile = new File(outputFolder + filename+".png"); // File Name
			ImageIO.write(image, "png", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *            ignored.
	 */
	public static void main(final String[] args) {
		
		List <List<Double>>myList = new ArrayList<List<Double>>();
		List <Double>data = new ArrayList<Double>();
		data.add(0.0003);
		data.add(0.3);
		data.add(5461.0);
		myList.add(data);
		data = new ArrayList<Double>();
		data.add(0.008);
		data.add(0.5);
		data.add(4571.0);
		myList.add(data);
		
		GeneratesPlot.create(myList,"","Test");
	}

}