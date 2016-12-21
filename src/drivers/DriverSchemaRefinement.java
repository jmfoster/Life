package drivers;

import engine.CapstoneConcept;
import engine.Concept;
import engine.ConsolidatedConcept;
import engine.FACMatch;
import engine.MacFac;
import engine.SchemaAlignment;
import engine.SchemaEngine;
import game.DriverToroid;
import game.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import model.Model;

public class DriverSchemaRefinement {
	//parameters
	private static int initialSchemaCount = 2;
	private static int numRefinements = 20;
	
	private static double clusteringSimScoreThreshold = 24;
	
	private static boolean pruneSmallest = false;
	
	//file locations
	private static File initialSerEntities = new File("/Developer/analogy/data/initialEntityList.ser");
	private static File finalSerEntities = new File("/Developer/analogy/data/finalEntityList.ser");
	private static File initialSerAlignments = new File("/Developer/analogy/data/initialAlignmentList.ser");
	private static File finalSerAlignments = new File("/Developer/analogy/data/finalAlignmentList.ser");
	
	private static File schemaSizes = new File("/Developer/analogy/data/schemaSizes.txt");
	
	private static List<World> worldList;
	
	public static void main(String[] args) throws Exception 
	{
		MacFac.facWriter = new FileWriter(MacFac.facScores);	//hack to instantiate this here instead of MacFac
		
		runSchemaEvolution(args);
		//compareSchemaSnapshots();
		
		MacFac.facWriter.flush();
		MacFac.facWriter.close();
		
	}
	
	private static void runSchemaEvolution(String[] args) throws Exception
	{
		String seedFilename = args[0];
		int numIterations = Integer.parseInt(args[1]);
		
		//run Life and create schemas
		List<Concept> entities;
		worldList = DriverToroid.runLife(seedFilename, numIterations);
		entities = Model.createEntityList(worldList);	 //Note: graph structure of entities is implicit in the roles and relations links between Concepts in entities list, rather than being an actual graph data structure
		
		//TODO should probably split this method into subparts to control schema creation
		entities = MacFac.createSchemas(entities, initialSchemaCount);	//Note: Untyped schema nodes have type = -1 and capstones have typed = -2
		//serializeEntities(serEntities, entities);
		
		
		//get list of initialSchemaCapstones
		List<CapstoneConcept> initialSchemaCapstones = new ArrayList<CapstoneConcept>();
		int numCapstones = 0;
		for(int i = 0; i < entities.size(); ++i)
		{
			Concept c = entities.get(i);
			if(c instanceof CapstoneConcept)
			{
				numCapstones++;
				initialSchemaCapstones.add( (CapstoneConcept) c);
			}
		}
		System.out.println("num initial capstones = " + numCapstones);
		
		//schemaCapstones is the working set of schema capstones
		List<Concept> schemaCapstones = new ArrayList<Concept>();
		schemaCapstones.addAll(initialSchemaCapstones);
		
		//save snapshot of initial world
		SaveLoad.serializeEntities(initialSerEntities, entities);
		
		FileWriter schemaSizeWriter = new FileWriter(schemaSizes);
		for(int i = 0; i < numRefinements; ++i)
		{
			//output avg size of schema pool
			double sumSchemaSizes = 0;
			for(Concept cap : schemaCapstones)
			{
				int schemaSize = cap.roles.size();
				sumSchemaSizes += schemaSize;
				schemaSizeWriter.append(Integer.toString(schemaSize) + " ");
			}
			double avgSchemaSize = sumSchemaSizes / schemaCapstones.size();
			schemaSizeWriter.append(avgSchemaSize + "\n");
			schemaSizeWriter.flush();
			
			System.out.println("refinement iteration # " + i);
			
			//probe with random capstone created from random spotlight on the world
			//TODO should probably split this method into parts to control when schemas are created with a (relative?) threshold
			MacFac.probeWithCapstones(entities, 1);	//resulting schema and schemaCapstone gets added to entities
			
			
			//prune schema from pool
			Concept schemaCapstone;	//note: could be ConsolidatedConcept also
			if(pruneSmallest)
			{
				schemaCapstone = selectSmallestSchema(schemaCapstones);
			}else{
				schemaCapstone = selectRandomSchema(schemaCapstones);
			}
			
			//delete schema from schemaCapstones and from world
			List<Concept> randSchema = MacFac.recoverSchema(schemaCapstone);
			schemaCapstones.remove(schemaCapstone);
			for(Concept c : randSchema)
			{
				entities.remove(c);
			}
			
			//update list of schemaCapstones
			schemaCapstones.clear();
			numCapstones = 0;
			for(int j = 0; j < entities.size(); ++j)
			{
				Concept c = entities.get(j);
				if(c instanceof CapstoneConcept || c instanceof ConsolidatedConcept)
				{
					numCapstones++;
					schemaCapstones.add( (Concept) c);
				}
			}
		
		}
		
		schemaSizeWriter.close();
		
		System.out.println("num final capstones = " + numCapstones);
		
		//save snapshot of final world
		SaveLoad.serializeEntities(finalSerEntities, entities);
	}
	
