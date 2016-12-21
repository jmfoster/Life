package engine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.PriorityQueue;

import model.DirectedGraphAdapter;
import model.WeightedEdgeAdapter;

public class SchemaEngine {
	
	
	public static void process(DirectedGraphAdapter<Concept> mapping, List<Concept> entities) throws IOException
	{
		//mapping is the analogical mapping found from AnalogyEngine
		//conceptGraph is the full graph of all concepts from Model
		
		//MACVectorCreator.concepts = entities;
		
		//create schema from mapping
		List<Concept> schema = createSchema(mapping);
		//add schema nodes back into conceptGraph
		for(Concept s : schema)
		{
			entities.add(s);
			//add roles of s
			for(Concept role : s.roles)
			{
				entities.add(role);
			}
			//add relations of s
			for(Concept relation : s.relations)
			{
				entities.add(relation);
			}
		}
		
		//predicate schema
		Concept predicatedConcept = consolidateSchema(schema, entities);
		//add predicated schema to conceptGraph
		entities.add(predicatedConcept);
		
	
		//TODO update all concept's MACVectors?	//do this here, or inside MACVectorCreator which is called by predicatedConcept's initializeMACVector method?
		
		
		System.out.println("schema size = " + schema.size());
		System.out.println("predicatedConcept = " + predicatedConcept);
		System.out.println("predicatedConcept vector = " + predicatedConcept.macVector);
		//String s = "predicatedConcept = " + c.id + "(" + c.type;
		String s = "roles:\n";
		for(Concept r : predicatedConcept.roles)
		{
			s += r + " " + r.macVector + "\n";
			//s += "," + r;
			//s += "," + r.id + "(" + r.type + ")"; 
		}
		//s += ")";
		System.out.println(s);
	
		
		//can recurse on AnalogyEngine with modified conceptGraph
		//AnalogyEngine.process(conceptGraph);
	
	}
	
	
	
	public static List<SchemaAlignment> alignSchemas(List<SchemaAlignment> schemaAlignments, List<Concept> entityList, List<CapstoneConcept> allSchemaCapstones, int oldCapstonesSize) throws Exception
	{
		
		System.out.println();
		System.out.println("ALIGNING SCHEMAS");
		System.out.println("number of schemaCapstones = " + allSchemaCapstones.size());
		
		//use Analogy Mapping Engine seeded with capstones to align all schemas pairwise
		int alignmentNumber = 0;
		int size = allSchemaCapstones.size();
		double totalAlignments = size*(size-1) / 2;
		for(int i = 0; i < size-1; ++i)
		{
			Concept cap1 = allSchemaCapstones.get(i);
			for(int j = i+1; j < size; ++j)
			{
				
				if(i < oldCapstonesSize && j < oldCapstonesSize)	//schemaAlignment was already done and exists in schemaAlignments
				{
					//old alignment
				}else{	//new alignment
					Concept cap2 = allSchemaCapstones.get(j);
					Concept[] seedPair = {cap1, cap2};
					List<Concept[]> seedPairs = new ArrayList<Concept[]>();
					seedPairs.add(seedPair);
					DirectedGraphAdapter<Concept> alignment = AnalogyEngine.map(seedPairs, entityList);
					
					//use getAnalogyGoodness() to score alignments to get similarityScores for all pairs of schema capstones
					double simScore = MacFac.calculateAnalogyQuality(alignment);
					//normalize simScore by self-similarities
					double sim1 = MacFac.calculateSelfSimilarityScore(MacFac.recoverSchema(cap1));
					double sim2 = MacFac.calculateSelfSimilarityScore(MacFac.recoverSchema(cap2));
					simScore = simScore / Math.sqrt(sim1 * sim2);
					SchemaAlignment sa = new SchemaAlignment(cap1, cap2, alignment, simScore);
					schemaAlignments.add(sa);
					System.out.println("schemaAlignment #" + ++alignmentNumber + " / " + totalAlignments + " :: simScore= " + simScore);
				}
				
			}
		}
		
		
		return schemaAlignments;
	}
	
