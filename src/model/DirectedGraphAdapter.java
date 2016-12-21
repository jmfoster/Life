package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.*;

public class DirectedGraphAdapter<T> implements Serializable {
	DefaultDirectedWeightedGraph<T, DefaultWeightedEdge> g;
	 /**
     * 
     *
     * @return a graph based on Ts.
     */
    private DefaultDirectedWeightedGraph<T, DefaultWeightedEdge> createDirectedGraph()
    {
    	DefaultDirectedWeightedGraph<T, DefaultWeightedEdge> g = new DefaultDirectedWeightedGraph<T, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        return g;
    }
    
    public DirectedGraphAdapter()
    {
    	g = createDirectedGraph();
    }
    
    /* TODO
    private DirectedGraphAdapter(Subgraph<T, DefaultWeightedEdge, DirectedGraph<T, DefaultWeightedEdge>> g)
    {
    	this.g = (DirectedGraph) g;
    }
    */
    
    public Set<T> getVertices()
    {
    	return g.vertexSet();
    }
    
    public List<T> getParents()
    {
    	List<T> parents = new ArrayList<T>();
    	Set<DefaultWeightedEdge> edges = g.edgeSet();
    	for(DefaultWeightedEdge e : edges)
    	{
    		parents.add(g.getEdgeSource(e));
    	}
    	return parents;
    }
    
    public List<T> getChildren()
    {
    	List<T> children = new ArrayList<T>();
    	Set<DefaultWeightedEdge> edges = g.edgeSet();
    	for(DefaultWeightedEdge e : edges)
    	{
    		children.add(g.getEdgeTarget(e));
    	}
    	return children;
    }
    	
    public List<WeightedEdgeAdapter<T>> getEdges()
    {
    	List<WeightedEdgeAdapter<T>> edges = new ArrayList<WeightedEdgeAdapter<T>>();
    	for(DefaultWeightedEdge e : g.edgeSet())
    	{
    		edges.add(new WeightedEdgeAdapter(g.getEdgeSource(e), g.getEdgeTarget(e)));
    	}
    	return edges;
    }
    
    public List<T> getChildrenOf(T o)
    {
    	List<T> children = new ArrayList<T>();
    	
    	Set<DefaultWeightedEdge> edges = g.outgoingEdgesOf(o);
    	for(DefaultWeightedEdge e : edges)
    	{
    		T target = g.getEdgeTarget(e);
    		children.add(target);
    	}
    	return children;
    }
    
    public List<T> getParentsOf(T o)
    {
    	List<T> parents = new ArrayList<T>();
    	Set<DefaultWeightedEdge> edges = g.incomingEdgesOf(o);
    	for(DefaultWeightedEdge e : edges)
    	{
    		T source = g.getEdgeSource(e);
    		parents.add(source);
    	}
    	return parents;
    }
    
    public List<T> getNeighborsOf(T o)
    {
    	List<T> neighbors = new ArrayList<T>();
    	Set<DefaultWeightedEdge> outgoingEdges = g.outgoingEdgesOf(o);
    	Set<DefaultWeightedEdge> incomingEdges = g.incomingEdgesOf(o);
    	for(DefaultWeightedEdge e : outgoingEdges)
    	{
    		T neighbor = g.getEdgeTarget(e);
    		neighbors.add(neighbor);
    	}
    	for(DefaultWeightedEdge e : incomingEdges)
    	{
    		T neighbor = g.getEdgeSource(e);
    		neighbors.add(neighbor);
    	}
    	return neighbors;
    }
    
    //preserves T links
    @Override
	public DirectedGraphAdapter<T> clone()
    {
    	DirectedGraphAdapter<T> clone = new DirectedGraphAdapter<T>();
    	Set<T> vertices = g.vertexSet();
    	Set<DefaultWeightedEdge> edges = g.edgeSet();
    	for(T v : vertices)
    	{
    		clone.addVertex(v);
    	}
    	for(DefaultWeightedEdge e : edges)
    	{
    		T o1 = g.getEdgeSource(e);
    		T o2 = g.getEdgeTarget(e);
    		clone.addEdge(o1, o2);
    	}
    	return clone;
    }
    
    /***
     * Doesn't allow repeat vertexes
     * @param o
     */
    public void addVertex(T o)
    {
    	g.addVertex(o);
    }
    
    public void addEdge(T o1, T o2)
    {
    	g.addEdge(o1, o2);
    }
    
    
    
    public void addWeightedEdge(T o1, T o2, double weight)
    {
    	DefaultWeightedEdge edge = new DefaultWeightedEdge();
    	g.addEdge(o1, o2, edge);
    	g.setEdgeWeight(edge, weight);    	
    }
    
    public void setEdgeWeight(T o1, T o2, double weight)
    {
    	DefaultWeightedEdge edge = g.getEdge(o1, o2);
    	g.setEdgeWeight(edge, weight);
    }
    
    public double getEdgeWeight(T o1, T o2)
    {
    	double weight;
    	DefaultWeightedEdge edge = g.getEdge(o1, o2);
    	if(edge == null)
    	{
    		//System.err.println("edge is null");
    		weight = 0;
    	}else{
    		weight = g.getEdgeWeight(edge);
    	}
    	//System.out.println(weight);
    	return weight;
    	
    }
    
    /**
     * also removes all edges that touch the removed vertex
     * @param o
     */
    public void removeVertex(T o)
    {
    	g.removeVertex(o);
    }
    
    public void removeEdge(T o1, T o2)
    {
    	g.removeEdge(o1, o2);
    }
    
    public List<Set<T>> getPartitions()
    {
    	ConnectivityInspector<T, DefaultWeightedEdge> ci = new ConnectivityInspector<T, DefaultWeightedEdge>(g);
    	return ci.connectedSets();
    	
    }
    
   
    /* TODO
    public DirectedGraphAdapter getSubgraph(Set<T> vertices)
    {
    	Subgraph<T, DefaultWeightedEdge, DirectedGraph<T, DefaultWeightedEdge>> sg 
    		= new Subgraph<T, DefaultWeightedEdge, DirectedGraph<T, DefaultWeightedEdge>>(g, vertices);
    	DirectedGraphAdapter ga = new DirectedGraphAdapter(sg);
    	return ga;
    }
    
    */
    
    /*
    //TODO
    public void display()
    {
    	JGraphDisplay<T> jGraphDisplay= new JGraphDisplay<T>();
    	jGraphDisplay.main(g);
    	
    	System.out.println();
    	System.out.println("GRAPH: Vertices");    	
    	Set<T> vertices = g.vertexSet();
    	for(T v : vertices)
    	{
    		System.out.print(v);
    		//System.out.print(" :: born:" + v.getBirth() + " died:" + v.getDeath());
    		//System.out.println(" :: " + v.subObjectsToString());
    		
    	}
    
    	System.out.println();
    	System.out.println("GRAPH: Edges");
    	Set<DefaultWeightedEdge> edges = g.edgeSet();
    	for(DefaultWeightedEdge e : edges)
    	{
    		System.out.println(e);
    	}
    	
    }
    */
}