	public static void processPerfectMatch(FACMatch match, int newSchemaID) throws IOException
	{
		File file = new File("/code/analogy/data/visualization/perfectMatches/" + Integer.toString(newSchemaID) + "_" + match.scoreRaw + "_" + match.mapping.getChildren().size() +  ".txt");
		Model.outputWorldListForVisualization(file, worldList, match.mapping);
	}
	
	private static Concept selectSmallestSchema(List<Concept> schemaCapstones)
	{
		//select smallest schema for pruning
		Concept smallestSchemaCapstone = schemaCapstones.get(0);
		for(int i = 0; i < schemaCapstones.size(); ++i)
		{
			Concept cap = schemaCapstones.get(i);
			if(cap.roles.size() < smallestSchemaCapstone.roles.size())
			{
				smallestSchemaCapstone = cap;
			}
		}
		return smallestSchemaCapstone;
	}
	
	private static Concept selectRandomSchema(List<Concept> schemaCapstones)
	{
		//select random schema for pruning
		int rand = (int) Math.floor(Math.random() * schemaCapstones.size());
		return schemaCapstones.get(rand);
	}
	
	private static void compareSchemaSnapshots() throws Exception
	{
		//restore entity lists (including schemas)
		List<Concept> initialEntities = SaveLoad.restoreSerializedEntities(initialSerEntities);
		List<Concept> finalEntities = SaveLoad.restoreSerializedEntities(finalSerEntities);
		
		//extract schema capstones from entitiy lists
		List<CapstoneConcept> initialCapstones = new ArrayList<CapstoneConcept>();
		List<CapstoneConcept> finalCapstones = new ArrayList<CapstoneConcept>();
		for(Concept c : initialEntities)
		{
			if(c instanceof CapstoneConcept)
			{
				initialCapstones.add((CapstoneConcept) c);
			}
		}
		
		for(Concept c : finalEntities)
		{
			if(c instanceof CapstoneConcept)
			{
				finalCapstones.add((CapstoneConcept) c);
			}
		}
		
		
		//align and cluster initialEntities
		List<SchemaAlignment> initialSchemaAlignments = new ArrayList<SchemaAlignment>();	//starts off empty
		initialSchemaAlignments = SchemaEngine.alignSchemas(initialSchemaAlignments, initialEntities, initialCapstones, 0);
		SchemaEngine.outputSchemaAlignments(new File("/code/analogy/data/initialSchemaAlignments.txt"), initialSchemaAlignments);
		SchemaEngine.clusterSchemas(initialSchemaAlignments, clusteringSimScoreThreshold);
		
		//align and cluster finalEntities
		List<SchemaAlignment> finalSchemaAlignments = new ArrayList<SchemaAlignment>();	//starts off empty
		finalSchemaAlignments = SchemaEngine.alignSchemas(finalSchemaAlignments, finalEntities, finalCapstones, 0);
		SchemaEngine.outputSchemaAlignments(new File("/code/analogy/data/finalSchemaAlignments.txt"), finalSchemaAlignments);
		SchemaEngine.clusterSchemas(finalSchemaAlignments, clusteringSimScoreThreshold);
		
		//save alignments
		SaveLoad.serializeAlignments(initialSerAlignments, initialSchemaAlignments);
		//serializeAlignments(finalSerAlignments, finalSchemaAlignments);
	}
	
	
	
	
}
