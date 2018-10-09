package edu.handong.csee.pp1.bigdata.movielens ;

import java.util.* ;
import com.google.common.collect.* ;
import org.apache.commons.configuration.* ;

public class 
Recommender
{
	TreeMap<Integer, Integer> countForAllItemsetsWithSize1 = new TreeMap<Integer, Integer>() ; // first item, second count
	
	TreeMap<Integer, Integer> 
	freqItemsetsWithSize1 = new TreeMap<Integer, Integer>() ; 
	/* support1 : MovieId -> Num */

	TreeMap<ItemSetWithSize2, Integer> 
	freqItemsetsWithSize2 = new TreeMap<ItemSetWithSize2, Integer>() ; 
	/* support2 : MovieId x MovieId -> Num */

	TreeMap<ItemsetWithSize3, Integer> 
	freqItemsetsWithSize3 = new TreeMap<ItemsetWithSize3, Integer>() ; 
	/* support3 : MovieId x MovieId x MovieId -> Num */

	PropertiesConfiguration config ;
	int minSupport ;
	int min_evidence_3 ;
	double threshold_2 ;
	double threshold_3 ;


	Recommender(PropertiesConfiguration config) {
		this.config = config ;
		this.minSupport = 
			config.getInt("training.min_supports") ;
		this.threshold_2 = 
			config.getDouble("prediction.threshold_2") ;
		this.threshold_3 = 
			config.getDouble("prediction.threshold_3") ;
		this.min_evidence_3 = 
			config.getInt("prediction.min_evidence_3") ;
	}

	public 
	void train(MovieData data) {
		TreeMap<Integer, HashSet<Integer>> 
		baskets = data.getBaskets() ;
		/* Baskets : UserID -> Set<MovieId> */

		for (Integer user : baskets.keySet()) {
			HashSet<Integer> aBasket = baskets.get(user) ;

			computeFreqItemsetsWithSize1(aBasket) ;
			computeFreqItemsetsWithSize2(aBasket) ;
			computeFreqItemsetsWithSize3(aBasket) ;
			// Optional TODO: can you do this for Size K???
		}
	}

	public
	int predict(HashSet<Integer> profile, Integer q) {
		if (predictPair(profile, q) == 1)
			return 1 ;
		return predictTriple(profile, q) ;
	}


	private
	void computeFreqItemsetsWithSize1(HashSet<Integer> aBasket) {
		
		for (Integer item : aBasket) {
			Integer count = countForAllItemsetsWithSize1.get(item) ;
			if (count == null)
				count = 1 ;
			else
				count = count.intValue() + 1 ;
			countForAllItemsetsWithSize1.put(item, count) ; // the item is updated with new count.
		}
		
		for(Integer item:countForAllItemsetsWithSize1.keySet()) {
			
			if(countForAllItemsetsWithSize1.get(item)>=minSupport)
				freqItemsetsWithSize1.put(item, countForAllItemsetsWithSize1.get(item));
		}
	}

	private
	void computeFreqItemsetsWithSize2(HashSet<Integer> aBasket) {
		
		HashSet<Integer> allItemsetsWithSize1ThatSatisfyMinSupportInTheBasket = new HashSet<Integer>() ;
		for (Integer item : aBasket) {
			// We only need to consider items in the frequent itemsets with Size 1. => Using monotonicity to improve this algorithm
			if (freqItemsetsWithSize1.containsKey(item))
				allItemsetsWithSize1ThatSatisfyMinSupportInTheBasket.add(item) ;
		}
		
		aBasket = allItemsetsWithSize1ThatSatisfyMinSupportInTheBasket;
		
		// it is obvious that aBasket must have at least two items for computing its frequency.
		if (aBasket.size() >= 2) {
			
			TreeMap<ItemSetWithSize2, Integer> countForAllItemsetsWithSize2 = new TreeMap<ItemSetWithSize2, Integer>() ; // first item, second count
			
			// Sets.combinations is a public method from a google's common collection package.
			// this method returns all combinations of elements of a specific size (the second parameter).
			// for example, when we have the first parameter value {1,2,3} and the value of the second parameter '2',
			// it returns all subsets from the combinations with the given size {{1,2},{1,3},{2,3}}.
			for (Set<Integer> aSubsetWithTwoItems : Sets.combinations(aBasket, 2)) {
				Integer count = countForAllItemsetsWithSize2.get(new ItemSetWithSize2(aSubsetWithTwoItems)) ;
				if (count == null) 
					count = 1 ;
				else
					count = count.intValue() + 1 ;
				countForAllItemsetsWithSize2.put(new ItemSetWithSize2(aSubsetWithTwoItems), count) ;
			}
			
			for(ItemSetWithSize2 itemset:countForAllItemsetsWithSize2.keySet()) {
				
				if(countForAllItemsetsWithSize2.get(itemset)>=minSupport)
					freqItemsetsWithSize2.put(itemset, countForAllItemsetsWithSize2.get(itemset));
			}
		}
	}

	private
	void computeFreqItemsetsWithSize3(HashSet<Integer> aBasket) {
		
		// Naively using monotonicity to improve efficiency of this algorithm
		// Based on slides, we could apply monotonicity for itemsets with Size 2 but we did this with Itemsets with Size 1 for simplicity.
		// Optional TODO: you can update this part with itemsets with Size 2.
		HashSet<Integer> allItemsThatSatisfyMinSupportInTheBasket = new HashSet<Integer>() ;
		for (Integer item : aBasket) {
			// We do not need to count size 1 items whose frequency is less than minimum support. => Using monotonicity to improve this algorithm
			if (freqItemsetsWithSize1.containsKey(item))
				allItemsThatSatisfyMinSupportInTheBasket.add(item) ;
		}
		
		aBasket = allItemsThatSatisfyMinSupportInTheBasket ;

		// it is obvious that aBasket must have at least three items for computing its frequency.
		if (aBasket.size() >= 3) {
			// Sets.combinations is a public method from a google's common collection package.
			// this method returns all combinations of elements of a specific size (the second parameter).
			// for example, when we have the first parameter value {1,2,3,4} and the value of the second parameter '2',
			// it returns all subsets from the combinations with the given size {{1,2,3},{1,2,4},{1,3,4},{2,3,4}}.
			for (Set<Integer> aSubsetWithThreeItems : Sets.combinations(aBasket, 3)) {
				Integer count = freqItemsetsWithSize3.get(new ItemsetWithSize3(aSubsetWithThreeItems));
				if (count == null) 
					count = 1 ;
				else
					count = count.intValue() + 1 ;
				
				freqItemsetsWithSize3.put(new ItemsetWithSize3(aSubsetWithThreeItems), count) ;
			}
		}
	}

	private
	int predictPair(HashSet<Integer> profile, Integer q) {
		/* TODO: implement this method */
		return 0 ;
	}

	private
	int predictTriple(HashSet<Integer> profile, Integer q) {
		if (profile.size() < 2)
			return 0 ;

		int evidence = 0 ;
		for (Set<Integer> p : Sets.combinations(profile, 2)) {
			Integer den = freqItemsetsWithSize2.get(new ItemSetWithSize2(p)) ;
			if (den == null)
				continue ;

			TreeSet<Integer> t = new TreeSet<Integer>(p) ;
			t.add(q) ;
			ItemsetWithSize3 item = new ItemsetWithSize3(t) ;			
			Integer num = freqItemsetsWithSize3.get(item) ;
			if (num == null)
				continue ;

			if (num.intValue() < minSupport)
				continue ;

			if ((double)num / (double)den >= threshold_3) 
				evidence++ ;
		}

		if (evidence >= min_evidence_3) 
			return 1 ;

		return 0 ;
	}	
}

