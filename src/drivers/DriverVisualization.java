package drivers;

import engine.AnalogyEngine;
import engine.CapstoneConcept;
import engine.Concept;
import engine.FACMatch;
import engine.MACMatch;
import engine.MacFac;
import engine.SchemaAlignment;
import game.DriverToroid;
import game.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import model.DirectedGraphAdapter;
import model.Model;
import model.Object;


//schema generation and save source of that schema
//send fabian the driver that resulted in that analogy/schema


public class DriverVisualization {
	
	//parameters
	
	//file locations
	private static File initialSerEntities = new File("data/initialEntityList.ser");
	private static File finalSerEntities = new File("data/finalEntityList.ser");
	
	
	public static void main(String[] args) throws IOException 
	{
		//run Life
		String seedFilename = args[0];
		int numIterations = Integer.parseInt(args[1]);
		List<World> worldList = DriverToroid.runLife(seedFilename, numIterations);
		List<Concept> entities = Model.createEntityList(worldList);	 //Note: graph structure of entities is implicit in the roles and relations links between Concepts in entities list, rather than being an actual graph data structure
	
		
		//File outputFile = new File("data/worldAnalogy6.txt");
		//makeWorldAnalogy(outputFile, entities, worldList);

		//analyzeSchemas(false, entities, worldList);
		
		analyzeMACVectors();
		
		//probeWithRandomBasicLevelObject(false, entities, worldList);
		
		//probeWithAllBasicLevelObjects(true, entities, worldList);
		//probeWithAllBasicLevelObjectsfalse, entities, worldList);
	}
	
	
	private static void analyzeMACVectors() throws IOException
	{
		List<Concept> entities = SaveLoad.restoreSerializedEntities(finalSerEntities);
		for(Concept c : entities)
		{
			System.out.println(c.macVector + " :: " + c);
		}
	}
	
	
	private static void makeWorldAnalogy(File outputFile, List<Concept> entities, List<World> worldList) throws Exception
	{
		MACMatch mm = MacFac.getMACMatchSeedPair(entities, entities);
		Concept[] seedPair = {mm.probe, mm.match};
		//make schema from initial analogy (random softmax seedPair)
		List<Concept[]> seedPairs = new ArrayList<Concept[]>();
		seedPairs.add(seedPair);
		DirectedGraphAdapter<Concept> mapping = AnalogyEngine.map(seedPairs, entities);
		Model.outputWorldListForVisualization(outputFile, worldList, mapping);
			
	}
	
	private static void analyzeSchemas(boolean initial, List<Concept> entities, List<World> worldList) throws IOException
	{
		File serEntities;
		String driverDirectory;
		File cellHeatmap;
		if(initial)
		{
			serEntities = initialSerEntities;
			driverDirectory = "data/visualization/drivers/initial/";
			cellHeatmap = new File("data/visualization/initialCellHeatmap");
			
		}else{
			serEntities = finalSerEntities;
			driverDirectory = "data/visualization/drivers/final/";
			cellHeatmap = new File("data/visualization/finalCellHeatmap");
		}
		
			
		List<Concept> storedEntities = SaveLoad.restoreSerializedEntities(serEntities);
		
		
		//TODO ancestry generation is not by size of priorcapstones, but rather by length of chain!
		//this algorithm only outputs the leftmost chain of the tree
		for(Concept c : storedEntities)
		{
			if(c instanceof CapstoneConcept)	//assumes only schemas have capstones
			{
				
				System.out.println("restored capstone concept = " + c + "     " + c.macVector);
				System.out.println("schema generation = " + ((CapstoneConcept)c).generation);
				for(Concept role : c.roles)
				{
					System.out.println("          " + role + "     " + role.macVector);
				}
				
				CapstoneConcept cap = (CapstoneConcept) c;
				//outputCapstoneAncestry(cap);
				CapstoneConcept priorCap = null;
				if(cap.priorTargetCapstones.size() > 0)
				{
					priorCap = cap.priorTargetCapstones.get(0);
				}else{
					priorCap = null;
				}
				String indent = "";
				while(priorCap != null)
				{
					indent += "     ";
					System.out.println(indent + "priorCap = " + priorCap);
					if(priorCap.priorTargetCapstones.size() > 0)
					{
						priorCap = priorCap.priorTargetCapstones.get(0);
					}else{
						priorCap = null;
					}
				}
			}
		}
	}
	
	//TODO
	/*
	private static void outputCapstoneAncestry(CapstoneConcept cap)
	{
		int topIndex = 0;
		int bottomIndex = 0;
		CapstoneConcept priorCap = null;
		while(cap != null)
		{
			if(cap.priorTargetCapstones.size() > topIndex)
			{
				priorCap = cap.priorTargetCapstones.get(topIndex++);
			
			String indent = "";
			while(priorCap != null)
			{
				indent += "     ";
				System.out.println(indent + "priorCap = " + priorCap);
				if(priorCap.priorTargetCapstones.size() > bottomIndex)
				{
					priorCap = priorCap.priorTargetCapstones.get(bottomIndex++);
				}else{
					priorCap = null;
				}
			}
		}
	}
	*/
	
