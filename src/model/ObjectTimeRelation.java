package model;

import engine.Concept;
import engine.DimensionNameSet;
import engine.MACVector;

public class ObjectTimeRelation extends Concept {
	private Object parent;
	private Object child;
	
	public ObjectTimeRelation(Object parent, Object child)
	{
		this.parent = parent;
		this.child = child;
		
		this.roles.add(parent);
		this.roles.add(child);
		
		initializeMACVector();
		
		
		parent.macVector.incrementDimension(DimensionNameSet.ROLE_5_0);
		child.macVector.incrementDimension(DimensionNameSet.ROLE_5_1);
	}
	
	@Override
	protected void setType()
	{
		type = 5;
	}
	
	@Override
	protected void initializeMACVector()
	{
		this.macVector.setDimension(DimensionNameSet.TYPE_5, 1);
		this.macVector.setDimension(DimensionNameSet.HASCHILD_TYPE_3, 2);
	}
	
	public Object getParent()
	{
		return parent;
	}
	
	public Object getChild()
	{
		return child;
	}
	
	/*
	public String toString()
	{
		String s = "OTR(";
		s += parent.id + "->" + child.id + ")";
		return s;
	}
	*/
}
