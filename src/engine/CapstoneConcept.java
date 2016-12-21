package engine;

import model.DirectedGraphAdapter;

import java.util.ArrayList;
import java.util.List;

public class CapstoneConcept extends Concept {
	public List<DirectedGraphAdapter<Concept>> mappings = new ArrayList<DirectedGraphAdapter<Concept>>();	//mappings that gave rise to this capstone'd schema
	//Note: priorCapstones lists include only direct parents; more ancient generations can be accessed recursively through parents' priorCapstones lists
	public List<CapstoneConcept> priorSourceCapstones = new ArrayList<CapstoneConcept>();
	public List<CapstoneConcept> priorTargetCapstones = new ArrayList<CapstoneConcept>();
	public int generation = 0;
	
	public CapstoneConcept(int type)	
	{
		//hardcode as type -2?
		//TODO type could indicate what level of schema -- 1st order, 2nd order, etc (counting negatively)
		this.type = type;
		initializeMACVector();
		
	}
	
	@Override
	protected void initializeMACVector() 
	{
		MACVectorCreator.updateMACVector(this);
		//addLegacyMACDimensions();
	}

	
	
	@Override
	protected void setType() 
	{
		//done in constructor; could hard code though
	}

}
