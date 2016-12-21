package drivers;

import engine.CapstoneConcept;
import engine.Concept;
import engine.MacFac;
import engine.SchemaAlignment;
import engine.SchemaEngine;
import game.DriverToroid;
import game.ToroidWorld;
import game.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import model.Model;

public class Driver {
	//parameters
	private static boolean firstRun = true;
	private static int initialSchemaCount = 5;
	
	private static boolean alignSchemas = true;
	private static int probeCount = 5;
	
	private static boolean clusterSchemas = true;
	private static double clusteringSimScoreThreshold = 24;
	
	
	//file locations
	private static File serEntities = new File("Developer/analogy/data/entityList.ser");
	private static File serAlignments = new File("Developer/analogy/data/alignmentList.ser");
	
	
	public static void main(String[] args) throws Exception {
		String seedFilename = args[0];
		int numIterations = Integer.parseInt(args[1]);
		
		List<Concept> entities;
		if(args.length > 2)
		{
			firstRun = Boolean.parseBoolean(args[2]);
		}
		if(args.length > 3)
		{
			initialSchemaCount = Integer.parseInt(args[3]);
		}
		if(args.length > 4)
		{
			initialSchemaCount = Integer.parseInt(args[4]);
		}
		if(args.length > 5)
		{
			clusteringSimScoreThreshold = Double.parseDouble(args[5]);
		}
		
		List<SchemaAlignment> schemaAlignments;
		int oldEntitySize;
		if(firstRun)
		{
			oldEntitySize = 0;
			List<World> worldList = DriverToroid.runLife(seedFilename, numIterations);
			entities = Model.createEntityList(worldList);	 //Note: graph structure of entities is implicit in the roles and relations links between Concepts in entities list, rather than being an actual graph data structure
			entities = MacFac.createSchemas(entities, initialSchemaCount);	//Note: Untyped schema nodes have type = -1 and capstones have typed = -2
			SaveLoad.serializeEntities(serEntities, entities);
			schemaAlignments = new ArrayList<SchemaAlignment>();	//starts off empty
		}else{
			entities = SaveLoad.restoreSerializedEntities(serEntities);
			oldEntitySize = entities.size();
			schemaAlignments = SaveLoad.restoreSerializedAlignments(serAlignments);
			entities = MacFac.probeWithCapstones(entities, probeCount);
			SaveLoad.serializeEntities(serEntities, entities);
		}
		
		//TODO need to normalize by schema size? divide by number of nodes?
		if(alignSchemas)
		{
			List<CapstoneConcept> allSchemaCapstones = new ArrayList<CapstoneConcept>();
			int numCapstones = 0;
			int oldCapstonesSize = 0;
			//add capstones to allSchemaCapstones
			for(int i = 0; i < entities.size(); ++i)
			{
				Concept c = entities.get(i);
				if(c instanceof CapstoneConcept)
				{
					numCapstones++;
					allSchemaCapstones.add( (CapstoneConcept) c);
					if(i == oldEntitySize)
					{
						oldCapstonesSize = numCapstones;
					}
				}
			}
			schemaAlignments = SchemaEngine.alignSchemas(schemaAlignments, entities, allSchemaCapstones, oldCapstonesSize);
			SaveLoad.serializeAlignments(serAlignments, schemaAlignments);
		}
		
		
		if(clusterSchemas)
		{
			SchemaEngine.clusterSchemas(schemaAlignments, clusteringSimScoreThreshold);
		}
	}
	
	
}
