package edu.handong.csee.pp1.bigdata.movielens;

import java.util.HashSet;
import java.util.TreeMap;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;

public class ChartGenerator {
	
	TreeMap<Integer, HashSet<Integer>>
	Baskets = new TreeMap<Integer, HashSet<Integer>>() ;

	TreeMap<Integer, Integer>
	numRatingsOfMovies = new TreeMap<Integer, Integer>() ;

	TreeMap<Integer, Double>
	accRatingsOfMovies = new TreeMap<Integer, Double>() ;
	
	public ChartGenerator(TreeMap<Integer, HashSet<Integer>> Baskets,
							TreeMap<Integer, Integer> numRatingsOfMovies,
							TreeMap<Integer, Double> accRatingsOfMovies) {
		
		this.Baskets = Baskets;
		this.numRatingsOfMovies = numRatingsOfMovies;
		this.accRatingsOfMovies = accRatingsOfMovies;
		
	}
	
	public
	void showMovieStat() {
		ApplicationFrame frame = new ApplicationFrame("Movie Stat.") ;

		XYDataset dataset = getNumAvgRatingDataset() ;
		JFreeChart chart = ChartFactory.createScatterPlot("Num vs. Avg Rating", "Num", "Avg Rating", 
			dataset, PlotOrientation.VERTICAL, true, true, false) ;
		XYPlot plot = (XYPlot) chart.getPlot() ;
		XYDotRenderer renderer = new XYDotRenderer() ;
		renderer.setDotWidth(2) ;
		renderer.setDotHeight(2) ;
		plot.setRenderer(renderer) ;
		JPanel panel = new ChartPanel(chart) ;
		panel.setPreferredSize(new java.awt.Dimension(500, 270)) ;

		frame.setContentPane(panel) ;
		frame.pack() ;
		frame.setVisible(true) ;
	}

	private
	XYDataset getNumAvgRatingDataset() {
		return (XYDataset) new NumAvgDataset(numRatingsOfMovies, accRatingsOfMovies) ;
	}

	public
	void showUserStat() {
		ApplicationFrame frame = new ApplicationFrame("User Stat.") ;

		double [] ratings = new double[Baskets.keySet().size()] ;

		int i = 0 ;
		for (Integer user : Baskets.keySet()) {
			ratings[i] = (double) Baskets.get(user).size() ;
			i++ ;
		}

		HistogramDataset dataset = new HistogramDataset() ;
		dataset.setType(HistogramType.RELATIVE_FREQUENCY) ;
		dataset.addSeries("Histogram", ratings, 20) ;
		JFreeChart chart = ChartFactory.createHistogram("Num. Ratings by Users",
			"Num", "value", dataset, PlotOrientation.VERTICAL, false, false, false) ;
		JPanel panel = new ChartPanel(chart) ;
		frame.setContentPane(panel) ;
		frame.pack() ;
		frame.setVisible(true) ;
	}

	public
	void showRatingStat() {
		/* TODO: 
			implement this method to draw a histogram 
			that shows the distribution of ratings (1.0~5.0) 
		*/
	}

}
