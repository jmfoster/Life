package engine;

import model.DirectedGraphAdapter;

public class FACMatch implements Comparable{
	public DirectedGraphAdapter<Concept> mapping;
	public Double scoreNormalized;
	public Double scoreRaw;
	public Concept seed0;
	public Concept seed1;
	public CapstoneConcept sourceCapstone;
	public CapstoneConcept targetCapstone;
	
	public FACMatch(DirectedGraphAdapter<Concept> mapping, Double scoreRaw, Double scoreNormalized, Concept seed0, Concept seed1)
	{
		this.mapping = mapping;
		this.scoreRaw = scoreRaw;
		this.scoreNormalized = scoreNormalized;
		this.seed0 = seed0;
		this.seed1 = seed1;
	}
	
	public int compareTo(java.lang.Object o) 
	{
		if(o instanceof FACMatch)
		{
			FACMatch fm = (FACMatch) o;
			if(this.scoreRaw < fm.scoreRaw)
			{
				return +1;
			}else if(this.scoreRaw == fm.scoreRaw){
				return 0;
			}else{
				return -1;
			}
		}else{
			return -1;	//undefined if o is not type MACMatch
		}
	}
}