	public static void clusterSchemasHierarchically(List<SchemaAlignment> schemaAlignments)
	{
		//sort schemaAlignments in priorityqueue by decreasing simScore
		PriorityQueue<SchemaAlignment> pq = new PriorityQueue<SchemaAlignment>();
		for(SchemaAlignment sa : schemaAlignments)
		{
			pq.add(sa);
		}
		
		//Join the most similar pair of objects that are not yet in the same cluster. 
		//Distance between 2 clusters is the distance between the closest pair of points, each of which is in one of the two clusters.
		
	}
	
	
	//TODO test!
	/**
	 * Hierarchical clustering method
		single link method
		Similarity: Join the most similar pair of objects that are not yet in the same cluster. Distance between 2 clusters is the distance between the closest pair of points, each of which is in one of the two clusters.
		Type of clusters: Long straggly clusters, chains, ellipsoidal
		Time: Usually O(N**2) though can range from O(N log N) to O(N**5)
		Space: O(N)
		Advantages: Theoretical properties, efficient implementations, widely used. No cluster centroid or representative required, so no need arises to recalculate the similarity matrix.
		Disadvantages: Unsuitable for isolating spherical or poorly separated clusters
	 * @param schemaAlignments
	 */
	public static void clusterSchemas(List<SchemaAlignment> schemaAlignments, double simScoreThreshold)
	{
		//sort schemaAlignments in priorityqueue by decreasing simScore
		PriorityQueue<SchemaAlignment> pq = new PriorityQueue<SchemaAlignment>();
		for(SchemaAlignment sa : schemaAlignments)
		{
			pq.add(sa);
		}
		
		//Join the most similar pair of objects that are not yet in the same cluster. 
		//Distance between 2 clusters is the distance between the closest pair of points, each of which is in one of the two clusters.
		//clusters hashtable is a mapping from capstoneID to clusterID
		Hashtable<Integer, Integer> clusters = new Hashtable<Integer, Integer>();	//mapping from capstoneID -> clusterID
		Integer clusterCount = 0;
		while(!pq.isEmpty() && pq.peek().simScore >= simScoreThreshold)
		{
			SchemaAlignment sa = pq.poll();
			System.out.println("simScore = " + sa.simScore);
			Integer cap1 = sa.capstone1.id;
			Integer cap2 = sa.capstone2.id;
			Integer cap1Cluster = clusters.get(cap1);
			Integer cap2Cluster = clusters.get(cap2);
			
			if(cap1Cluster == cap2Cluster)
			{
				if(cap1Cluster == null && cap2Cluster == null)	//neither capstone is in a cluster yet
				{
					//create new cluster
					++clusterCount;
					clusters.put(cap1, clusterCount);
					clusters.put(cap2, clusterCount);
				}else{
					//do nothing, most similar pair of capstones are already in same cluster
				}
			}else{	//cap1 and cap2 are in different clusters
				if(cap1Cluster == null)
				{
					//assign cap1 to cap2's cluster
					clusters.put(cap1, cap2Cluster);
				}else if(cap2Cluster == null)
				{
					//assign cap2 to cap1's cluster
					clusters.put(cap2, cap1Cluster);
				}else{
					//merge cap1 and cap2's clusters into a new cluster
					++clusterCount;
					for(Integer capID : clusters.keySet())
					{
						Integer capCluster = clusters.get(capID);
						if(capCluster == cap1Cluster || capCluster == cap2Cluster)
						{
							clusters.put(capID, clusterCount);
						}
					}
				}
			}
		}
		while(!pq.isEmpty())
		{
			//create clusters for leftover capstones
			SchemaAlignment sa = pq.poll();
			Integer cap1 = sa.capstone1.id;
			Integer cap2 = sa.capstone2.id;
			if(clusters.get(cap1) == null)
			{
				clusters.put(cap1, ++clusterCount);
			}
			if(clusters.get(cap2) == null)
			{
				clusters.put(cap2, ++clusterCount);
			}
		}
		
		//convert capstoneID -> clusterID hashtable into clusterID -> List<capstoneID> hashtable
		Hashtable<Integer, List<Integer>> reverseClusters = new Hashtable<Integer, List<Integer>>();
		for(Integer capstoneID : clusters.keySet())
		{
			Integer clusterID = clusters.get(capstoneID);
			List<Integer> capstoneIDs = reverseClusters.get(clusterID);
			if(capstoneIDs == null)
			{
				capstoneIDs = new ArrayList<Integer>();
			}
			capstoneIDs.add(capstoneID);
			reverseClusters.put(clusterID, capstoneIDs);
		}
		
		//output clusters
		for(Integer clusterID : reverseClusters.keySet())
		{
			System.out.print("Cluster:" + clusterID + " = {");
			for(Integer capstoneID : reverseClusters.get(clusterID))
			{
				System.out.print(capstoneID + ",");
			}
			System.out.println("}");
		}
		
		
		
	}
	
	
	public static void outputSchemaAlignments(File out, List<SchemaAlignment> schemaAlignments) throws IOException
	{
		System.out.println();
		System.out.println("Schema Alignment SimScores:");
		FileWriter writer = new FileWriter(out);
		
		
		//calculate mean
		double simScoreSum = 0;
		for(SchemaAlignment sa : schemaAlignments)
		{
			System.out.println(sa.simScore);
			writer.write(sa.capstone1.id + "," + sa.capstone2.id + "," + Double.toString(sa.simScore) + "\n");
			simScoreSum += sa.simScore;
		}
		
		writer.flush();
		writer.close();
		//System.out.println("mean simScore = " + simScoreSum / (double) schemaAlignments.size());
	}
	
	
	public static ConsolidatedConcept consolidateSchema(List<Concept> schema, List<Concept> entities)
	{
		//MACVectorCreator.concepts = entities;
		
		ConsolidatedConcept consolidatedConcept = new ConsolidatedConcept();
		for(Concept role : schema)
		{
			consolidatedConcept.roles.add(role);
		}
		consolidatedConcept.initializeMACVector();	//gets called late so consolidatedConcept has all its roles filled
		return consolidatedConcept;
	}
	
	
	
