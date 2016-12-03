package csc385finalproject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

import dataStructures.*;

public class Driver {
	
	/*	TODO
	 * 		BUG:in some users, the top 5 includes movies with 'NaN' as a rating. related to BMH and comparables?
	 * 		needs commenting
	 * 		possibly change ArrayLists to another data structure
	 * 		optimizing!
	 */
	
	public static BinaryMaxHeap<MovieRatingPair> userPredictedRatings;
	public static ArrayList<String> moviesdat=new ArrayList<String>();
	public static ArrayList<String> ratingsdat=new ArrayList<String>();
	public static ArrayList<String> similarityTableRaw=new ArrayList<String>();
	public static String[][] movies;										//2d array to store movies and their id
	public static int[][] ratings;											//2d array to store users and their ratings
	public static int[][] uservmovies;										//2d array with users(row) movies(col) and the rating for the movie
	public static double[][] similarityTable;								//table to hold similarity values
	public static int numUsers;
	public static int numMovies;
	
	public static void main(String[] args) throws IOException,FileNotFoundException,Exception{
		long d1,d2,d3,d4;
		
		d3=System.currentTimeMillis();
		
		d1=System.currentTimeMillis();
		Tools.init();		
		d2=System.currentTimeMillis();
		System.out.println("INIT: ("+(d2-d1)+"ms)");

		d1=System.currentTimeMillis();
		System.out.println(Tools.writeSimilarityTable());	
		d2=System.currentTimeMillis();
		System.out.println("WRITE_SIM_TABLE: ("+(d2-d1)+"ms)");
		
		d1=System.currentTimeMillis();
		System.out.println(Tools.createSimilarityTable());
		d2=System.currentTimeMillis();
		System.out.println("CREATE_SIM_TABLE: ("+(d2-d1)+"ms)");

		d1=System.currentTimeMillis();
		System.out.println(Tools.readSimilarityTable());
		d2=System.currentTimeMillis();
		System.out.println("READ_SIM_TABLE: ("+(d2-d1)+"ms)");

		d1=System.currentTimeMillis();
		Tools.predictRating(5,1235);
		d2=System.currentTimeMillis();
		System.out.println("PREDICT_SINGLE_RATING: ("+(d2-d1)+"ms)");
		
		userPredictedRatings=new BinaryMaxHeap<MovieRatingPair>();
		
		d1=System.currentTimeMillis();
		for(int x=1;x<numUsers+1;x++){
			for(int y=1;y<numMovies+1;y++){
				if(uservmovies[x-1][y-1]==0)
					userPredictedRatings.add(new MovieRatingPair(movies[y-1][1],Tools.predictRating(x,y)));
			}
			System.out.print("user ID: "+x+" top 5 recomendations: ");
			for(int z=0;z<5;z++){
				System.out.print(userPredictedRatings.get().getMovie()+"::"+userPredictedRatings.get().getRating()+" | ");
				userPredictedRatings.remove();
			}
			System.out.println();
			userPredictedRatings.clear();
		}
		d2=System.currentTimeMillis();
		System.out.println("PREDICT_AND_PRINT_ALL_RATINGS: ("+(d2-d1)+"ms)");
		
		d4=System.currentTimeMillis();
		System.out.println("TOTAL_RUNTIME: ("+(d4-d3)+"ms)");
	}

	public static class Tools {
		
		public static void init() throws FileNotFoundException{
			Scanner fileIn = new Scanner(new File("movies.dat"));
			while(fileIn.hasNextLine()){
				moviesdat.add(fileIn.nextLine());								//add all of movies.dat to 'moviesdat'
			}
			fileIn = new Scanner(new File("ratings.dat"));
			while(fileIn.hasNextLine()){
				ratingsdat.add(fileIn.nextLine());								//add all of ratings.dat to 'ratingsdat'
			}
			fileIn.close();														//close scanner
			
			movies=new String[moviesdat.size()][2];								//2d array to store movies and their id
			ratings=new int[ratingsdat.size()][3];								//2d array to store users and their ratings
			String[] tempI=new String[4];											//temp var used while parsing through ratingsdat
			String[] tempS=new String[23];											//temp var used while parsing through moviesdat
			
			for(int x=0;x<moviesdat.size();x++){								//cycles through 'moviesdat'
				tempS=moviesdat.get(x).split("[|]");									//takes first two args after split (movieid and movietitle)
				movies[x][0]=tempS[0];													//and stores in 'movies'
				movies[x][1]=tempS[1];
			}
			
			numMovies=movies.length;
			numUsers=0;
			
			for(int x=0;x<ratingsdat.size();x++){								//does the same as above, only with 'ratingsdat'
				tempI=ratingsdat.get(x).split("\t");
				ratings[x][0]=Integer.parseInt(tempI[0]);
				if(ratings[x][0]>numUsers)
					numUsers=ratings[x][0];
				ratings[x][1]=Integer.parseInt(tempI[1]);
				ratings[x][2]=Integer.parseInt(tempI[2]);
			}
			
			uservmovies=new int[numUsers][numMovies];
			
			for(int x=0;x<ratings.length;x++){
				uservmovies[ratings[x][0]-1][ratings[x][1]-1]=ratings[x][2];	// -1 because there is no user 0
			}
		}
		
