package model;

import engine.Concept;
import engine.DimensionNameSet;
import engine.MACVector;
import game.Cell;

public class CellTimeRelation extends Concept {
	private Cell source;
	private Cell target;
	
	public CellTimeRelation(Cell source, Cell target)
	{
		this.source = source;
		this.target = target;
		
		this.roles.add(source);
		this.roles.add(target);
		
		initializeMACVector();
		
		
		source.macVector.incrementDimension(DimensionNameSet.ROLE_CELL_SOURCE);
		target.macVector.incrementDimension(DimensionNameSet.ROLE_CELL_TARGET);
	}
	
	@Override
	protected void setType()
	{
		type = 2;
	}
	
	@Override
	protected void initializeMACVector()
	{	
		this.macVector.setDimension(DimensionNameSet.TYPE_CELL_TIME_RELATION, 1);
	}
	
	public Cell getSource()
	{
		return source;
	}
	
	public Cell getTarget()
	{
		return target;
	}
	
	@Override
	public String toString()
	{
		String s = "(";
		s += source + " -> " + target;
		return s;
	}
	
}
