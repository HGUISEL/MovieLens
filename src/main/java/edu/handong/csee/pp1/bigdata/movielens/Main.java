package edu.handong.csee.pp1.bigdata.movielens ;

import java.io.* ;
import java.util.* ;

import org.apache.commons.cli.* ;
import org.apache.commons.configuration.* ;
import org.apache.commons.csv.* ;

public 
class Main 
{
	// PropertiesConfiguration class is from Apache commons configuration which is a helpful external library
	// when implementing our program to support various configuration features.
	static PropertiesConfiguration config ;
	static boolean isToShow = false ;
	static String configFilePath = "config.properties" ;
	static boolean DEBUG = true;
	static boolean INFO = true;

	public static 
	void main (String [] args) 
	{
		// (1) Prepare for options for a program execution
		//     Options class is from an external library "Apache commons CLI"
		//     providing convenient APIs for implementing command line based programs.
		Options options = new Options() ;
		options.addOption("c", "config", true, "configuration file") ;
		options.addOption("d", "display", false, "show statistics") ;
		options.addOption("h", "help", false, "show help message") ;

		// (2) Prepare for a parser for the options
		//     CommandLineParser class is also from an external library "Apache commons CLI"
		//     parsing options from command line when executing a program.
		CommandLineParser parser = new DefaultParser() ;
		CommandLine cmd = null ; // An object of the CommandLine class is actually processing command line options.
		try {
			cmd = parser.parse(options, args) ; // parse method parse all options from a command line.

			// parsed options now will be set to corresponding variables to use them in our code.
			if (cmd.hasOption("d"))
				isToShow = true ;
			if (cmd.hasOption("c"))
				configFilePath = cmd.getOptionValue("c") ;

			// 'h' option generates help messages automatically.
			if (cmd.hasOption("h")) {
				HelpFormatter formater = new HelpFormatter() ;
				formater.printHelp("Usage", options) ;
				System.exit(0) ; // after showing the help message, terminate the program.
			}
		}
		catch (ParseException e) {	// logics in a 'catch' block handle any error (exception) thrown from 'try' block.
			System.err.println(e) ;
			System.exit(1) ;
		}

		// (3) load configurations for a movie recommender like data file paths
		//     and various settings for the movie recommender
		config(configFilePath) ;
		
		// (4) from loading data files to training a recommender and testing the recommender.
		try {
			// (4-1) Preparing file readers for both training and test data files
			MovieData trainingData = new MovieData(config) ;
			FileReader fileReaderForTrainingData = new FileReader(config.getString("data.training")) ;
			FileReader fileReaderForTestData =  new FileReader(config.getString("data.testing")) ;

			// (4-2) Load the training data
			if(DEBUG)
				System.out.println("Training Data loading starts.") ;		

			trainingData.load(fileReaderForTrainingData) ; // creating Movie baskets for each user from training data.

			if(DEBUG)
				System.out.println("Training Data loading finishes.") ;

			// (4-3) Draw graphical charts from the training data when 'isToShow' options is true.
			if (isToShow)
				trainingData.show() ;
			
			// (4-4) Remove outliers (Noisy data cleansing by removing unordinary data)
			trainingData.removeOutliers() ;

			// (4-5) Training a recommender based on the configuration
			Recommender recommender = new Recommender(config) ;
			recommender.train(trainingData) ;

			// (4-6) test recommender on the test data to check its prediction performance.
			test(fileReaderForTestData, recommender) ;
		}
		catch (IOException e) {
			System.err.println(e) ;
			System.exit(1) ;
		}
	}

	/**
	 * @param fpath
	 * 
	 * This is a method to get configuration info from a configuration file (in our program, config.properties)
	 * 
	 */
	public static void config (String fpath) {
		try {
			config = new PropertiesConfiguration(fpath) ;
		}
		catch (ConfigurationException e) {
			System.err.println(e) ;
			System.exit(1) ;
		}
	}


	public static void test (FileReader fileReader, Recommender rec) throws IOException
	{
		int [][] error = new int[2][2] ; // actual x predict -> # 	

		TreeMap<Integer, HashSet<Integer>> 
		users = new TreeMap<Integer, HashSet<Integer>>();

		TreeMap<Integer, HashSet<Integer>> 
		q_positive = new TreeMap<Integer, HashSet<Integer>>();

		TreeMap<Integer, HashSet<Integer>> 
		q_negative = new TreeMap<Integer, HashSet<Integer>>();

		for (CSVRecord r : CSVFormat.newFormat(',').withFirstRecordAsHeader().parse(fileReader)) {
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