	//schemas must keep track of the original objects the came from
	//-- keep mapping b/w schemaNodes and individual objects they correspond??
	//--or, keep mapping b/w capstoneConcept & all the particular objects that are gave rise to the capstone?
	
	//TODO fill schema's empty roles with TOKENS!
	public static List<Concept> createSchema(DirectedGraphAdapter<Concept> mapping)
	{
		//TODO if source and target (parent and child)'s roles are crossmapped, resulting schema node should lose its type

		//create schema and add to conceptGraph
		List<Concept> schema = new ArrayList<Concept>();  
		List<WeightedEdgeAdapter<Concept>> edges = mapping.getEdges();
		for(WeightedEdgeAdapter<Concept> edge : edges)
		{
			//TODO if source and target (parent and child)'s roles are crossmapped, resulting schema node should be untyped?  No, keep the type
			Concept c;
			if(edge.parent.type == edge.child.type)
			{
				//Capstones should not be part of the schema itself
				//create new entity of that type
				c = new GenericConcept(edge.parent.type);
			
			}else{
				//create new untyped entity
				c = new UntypedConcept();
			}
			edge.schemaNode = c;
		}
		
		//link schemaNodes if corresponding nodes in both source and target are linked 
		for(WeightedEdgeAdapter<Concept> edge1 : edges)
		{
			edge1.schemaNode.roles = new ArrayList<Concept>();
			
			//add nulls as slot holders in schemaNode's roles
			//TODO do these need to be created at all?  No! add TOKENS instead
			//Note: schemas now have nulls in them that must be checked for everywhere
			int rolesSize = Math.min(edge1.parent.roles.size(), edge1.child.roles.size());
			for(int i = 0; i < rolesSize; ++i)
			{
				edge1.schemaNode.roles.add(new AnonymousToken());
			}
			
			
			for(WeightedEdgeAdapter<Concept> edge2 : edges)
			{
				//roles
				if(edge1.parent.roles.contains(edge2.parent) && edge1.child.roles.contains(edge2.child))
				{
					int index1 = edge1.parent.roles.indexOf(edge2.parent);
					int index2 = edge1.child.roles.indexOf(edge2.child);
					if(index1 == index2)
					{
						edge1.schemaNode.roles.set(index1, edge2.schemaNode);
						//edge1.schemaNode.roles.add(edge2.schemaNode);
					}else{	//TODO what to do if source and target role index don't align?  default to source's index? and schemaNode loses type?
						edge1.schemaNode.roles.set(index1, edge2.schemaNode);
						//make schemaNode lose type
						UntypedConcept utc = new UntypedConcept();
						utc.roles = edge1.schemaNode.roles;
						utc.relations = edge1.schemaNode.relations;  //should be unnecessary as relations are added below
						edge1.schemaNode = utc;
						
					}
				}
				
				//relations
				if(edge1.parent.relations.contains(edge2.parent) && edge1.child.relations.contains(edge2.child))
				{
					edge1.schemaNode.relations.add(edge2.schemaNode);
				}
						
			}
		}
		
		for(WeightedEdgeAdapter<Concept> edge : edges)
		{
			edge.schemaNode.initializeMACVector();	//schema nodes MACVectors get initialized late because they need to have all their roles filled
			schema.add(edge.schemaNode);
		}
		return schema;
		
	}
	
}
