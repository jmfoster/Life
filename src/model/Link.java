package model;

import java.util.LinkedList;
import java.util.List;

public class Link {
	private List<Object> vertices;
	
	
	public Link()
	{
		vertices = new LinkedList<Object>();
	}
	
	public void addVertex(Object o)
	{
		//no check on uniqueness
		vertices.add(o);
		
	}
	

	
	public Link mergeWith(Link link)	//adds link's vertices to this object's vertices
	{
		for(Object o : link.vertices)
		{
			if(!this.vertices.contains(o))
			{
				this.vertices.add(o);
			}
		}
		return this;
	}
	
	public boolean verticesOverlapWith(Link link)
	{
		for(Object o : this.vertices){
			if(link.containsVertex(o))
			{
				return true;
			}
		}
		return false;
		
	}
	
	public boolean containsVertex(Object o)
	{
		return vertices.contains(o);
	}
	
	public List<Object> getVertices()
	{
		return vertices;
	}
	
	@Override
	public String toString()
	{
		String s = "{ ";
		for(Object o : vertices)
		{
			s += "objectID:" + o.id + " ";
		}
		s += "}";
		return s;
	}
	
}
