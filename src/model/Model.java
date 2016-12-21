package model;

import engine.Concept;
import engine.FACMatch;
import game.Cell;
import game.World;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
	

public class Model {
	
	//parameters
	public static boolean includeCells = false;
	
	
	public static List<Concept> createEntityList(List<World> worldList) throws IOException
	{
		System.out.println("MODEL OUTPUT");
		
		
		//create worldTime 3D array
		int size = worldList.get(0).getSize();	//assume first world's size is same for all worlds
		Integer[][][] worldTime = new Integer[size][size][worldList.size()];
		for(int k = 0; k < worldList.size(); ++k)
		{			
			World world = worldList.get(k);			
			for(int i = 0; i < size; ++i)
			{
				for(int j = 0; j < size; ++j)
				{
					worldTime[i][j][k] = world.getState(i, j);
				}
			}			
		}		
	
		//get cells
		List<Cell> cells = new ArrayList<Cell>();
		for(World w : worldList)
		{
			int worldSize = w.getSize();
			for(int i = 0; i < worldSize; ++i)
			{
				for(int j = 0; j < worldSize; ++j)
				{
					cells.add(w.getCell(i, j));
				}
			}
		}
		
		System.out.println();
		System.out.print("Model Iteration... ");
		
		//findObjects
		List<List<Object>> worldObjectLists = new ArrayList<List<Object>>();
		for(int i = 0; i < worldList.size(); ++i)
		{
			System.out.print(i + " ");
			worldObjectLists.add(findObjects(worldList.get(i), i));
			//System.out.println();
			
		}
		
		//find fullEvolutionGraph
		DirectedGraphAdapter<Object> fullEvolutionGraph = findFullEvolutionGraph(worldObjectLists);
		//fullEvolutionGraph.display();
		//System.out.println();
		
		//find objectEvolutionLinks
		for(int i = 0; i < worldList.size()-1; ++i)
		{
			//System.out.println("ITERATION " + i + "->" + (i+1) + " Object Evolutions");
			List<Set<Object>> partitions = findObjectEvolution(worldObjectLists.get(i), worldObjectLists.get(i+1));
			/*
			for(Set<Object> p : partitions)
			{
				//TODO: output is not sorted correctly.  could sort by objectID but kind of a hack
				System.out.println("objectEvolutionLinks = " + p);
				//DirectedGraphAdapter subgraph = fullEvolutionGraph.getSubgraph(p);
				subgraph.display();
			}		
			*/	
		}
		
		//find condensedEvolutionGraph
		DirectedGraphAdapter<Object> condensedEvolutionGraph = findCondensedEvolutionGraph(fullEvolutionGraph.clone(), worldObjectLists.get(0));
		//condensedEvolutionGraph.display();
		//System.out.println();
		
		List<CellTimeRelation> cellTimeRelations;
		List<CellSpaceRelation> cellSpaceRelations;
		List<ObjectMembershipRelation> objectMembershipRelations;
		if(includeCells)
		{
			//find cellTimeRelations
			cellTimeRelations = findCellTimeRelations(worldList, size);
			System.out.println("CELL TIME RELATIONS");
			/*
			for(CellTimeRelation ctr : cellTimeRelations)
			{
				System.out.println(ctr);
			}
			*/
			//System.out.println();
			
			//find cellSpaceRelations
			cellSpaceRelations = getCellSpaceRelations(worldList);
			System.out.println("CELL SPACE RELATIONS");
			/*
			for(CellSpaceRelation csr : cellSpaceRelations)
			{
				System.out.println(csr);
			}
			
			System.out.println();
			*/

			//get objectMembershipRelations
			objectMembershipRelations = getObjectMembershipRelations(condensedEvolutionGraph);
			System.out.println("OBJECT MEMBERSHIP RELATIONS");
			/*
			for(ObjectMembershipRelation omr : objectMembershipRelations)
			{
				System.out.println(omr);
			}
			System.out.println();
			*/
			
		}else{
			cellTimeRelations = null;
			cellSpaceRelations = null;
			objectMembershipRelations = null;
		}
		
		//find objectTimeRelations
		List<ObjectTimeRelation> objectTimeRelations = findObjectTimeRelations(condensedEvolutionGraph);
		System.out.println("OBJECT TIME RELATIONS");
		/*
		for(ObjectTimeRelation otr : objectTimeRelations)
		{
			System.out.println(otr);
		}
		*/
		System.out.println();
		
		
		//DirectedGraphAdapter<Concept> entityGraph = createEntityGraph(cells, condensedEvolutionGraph, cellTimeRelations, cellSpaceRelations, objectTimeRelations, objectMembershipRelations);
		
		List<Concept> entities = createEntityList(cells, condensedEvolutionGraph, cellTimeRelations, cellSpaceRelations, objectTimeRelations, objectMembershipRelations);
		
		//outputWorldListForVisualization(new File("data/cellEvolution"), worldList, AnalogyEngine.map(MacFac.computeMACSimilarityMatrix(entityGraph.getVertices()), entityGraph));
		//AnalogyEngine.process(entityGraph);
		//MacFac.process(entities);
		return entities;
	}
	
	
	/**map world and output worldList with cells marked
	* 0 == off
	* 1 == on
	* 2 == on and part of base
	* 3 == on and part of target
	* 4 == on and part of base and target
	 * @throws IOException 
	**/
	public static void outputWorldListForVisualization(File outputFile, List<World> worldList, DirectedGraphAdapter<Concept> mapping) throws IOException
	{
		FileWriter writer = new FileWriter(outputFile);
		writer.append(worldList.get(0).getSize().toString() + "\n");
		
		//set Cells to be part of base
		List<Object> baseObjects = breakConceptListIntoObjects(mapping.getParents());
		for(Object bo : baseObjects)	
		{
				Iterator<Cell> iter = bo.footprintIterator();
				while(iter.hasNext())
				{
					Cell c = iter.next();
					c.partOfBase = true;
				}
		}
		
		//set Cells to be part of target
		List<Object> targetObjects = breakConceptListIntoObjects(mapping.getChildren());
		for(Object to : targetObjects)
		{
			Iterator<Cell> iter = to.footprintIterator();
			while(iter.hasNext())
			{
				Cell c = iter.next();
				c.partOfTarget = true;
			}
		}
		
		for(World world : worldList)
		{
			for(int i = 0; i < world.getSize(); ++i)
			{
				for (int j = 0; j < world.getSize(); ++j)
				{
					Cell c = world.getCell(i, j);
					if(c.state == 0)	//cell is off
					{
						writer.append("0 ");
					}else{	//cell is on
						if(c.partOfBase && c.partOfTarget)
						{
							writer.append("4 ");
						}else if(c.partOfBase)
						{
							writer.append("2 ");
						}else if(c.partOfTarget)
						{
							writer.append("3 ");
						}else{	//cell is on but not part of base or target
							writer.append("1 ");
						}
					}
				}
			}
			writer.append("\n");
		}
		writer.flush();
		writer.close();
	}
	
