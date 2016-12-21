package model;

import engine.Concept;
import engine.DimensionNameSet;
import engine.MACVector;
import game.Cell;

import java.util.ArrayList;
import java.util.List;

public class CellSpaceRelation extends Concept {
	private List<Cell> cells;
	
	
	public CellSpaceRelation(Cell c1, Cell c2)
	{
		cells = new ArrayList<Cell>();
		cells.add(c1);
		cells.add(c2);
		
		this.roles.add(c1);
		this.roles.add(c2);
		
		initializeMACVector();
		
		
		c1.macVector.incrementDimension(DimensionNameSet.ROLE_CELL_NEIGHBOR);
		c2.macVector.incrementDimension(DimensionNameSet.ROLE_CELL_NEIGHBOR);
		
		
	}
	
	@Override
	protected void setType()
	{
		type = 1;
	}
	
	@Override
	protected void initializeMACVector()
	{
		this.macVector.setDimension(DimensionNameSet.TYPE_CELL_SPACE_RELATION, 1);
	}
	
	public List<Cell> getCells()
	{
		return cells;
	}
	
	@Override
	public String toString()
	{
		String s = "{";
		for(Cell c : cells)
		{
			s += c + ",";
		}
		s += "}";
		return s;
	}
}
