package csc385finalproject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

import dataStructures.*;

public class Driver {
	
	/*	TODO
	 * 		optimizing!
	 */
	
	
	public static BinaryMaxHeap<MovieRatingPair> userPredictedRatings;
	public static ArrayList<String> moviesdat=new ArrayList<String>();			//raw input read from movies.dat
	public static ArrayList<String> ratingsdat=new ArrayList<String>();			//raw input read from ratings.dat
	public static ArrayList<String> similarityTableRaw=new ArrayList<String>();	
	public static String[][] movies;											//2d array to store movies and their id
	public static int[][] ratings;												//2d array to store users and their ratings
	public static int[][] uservmovies;											//2d array with users(row) movies(col) and the rating for the movie
	public static double[][] similarityTable;									//table to hold similarity values
	public static int numUsers;
	public static int numMovies;
	public static boolean writeRead=false;										//whether to write and read from similarityTable.dat, or write/read from memory
	
	public static void main(String[] args) throws IOException,FileNotFoundException,Exception,InputMismatchException{
		//d1-d4 are used for checking runtime
		long d1,d2,d3,d4;
		
		//have user decide whether to create the similarityTable in memory or write to file and read from that
		int choice=-1;
		Scanner sc = new Scanner(System.in);
		while(choice<0 || choice>2){
			System.out.println("Would you like to write the similarityTable to a file and read from that?\nWARNING:Takes longer than creating in memory, but once it is made, run time improves SIGNIFICANTLY!\n(0 to exit, 1 to write/read to/from file, 2 to create in memory)");
			choice=sc.nextInt();
			if(choice==0)
				System.exit(0);
			if(choice==1)
				writeRead=true;
			if(choice==2)
				writeRead=false;
		}
		sc.close();
		
		//d3: start of overall runtime
		d3=System.currentTimeMillis();
		
		//for each step of program, check time to execute and print
		d1=System.currentTimeMillis();
		Tools.init();		
		d2=System.currentTimeMillis();
		System.out.println("INIT: ("+(d2-d1)+"ms)");

		//writing to file if it does not exist, then reading from it
		if(writeRead){
			d1=System.currentTimeMillis();
			Tools.writeSimilarityTable();	
			d2=System.currentTimeMillis();
			System.out.println("WRITE_SIM_TABLE: ("+(d2-d1)+"ms)");
			
			d1=System.currentTimeMillis();
			Tools.readSimilarityTable();
			d2=System.currentTimeMillis();
			System.out.println("READ_SIM_TABLE: ("+(d2-d1)+"ms)");
		}
		
		if(!writeRead){
			d1=System.currentTimeMillis();
			Tools.createSimilarityTable();
			d2=System.currentTimeMillis();
			System.out.println("CREATE_SIM_TABLE: ("+(d2-d1)+"ms)");
		}
		
		//initialize binaryMaxHeap, which is used to get the top5 ratings for each user
		userPredictedRatings=new BinaryMaxHeap<MovieRatingPair>();
		
		d1=System.currentTimeMillis();
		double temp;
		//for each user(starts at one and goes 1 over because there is no 0th user or movie)
		//loop is of O(n^2+5n)
		for(int x=1;x<numUsers+1;x++){
			//for each movie
			for(int y=1;y<numMovies+1;y++){
				//get the predicted rating
				temp=Tools.predictRating(x,y);
				//if the user has not rated the current movie and the predicted rating is a valid rating 0-5
				if(uservmovies[x-1][y-1]==0 && (temp>=0 && temp<=5))
					//add a MovieRatingPair to userPredictedRatings to help find top5
					userPredictedRatings.add(new MovieRatingPair(movies[y-1][1],temp));
			}
			System.out.print("user ID: "+x+" top 5 recomendations: ");
			//to find the top5 movies for each user:
			for(int z=0;z<5;z++){
				//print the root of userPredictedRatings. due to being a BinaryMaxHeap, it will be the highest rated movie
				System.out.print(userPredictedRatings.get().getMovie()+"::"+userPredictedRatings.get().getRating());
				//remove the root, as we have already printed it
				userPredictedRatings.remove();
			}
			System.out.println();
			//after the top5 are printed, clear the heap
			userPredictedRatings.clear();
		}
		d2=System.currentTimeMillis();
		System.out.println("PREDICT_AND_PRINT_ALL_RATINGS: ("+(d2-d1)+"ms)");
		
		d4=System.currentTimeMillis();
		System.out.println("TOTAL_RUNTIME: ("+(d4-d3)+"ms)");
	}

