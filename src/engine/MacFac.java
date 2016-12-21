package engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import drivers.DriverSchemaRefinement;

import model.DirectedGraphAdapter;
import model.Model;
import model.WeightedEdgeAdapter;

public class MacFac {
	//parameters
	static double temperature = .01;	//for softmax function in computeMACSimilarity()
	
	//for MAC
	static double macWidth = .10;
	static int macMaxMatches = 2;
	static double macThreshold = 0;
	
	//forFAC
	static double facWidth = .10;
	static double facThreshold = 0;
	static int facMaxMatches = 1;
	
	//for calculateAnalogyGoodness()
	static double initialMatchScore = 1;	//needs to be tuned
	static double trickleDownFactor = .5;	//needs to be tuned
	
	public static File facScores = new File("/Developer/analogy/data/facScores.txt");
	public static FileWriter facWriter;
	
	
	/**
	 * probes world graph with numProbings of spotlightCapstones centered on randomly chosen nodes from entities list
	 * Note:  world graph is implicit in the links between Concepts in entities list, rather than being an actual graph data structure)
	 * @param entities
	 * @param numProbings
	 * @return
	 * @throws Exception 
	 */
	public static List<Concept> probeWithCapstones(List<Concept> entities, int numProbings) throws Exception
	{
		System.out.println("entity size = " + entities.size());
		System.out.println();
		
		//probe with capstone centered on randomly chosen node from entityGraph and grown outward numLevels
		int numLevels = 2;
		for(int i = 1; i <= numProbings; ++i)
		{
			Concept spotlightEntity = getRandomEntity(entities);
			List<Concept> spotlight = expandSpotlight(spotlightEntity, numLevels);
			CapstoneConcept spotlightCapstone = createCapstoneByChildTypes(spotlight);
			if(!entities.contains(spotlightCapstone) && spotlightCapstone != null)
			{
				entities.add(spotlightCapstone);	//TODO don't add spotlightCapstone to entities at all?
			}
			System.out.println();
			System.out.println("Probe # " + i);
			
			//List<FACMatch> facMatches = runMacFac(spotlightCapstone, entities);
			List<FACMatch> facMatches = runSpotlightMacFac(spotlightCapstone, entities);	//doesn't include capstones in the mapping dynamics (but still uses them for memory probing)
			
			
			
			//remove spotlightCaptsone from entityGraphy after matches are found, along with links to/from spotlightCapstone
			entities.remove(spotlightCapstone);
			List<Concept> spotlightRoles = new ArrayList<Concept>();
			spotlightRoles.addAll(spotlightCapstone.roles);
			for(Concept c : spotlightRoles)
			{
				c.relations.remove(spotlightCapstone);
				spotlightCapstone.roles.remove(c);
			}
			
			//create schema for each facMatch and add schema to entityGraph
			int matchNum = 0;
			for(FACMatch match : facMatches)
			{
				System.out.println("facMatch # " + ++matchNum);
				System.out.println("facMatch vertices = " + match.mapping.getVertices().size() + " :: edges = " + match.mapping.getEdges().size());
				//System.out.println("facMatch vertices:");
				List<Concept> newSchema = SchemaEngine.createSchema(match.mapping);	
				
				System.out.println("newSchema size = " + newSchema.size());
				CapstoneConcept schemaCapstone = createCapstoneByChildTypes(newSchema);
				//TODO need to pass on history of previous schema to this capstone
				updateCapstoneAncestry(schemaCapstone, match);
				
				//output perfect matches
				if(newSchema.size() == match.mapping.getChildren().size())
				{
					DriverSchemaRefinement.processPerfectMatch(match, schemaCapstone.id);
				}
				
				
				
				
				//add newSchema back into entityGraph
				addSchemaToGraph(newSchema, entities);
				if(!entities.contains(schemaCapstone) && schemaCapstone != null)
				{
					entities.add(schemaCapstone);
				}
				
				
				//consolidation criteria
				if(meetsConsolidationCriteria(schemaCapstone))
				{
					ConsolidatedConcept consolidatedConcept = SchemaEngine.consolidateSchema(newSchema, entities);
					entities.add(consolidatedConcept);
				}
				
				//just for evaluation
				Concept schemaNode = match.mapping.getChildren().get(0);	//arbitrary node from target, should be a node from one of the schemas found earlier
				evaluateSchemaCoverage(recoverSchema(schemaNode), match.mapping);	
			}
		}
		
		return entities;
	}
	
