package engine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import model.DirectedGraphAdapter;



public class AnalogyEngine implements Runnable {
	//parameters
	static double weightMACSimilarity = 0;
	static double minimumExcitation = 0;
	static double alpha = .1;
	static double beta = .1;
	static int activeSetSizeLimit = 15;
	static double maxIterations = 500;	//TODO hack to force mapping to converge
	
	public static File fileOut = new File("/Developer/analogy/data/analogyMappingDynamics.txt");
	static BufferedWriter writer;	
	static boolean mappingSettled;
	
	static boolean outputMappingDynamics = true;
	
	
	private DirectedGraphAdapter<Concept> mapping;
	
	public AnalogyEngine(DirectedGraphAdapter<Concept> mapping)
	{
		this.mapping = mapping;
	}
	
	
	public void run()
	{
		//TODO parallelize analogy engine for computational efficiency
	
	}
	
	
	public static DirectedGraphAdapter<Concept> map(List<Concept[]> seedPairs, List<Concept> entities) throws Exception	
	{
		if(outputMappingDynamics)
		{
			writer = new BufferedWriter(new FileWriter(fileOut));
		}
		
		
		System.out.println();
		//System.out.println("ANALOGY ENGINE OUTPUT");		
		
		//print out seedPairs
		for(int i = 0; i < seedPairs.size(); ++i)
		{
			Concept cA = seedPairs.get(i)[0];
			Concept cB = seedPairs.get(i)[1];
			System.out.println("seedA_" + i + " =" + cA);
			System.out.println("seedB_" + i + " =" + cB);
			
			if(outputMappingDynamics)
			{
				writer.append("seedA_" + i + " =" + cA + " : " + cA.macVector);
				writer.append("seedB_" + i + " =" + cB + " : " + cB.macVector);
			}
		}
		
		
		DirectedGraphAdapter<Concept> map1 = new DirectedGraphAdapter<Concept>();		
		
		List<Concept> concepts = entities;	//assumes concepts aren't added/deleted during mapping
		for(Concept c : concepts)
		{
			map1.addVertex(c);
		}
		
		List<Concept> activeSet = new ArrayList<Concept>();
		
		for(Concept[] seedPair : seedPairs)
		{
			//set initial seedPairs' edgeweight to 1
			Concept cA = seedPair[0];
			Concept cB = seedPair[1];
			System.out.println("seedPair = " + cA + " " + cB);
			if(!map1.getVertices().contains(cA))
			{
				System.err.println("cA not in map1");
			}
			if(!map1.getVertices().contains(cB))
			{
				System.err.println("cB not in map1");
				//temporary
				//entities.add(cB);
				//map1.addVertex(cB);
			}
			map1.addWeightedEdge(cA, cB, 1);
			
			//add initial seedPairs to activeSet
			activeSet.add(cA);
			activeSet.add(cB);
		}
			
		
		System.out.print("Mapping Iteration 0...");
		int iteration = 0;
		mappingSettled = false;
		while(!mappingSettled)
		{
			//System.out.print("Mapping Iteration " + iteration + " :: size = " + activeSet.size());
			
			if(outputMappingDynamics)
			{
				writer.newLine();
				writer.append("Mapping Iteration " + iteration);
				writer.newLine();
			}
			
			++iteration;
			
			if(iteration >= maxIterations)
			{
				//TODO infrequent nonconvergence... example: slight activation of a type 3 pair, where type 5 driver includes type 3 driver but corresponding type 5 recipient is null 
				//throw new Exception("max iterations reached");
				System.err.println("max iterations reached; no convergence");
				break;
			}
			
			
			//create and populate intermediate maps
			//TODO create a single graph of new object type that can hold rawEvidence, filteredEvidence, and mapweight values
			DirectedGraphAdapter<Concept> rawEvidenceMap = new DirectedGraphAdapter<Concept>();
			DirectedGraphAdapter<Concept> filteredEvidenceMap = new DirectedGraphAdapter<Concept>();
			//map for next iteration
			DirectedGraphAdapter<Concept> map2 = new DirectedGraphAdapter<Concept>();
			
			for(Concept c : concepts)	//TODO could just add neighborhood and neighbors of neighborhood for efficiency
			{
				rawEvidenceMap.addVertex(c);
				filteredEvidenceMap.addVertex(c);
				map2.addVertex(c);
			}
			
			//System.out.println("ACTIVE SET");
			List<Concept> neighborhood = new ArrayList<Concept>();
			for(Concept a : activeSet)
			{
				//System.out.println(a);
				//add activeSet
				if(!neighborhood.contains(a) && a != null)
				{
					neighborhood.add(a);
				}
				//add roles
				for(Concept r : a.roles)
				{
					if(!neighborhood.contains(r) && r != null)
					{
						neighborhood.add(r);
					}
				}
				//add relations
				for(Concept r : a.relations){
					if(!neighborhood.contains(r) && r != null)
					{
						neighborhood.add(r);
					}
				}
			}
			
			//System.out.println("activeSet size = " + activeSet.size());		
			//System.out.println("neighborhood size = " + neighborhood.size());
			
			if(outputMappingDynamics)
			{
				writer.append("activeSet size = " + activeSet.size());
				writer.newLine();
				writer.append("neighborhood size = " + neighborhood.size());
				writer.newLine();
			}
			
			//System.out.println("findingRawEvidence");
			findRawEvidence(neighborhood, map1, rawEvidenceMap);
			
			//System.out.println("filteringRawEvidence");
			filterRawEvidence(neighborhood, rawEvidenceMap, filteredEvidenceMap);
			
			//identify new activeSet based on deltaM
			
			//System.out.println("updatingMappingWeights");
			activeSet = updateMapWeights(neighborhood, activeSet, map1, filteredEvidenceMap, map2);
			
			map1 = map2;	//replace map1 with next iteration (map2) and loop 
		
		}
		System.out.print(iteration + " ");
		
		if(outputMappingDynamics)
		{
			writer.append("DONE");
			writer.newLine();
			writer.flush();
		}
		System.out.println("DONE");
	
		//System.out.println("seed1 = " + c1);
		//System.out.println("seed2 = " + c2);
	
		if(outputMappingDynamics)
		{
			writer.close();
		}
		
		//build analogy out of activeSet from map1
		DirectedGraphAdapter<Concept> mapping = new DirectedGraphAdapter<Concept>();
		for(Concept c : activeSet)
		{
			//TODO should only mapping weights of 1 be included in final analogy?
			mapping.addVertex(c);
		}
		for(Concept source : activeSet)
		{
			for(Concept target : activeSet)
			{
				if(map1.getEdgeWeight(source, target) == 1)
				{
					mapping.addWeightedEdge(source, target, 1);
				}
			}
		}
		return mapping;
		
	}
	
	
	private static void findRawEvidence(List<Concept> neighborhood, DirectedGraphAdapter<Concept> map1, DirectedGraphAdapter<Concept> rawEvidenceMap) throws Exception
	{
		for(Concept i : neighborhood)
		{
			for(Concept j : neighborhood)
			{
				//System.out.println("mapping nodes i: " + i + "  and j: " + j);
				
				double Rij = weightMACSimilarity * i.macVector.computeSimilarity(j.macVector);	//raw evidence for mapping weight between i and j starts off with cosine similarity
				double sim = 0;
				double mapWeight = 0;
				double n = Math.sqrt(i.roles.size() * j.roles.size());
				//excitation from ROLES
				for(int k = 0; k < i.roles.size(); ++k)
				{
					for(int l = 0; l < j.roles.size(); ++l)
					{
						sim = getExcitation(i.type, j.type, k, l, n, minimumExcitation);	//get similarity from excitation matrixes 
						mapWeight = map1.getEdgeWeight(i.roles.get(k), j.roles.get(l));
						//System.out.println("roleMapWeight = " + mapWeight);
						//System.out.println("sim = " + sim);
						Rij += sim * mapWeight;
					}
				}
				
				//excitation from RELATIONS
				for(Concept k : i.relations)
				{
					for(Concept l : j.relations)
					{
						int iRole = -1;	//roleNumber of i in relation k
						int jRole = -1;	//roleNumber of j in relation l
						//find what role i plays in k
						for(int role = 0; role < k.roles.size(); ++role)
						{
							if(i.equals(k.roles.get(role)))	//if i plays this role# in k
							{
								iRole = role;
							}
						}
						//find what role j plays in l
						for(int role = 0; role < l.roles.size(); ++role)
						{
							if(j.equals(l.roles.get(role)))	//if j plays this role# in l
							{
								jRole = role;
							}
						}
						
						n = Math.sqrt(k.roles.size() * l.roles.size());
						sim = getExcitation(k.type, l.type, iRole, jRole, n, minimumExcitation);
						//System.out.println("sim = " + sim);
						mapWeight = map1.getEdgeWeight(k, l);
						//System.out.println("edgeWeight = " + mapWeight);
						Rij += sim * mapWeight;
					}
				}

				/*
				System.out.println();
				System.out.println(i);
				System.out.println(j);
				System.out.println("n = " + n);
				System.out.println("sim = " + sim);
				System.out.println("mapWeight = " + mapWeight);
				System.out.println("Rij = " + Rij);
				*/
				//accumulate raw evidence
				rawEvidenceMap.addWeightedEdge(i, j, Rij);	
			}
		}
	}

