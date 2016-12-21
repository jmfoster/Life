package drivers;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import model.Model;

import engine.AnalogyEngine;
import engine.Concept;
import engine.MACMatch;
import engine.MacFac;
import game.DriverToroid;
import game.World;

public class DriverConsolidation {

	private static List<World> worldList;
	
	public static void main(String[] args) throws IOException
	{
		MacFac.facWriter = new FileWriter(MacFac.facScores);	//hack to instantiate this here instead of MacFac
		
		run(args);
		
		
		MacFac.facWriter.flush();
		MacFac.facWriter.close();
	}
	
	public static void run(String[] args) throws IOException
	{
		String seedFilename = args[0];
		int numIterations = Integer.parseInt(args[1]);
		
		//run Life and create schemas
		List<Concept> entities;
		worldList = DriverToroid.runLife(seedFilename, numIterations);
		entities = Model.createEntityList(worldList);	 //Note: graph structure of entities is implicit in the roles and relations links between Concepts in entities list, rather than being an actual graph data structure
		
		MACMatch seedPairs = MacFac.getMACMatchSeedPair(entities, entities);
		AnalogyEngine.map({seedPairs, entities)
	}
}