	//this class contains all of my helper methods
	public static class Tools {
		
		//initializes movies[][], ratings[][], and uservmovies[][]
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
				uservmovies[ratings[x][0]-1][ratings[x][1]-1]=ratings[x][2];	// -1 because there is no user 0 or movie 0
			}
		}
		
		//method to check the similarity between to movies
		//used to create the similarityTable
		public static double similarity(int item1, int item2){
			double sumOfItem1vsItem2=0,sumOfSquaresItem1=0,sumOfSquaresItem2=0,ret;
			//for each user
			for(int x=1;x<numUsers+1;x++){
				sumOfItem1vsItem2+=uservmovies[x-1][item1-1]*uservmovies[x-1][item2-1];
				sumOfSquaresItem1+=Math.pow(uservmovies[x-1][item1-1],2);
				sumOfSquaresItem2+=Math.pow(uservmovies[x-1][item2-1],2);
			}
			//checking to see if we will divide by zero, and if so, return 0
			if((Math.sqrt(sumOfSquaresItem1)*Math.sqrt(sumOfSquaresItem2))==0)
				return 0;
			ret=sumOfItem1vsItem2/((Math.sqrt(sumOfSquaresItem1)*Math.sqrt(sumOfSquaresItem2)));
			return ret;
		}
		
		//method to calculate the predicted rating for a movie that user has not rated yet
		//This is done by going through previously rated movies
		public static double predictRating(int user, int item) throws Exception{
			double sum=0,count=0,sim=0;
			//for each movie
			for(int x=1;x<numMovies+1;x++){
				//check to see if the movie has been rated, continue if it has
				if(uservmovies[user-1][x-1]!=0){
					sim=similarityTable[x-1][item-1];
					sum+=sim*uservmovies[user-1][x-1];
					count+=sim;
				}
			}
			return sum/count;
		}
		
		public static boolean createSimilarityTable(){
			if(writeRead)
				return false;
			//init similarityTable
			similarityTable=new double[numMovies][numMovies];
			double temp;
			//for each movie(starting at 1 and going over 1 since there is no 0th movie)
			for(int x=1;x<numMovies+1;x++){
				for(int y=1;y<=x;y++){
					//find the similarity value and
					temp=Tools.similarity(x,y);
					//since the similartityTable is symmetric, sT[x][y]=sT[y][x]
					similarityTable[x-1][y-1]=temp;
					similarityTable[y-1][x-1]=temp;
				}
			}
			return true;
		}
		
		public static boolean writeSimilarityTable() throws IOException{
			if(!writeRead)
				return false;
			//create new file and check to see if it exists in base directory
			File st=new File("similarityTable.dat");
			if(st.exists())
				return false;
			//init similarityTable
			similarityTable=new double[numMovies][numMovies];
			double temp;
			Path path=Paths.get("similarityTable.dat");
			BufferedWriter writer = Files.newBufferedWriter(path);
			//unfortunately, to make writing easier I chose to forgo only calculating y to x, instead going y to numMovies, which is quite a bit longer
			//in terms of time complexity
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
		
		//this method does the same thing as Tools.init(), only with the similarityTable
		public static boolean readSimilarityTable() throws FileNotFoundException{
			if(!writeRead)
				return false;
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
	
	//this class was made so that I could easily add movies and their respective ratings to a BinaryMaxHeap
	//only the constructor, a compareTo, and two getters are included/needed
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