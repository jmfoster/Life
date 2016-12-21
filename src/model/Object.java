package model;

import engine.Concept;
import engine.DimensionNameSet;
import engine.MACVector;
import game.Cell;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Object extends Concept {
	private List<Cell> footprint;
	//protected int id;	//id only unique to current World
	private Integer birth;
	private Integer death;
	private List<Object> subObjects = new ArrayList<Object>();
	private List<ObjectTimeRelation> objectTimeRelations = new ArrayList<ObjectTimeRelation>();
	private List<ObjectMembershipRelation> objectMembershipRelations = new ArrayList<ObjectMembershipRelation>();
	
	
	/*
	public String toString()
	{
		String s = "objID:" + id;
		
		//for(Cell c : footprint)
		//{
		//	s += "(" + c.i + "," + c.j + ") ";
		//}
		return s;
	}
	*/
	
	public void addObjectTimeRelation(ObjectTimeRelation otr)
	{
		objectTimeRelations.add(otr);
		this.relations.add(otr);
	}
	
	public List<ObjectTimeRelation> getObjectTimeRelations()
	{
		return objectTimeRelations;
	}
	
	public void addObjectMembershipRelation(ObjectMembershipRelation omr)
	{
		objectMembershipRelations.add(omr);
		if(Model.includeCells)
		{
			this.relations.add(omr);
		}
		
	}
	
	public List<ObjectMembershipRelation> getObjectMembershipRelations()
	{
		return objectMembershipRelations;
	}
	
	public String subObjectsToString()
	{
		String s = "";
		s += "subobjects: ";
		for(Object o : subObjects)
		{
			s += o.id + ",";
		}	
		return s;
	}
	
	public String footprintToString()
	{
		return footprint.toString();
	}
	
	@Override
	public boolean equals(java.lang.Object o)	
	{
		if(o == this)
		{
			return true;
		}else if(o instanceof Object)
		{
			Object obj = (Object) o;
			return obj.id == this.id;
		}else{
			return false;
		}
	}
	
	//@Override 
	//TODO hash
	
	/**
	 * adds object's footprint cells to this object's footprint (non duplicating)
	 * calling function must handle merging of subobjects!
	 */
	public void merge(Object object)
	{
		//merge footprints
		for(Cell c : object.footprint )
		{
			
			if(!this.footprint.contains(c))
			{
				c.setContainingObject(this);
				addCell(c);				
			}
		}
		//set birth to earliest birth
		if(this.birth == null && object.birth == null)
		{
			this.setBirth(null);
		}else if(this.birth != null && object.birth == null)
		{
			//keep birth the same
		}else if(this.birth == null && object.birth != null)
		{
			this.setBirth(object.birth);
		}else{
			this.setBirth(this.birth < object.birth ? this.birth : object.birth);
		}
		
		//set death to latest death
		if(this.death == null && object.death == null)
		{
			this.setDeath(null);
		}else if(this.death != null && object.death == null)
		{
			//keep death the same
		}else if(this.death == null && object.death != null)
		{
			this.setDeath(object.death);
		}else{
			this.setDeath(this.death > object.death ? this.death : object.death);
		}		
		
		this.subObjects.add(object); //do this in specialized merge methods? no, it works here
	}
	
	
	public Object()
	{
		footprint = new ArrayList<Cell>();
		initializeMACVector(); 
	}
	
	@Override
	protected void setType()
	{
		this.type = 3;
	}
	
	@Override
	protected void initializeMACVector()
	{
		this.macVector.setDimension(DimensionNameSet.TYPE_3, 1);
	}
	
	public void addCell(Cell c)
	{
		if(!footprint.contains(c))
		{
			c.setContainingObject(this);
			footprint.add(c);		
			ObjectMembershipRelation omr = new ObjectMembershipRelation(this, c);
			this.addObjectMembershipRelation(omr);			
			c.addObjectMembershipRelation(omr);
			this.macVector.incrementDimension(DimensionNameSet.SIZE);
		}
	}
	
	/*
	public void removeCell(Cell c)
	{
		footprint.remove(c);
	}
	*/
	
	public boolean containsCell(Cell c)
	{
		return footprint.contains(c);
	}
	
	public Integer getSize()
	{
		return footprint.size();
	}
	
	public Iterator<Cell> footprintIterator()
	{
		return footprint.iterator();
	}
	
	/**
	 * ignores time (iteration) and state (on or off), only position
	 * @param o
	 * @return
	 */
	public boolean footprintPositionOverlapsWith(Object o)
	{
		for(Cell c1 : o.footprint)
		{
			for(Cell c2 : this.footprint)
			{
				if(c1.i == c2.i && c1.j == c2.j)
				{
					return true;
				}
			}
		}
		return false;
		
		
	}

	public void setBirth(Integer birth) {
		this.birth = birth;
		updateLifetime();
	}

	public Integer getBirth() {
		return birth;
	}

	public void setDeath(Integer death) {
		this.death = death;
		updateLifetime();
	}

	public Integer getDeath() {
		return death;
	}	
	
	private void updateLifetime()
	{
		if(this.death != null && this.birth != null)
		{
			int lifetime = this.death - this.birth;
			this.macVector.setDimension(DimensionNameSet.LIFETIME, lifetime);
		}
	}
	
	public int getLifetime()
	{
		return this.death - this.birth;
	}
	
}
