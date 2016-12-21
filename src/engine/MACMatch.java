package engine;

public class MACMatch implements Comparable{
	public Concept probe;
	public Concept match;
	public double simScore;
	public CapstoneConcept sourceCapstone;
	public CapstoneConcept targetCapstone;
	
	public MACMatch(Concept probe, Concept match, double simScore)
	{
		this.probe = probe;
		this.match = match;
		this.simScore = simScore;
	}
	
	public int compareTo(java.lang.Object o) 
	{
		if(o instanceof MACMatch)
		{
			MACMatch mm = (MACMatch) o;
			if(this.simScore < mm.simScore)
			{
				return +1;
			}else if(this.simScore == mm.simScore){
				return 0;
			}else{
				return -1;
			}
		}else{
			return -1;	//undefined if o is not type MACMatch
		}
	}
}
