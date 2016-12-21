package game;

import java.util.ArrayList;
import java.util.List;

import engine.Concept;
import engine.DimensionNameSet;
import engine.MACVector;

import model.CellTimeRelation;
import model.CellSpaceRelation;
import model.Model;
import model.ObjectMembershipRelation;


public class Cell extends Concept {
	public Integer state;
	public Integer i;
	public Integer j;
	public Integer k;	//k is iteration (time)
	public boolean partOfBase = false;
	public boolean partOfTarget = false;
	public double facScore = 0;
	public List<Concept> capstoneList = new ArrayList<Concept>();
	private model.Object containingObject;	//
	private List<CellTimeRelation> cellTimeRelations = new ArrayList<CellTimeRelation>();
	private List<CellSpaceRelation> cellSpaceRelations = new ArrayList<CellSpaceRelation>();
	private List<ObjectMembershipRelation> objectMembershipRelations = new ArrayList<ObjectMembershipRelation>();
	
	@Override
	public boolean equals(Object o)
	{
		if(this == o)
		{
			return true;
		}else if(o instanceof Cell)
		{
			Cell c = (Cell) o;
			return c.i == i && c.j == j && c.state == state && c.k == k;
		}else{
			return false;
		}
	}
	
	public void addObjectMembershipRelation(ObjectMembershipRelation omr)
	{
		if(Model.includeCells)
		{
			objectMembershipRelations.add(omr);
			this.relations.add(omr);
		}
	}
	
	public List<ObjectMembershipRelation> getObjectMembershipRelations()
	{
		return objectMembershipRelations;
	}
	
	public void addCellTimeRelation(CellTimeRelation ctr)
	{
		if(Model.includeCells)
		{
			cellTimeRelations.add(ctr);
			this.relations.add(ctr);

		}
	}
	
	public List<CellTimeRelation> getCellTimeRelations()
	{
		return cellTimeRelations;
	}
	
	public void addCellSpaceRelation(CellSpaceRelation csr)
	{
		if(Model.includeCells)
		{
			cellSpaceRelations.add(csr);
			this.relations.add(csr);
		}
	}
	
	public List<CellSpaceRelation> getCellSpaceRelations()
	{
		return cellSpaceRelations;
	}
	
	public model.Object getContainingObject()
	{
		return containingObject;
	}
	
	public void setContainingObject(model.Object co)
	{
		this.containingObject = co;
	}
	
	
	public Cell(Integer i, Integer j, Integer state, Integer iteration)
	{
		this.i = i;
		this.j = j;
		this.state = state;
		this.k = iteration;
		
		initializeMACVector();	
	}
	
	@Override
	protected void setType()
	{
		this.type = 0;
	}
	
	@Override
	protected void initializeMACVector()
	{
		this.macVector.setDimension(DimensionNameSet.TYPE_CELL, 1);
		if(this.state == 0)
		{
			this.macVector.setDimension(DimensionNameSet.OFF, 1);
		}else if(this.state == 1){
			this.macVector.setDimension(DimensionNameSet.ON, 1);
		}
		
	}
	
	@Override
	public String toString()
	{
		return "(" + i + "," + j + "," + k + "):" + state;
	}
}