	public static void setFACMatchScoreInCells(Object blo, double facScore)
	{
		//set blo Cells to have facMatch score
		Iterator<Cell> iter = blo.footprintIterator();
		while(iter.hasNext())
		{
			Cell c = iter.next();
			c.facScore = facScore;
		}
	}
	
	public static void outputWorldListforHeatmap(File cellHeatmapFile, List<World> worldList) throws IOException
	{
		FileWriter writer = new FileWriter(cellHeatmapFile);
		writer.append(worldList.get(0).getSize().toString() + "\n");
		
		for(World world : worldList)
		{
			for(int i = 0; i < world.getSize(); ++i)
			{
				for (int j = 0; j < world.getSize(); ++j)
				{
					Cell c = world.getCell(i, j);
					if(c.state == 0)	//cell is off
					{
						writer.append("0 ");
					}else{	//cell is on
						if(c.facScore != 0)	//cell is part of driver
						{
							writer.append(c.facScore + " ");
						}else{	//cell is on but not part of driver
							writer.append("1 ");
						}
					}
				}
			}
			writer.append("\n");
		}
		writer.flush();
		writer.close();
	}
	
	private static List<Object> breakConceptListIntoObjects(List<Concept> concepts)
	{
		List<Object> objects = new ArrayList<Object>();
		Queue<Concept> roles = new LinkedList<Concept>();
		for(Concept c : concepts)
		{
			if(c instanceof Object)
			{
				objects.add((Object) c);
			}else{
				roles.addAll(c.roles);
			}
		}
		
		while(!roles.isEmpty())
		{
			Concept r = roles.poll();
			if(r != null)
			{
				if(r instanceof Object)
				{
					objects.add((Object) r);
				}else{
					roles.addAll(r.roles);
				}
			}
		}
		
		return objects;
	}
	
