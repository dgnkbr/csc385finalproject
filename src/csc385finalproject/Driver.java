package csc385finalproject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Driver {

	public static void main(String[] args) throws FileNotFoundException {
		ArrayList<String> moviesdat=new ArrayList<String>();
		ArrayList<String> ratingsdat=new ArrayList<String>();
		//make sure file exists
		Scanner fileIn = new Scanner(new File("movies.dat"));
		while(fileIn.hasNextLine()){
			moviesdat.add(fileIn.nextLine());								//add all of movies.dat to 'moviesdat'
		}
		fileIn = new Scanner(new File("ratings.dat"));
		while(fileIn.hasNextLine()){
			ratingsdat.add(fileIn.nextLine());								//add all of ratings.dat to 'ratingsdat'
		}
		fileIn.close();														//close scanner
		
		String[][] movies=new String[moviesdat.size()][2];					//2d array to store movies and their id
		int[][] ratings=new int[ratingsdat.size()][3];
		String[] tempI=new String[4];
		String[] tempS=new String[23];
		
		for(int x=0;x<moviesdat.size();x++){								//cycles through 'moviesdat'
			tempS=moviesdat.get(x).split("[|]");									//takes first two args after split (movieid and movietitle)
			movies[x][0]=tempS[0];													//and stores in 'movies'
			movies[x][1]=tempS[1];
		}
		for(int x=0;x<ratingsdat.size();x++){								//does the same as above, only with 'ratingsdat'
			tempI=ratingsdat.get(x).split("\t");
			ratings[x][0]=Integer.parseInt(tempI[0]);
			ratings[x][1]=Integer.parseInt(tempI[1]);
			ratings[x][2]=Integer.parseInt(tempI[2]);
		}
		
		/*
		for(int x=0;x<ratings.length;x++){
			for(int y=0;y<3;y++)
				System.out.print(ratings[x][y]+"\t");
			System.out.println();
		}
		*/
	}
}
