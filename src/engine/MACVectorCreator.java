package engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * TODO this class is in progress and needs work for predication to work correctly
 * @author james
 *
 */
public class MACVectorCreator {
	
	//param
	//public static List<Concept> concepts;
	
	
	public static void updateMACVector(Concept c)
	{
		//order does not matter!  don't need to keep consistent MACVector dimension order across all concepts
		
		if(c.type < 0)
		{
			//addLegacyMACDimensions(c);
			//do nothing, special case: either CapstoneConcept or UntypedConcept
			
		}else if(c.type <= Concept.typeTracker)
		{
			//existing concept type, fill in MACVector appropriately
			DimensionNameString dnsType = new DimensionNameString("TYPE_" + c.type);
			c.macVector.setDimension(dnsType, 1);
			
			//roles' contribution to c's macvector
			for(Concept role : c.roles)
			{
				if(role != null)
				{
					DimensionNameString dnsRelation = new DimensionNameString("HASCHILD_TYPE_" + role.type);
					c.macVector.incrementDimension(dnsRelation);
				}
			}
			
			//relations' contribution to c's macvector
			for(Concept relation : c.relations)
			{
				int i = relation.roles.indexOf(c);
				DimensionName dnsRole = new DimensionNameString("ROLE_" + c.type + "_" + i);
				c.macVector.incrementDimension(dnsRole); 
			}
			
		}else{
			//newly consolidated concept
			++Concept.typeTracker;
			
			DimensionNameString dnsType = new DimensionNameString("TYPE_" + c.type);
			c.macVector.setDimension(dnsType, 1);
			
			//roles' contribution to c's macvector
			for(Concept role : c.roles)
			{
				if(role != null)
				{
					DimensionNameString dnsRelation = new DimensionNameString("HASCHILD_TYPE_" + role.type);
					c.macVector.incrementDimension(dnsRelation);
				}
			}
			
			//TODO c's contribution to roles macvector?  should this go here, or should each role update its own macvector after consolidation?
			for(int i = 0; i < c.roles.size(); ++i)
			{
				Concept role = c.roles.get(i);
				DimensionNameString dnsRole = new DimensionNameString("ROLE_" + c.type + "_" + i);
				role.macVector.setDimension(dnsRole, 1);
			}
			
			//note: newly consolidated concept shouldn't have any relations
			for(Concept relation : c.relations)
			{
				int i = relation.roles.indexOf(c);
				DimensionName dnsRole = new DimensionNameString("ROLE_" + c.type + "_" + i);
				c.macVector.incrementDimension(dnsRole);
			}
			
		}
		
	}
	
	/*	this is now done in Concept constructor
	private static void addLegacyMACDimensions(Concept c)
	{
		for(DimensionName ldn : legacyDimensionNames)
		{
			c.macVector.setDimension(ldn, 0);
		}
	}
	*/
	
	

	/*
	//TODO this method may need to grow with newly predicated concepts;  i.e., dynamically during program execution!
	//TODO problem:  newly predicated concepts DON'T include dimensions created from previous predications
	public static void updateMACVectorOld(Concept c)
	{
		//order matters!  need to keep consistent MACVector dimension order across all concepts
		List<DimensionName> newDimensionNames = new ArrayList<DimensionName>();	
		
		if(c.type == -2)
		{
			//do nothing, capstone concept?
			//macvector is being updated in createCapstone method
		}else if(c.type == -1)
		{
			//TODO
			//do nothing, untyped concept?
			//should untyped concepts have their own dimension in macvector?  should their roles also get macvector dimensions?
		}
		else if(c.type == 3)	//basic level object
		{
			c.macVector.setDimension(DimensionNameSet.TYPE_3, 1);
		}else if(c.type == 5)	//objectTimeRelation
		{
			c.macVector.setDimension(DimensionNameSet.TYPE_5, 1);
			//TODO not incrementing correctly!
			if(c.roles.get(0) != null)
			{	
				c.roles.get(0).macVector.incrementDimension(DimensionNameSet.ROLE_5_0);
				c.macVector.incrementDimension(new DimensionNameString("HASCHILD_TYPE_" + c.roles.get(0).type));
			}
			if(c.roles.get(1) != null)
			{
				c.roles.get(1).macVector.incrementDimension(DimensionNameSet.ROLE_5_1);
				c.macVector.incrementDimension(new DimensionNameString("HASCHILD_TYPE_" + c.roles.get(1).type));
			}
		}else{
			//add legacy dimensions to newly predicated concept
			for(DimensionName dn : legacyDimensionNames)
			{
				c.macVector.setDimension(dn, 0);
				//add legacy dimensions to newly predicated concept's roles as well
				for(Concept role : c.roles)
				{
					role.macVector.setDimension(dn, 0);
				}
			}			
			//predicated concept, create new dimension for this concept, and for each of its roles with values = 1
			DimensionNameString dnsType = new DimensionNameString("TYPE_" + c.type);
			newDimensionNames.add(dnsType);
			c.macVector.setDimension(dnsType, 1);
			//add new type to c's roles macvector
			for(int i = 0; i < c.roles.size(); ++i)
			{
				c.roles.get(i).macVector.setDimension(dnsType, 0);
			}
			//add new roles to c and to c's roles macvectors
			for(int i = 0; i < c.roles.size(); ++i)
			{
				DimensionName dnsRole = new DimensionNameString("ROLE_" + c.type + "_" + i);
				newDimensionNames.add(dnsRole);
				c.macVector.setDimension(dnsRole, 0);
				for(int j = 0; j < c.roles.size(); ++j)
				{
					if(j == i)
					{
						c.roles.get(j).macVector.setDimension(dnsRole, 1);
					}else{
						c.roles.get(j).macVector.setDimension(dnsRole, 0);
					}
				}				
			}
			//legacyDimensionNames.addAll(newDimensionNames);
			//updateAllConceptsMACVectors(newDimensionNames);
		}
		
		
		
		//return mv;
	}
	*/
	
	/* deprecated
	private static void updateAllConceptsMACVectors(List<DimensionName> dimensionNames)
	{
		for(Concept c : concepts)	//updated all concepts b/c macVector.addDimension() doesn't change anything if dimensionName already exists in the concept's macVector
		{
			for(DimensionName dn : dimensionNames)
			{
				c.macVector.addDimension(dn, 0);
			}
			System.out.println(c.macVector + " :: " + c);
		}
	}
	*/
}