	private static boolean meetsConsolidationCriteria(CapstoneConcept schemaCapstone)
	{
		return schemaCapstone.generation >= 3;
	}

	private static List<FACMatch> runSpotlightMacFac(CapstoneConcept spotlightCapstone, List<Concept> entities) throws Exception
	{
		List<MACMatch> macMatches = findMACMatches(spotlightCapstone, entities);
		
		//convert capstone matches to concept matches
		List<MACMatch> entityMatches = new ArrayList<MACMatch>();
		for(MACMatch mm : macMatches)
		{
			if(mm.match instanceof CapstoneConcept)	//means only schemas will be recalled from world and included in entityMatches
			{
				CapstoneConcept schemaCap = (CapstoneConcept) mm.match;
				List<Concept> schema = schemaCap.roles;
				List<Concept> spotlightNodes = spotlightCapstone.roles;
				MACMatch entityMM = MacFac.getMACMatchSeedPair(spotlightNodes, schema);	//TODO for now, only one seedPair; needs to be parameterized to return n seedPairs
				if(mm.sourceCapstone != null)
				{
					entityMM.sourceCapstone = mm.sourceCapstone;
				}
				if(mm.targetCapstone != null)
				{
					entityMM.targetCapstone = mm.targetCapstone;
				}
				entityMatches.add(entityMM);
			}
		}
		
		List<FACMatch> facMatches = MacFac.findFACMatches(entityMatches, entities);
		return facMatches;
	}
	
	//TODO now outdated
	private static void updateCapstoneAncestry(CapstoneConcept capstone, FACMatch facMatch)
	{
		//update priorCapstones (source and target)
		if(facMatch.sourceCapstone != null)
		{
			capstone.priorSourceCapstones.add(facMatch.sourceCapstone);
		}
		if(facMatch.targetCapstone != null)
		{
			capstone.priorTargetCapstones.add(facMatch.targetCapstone);
			
			//update generation
			capstone.generation = facMatch.targetCapstone.generation + 1;
			System.out.println("schema generation = " + capstone.generation);
		}
		
		//TODO is this part necessary?  the seeds shouldn't ever be capstones anymore
		if(facMatch.seed0 instanceof CapstoneConcept)
		{
			capstone.priorSourceCapstones.add((CapstoneConcept) facMatch.seed0);
		}
		if(facMatch.seed1 instanceof CapstoneConcept)
		{
			capstone.priorTargetCapstones.add((CapstoneConcept) facMatch.seed1);
		}
		
		//update mappings
		capstone.mappings.add(facMatch.mapping);
	}
	
	public static List<FACMatch> probeWithSpotlightEntity(List<Concept> entities, Concept spotlightEntity) throws Exception
	{
		//probe with capstone centered on passed spotlightEntity argument from entityGraph and grown outward numLevels
		int numLevels = 2;
		List<Concept> spotlight = expandSpotlight(spotlightEntity, numLevels);
		Concept spotlightCapstone = createCapstoneByChildTypes(spotlight);	
		if(!entities.contains(spotlightCapstone) && spotlightCapstone != null)
		{
			entities.add(spotlightCapstone);
		}
		
		List<FACMatch> facMatches = runMacFac(spotlightCapstone, entities);
		//TODO need to filter out world-world analogies
		
		//remove spotlightCaptsone from entityGraphy after matches are found, along with links to/from spotlightCapstone
		entities.remove(spotlightCapstone);
		List<Concept> spotlightRoles = new ArrayList<Concept>();
		spotlightRoles.addAll(spotlightCapstone.roles);
		for(Concept c : spotlightRoles)
		{
			c.relations.remove(spotlightCapstone);
			spotlightCapstone.roles.remove(c);
		}
		
		return facMatches;
	}
	
