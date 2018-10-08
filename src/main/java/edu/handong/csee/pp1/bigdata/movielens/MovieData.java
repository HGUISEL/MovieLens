package edu.handong.csee.pp1.bigdata.movielens ;

import java.io.* ;
import java.util.* ;

import org.apache.commons.csv.* ;
import org.apache.commons.configuration.* ;

public 
class MovieData 
{
	TreeMap<Integer, HashSet<Integer>>
	Baskets = new TreeMap<Integer, HashSet<Integer>>() ;

	TreeMap<Integer, Integer>
	numRatingsOfMovies = new TreeMap<Integer, Integer>() ;

	TreeMap<Integer, Double>
	accRatingsOfMovies = new TreeMap<Integer, Double>() ;

	PropertiesConfiguration config ;
	double like_threshold ;
	int outlier_threshold ;

	public
	MovieData (PropertiesConfiguration config) {
		this.config = config ;
		this.like_threshold = config.getDouble("data.like_threshold") ;
		this.outlier_threshold = config.getInt("data.outlier_threshold") ;
	}

	public 
	void load (FileReader f) throws IOException {
		for (CSVRecord r : CSVFormat.newFormat(',').withFirstRecordAsHeader().parse(f)) {
			Integer user   = Integer.parseInt(r.get(0)) ;
			Integer movie  = Integer.parseInt(r.get(1)) ;
			Double  rating = Double.parseDouble(r.get(2)) ;

			if (numRatingsOfMovies.containsKey(movie) == false) {
				numRatingsOfMovies.put(movie, 1) ;
				accRatingsOfMovies.put(movie, rating) ;
			}
			else {
				numRatingsOfMovies.put(movie, numRatingsOfMovies.get(movie) + 1) ;
				accRatingsOfMovies.put(movie, accRatingsOfMovies.get(movie) + rating) ;
			}

			if (rating >= like_threshold) {
				HashSet<Integer> basket = Baskets.get(user) ;
				if (basket == null) {
					basket = new HashSet<Integer>() ;
					Baskets.put(user, basket) ;
				}
				basket.add(movie) ;
			}
		}
	}

	public
	void removeOutliers() {
		HashSet<Integer> outliers = new HashSet<Integer>() ;
		for (Integer userId : Baskets.keySet()) {
			HashSet<Integer> basket = Baskets.get(userId) ;
			if (basket.size() > outlier_threshold) 
				outliers.add(userId) ;
		}
		for (Integer userId : outliers) 
			Baskets.remove(userId) ;
	}

	public 
	TreeMap<Integer, HashSet<Integer>>
	getBaskets() {
		return Baskets ;
	}

	public
	void show() {
		ChartGenerator chartGenerator = new ChartGenerator(Baskets, numRatingsOfMovies, accRatingsOfMovies);
		chartGenerator.showMovieStat() ;
		chartGenerator.showUserStat() ;
		chartGenerator.showRatingStat() ;
	}
}
