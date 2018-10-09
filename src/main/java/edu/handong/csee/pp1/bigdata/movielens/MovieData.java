package edu.handong.csee.pp1.bigdata.movielens ;

import java.io.* ;
import java.util.* ;

import org.apache.commons.csv.* ;
import org.apache.commons.configuration.* ;

public class MovieData 
{
	TreeMap<Integer, HashSet<Integer>>
	Baskets = new TreeMap<Integer, HashSet<Integer>>() ;

	TreeMap<Integer, Integer>
	numRatingsOfMovies = new TreeMap<Integer, Integer>() ;

	TreeMap<Integer, Double>
	accumulatedRatingsOfMovies = new TreeMap<Integer, Double>() ;

	PropertiesConfiguration config ;
	double like_threshold ;
	int outlier_threshold ;

	public MovieData (PropertiesConfiguration config) {
		this.config = config ;
		this.like_threshold = config.getDouble("data.like_threshold") ;
		this.outlier_threshold = config.getInt("data.outlier_threshold") ;
	}

	public void load (FileReader f) throws IOException {
		
		// Load each instance from a csv file.
		for (CSVRecord r : CSVFormat.newFormat(',').withFirstRecordAsHeader().parse(f)) {
			Integer user   = Integer.parseInt(r.get(0)) ;
			Integer movie  = Integer.parseInt(r.get(1)) ;
			Double  rating = Double.parseDouble(r.get(2)) ;

			// count ratings and accumulate rating scores by a user
			if (numRatingsOfMovies.containsKey(movie) == false) {
				numRatingsOfMovies.put(movie, 1) ; // first rating count
				accumulatedRatingsOfMovies.put(movie, rating) ;
			}
			else {
				numRatingsOfMovies.put(movie, numRatingsOfMovies.get(movie) + 1) ;
				accumulatedRatingsOfMovies.put(movie, accumulatedRatingsOfMovies.get(movie) + rating) ;
			}

			// Deal with a good rating based on the like_threshold.
			// We consider that a user likes the movie when he/she rates the movie not less than this score.
			// If a user likes this movie, put it in the Basket for getting association rules
			if (rating >= like_threshold) {
				
				// Get the basket of this user.
				HashSet<Integer> basket = Baskets.get(user) ;
				if (basket == null) { // no Basket for the user? Create one.
					basket = new HashSet<Integer>() ;
					Baskets.put(user, basket) ;
				}
				
				// put the movie for the user.
				basket.add(movie) ;
			}
		}
	}

	public void removeOutliers() {
		HashSet<Integer> outliers = new HashSet<Integer>() ;
		for (Integer userId : Baskets.keySet()) {
			HashSet<Integer> basket = Baskets.get(userId) ;
			if (basket.size() > outlier_threshold) 
				outliers.add(userId) ;
		}
		System.out.print("Outlier removed: ");
		int i=0;
		for (Integer userId : outliers) {
			Baskets.remove(userId) ;
			i++;
		}
		System.out.println(i + " users who have a big basket (size > " + outlier_threshold + ") were removed");
	}

	public TreeMap<Integer, HashSet<Integer>>
	getBaskets() {
		return Baskets ;
	}

	public void show() {
		ChartGenerator chartGenerator = new ChartGenerator(Baskets, numRatingsOfMovies, accumulatedRatingsOfMovies);
		chartGenerator.showMovieStat() ;
		chartGenerator.showUserStat() ;
		chartGenerator.showRatingStat() ;
	}
}
