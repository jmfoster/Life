package engine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * implementing classes must call initializeMacVector() and setType() in constructor
 * @author James
 *
 */
public abstract class Concept implements Serializable {
	public static int idTracker = 0;
	public static int typeTracker = 5;
	
	public int type;
	public int id;
	public MACVector macVector = new MACVectorSparse();
	//protected double activation;
	public List<Concept> relations = new ArrayList<Concept>();
	public List<Concept> roles = new ArrayList<Concept>();
	public double score = 0;  //used for evaluating goodness of schema mapping
	
	//these two functions must be called in the constructor of implementing classes
	protected abstract void initializeMACVector();
	protected abstract void setType();

	
	public Concept()
	{
		this.setType();
		this.id = idTracker++;
	}
	
	
	
	
	@Override
	public String toString()
	{
		String s = id + "(" + type;
		for(Concept c : roles)
		{
			if(c != null)
			{
				s += "," + c.id;
			}else{
				s += "," + "null";
			}
		}
		s += ")";
		return s;
	}
	
	@Override
	public boolean equals(java.lang.Object o)
	{
		if(o instanceof Concept)
		{
			Concept c = (Concept) o;
			return this.id == c.id;
		}else{
			return false;
		}
		
	}
	
}