class 
ItemSetWithSize2 implements Comparable 
{

	int first ;
	int second ;

	public
	ItemSetWithSize2(int first, int second) {
		if (first <= second) {
			this.first = first ;
			this.second = second ;
		}
		else {
			this.first = second ;
			this.second = first ;
		}
	}

	public
	ItemSetWithSize2(Set<Integer> s) {
		Integer [] elem = s.toArray(new Integer[2]) ;
		if (elem[0] < elem[1]) {
			this.first = elem[0] ;
			this.second = elem[1] ;
		}
		else {
			this.first = elem[1] ;
			this.second = elem[0] ;
		}
	}

	public 
	int compareTo(Object obj) {
		ItemSetWithSize2 p = (ItemSetWithSize2) obj ;

		if (this.first < p.first) 
			return -1 ;
		if (this.first > p.first)
			return 1 ;

		return (this.second - p.second) ;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ItemSetWithSize2) {
			ItemSetWithSize2 other = (ItemSetWithSize2) obj;
			return (this.first == other.first && this.second == other.second) || (this.first == other.second && this.second == other.first) ;
		}
		
		return false;
	}
}

class 
ItemsetWithSize3 implements Comparable 
{
	int [] elem ;

	ItemsetWithSize3(Set<Integer> s) {
		/* TODO: implement this method */
	}

	public 
	int compareTo(Object obj) {
		/* TODO: implement this method */
		return 0 ;
	}
}
