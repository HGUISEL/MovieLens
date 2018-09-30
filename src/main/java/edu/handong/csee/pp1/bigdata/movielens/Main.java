package edu.handong.csee.pp1.bigdata.movielens ;

import java.io.* ;
import java.util.* ;

import org.apache.commons.cli.* ;
import org.apache.commons.configuration.* ;
import org.apache.commons.csv.* ;

public 
class Main 
{
	static PropertiesConfiguration config ;
	static boolean isToShow = false ;
	static String configFilePath = "config.properties" ;
	static boolean DEBUG = true;
	static boolean INFO = true;

	public static 
	void main (String [] args) 
	{
		Options options = new Options() ;
		options.addOption("c", "config", true, "configuration file") ;
		options.addOption("d", "display", false, "show statistics") ;
		options.addOption("h", "help", false, "show help message") ;

		CommandLineParser parser = new DefaultParser() ;
		CommandLine cmd = null ;
		try {
			cmd = parser.parse(options, args) ;
			if (cmd.hasOption("d"))
				isToShow = true ;
			if (cmd.hasOption("c"))
				configFilePath = cmd.getOptionValue("c") ;
			if (cmd.hasOption("h")) {
				HelpFormatter formater = new HelpFormatter() ;
				formater.printHelp("Usage", options) ;
				System.exit(0) ;
			}
		}
		catch (ParseException e) {
			System.err.println(e) ;
			System.exit(1) ;
		}

		config(configFilePath) ;

		try {
			MovieRatingData data = new MovieRatingData(config) ;
			FileReader ftrain = new FileReader(config.getString("data.training")) ;
			FileReader ftest =  new FileReader(config.getString("data.testing")) ;

			if(DEBUG) System.out.println("Data loading starts.") ;		
			data.load(ftrain) ;
			if(DEBUG) System.out.println("Data loading finishes.") ;

			if (isToShow)
				data.show() ;
			data.removeOutliers() ;

			Recommender rec = new Recommender(config) ;
			rec.train(data) ;

			test(ftest, rec) ;
		}
		catch (IOException e) {
			System.err.println(e) ;
			System.exit(1) ;
		}
	}
	
	public static
	void config (String fpath) {
		try {
			config = new PropertiesConfiguration(fpath) ;
		}
		catch (ConfigurationException e) {
			System.err.println(e) ;
			System.exit(1) ;
		}
	}


	public static
	void test (FileReader ftest, Recommender rec) throws IOException
	{
		int [][] error = new int[2][2] ; // actual x predict -> # 	

		TreeMap<Integer, HashSet<Integer>> 
		users = new TreeMap<Integer, HashSet<Integer>>();

		TreeMap<Integer, HashSet<Integer>> 
		q_positive = new TreeMap<Integer, HashSet<Integer>>();

		TreeMap<Integer, HashSet<Integer>> 
		q_negative = new TreeMap<Integer, HashSet<Integer>>();

		for (CSVRecord r : CSVFormat.newFormat(',').parse(ftest)) {
			Integer user = Integer.parseInt(r.get(0)) ;
			Integer movie = Integer.parseInt(r.get(1)) ;
			Double rating = Double.parseDouble(r.get(2)) ;
			String type = r.get(3) ;

			if (users.containsKey(user) == false) {
				users.put(user, new HashSet<Integer>()) ;
				q_positive.put(user, new HashSet<Integer>()) ;
				q_negative.put(user, new HashSet<Integer>()) ;
			}

			if (type.equals("c")) {
				if (rating >= config.getDouble("data.like_threshold"))
					users.get(user).add(movie) ;								
			}
			else /* r.get(3) is "q" */{
				if (rating >= config.getDouble("data.like_threshold"))
					q_positive.get(user).add(movie) ;
				else
					q_negative.get(user).add(movie) ;
			}
		}

		for (Integer u : users.keySet()) {
			HashSet<Integer> u_movies = users.get(u) ;
			
			for (Integer q : q_positive.get(u))
				error[1][rec.predict(u_movies, q)] += 1 ;
	
			for (Integer q : q_negative.get(u))
				error[0][rec.predict(u_movies, q)] += 1 ;
		}

		if (error[0][1] + error[1][1] > 0)
			if(INFO)
				System.out.println("Precision: " +
						String.format("%.3f", 
					(double)(error[1][1]) / (double)(error[0][1] + error[1][1]))) ;
		else
			if(INFO)
				System.out.println("Precision: undefined.") ;

		if (error[1][0] + error[1][1] > 0)
			if(INFO)
				System.out.println("Recall: " +
			  String.format("%.3f", 
				((double)(error[1][1]) / (double)(error[1][0] + error[1][1])))) ;
		else
			if(INFO)
				System.out.println("Recall: undefined.") ;

		if (error[0][0] + error[1][1] > 0)
			if(INFO)
				System.out.println("All case accuracy: " +
			  String.format("%.3f", 
				((double)(error[1][1] + error[0][0]) / 
				(double)(error[0][0] + error[0][1] + error[1][0] + error[1][1])))) ;
		else
			if(INFO)
				System.out.println("All case accuracy: undefined.") ;

		if(INFO)
			System.out.println("[[" + error[0][0] + ", " + error[0][1] + "], "  + 
			"[" + error[1][0] + ", " + error[1][1] + "]]") ;
	}
}
