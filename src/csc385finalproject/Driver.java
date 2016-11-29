package csc385finalproject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Driver {
	
	public static ArrayList<String> moviesdat=new ArrayList<String>();
	public static ArrayList<String> ratingsdat=new ArrayList<String>();
	private static String[][] movies;										//2d array to store movies and their id
	private static int[][] ratings;											//2d array to store users and their ratings
	private static int[][] uservmovies;										//2d array with users(row) movies(col) and the rating for the movie
	private static double[][] similarityTable;								//table to hold similarity values
	public static int numUsers;
	public static int numMovies;
	
	public static void main(String[] args) throws FileNotFoundException{
		
		Tools.init();
		
		uservmovies=new int[numUsers][numMovies];
		
		for(int x=0;x<movies.length;x++){
			uservmovies[ratings[x][0]][ratings[x][1]]=ratings[x][2];
		}
		
		/*
		for(int x=0;x<10;x++){
			for(int y=0;y<movies.length;y++)
				System.out.print(uservmovies[x][y]);
			System.out.println();
		}
		*/
		
		Tools.createSimilarityTable();

		/*
		for(int x=1;x<100;x++){
			for(int y=1;y<100;y++)
				System.out.print(similarityTable[x][y]+"|"+x+","+y+"\t");
			System.out.println();
		}
		*/
		System.out.println(Tools.predictRating(1,1156));
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
			/*
			for(int x=0;x<movies.length;x++){
				for(int y=0;y<2;y++)
					System.out.print(movies[x][y]+"\t");
				System.out.println();
			}
			*/
			/*
			for(int x=0;x<ratings.length;x++){
				for(int y=0;y<3;y++)
					System.out.print(ratings[x][y]+"\t");
				System.out.println();
			}
			*/
			
		}
		
		public static double similarity(int item1, int item2){
			int sumOfItem1vsItem2=0,squareSumItem1=0,squareSumItem2=0;
			for(int x=0;x<uservmovies.length;x++){
				sumOfItem1vsItem2+=uservmovies[x][item1]*uservmovies[x][item2];
			}
			for(int x=0;x<uservmovies.length;x++){
				squareSumItem1+=(int)Math.pow(uservmovies[x][item1],2);
			}
			for(int x=0;x<uservmovies.length;x++){
				squareSumItem2+=(int)Math.pow(uservmovies[x][item2],2);
			}
			if((Math.sqrt(squareSumItem1)*Math.sqrt(squareSumItem2))==0)
				return 0;
			return (sumOfItem1vsItem2/(Math.sqrt(squareSumItem1)*Math.sqrt(squareSumItem2)));
		}
		
		public static double predictRating(int user, int item){
			double sum=0,count=0,sim=0;
			for(int x=0;x<numMovies;x++){
				if(uservmovies[user][x]!=0){
					sim=similarityTable[item][x];
					sum+=sim*uservmovies[user][x];
					count+=sim;
				}
			}
			return sum/count;
		}
		
		public static void createSimilarityTable(){
			similarityTable=new double[numMovies][numMovies];
			double temp;
			for(int x=1;x<numMovies;x++){
				for(int y=1;y<=x;y++){
					temp=Tools.similarity(x,y);
					similarityTable[x][y]=temp;
					similarityTable[y][x]=temp;
				}
			}
		}
	}
}