	private static List<Concept> createEntityList(List<Cell> cells, DirectedGraphAdapter<Object> basicLevelObjectGraph, List<CellTimeRelation> cellTimeRelations, 
			List<CellSpaceRelation> cellSpaceRelations, List<ObjectTimeRelation> objectTimeRelations, 
			List<ObjectMembershipRelation> objectMembershipRelations)
	{
		
		List<Concept> entities = new ArrayList<Concept>();
		for(Object o : basicLevelObjectGraph.getVertices())
		{
			entities.add(o);
		}
		for(ObjectTimeRelation otr : objectTimeRelations)
		{
			entities.add(otr);
		}
		
		if(includeCells)
		{
			for(Cell c : cells)
			{
				entities.add(c);
			}
			for(CellTimeRelation ctr : cellTimeRelations)
			{
				entities.add(ctr);
			}
			for(CellSpaceRelation csr : cellSpaceRelations)
			{
				entities.add(csr);
			}
			for(ObjectMembershipRelation omr : objectMembershipRelations)
			{
				entities.add(omr);
			}
		}
		
		return entities;
	}
	
	private static DirectedGraphAdapter<Concept> createEntityGraph(List<Cell> cells, DirectedGraphAdapter<Object> basicLevelObjectGraph, List<CellTimeRelation> cellTimeRelations, 
			List<CellSpaceRelation> cellSpaceRelations, List<ObjectTimeRelation> objectTimeRelations, 
			List<ObjectMembershipRelation> objectMembershipRelations)
	{
		DirectedGraphAdapter<Concept> entityGraph = new DirectedGraphAdapter<Concept>();
		
		
		Set<Object> basicLevelObjects = basicLevelObjectGraph.getVertices();
		for(Object o : basicLevelObjects)
		{
			entityGraph.addVertex(o);
		}
		
		for(ObjectTimeRelation otr : objectTimeRelations)
		{
			entityGraph.addVertex(otr);
			entityGraph.addEdge(otr, otr.getChild());
			entityGraph.addEdge(otr, otr.getParent());
			entityGraph.addEdge(otr.getChild(), otr);
			entityGraph.addEdge(otr.getParent(), otr);
		}
		
		if(includeCells)
		{
			for(Cell c : cells)
			{
				entityGraph.addVertex(c);
			}
			
			for(CellTimeRelation ctr : cellTimeRelations)
			{
				entityGraph.addVertex(ctr);
				entityGraph.addEdge(ctr, ctr.getSource());
				entityGraph.addEdge(ctr, ctr.getTarget());
				entityGraph.addEdge(ctr.getSource(), ctr);
				entityGraph.addEdge(ctr.getTarget(), ctr);
			}
			
			for(CellSpaceRelation csr : cellSpaceRelations)
			{
				entityGraph.addVertex(csr);
				List<Cell> neighbors = csr.getCells();
				Cell c1 = neighbors.get(0);
				Cell c2 = neighbors.get(1);
				entityGraph.addEdge(csr, c1);
				entityGraph.addEdge(csr, c2);
				entityGraph.addEdge(c1, csr);
				entityGraph.addEdge(c2, csr);
			}
			
			for(ObjectMembershipRelation omr : objectMembershipRelations)
			{
				entityGraph.addVertex(omr);
				entityGraph.addEdge(omr, omr.getContainer());
				entityGraph.addEdge(omr, omr.getMember());
				entityGraph.addEdge(omr.getContainer(), omr);
				entityGraph.addEdge(omr.getMember(), omr);
			}
		}
		
		return entityGraph;
	}
	
	
	private static List<ObjectMembershipRelation> getObjectMembershipRelations(DirectedGraphAdapter<Object> condensedEvolutionGraph)
	{
		List<ObjectMembershipRelation> objectMembershipRelations = new ArrayList<ObjectMembershipRelation>();
		Set<Object> vertices = condensedEvolutionGraph.getVertices();
		for(Object v : vertices)
		{
			List<ObjectMembershipRelation> omrs = v.getObjectMembershipRelations();
			for(ObjectMembershipRelation omr : omrs)
			{
				objectMembershipRelations.add(omr);
			}
		}
		return objectMembershipRelations;
	}
	
	
	private static List<CellSpaceRelation> getCellSpaceRelations(List<World> worldList)
	{
		List<CellSpaceRelation> cellSpaceRelations = new ArrayList<CellSpaceRelation>();
		for(World world : worldList)
		{
			List<CellSpaceRelation> relations = world.getCellSpaceRelations();
			for(CellSpaceRelation csr : relations)
			{
				cellSpaceRelations.add(csr);
			}
		}
		return cellSpaceRelations;
	}
	