	/**
	 * creates numSchemas initial schemas from analogical mappings seeded by softmax macvector pairing of nodes in entities list
	 * TODO this needs to be changed to only create World-World analogies!
	 * @param entities
	 * @param numSchemas
	 * @return
	 * @throws Exception 
	 */
	public static List<Concept> createSchemas(List<Concept> entities, int numSchemas) throws Exception
	{
		System.out.println();
		
		//TODO filter out worldEntities from schemaEntities
		//TODO how to tell if an entity is schema or world?
		/*
		List<Concept> worldEntities = new ArrayList<Concept>();
		for(Concept c : entities)
		{
			if()
		}
		*/
		
		//find numSchemas from softmax macvector seed pairings and add them to entityGraph
		for(int i = 0; i < numSchemas; ++i)
		{
			MACMatch mm = getMACMatchSeedPair(entities, entities);
			Concept[] seedPair = {mm.probe, mm.match};
			
			//make schema from initial analogy (random softmax seedPair)
			List<Concept[]> seedPairs = new ArrayList<Concept[]>();
			seedPairs.add(seedPair);
			DirectedGraphAdapter<Concept> mapping = AnalogyEngine.map(seedPairs, entities);
			List<Concept> schema = SchemaEngine.createSchema(mapping);
			addSchemaToGraph(schema, entities);
			
			//TODO is schema size ever 0?
			//create capstone with composite macvector from all schema nodes
			CapstoneConcept schemaCapstone = createCapstoneByChildTypes(schema);	
			//TODO need to pass on history of where this capstone came from; ancestry of schemas
			schemaCapstone.mappings.add(mapping);
			if(!entities.contains(schemaCapstone) && schemaCapstone != null)
			{
				entities.add(schemaCapstone);
			}
		}
		return entities;
	}
	
	
	private static Concept getRandomEntity(List<Concept> entities)
	{
		int size = entities.size();
		int rand = (int)  Math.random() * size;
		return entities.get(rand);
	}
	
	public static List<Concept> recoverSchema(Concept schemaNode)
	{
		List<Concept> schema = new ArrayList<Concept>();
		Queue<Concept> q = new LinkedList<Concept>();
		q.add(schemaNode);
		while(!q.isEmpty())
		{
			Concept c = q.poll();
			schema.add(c);
			//add roles
			for(Concept role : c.roles)
			{
				if(role != null && !schema.contains(role) && !q.contains(role))
				{
					q.add(role);
				}
			}
			//add relations
			for(Concept relation : c.relations)
			{
				if(relation != null && !schema.contains(relation) && !q.contains(relation))
				{
					q.add(relation);
				}
			}
		}
		return schema;
	}
	
	//TODO grows too many nodes!?
	private static List<Concept> expandSpotlight(Concept spotlightConcept, int numLevels)
	{
		System.out.println("ExpandingSpotlight");
		int maxSize = 30;
		int curSize = 0;
		
		List<Concept> spotlight = new ArrayList<Concept>();
		Queue<Concept> q1 = new LinkedList<Concept>();	//level 1
		Queue<Concept> q2 = new LinkedList<Concept>();	//level 2
		q1.add(spotlightConcept);
		
		
		for(int i = 0; i < numLevels; ++i)
		{
			while(!q1.isEmpty())
			{
				Concept c = q1.poll();
				spotlight.add(c);
				curSize++;
				if(curSize >= maxSize)	//TODO hack to limit size of attention
				{
					return spotlight;
				}
				//add roles to q2
				for(Concept role : c.roles)
				{
					if(!spotlight.contains(role))
					{
						q2.add(role);
					}
				}
				//add relations to q2
				for(Concept relation : c.relations)
				{
					if(!spotlight.contains(relation))
					{
						q2.add(relation);
					}
				}
			}
			q1 = q2;	//level 2 becomes new level 1
		}
		//System.out.println("spotlight size = " + spotlight.size() + " numLevels = " + numLevels);
		return spotlight;
	}
	
