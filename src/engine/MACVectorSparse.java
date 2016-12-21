package engine;

public class MACVectorSparse extends MACVector {

	public double computeSimilarity(MACVector macVector) throws Exception 
	{
		/*
		System.out.println("computing sparse similarity: ");
		System.out.println("     " + this);
		System.out.println("     " + macVector);
		*/
		
		
		double simSum = 0;
		for(MACVectorDimension mvdX : this.vector)
		{
			double yValue = macVector.getDimensionValue(mvdX.name);
			
			double SIMi = 0;
			double min = Math.min(mvdX.value, yValue);
			double max = Math.max(mvdX.value, yValue);
			if(max != 0)
			{
				SIMi = min / max;
			}
			simSum += SIMi;
		}
		return simSum;
	}
}