	private static List<CellTimeRelation> findCellTimeRelations(List<World> worldList, Integer size)
	{
		ArrayList<CellTimeRelation> cellTimeRelations = new ArrayList<CellTimeRelation>();
		for(int i = 0; i < size; ++i)
		{
			for(int j = 0; j < size; ++j)
			{
				for(int k = 0; k < worldList.size()-1; ++k)
				{
					Cell source = worldList.get(k).getCell(i, j);
					Cell target = worldList.get(k+1).getCell(i, j);
					//if(source.state == 1 && target.state == 1)	//include ALL cells, not just on cells
					//{
						CellTimeRelation cellTimeRelation = new CellTimeRelation(source, target);
						source.addCellTimeRelation(cellTimeRelation);
						target.addCellTimeRelation(cellTimeRelation);
						cellTimeRelations.add(cellTimeRelation);
					//}
				}
			}
		}
		return cellTimeRelations;
		
	}
	
	private static List<ObjectTimeRelation> findObjectTimeRelations(DirectedGraphAdapter<Object> condensedEvolutionGraph)
	{
		List<ObjectTimeRelation> objectTimeRelations = new ArrayList<ObjectTimeRelation>();
		Set<Object> vertices = condensedEvolutionGraph.getVertices();
		for(Object vertex : vertices)
		{
			List<Object> children = condensedEvolutionGraph.getChildrenOf(vertex);
			for(Object child : children)
			{
				ObjectTimeRelation otr = new ObjectTimeRelation(vertex, child);
				vertex.addObjectTimeRelation(otr);
				child.addObjectTimeRelation(otr);
				objectTimeRelations.add(otr);
			}
		}
		return objectTimeRelations;		
	}
	
	
	private static DirectedGraphAdapter<Object> findCondensedEvolutionGraph(DirectedGraphAdapter<Object> ga, List<Object> objects)
	{
		//DirectedGraphAdapter newga = new DirectedGraphAdapter();
		List<Object> newobjects = new ArrayList<Object>();
		
		for(Object o : objects)
		{
			List<Object> children = ga.getChildrenOf(o);			
			List<Object> parents = ga.getParentsOf(o);
			
			
			if(children.size() == 0)	//object dies
			{				
				//birth, death, and incoming links preserved
			}else if(children.size() == 1)
			{
				//check if this child object was created from the merging of two parent objects
				Object child = children.get(0); //get only child
				List<Object> childsParents = ga.getParentsOf(child);
				List<Object> childsChildren = ga.getChildrenOf(child);
				if(childsParents.size() == 1)	//this object lives on b/c 1:1 relationship w/ next generation
				{
					
					//remove this object from ga
					ga.removeVertex(o);
					//remove child from ga
					ga.removeVertex(child);
					
					//merge child and parent while preserving this object's incoming links and child's outgoing links 
					//and preserving parent's birth
					//merging automatically takes earliest birth and latest death of the two objects	
					Object mergedObject;
					if(o.getBirth() == o.getDeath())
					{
						mergedObject = new Object();
						mergedObject.merge(o);
						mergedObject.merge(child);
						mergedObject.setDeath(child.getDeath());
					}else{
						mergedObject = o;
						mergedObject.merge(child);
						mergedObject.setDeath(child.getDeath());
					}
					ga.addVertex(mergedObject);	//add merged object back to ga
					for(Object p : parents)
					{
						ga.addEdge(p, mergedObject);
					}
					for(Object c : childsChildren)
					{
						ga.addEdge(mergedObject, c);
					}										
					
					//recurse on the merged object
					if(!newobjects.contains(mergedObject))
					{
						newobjects.add(mergedObject);
					}					
				}else{	//child created from a merge event, don't merge with o
					if(!newobjects.contains(child))
					{
						newobjects.add(child);
					}					
				}				
			}else{	//this object splits
				//object dies and gives birth to 2 or more objects
				//set births and deaths on child objects (already done b/c all subobjects have their iteration's birth and death)
				for(Object c : children)
				{
					c.setBirth(o.getDeath());
					if(!newobjects.contains(c))
					{
						newobjects.add(c);	//recurse on child objects
					}
					
				}			
			}
			
			
			
		}
		
		if(newobjects.size() > 0)
		{
			return findCondensedEvolutionGraph(ga, newobjects);	//actually do recursion call
		}else{
			return ga;	//finish
		}
		
	}
	
	
	private static DirectedGraphAdapter<Object> findFullEvolutionGraph(List<List<Object>> worldObjectLists)
	{
		DirectedGraphAdapter<Object> ga = new DirectedGraphAdapter<Object>();
	
		//add objects to ga's vertices
		for(int i = 0; i < worldObjectLists.size(); ++i)
		{
			List<Object> wol = worldObjectLists.get(i);
			for(Object o : wol)
			{
				ga.addVertex(o);
			}
		}
		
		for(int i = 0; i < worldObjectLists.size()-1; ++i)
		{
			List<Object> wol1 = worldObjectLists.get(i);
			int j = i+1;
			List<Object> wol2 = worldObjectLists.get(j);
			for(Object o1 : wol1)
			{
				for(Object o2 : wol2)
				{
					if(o1.footprintPositionOverlapsWith(o2))
					{
						ga.addEdge(o1, o2);
					}
				}
			}			
		}
		return ga;
	}
	