	private static List<FACMatch> runMacFac(Concept probe, List<Concept> entities) throws Exception
	{
		//MAC stage
		List<MACMatch> macMatches = findMACMatches(probe, entities);
		
		//FAC stage
		List<FACMatch> facMatches = findFACMatches(macMatches, entities);
		
		return facMatches;
		
	}
	
	
	private static CapstoneConcept createCapstoneByChildTypes(List<Concept> schemaNodes)
	{
		CapstoneConcept capstone = new CapstoneConcept(-2);
		for(Concept s : schemaNodes)
		{
			capstone.roles.add(s);
			//s.relations.add(capstone);  //NOTE:  removed link from roles to capstone to prevent capstone being in mapping
			//System.out.println(s + ":" + s.macVector.toString());
			DimensionNameString dns = new DimensionNameString("HASCHILD_TYPE_" + s.type);
			capstone.macVector.incrementDimension(dns);
			
			
			//pass on any capstone node's mappings to the new capstone
			
			//NOTE: this stuff should not go here; should go in calling function
			//if(s instanceof CapstoneConcept || s.type == -2)
			//{
				//CapstoneConcept priorCapstone = (CapstoneConcept) s;
				//capstone.priorCapstones.add(priorCapstone);
				//TODO should capstone just keep link to priorMappings through its priorCapstones?  I think yes.
				/*
				for(DirectedGraphAdapter<Concept> priorMapping : priorCapstone.priorMappings)
				{
					capstone.priorMappings.add(priorMapping);
				}
				*/
				//System.out.println("new capstone = " + capstone);
				//System.out.println("prior capstones size = " + capstone.priorCapstones.size());
			//}
		}
		/*
		//keep record of what mapping gave rise to this capstone
		if(match != null)
		{
			if(match.mapping != null)
			{
				capstone.mappings.add(match.mapping);
			}
		}
		*/
		
		System.out.println("capstone = " + capstone + " :: " + capstone.macVector.toString());
		return capstone;
	}
	

	private static double evaluateSchemaCoverage(List<Concept> schema, DirectedGraphAdapter<Concept> mapping)
	{
		int schemaSize = schema.size();
		int matchSize = mapping.getChildren().size();	//match is only 1/2 the mapping (the target)
		int schemaCoverage = 0;
		
		for(Concept c : mapping.getChildren())
		{
			if(schema.contains(c))
			{
				++schemaCoverage;
			}
		}
		
		double coverageRatio = (double) schemaCoverage / (double) schemaSize;
		
		System.out.println("schemaSize = " + schemaSize);
		System.out.println("schemaCoverage = " + schemaCoverage);
		System.out.println("matchSize = " + matchSize);
		System.out.println("ratio = " + coverageRatio);
		
		return coverageRatio;
	}
	
	private static void addSchemaToGraph(List<Concept> schema, List<Concept> entities)
	{
		for(Concept s : schema)
		{
			if(!entities.contains(s) && s != null)
			{
				entities.add(s);
			}
			
			//add roles of s
			for(Concept role : s.roles)
			{
				if(!entities.contains(role) && role != null)
				{
					entities.add(role);
				}
			}
			//add relations of s
			for(Concept relation : s.relations)
			{
				if(!entities.contains(relation) && relation != null)
				{
					entities.add(relation);
				}
			}
		}
	}
	
	public static double calculateAnalogyQuality(DirectedGraphAdapter<Concept> mapping)
	{
		//create schema only in order to evaluate goodness of mapping; not making claim that analogy is schematized at this point
		//creating schema here is only to make implementation more elegant
		List<Concept> schema = SchemaEngine.createSchema(mapping);
		//sort schema nodes by type (decreasing)
		Collections.sort(schema, new ConceptTypeComparator());
		for(Concept s : schema)
		{
			s.score += initialMatchScore;	//TODO could have different initialMatchScores for different concept types, like Forbus & Gentner
			//TODO don't include roles that aren't part of the schema itself!  Actually, the role scores aren't included in the final tally; only nodes in the schema are added into the total score
			for(Concept role : s.roles)
			{
				if(role != null && !(role instanceof AnonymousToken))
				{
					role.score += trickleDownFactor*s.score;	//why does Forbus & Gentner use max{ W(MH2) + delta*W(MH1); 1.0}
				}
			}
		}
		
		//add up all scores from schema nodes
		double totalScore = 0;
		for(Concept s : schema)
		{
			totalScore += s.score;
		}
		return totalScore;
	}
	
	
	public static double calculateSelfSimilarityScore(List<Concept> schema)
	{
		Collections.sort(schema, new ConceptTypeComparator());
		for(Concept s : schema)
		{
			s.score += initialMatchScore;	//TODO could have different initialMatchScores for different concept types, like Forbus & Gentner
			//TODO don't include roles that aren't in the driver or recipient of the analogy!!  Actually, the role scores aren't included in the final tally; only nodes in the schema are added into the total score
			for(Concept role : s.roles)
			{
				if(role != null)
				{
					role.score += trickleDownFactor*s.score;	//why does Forbus & Gentner use max{ W(MH2) + delta*W(MH1); 1.0}
				}
			}
		}
		
		//add up all scores from schema nodes
		double totalScore = 0;
		for(Concept s : schema)
		{
			totalScore += s.score;
		}
		return totalScore;
	}
	