	private static void probeWithRandomBasicLevelObject(boolean initial, List<Concept> entities, List<World> worldList) throws Exception
	{
		File serEntities;
		String driverDirectory;
		File cellHeatmap;
		if(initial)
		{
			serEntities = initialSerEntities;
			driverDirectory = "data/visualization/drivers/initial/";
			cellHeatmap = new File("data/visualization/initialCellHeatmap");
			
		}else{
			serEntities = finalSerEntities;
			driverDirectory = "data/visualization/drivers/final/";
			cellHeatmap = new File("data/visualization/finalCellHeatmap");
		}
		
			
		//set up data lists to operate on
		List<Object> basicLevelObjects = extractBasicLevelObjects(entities);
		List<Concept> storedEntities = SaveLoad.restoreSerializedEntities(serEntities);
		List<Concept> entitiesWithSchemas = addStoredSchemasToEntities(entities, storedEntities);
		
		int rand = (int) Math.floor(Math.random() * basicLevelObjects.size());
		Object blo = basicLevelObjects.get(rand);
		
		//probe schemas with spotlight centered on this blo
		List<FACMatch> facMatches = MacFac.probeWithSpotlightEntity(entitiesWithSchemas, blo);
		for(FACMatch fm : facMatches)
		{
			File outputFile = new File("data/visualization/worldMatches/" + Double.toString(fm.scoreRaw).substring(0, 4));
			Model.outputWorldListForVisualization(outputFile, worldList, fm.mapping);
		}
		
		Model.outputWorldListforHeatmap(cellHeatmap, worldList);
	}
	
	private static void probeWithAllBasicLevelObjects(boolean initial, List<Concept> entities, List<World> worldList) throws Exception
	{
		File serEntities;
		String driverDirectory;
		File cellHeatmap;
		if(initial)
		{
			serEntities = initialSerEntities;
			driverDirectory = "data/visualization/drivers/initial/";
			cellHeatmap = new File("data/visualization/initialCellHeatmap");
			
		}else{
			serEntities = finalSerEntities;
			driverDirectory = "data/visualization/drivers/final/";
			cellHeatmap = new File("data/visualization/finalCellHeatmap");
		}
		
			
		//set up data lists to operate on
		List<Object> basicLevelObjects = extractBasicLevelObjects(entities);
		List<Concept> storedEntities = SaveLoad.restoreSerializedEntities(serEntities);
		List<Concept> entitiesWithSchemas = addStoredSchemasToEntities(entities, storedEntities);
		
		for(int i = 0; i < basicLevelObjects.size(); ++i)
		{
			System.out.println();
			System.out.println("#" + i + "/" + basicLevelObjects.size());
			
			Object blo = basicLevelObjects.get(i);
			
			//probe schemas with spotlight centered on this blo
			List<FACMatch> facMatches = MacFac.probeWithSpotlightEntity(entitiesWithSchemas, blo);	//Note: #facMatches is set in MacFac parameter.  should be set to 1 for this driver
			FACMatch facMatch = facMatches.get(0);	//take first (should be only) facMatch
			Model.setFACMatchScoreInCells(blo, facMatch.scoreNormalized);
			
			//output file for visualizing the driver of the facMatch with this blo as its probe
			File driverVisualizationFile = new File(driverDirectory + Integer.toString(blo.id) + ".txt");
			Model.outputWorldListForVisualization(driverVisualizationFile, worldList, facMatch.mapping);
		}
		
		Model.outputWorldListforHeatmap(cellHeatmap, worldList);
	}
	
	private static List<Object> extractBasicLevelObjects(List<Concept> entities)
	{
		List<Object> basicLevelObjects = new ArrayList<Object>();
		for(Concept c : entities)
		{
			if(c instanceof Object)
			{
				basicLevelObjects.add((Object) c);
			}
		}
		return basicLevelObjects;
	}
	
	private static List<Concept> addStoredSchemasToEntities(List<Concept> entities, List<Concept> storedEntities)
	{
		for(Concept c : storedEntities)
		{
			if(c instanceof CapstoneConcept)	//assumes only schemas have capstones
			{
				System.out.println("restored capstone concept = " + c + "     " + c.macVector);
				System.out.println("generation = " + ((CapstoneConcept)c).generation);
				for(Concept role : c.roles)
				{
					System.out.println("          " + role + "     " + role.macVector);
				}
				List<Concept> schema = MacFac.recoverSchema(c);
				for(Concept s : schema)
				{
					if(!entities.contains(s))
					{
						entities.add(s);
					}
				}
			}
		}
		return entities;
	}
	

}
