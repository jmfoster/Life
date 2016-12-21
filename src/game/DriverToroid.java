package game;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import engine.AnalogyEngine;

import model.Model;

public class DriverToroid {

	/**
	 * @param args
	 * @throws IOException 
	 */
	private static List<World> worldList = new ArrayList<World>();
	
	public static List<World> runLife(String seedFile, int iterations) throws IOException {
		
		World world = new ToroidWorld(new File(seedFile), 0);
		Integer size = world.getSize();
		worldList.add(world);
		System.out.println("ITERATION 0 (Seed Configuration)");
		System.out.println(world.toString());		
		
		System.out.println();
		System.out.print("World Iteration... ");
		//runRules on world
		for(int i = 1; i <= iterations; ++i)
		{			
			world = runRules(world);
			worldList.add(world);
			System.out.print(i + " ");
			//System.out.println(world.toString());	
		}
		
		//Model.process(worldList, size);
		return worldList;
	}
	
	
	public static World runRules(World currentWorld)
	{
		/*
		 * Any live cell with fewer than two live neighbours dies, as if caused by under-population.
		 * Any live cell with two or three live neighbours lives on to the next generation.
		 * Any live cell with more than three live neighbours dies, as if by overcrowding.
		 * Any dead cell with exactly three live neighbours becomes a live cell, as if by reproduction.
		 */
		int size = currentWorld.size;
		Integer[][] nextWorld = new Integer[size][size];
		
		for(int i=0; i < size; ++i)
		{
			for(int j=0; j < size; ++j)
			{
				//update this cell's value				
				Integer sum = currentWorld.getNeighborsSum(i, j);
				//RULES for live cells
				if(currentWorld.getState(i, j) == 1)
				{
					if(sum < 2)
					{
						nextWorld[i][j]=0;
					}else if(sum==2 || sum==3)
					{
						nextWorld[i][j]=1;
					}else if(sum > 3)
					{
						nextWorld[i][j]=0;
					}
				//RULES for dead cells
				}else if(currentWorld.getState(i, j) == 0)
				{
					if(sum==3)
					{
						nextWorld[i][j] = 1;
					}else{
						nextWorld[i][j] = 0;
					}
				}			
				
			}		
		}
		
		World nextW = new ToroidWorld(size, nextWorld, currentWorld.iteration+1);
		
		return(nextW);
		
	}

}