	/**
	 * Run AnalogyEngine map() individually using a mapping between probe and each of the entities from topMatches
	 * @param probe
	 * @param macMatches
	 * @param entityGraph
	 * @return
	 * @throws Exception 
	 */
	private static List<FACMatch> findFACMatches(List<MACMatch> macMatches, List<Concept> entities) throws Exception
	{
		System.out.println("findingFACMatches");
		
		Concept[] seedPair = new Concept[2];
		List<FACMatch> topMatches = new ArrayList<FACMatch>();
		PriorityQueue<FACMatch> pq = new PriorityQueue<FACMatch>();
		int matchNumber = 0;
		
		for(MACMatch mm : macMatches)
		{
			Concept c = mm.match;
			seedPair[0] = mm.probe;
			seedPair[1] = c;
			List<Concept[]> seedPairs = new ArrayList<Concept[]>();
			seedPairs.add(seedPair);
			DirectedGraphAdapter<Concept> mapping = AnalogyEngine.map(seedPairs, entities);
			double scoreRaw = calculateAnalogyQuality(mapping);
			//normalize score
			double sim1 = calculateSelfSimilarityScore(mapping.getParents());
			double sim2 = calculateSelfSimilarityScore(mapping.getChildren());
			double scoreNormalized = scoreRaw / Math.sqrt(sim1 * sim2);
			FACMatch fm = new FACMatch(mapping, scoreRaw, scoreNormalized, seedPair[0], seedPair[1]);
			if(mm.sourceCapstone != null)
			{
				fm.sourceCapstone = mm.sourceCapstone;
			}
			if(mm.targetCapstone != null)
			{
				fm.targetCapstone = mm.targetCapstone;
			}
			pq.add(fm);
			
			//TODO need to get MAC value and FAC values for output... 
			facWriter.append(mm.simScore + " " + fm.scoreRaw + " " + fm.scoreNormalized + "\n");
			
		}
		
		double maxScore;
		if(pq.isEmpty())
		{
			maxScore = 0;
		}else{
			maxScore = pq.peek().scoreRaw;
		}
		double scoreBound = maxScore * (1-facWidth);
		System.out.println();
		for(FACMatch fm : pq)
		{
			if(fm.scoreRaw >= scoreBound && fm.scoreRaw >= facThreshold && matchNumber < facMaxMatches)
			{
				topMatches.add(fm);
				matchNumber++;
				
				System.out.println("facMatchNumber:" + matchNumber + "  ::  score:" +  fm.scoreRaw);
				//facWriter.write(Double.toString(fm.score) + "\n");
			}
		}
		
		if(topMatches.size() == 0)
		{
			System.err.println("No FACMatches found");
		}
		return topMatches;
	}
	
	
	//subject to parameter constraints -- macWidth, macMaxMatches, macThreshold
	private static List<MACMatch> findMACMatches(Concept probe, List<Concept> entities) throws Exception
	{
		System.out.println("findingMACMatches:: probe = " + probe);
		
		List<MACMatch> topMatches = new ArrayList<MACMatch>();
		int matchNumber = 0;
		PriorityQueue<MACMatch> pq = new PriorityQueue<MACMatch>();
		
		for(Concept c : entities)
		{
			if(!c.equals(probe))	//TODO disallow probe from matching itself in entitySet?
			{
				double simScore = probe.macVector.computeSimilarity(c.macVector);
				if(simScore > 0 || c.type == -2)
				{
					//System.out.println(c);
					//System.out.println(c.macVector);
					//System.out.println(simScore);
				}
				MACMatch mm = new MACMatch(probe, c, simScore);
				if(probe instanceof CapstoneConcept)
				{
					mm.sourceCapstone = (CapstoneConcept) probe;
				}
				if(c instanceof CapstoneConcept)
				{
					mm.targetCapstone = (CapstoneConcept) c;
				}
				pq.add(mm);
			}
		}
		
		//get rid of NaNs from top of pq
		while(Double.isNaN(pq.peek().simScore))
		{
			pq.poll();
		}
		
		double maxSimScore = pq.peek().simScore;	//this is the top simScore
		double simScoreBound = maxSimScore * (1-macWidth);
		for(MACMatch mm : pq)
		{
			if(mm.simScore >= simScoreBound && mm.simScore >= macThreshold && matchNumber < macMaxMatches)
			{
				topMatches.add(mm);
				matchNumber++;
				System.out.println("macMatchNumber:" + matchNumber + "::  simScore:" +  mm.simScore + " :: " + mm.match);
			}
		}
		
		if(topMatches.size() == 0)
		{
			System.err.println("No MACMatches found for probe = " + probe);
		}
		return topMatches;
	}
	
