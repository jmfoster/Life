package engine;

import java.io.Serializable;

import model.DirectedGraphAdapter;

public class SchemaAlignment implements Serializable, Comparable<SchemaAlignment> {
	public Concept capstone1;
	public Concept capstone2;
	public DirectedGraphAdapter<Concept> alignment;
	public double simScore;
	
	
	
	public SchemaAlignment(Concept capstone1, Concept capstone2, DirectedGraphAdapter<Concept> alignment, double simScore)
	{
		this.capstone1 = capstone1;
		this.capstone2 = capstone2;
		this.alignment = alignment;
		this.simScore = simScore;
	}
	
	
	public int compareTo(SchemaAlignment sa)
	{
		if(this.simScore > sa.simScore)
		{
			return -1;
		}else if(this.simScore == sa.simScore)
		{
			return 0;
		}else{
			return 1;
		}
	}
}
