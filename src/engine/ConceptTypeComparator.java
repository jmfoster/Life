package engine;

import java.util.Comparator;

/**
 * highest to lowest
 * @author james
 *
 */
public class ConceptTypeComparator implements Comparator<Concept> {
	
	public int compare(Concept c1, Concept c2)
	{
		if(c1.type > c2.type)
		{
			return -1;
		}else if(c1.type == c2.type){
			return 0;
		}else{
			return 1;
		}
	}
}