	private static void filterRawEvidence(List<Concept> neighborhood, DirectedGraphAdapter<Concept> rawEvidenceMap, DirectedGraphAdapter<Concept> filteredEvidenceMap)
	{
		//filter raw evidence by taking the max
		for(Concept c : neighborhood)
		{
			
			boolean winner = false;
			List<Concept> dominantLs = getDominantOutgoingLinks(c, rawEvidenceMap);
			innerloop:
				for(Concept dominantL : dominantLs)
				{
					//if dominantL already has a filtered mapping weight coming into it, don't allow another between c and L
					List<Concept> dominantKs = getDominantIncomingLinks(dominantL, rawEvidenceMap);
					for(Concept dominantK : dominantKs)
					{
						if(filteredEvidenceMap.getEdgeWeight(dominantK, dominantL) > 0)
						{
							//don't allow another filtered mapping weight into dominantL
							break innerloop;
						}
					}
					
					if(dominantKs.contains(c))
					{
						//only allow one weight to win this competition
						if(!winner)
						{
							filteredEvidenceMap.addWeightedEdge(c, dominantL, rawEvidenceMap.getEdgeWeight(c, dominantL));
							winner = true;
							break innerloop;	//redundant to break ties, but improves efficiency
						}
					}
				}
		}
		
		
	}
	