	private static List<Set<Object>> findObjectEvolution(List<Object> wol1, List<Object> wol2)
	{
		DirectedGraphAdapter<Object> ga = new DirectedGraphAdapter<Object>();
		//add objects from wol1 and wol2 to ga's vertices
		for(int i = 0; i < wol1.size(); ++i)
		{
			ga.addVertex(wol1.get(i));
		}
		for(int j = 0; j < wol2.size(); ++j)
		{
			ga.addVertex(wol2.get(j));
		}
		
		//add edges to ga based on object footprint overlap between wol1 and wol2
		for(int i = 0; i < wol1.size(); ++i)
		{
			Object o1 = wol1.get(i);		
			for(int j = 0; j < wol2.size(); ++j)
			{
				Object o2 = wol2.get(j);				
				if(o1.footprintPositionOverlapsWith(o2))
				{
					ga.addEdge(o1, o2);
				}
			}
		}
		return ga.getPartitions();		
	}
	
	private static List<Object> findObjects(World world, Integer iteration)
	{
		List<Object> objects = new ArrayList<Object>();
		//world.populateCellList();  world now does this on its own when created
		List<Cell> liveCellList = world.getLiveCellList();

		
		while(!liveCellList.isEmpty())
		{
			Object o = new Object();
			o.setBirth(iteration);
			o.setDeath(iteration);
			Cell seedCell = liveCellList.remove(0);	//could be any cell from liveCellList
			//System.out.println(seedCell);
			Queue<Cell> q = new LinkedList<Cell>();
			q.add(seedCell);
			while(!q.isEmpty())
			{
				Cell cell = q.poll();
				//System.out.println(cell);
				o.addCell(cell);	//add c to this object's footprint
				if(cell.state == 1)	//if cell is live
				{
					liveCellList.remove(cell);	//don't consider c as a seedCell in the future
					List<Cell> neighbors = world.getNeighbors(cell);
					for(Cell c : neighbors)
					{
						if(!q.contains(c) && !o.containsCell(c))
						{
							q.add(c);
							
							if(includeCells)
							{
								//add cell -> c to cellSpaceRelations
								CellSpaceRelation csr = new CellSpaceRelation(cell, c);
								cell.addCellSpaceRelation(csr);
								c.addCellSpaceRelation(csr);
								world.addCellSpaceRelation(csr);
							}
						}
					}
				}
			}
			objects.add(o);
			//System.out.println(o + " " +  o.footprintToString());
		}
		List<Link> links = linkObjectOverlaps(objects);
		for(Link l : links)
		{
			//System.out.println("link = " + l);
		}
		List<Set<Object>> partitions = findObjectPartitions(objects, links);
		List<Object> mergedObjects = new ArrayList<Object>();
		for(Set<Object> p : partitions)
		{
			//System.out.println("partition = " + p);
			Object mergedObject = mergeObjects(p);
			mergedObject.setBirth(iteration);
			mergedObject.setDeath(iteration);
			mergedObjects.add(mergedObject);
		}
		
		for(Object o : mergedObjects)
		{
			//System.out.println("mergedObject = " + o + " :: " + o.subObjectsToString() );
		}		
		return mergedObjects;
		
		
		
	}
	