		public static double similarity(int item1, int item2){
			double sumOfItem1vsItem2=0,sumOfSquaresItem1=0,sumOfSquaresItem2=0,ret;
			for(int x=0;x<numUsers;x++){
				sumOfItem1vsItem2+=uservmovies[x][item1-1]*uservmovies[x][item2-1];
				sumOfSquaresItem1+=Math.pow(uservmovies[x][item1-1],2);
				sumOfSquaresItem2+=Math.pow(uservmovies[x][item2-1],2);
			}
			if((Math.sqrt(sumOfSquaresItem1)*Math.sqrt(sumOfSquaresItem2))==0)
				return 0;
			ret=sumOfItem1vsItem2/
					((Math.sqrt(sumOfSquaresItem1)*Math.sqrt(sumOfSquaresItem2))
					);
			return ret;
		}
		
		public static double predictRating(int user, int item) throws Exception{
			double sum=0,count=0,sim=0;
			for(int x=0;x<numMovies;x++){
				if(uservmovies[user-1][x]!=0){
					sim=similarityTable[x][item-1];
					sum+=sim*uservmovies[user-1][x];
					count+=sim;
				}
			}
//			if(!((sum/count)>0) && !((sum/count)<5))
//				throw new Exception();
			return sum/count;
		}
		
		public static boolean createSimilarityTable(){
			File st=new File("similarityTable.dat");
			if(st.exists())
				return false;
			similarityTable=new double[numMovies][numMovies];
			double temp;
			for(int x=1;x<numMovies+1;x++){
				for(int y=1;y<=x;y++){
					temp=Tools.similarity(x,y);
					similarityTable[x-1][y-1]=temp;
					similarityTable[y-1][x-1]=temp;
				}
			}
			return true;
		}
		public static boolean writeSimilarityTable() throws IOException{
			File st=new File("similarityTable.dat");
			if(st.exists())
				return false;
			similarityTable=new double[numMovies][numMovies];
			double temp;
			Path path=Paths.get("similarityTable.dat");
			BufferedWriter writer = Files.newBufferedWriter(path);
			for(int x=1;x<numMovies+1;x++){
				for(int y=1;y<numMovies+1;y++){
					temp=Tools.similarity(x,y);
					writer.write(String.valueOf(temp)+"\t");
				}
				writer.write("\n");
			}
			writer.close();
			return true;
		}
		public static boolean readSimilarityTable() throws FileNotFoundException{
			File st=new File("similarityTable.dat");
			if(!st.exists())
				return false;
			Scanner fileIn = new Scanner(new File("similarityTable.dat"));
			while(fileIn.hasNextLine()){
				similarityTableRaw.add(fileIn.nextLine());
			}
			String[] tempI=new String[numMovies];
			similarityTable=new double[numMovies][numMovies];
			for(int x=0;x<similarityTableRaw.size();x++){
				tempI=similarityTableRaw.get(x).split("\t");
				for(int y=0;y<tempI.length;y++){
					similarityTable[x][y]=Double.parseDouble(tempI[y]);
				}
			}
			fileIn.close();
			return true;
		}
	}
	public static class MovieRatingPair implements Comparable<MovieRatingPair>{
		public String movie;
		public double rating;
		MovieRatingPair(String movie,double rating){
			this.movie=movie;
			this.rating=rating;
		}
		public int compareTo(MovieRatingPair x){
			if(x.getRating()>rating)
				return -1;
			if(x.getRating()<rating)
				return 1;
			return 0;
		}
		public String getMovie(){
			return movie;
		}
		public double getRating(){
			return rating;
		}
	}
}