	/*
	public static List<FACMatch> seedFACMappingWithMacMatches(List<MACMatch> macMatches)
	{
		
	}
	*/
	
	
	//TODO slow
	//TODO parameterize to give top N matches (how to do top N using softmax?)
	/**
	 * uses softmax
	 * Note: prevents self-matching by setting self-simScore to 0
	 * @param entities1
	 * @param entities2
	 * @return
	 * @throws Exception 
	 */
	public static MACMatch getMACMatchSeedPair(List<Concept> entities1, List<Concept> entities2) throws Exception
	{
		
		int size1 = entities1.size();
		int size2 = entities2.size();
		
		System.out.println("MAC Matrix size = " + size1 + "x" + size2);
		
		
		Double[][] similarityMatrix = new Double[size1][size2];
		//double s = 0;
		double maxSimScore = 0;
		for(int i = 0; i < size1; ++i)
		{
			for(int j = 0; j < size2; ++j)
			{
				double simScore = entities1.get(i).macVector.computeSimilarity(entities2.get(j).macVector);
				if(entities1.get(i).equals(entities2.get(j)))	//note: specifically sets self simScore to 0 to avoid matching node to self
				{
					simScore = 0;
				}
				similarityMatrix[i][j] = simScore;
				if(simScore > maxSimScore)
				{
					maxSimScore = simScore;
					//c1 = concepts[i];
					//c2 = concepts[j];
				}
			}
		}
		//System.out.println("maxSimScore = " + maxSimScore);
		//System.out.println("v1 = " + c1 + " :: " + c1.macVector.vectorString());
		//System.out.println("v2 = " + c2 + " :: " + c2.macVector.vectorString());
		
		
		Double[][] probabilityMatrix = new Double[size1][size2];
		double sumSoftScores = 0;
		for(int i = 0; i < size1; ++i)
		{
			for(int j = 0; j < size2; ++j)
			{
				double softScore = getSoftScore(similarityMatrix[i][j]);
				probabilityMatrix[i][j] = softScore;	//unnormalized
				sumSoftScores += softScore;
			}
		}
		//normalize
		for(int i = 0; i < size1; ++i)
		{
			for(int j = 0; j < size2; ++j)
			{
				probabilityMatrix[i][j] = probabilityMatrix[i][j] / sumSoftScores;  //normalize probabilities to sum to 1
			}
		}
		//pick one concept pair probabilistically
		double rand = Math.random();
		double probabilitySum = 0;
		double simScore = 0;
		double probability = 0;
		Concept c1 = null;
		Concept c2 = null;
		for(int i = 0; i < size1; ++i)
		{
			for(int j = 0; j < size2; ++j)	
			{
				probabilitySum += probabilityMatrix[i][j];
				if(probabilitySum >= rand)	//TODO should this be just > 
				{
					c1 = entities1.get(i);
					c2 = entities2.get(j);
					simScore = similarityMatrix[i][j];
					probability = probabilityMatrix[i][j];
					String s = "";
					//s += "rand = " + rand + "\n";
					s += "simScore = " + simScore + " : probability = " + probability + "\n";
					s += "v1 = " + c1 + c1.macVector.toString() + "\n";
					s += "v2 = " + c2 + c2.macVector.toString() + "\n";
					System.out.println(s);
					Concept[] seedPair = new Concept[2];
					seedPair[0] = c1;
					seedPair[1] = c2;					
					return new MACMatch(c1, c2, simScore);
				}
			}
		}
		System.err.println("MAC match not found");
		return null;
		
	}
	
	private static double getSoftScore(double simScore)
	{
		return Math.exp(simScore / temperature);	//gibbs distribution 
	}

}