	//does not include self-links so all objects may not be included in at least one link
	private static List<Link> linkObjectOverlaps(List<Object> objects)
	{
		List<Link> links = new ArrayList<Link>();
		//return mapping between objects
		for(int i = 0; i < objects.size(); ++i)
		{	
			Object o1 = objects.get(i);
			//Link unaryLink = new Link();
			//unaryLink.addVertex(o1);
			//links.add(unaryLink);
			for(int j = i+1; j < objects.size(); ++j)
			{				
				Object o2 = objects.get(j);
				if(o1.footprintPositionOverlapsWith(o2))
				{
					Link binaryLink = new Link();
					binaryLink.addVertex(o1);
					binaryLink.addVertex(o2);
					links.add(binaryLink);					
				}			
			}		
		}
		return links;
	}
	
	
	//note:  only works with binary links!
	private static List<Set<Object>> findObjectPartitions(List<Object> objects, List<Link> links)
	{
		DirectedGraphAdapter<Object> ga = new DirectedGraphAdapter<Object>();
		
		//add objects to ga's vertices
		for(Object o : objects)
		{							
			ga.addVertex(o);				
		}		
		//use binary links to create ga's edges
		for(Link l : links)
		{
			List<Object> v = l.getVertices();
			if(v.size() == 1)
			{
				//TODO what to do with unlinked object?
			}
			if(v.size() == 2)
			{
				ga.addEdge(v.get(0), v.get(1));
			}
		}		
		return ga.getPartitions();
	}
	
	
	private static Object mergeObjects(Set<Object> set)
	{
		
		//merge footprints (union) without duplication
		Object object = new Object();
		for(Object o : set)
		{
			object.merge(o);
		}		
	
		return object;
	
	}
	
}