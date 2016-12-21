package engine;

public class ActiveMapping implements Comparable {
	double deltaMapWeight;
	double newMapWeight;
	double Eij;
	double maxCompetitionWeight;
	Concept i;
	Concept j;
	
	/**
	 * sort primarily by deltaMapWeight, then secondarily by newMapWeight if deltaMapWeights are equal, then by id of i, then by id of j
	 */
	
	public int compareTo(java.lang.Object o)
	{
		ActiveMapping am = (ActiveMapping) o;
		if(this.deltaMapWeight < am.deltaMapWeight)
		{
			return -1;
		}else if(this.deltaMapWeight == am.deltaMapWeight)
		{
			if(this.newMapWeight < am.newMapWeight)
			{
				return -1;
			}else if(this.newMapWeight == am.newMapWeight)
			{
				//TODO need tiebreaker! so consistent instead of flopping back and forth
				if(this.i.id < am.i.id)
				{
					return -1;
				}else if(this.i.id == am.i.id){
					//sort by j
					if(this.j.id < am.j.id)
					{
						return -1;
					}else if(this.j.id == am.j.id)	//should not happen
					{
						assert false;
						return 0;
					}else{
						return -1;
					}
				}else{
					return 1;
				}
				
			}else{
				return 1;
			}
		}else{
			return 1;
		}
	}
	
	/**
	 * only looks at identiies of the entities participating in the mapping, not their weights
	 * @return
	 */
	@Override
	public boolean equals(java.lang.Object o)
	{
		if(o instanceof ActiveMapping)
		{
			ActiveMapping am = (ActiveMapping) o;
			return am.i.equals(this.i) && am.j.equals(this.j);
		}else{
			return false;
		}
	}
	
}