	private static List<Concept> getDominantOutgoingLinks(Concept c, DirectedGraphAdapter<Concept> rawEvidenceMap)
	{
		List<Concept> dominantLs = new ArrayList<Concept>();
		List<Concept> outgoingLinks = rawEvidenceMap.getChildrenOf(c);
		double maxWeight = 0;
		for(Concept l : outgoingLinks)
		{
			double weight = rawEvidenceMap.getEdgeWeight(c, l);
			if(weight > maxWeight)
			{
				maxWeight = weight;
			}
		}
		
		for(Concept l : outgoingLinks)
		{
			double weight = rawEvidenceMap.getEdgeWeight(c, l);
			if(weight == maxWeight)
			{
				dominantLs.add(l);
			}
		}
		return dominantLs;
	}
	
	private static List<Concept> getDominantIncomingLinks(Concept l, DirectedGraphAdapter<Concept> rawEvidenceMap)
	{
		List<Concept> dominantKs = new ArrayList<Concept>();
		List<Concept> incomingLinks = rawEvidenceMap.getParentsOf(l);
		double maxWeight = 0;
		for(Concept k : incomingLinks)
		{
			double weight = rawEvidenceMap.getEdgeWeight(k, l);
			if(weight > maxWeight)
			{
				maxWeight = weight;
			}
		}
		
		for(Concept k : incomingLinks)
		{
			double weight = rawEvidenceMap.getEdgeWeight(k, l);
			if(weight == maxWeight)
			{
				dominantKs.add(k);
			}
		}
		return dominantKs;
	}
	
	
	private static List<Concept> updateMapWeights(List<Concept> neighborhood, List<Concept> activeSet, DirectedGraphAdapter<Concept> map1, 
			DirectedGraphAdapter<Concept> filteredEvidenceMap, DirectedGraphAdapter<Concept> map2) throws IOException
	{
		
		
		List<ActiveMapping> activeSetCandidates = new ArrayList<ActiveMapping>();
		
		mappingSettled = true;
		
		//System.out.println("NEIGHBORHOOD");
		
		//update mapWeights
		for(Concept i : neighborhood)
		{
			//System.out.println(i);
			for(Concept j : neighborhood)
			{
				double currentMapWeight = map1.getEdgeWeight(i, j);
				double Eij = filteredEvidenceMap.getEdgeWeight(i, j);
				
				
				double maxCompetitionWeight = 0;
				List<Concept> lConcepts = map1.getChildrenOf(i);
				for(Concept l : lConcepts)
				{
					if(!l.equals(j))
					{
						double competitionWeight = map1.getEdgeWeight(i, l);
						if(competitionWeight >= maxCompetitionWeight)
						{
							maxCompetitionWeight = competitionWeight;
						}
					}
				}
				List<Concept> kConcepts = map1.getParentsOf(j);
				for(Concept k : kConcepts)
				{
					if(!k.equals(i)){
						double competitionWeight = map1.getEdgeWeight(k, j);
						if(competitionWeight >= maxCompetitionWeight)
						{
							maxCompetitionWeight = competitionWeight;
						}
					}
				}
				
				Double deltaMapWeight = alpha * Eij - beta * maxCompetitionWeight;
				Double newMapWeight = currentMapWeight + deltaMapWeight;
				if(newMapWeight > 1)
				{
					newMapWeight = 1.0;
				}
				if(newMapWeight < 0)
				{
					newMapWeight = 0.0;
				}						
				
		
				ActiveMapping am = new ActiveMapping();
				am.deltaMapWeight = deltaMapWeight;
				am.newMapWeight = newMapWeight;
				am.Eij = Eij;
				am.maxCompetitionWeight = maxCompetitionWeight;
				am.i = i;
				am.j = j;
				activeSetCandidates.add(am);
			}
		}
		
		//find and update new active set
		Collections.sort(activeSetCandidates);
		
		/*
		System.out.println("activeSetCandidates size= " + activeSetCandidates.size());
		for(ActiveMapping am : activeSetCandidates)
		{
			System.out.println(am.i + " > " + am.j + " M:" + am.newMapWeight + " dM:" + am.deltaMapWeight);
		}
		*/
		activeSet = new ArrayList<Concept>();
		int candidateSize = activeSetCandidates.size();
		int lowerBound = Math.max(candidateSize - activeSetSizeLimit, 0);
		int upperBound = candidateSize;
		//System.out.println("candidateSize = " + candidateSize);
		//System.out.println("lowerBound = " + lowerBound + " upperbound = " + upperBound);
		//for the top activeSetSizeLimit deltaMs, update map weights and add to new active set
		
		//update map weights for neighborhood and update new activeSet
		//System.out.println("MAPPING WEIGHTS");
		int candidateNumber = 0;
		for(int n = 0; n < upperBound; ++n)
		{
			ActiveMapping am = activeSetCandidates.get(n);
			
			if(am.newMapWeight > 0)
			{
				
				//System.out.println("delta m = " + am.deltaMapWeight + " " + am.i + "-->" + am.j);
				//System.out.println(am.i + " --> " + am.j + "   (" + am.newMapWeight + ")");
				
				String s = "";
				if(n >= lowerBound)
				{
					//s += "*";
				}
				//s += candidateNumber++ + " ";
				s += am.i + " > " + am.j;
				s += " {" + "\"M\":" + am.newMapWeight + ", \"dM\":" + am.deltaMapWeight; 
				s += ", \"Eij\":" + am.Eij;
				s += ", \"maxCompetitionWeight\":" + am.maxCompetitionWeight;
				s += "}";
				
				//s += "(" + am.i.relations.size() + "," + am.i.roles.size() + ") (" + am.j.relations.size() + "," + am.j.roles.size() + ")";
				//System.out.println(s);
				if(outputMappingDynamics)
				{
					writer.append(s);
					writer.newLine();
					writer.flush();
				}
			}
			
			//update new activeSet
			if(n >= lowerBound /* &&  am.newMapWeight > 0 */)
			{
				if(am.newMapWeight > 0 && am.newMapWeight < 1.0)
				{
					mappingSettled = false;
				}
				
				map2.addWeightedEdge(am.i, am.j, am.newMapWeight);
				//TODO allow repeats in the activeSet?
				if(!activeSet.contains(am.i))
				{
					activeSet.add(am.i);
				}
				if(!activeSet.contains(am.j))
				{
					activeSet.add(am.j);
				}
			}
			System.out.println("newMapWeight = " + am.newMapWeight);
		}
		
		
		//System.out.println("deltaMapWeight = " + deltaMapWeight);
		//System.out.println("currentMapWeight = " + currentMapWeight);
		
		return activeSet;
	}

	//TODO replace this hardcoded function to use matrices which can be adjusted over time to indicate learning
	private static double getExcitation(int i, int j, int k, int l, double n, double minimumExcitation)
	{
		//i and j are concept types, k and l are role numbers
		if(i == j)	//same concept type
		{
			if(k == l)	//same role	TODO not allowing for symmetric roles, e.g., neighbor role, fix in matrix implementation
			{
				return 1;
			}else{
				return minimumExcitation; 
			}
		}else{	//different concept type
			return 1/n;
		}
	}
	
	
		
	
}

















