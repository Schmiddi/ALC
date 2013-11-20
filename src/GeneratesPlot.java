
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.TileObserver;
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

	final String title;
	
	/**
	 * Creates a new demo.
	 * 
	 * @param title
	 *            the frame title.
	 */
	public GeneratesPlot(final String title) {
		this.title = title;
	}
	

	/**
	 * Creates a sample dataset.
	 * 
	 * @return a sample dataset.
	 */

	private XYDataset createDataset(List <List> myList) {
		final XYSeries series = new XYSeries("Third");
		for(List tempList : myList){
			series.add((double)tempList.get(0),(double)tempList.get(1));
			System.out.println((double)tempList.get(0)+"hallo"+(double)tempList.get(1));
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
	private JFreeChart createChart(final XYDataset dataset) {

		// create the chart...
		final JFreeChart chart = ChartFactory.createXYLineChart(
				title, // chart title
				"Threshold", // x axis label
				"Y", // y axis label
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
		domain.setRange(0.00, 13.00);
		domain.setTickUnit(new NumberTickUnit(1));
		domain.setVerticalTickLabels(false);

		// Y-Axis
		NumberAxis range = (NumberAxis) plot.getRangeAxis();
		range.setRange(0.0, 11.0);
		range.setTickUnit(new NumberTickUnit(1));
		range.setVerticalTickLabels(false); // Horizontal Alignment

		return chart;
	}

	public void create(List <List> myList) {

		// Get the Data
		final XYDataset dataset = createDataset(myList);
		final JFreeChart chart = createChart(dataset);

		// Size: Width, Height
		BufferedImage objBufferedImage = chart.createBufferedImage(600, 300);

		ByteArrayOutputStream bas = new ByteArrayOutputStream();
		try {
			ImageIO.write(objBufferedImage, "png", bas); // File Type
			byte[] byteArray = bas.toByteArray();
			InputStream in = new ByteArrayInputStream(byteArray);
			BufferedImage image = ImageIO.read(in);
			File outputfile = new File("image.png"); // File Name
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
		final GeneratesPlot demo = new GeneratesPlot("Line Chart Demo 6");
		
		List <List>myList = new ArrayList<List>();
		List <Object>data = new ArrayList<Object>();
		data.add(11.1);
		data.add(2.2);
		data.add(5461);
		myList.add(data);
		data = new ArrayList<Object>();
		data.add(0.5);
		data.add(0.5);
		data.add(4571);
		myList.add(data);
		
		demo.create(myList);
	}

}