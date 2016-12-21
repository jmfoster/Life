package model;

import engine.Concept;
import engine.DimensionNameSet;
import engine.MACVector;
import game.Cell;

public class ObjectMembershipRelation extends Concept {
	private Object container;
	private Cell member;
	
	public ObjectMembershipRelation(Object container, Cell member)
	{
		this.container = container;
		this.member = member;
		
		this.roles.add(container);
		this.roles.add(member);
		
		initializeMACVector();
		
		
		container.macVector.incrementDimension(DimensionNameSet.ROLE_CONTAINER);
		member.macVector.incrementDimension(DimensionNameSet.ROLE_MEMBER);
	}
	
	@Override
	protected void setType()
	{
		type = 4;
	}
	
	@Override
	protected void initializeMACVector()
	{
		this.macVector.setDimension(DimensionNameSet.TYPE_OBJECT_MEMBERSHIP_RELATION, 1);
	}
	
	public Object getContainer()
	{
		return container;
	}
	
	public Cell getMember()
	{
		return member;
	}
	
	@Override
	public String toString()
	{
		String s = "(";
		s += container + "->" + member + ")";
		return s;
	}
}
