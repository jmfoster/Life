package engine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import model.Model;

public abstract class MACVector implements Serializable {
	//might want to use hashtable for the vector for quick access
	public List<MACVectorDimension> vector = new ArrayList<MACVectorDimension>();
	
	public int guard = 0;
	//protected abstract void initializeMACVector();
	
	
	//TODO does this get called when classes implementing MACVector are initialized?  A: yes, but it is the first thing called
	//initializeMACVector();  *** Now done in the implementing classes
	public MACVector()
	{
		if(Model.includeCells)
		{
			addDimension(DimensionNameSet.OFF, 0);
			addDimension(DimensionNameSet.ON, 0);
			addDimension(DimensionNameSet.TYPE_CELL, 0);
			addDimension(DimensionNameSet.TYPE_CELL_SPACE_RELATION, 0);
			addDimension(DimensionNameSet.TYPE_CELL_TIME_RELATION, 0);
			addDimension(DimensionNameSet.TYPE_OBJECT_MEMBERSHIP_RELATION, 0);
			addDimension(DimensionNameSet.ROLE_CELL_NEIGHBOR, 0);
			addDimension(DimensionNameSet.ROLE_CELL_SOURCE, 0);
			addDimension(DimensionNameSet.ROLE_CELL_TARGET, 0);
			addDimension(DimensionNameSet.ROLE_CONTAINER, 0);
			addDimension(DimensionNameSet.ROLE_MEMBER, 0);
		}
		addDimension(DimensionNameSet.TYPE_3, 0);
		addDimension(DimensionNameSet.TYPE_5, 0);
		addDimension(DimensionNameSet.ROLE_5_0, 0);
		addDimension(DimensionNameSet.ROLE_5_1, 0);
		addDimension(DimensionNameSet.HASCHILD_TYPE_3, 0);
		addDimension(DimensionNameSet.HASCHILD_TYPE_5, 0);
		addDimension(DimensionNameSet.SIZE, 0);
		addDimension(DimensionNameSet.LIFETIME, 0);
		
		
		//addLegacyMACDimensions();
		
		guard = vector.size();
	}
	
	/*
	protected void addLegacyMACDimensions()
	{
		for(DimensionName dn : MACVectorCreator.legacyDimensionNames)
		{
			this.setDimension(dn, 0);
		}
	}
	*/
	
	/**
	 * macvectors must already have equivalent dimensions
	 * @param v1, v2
	 * @return a new MACVector that is the sum of the two passed vectors
	 */
	public static MACVector composeMACVectors(MACVector v1, MACVector v2)
	{
		MACVector v = new MACVectorSparse();
		for(int i = 0; i < v1.vector.size() && i < v2.vector.size(); ++i)
		{
			v.setDimension(v1.getDimension(i).name, v1.getDimension(i).value + v2.getDimension(i).value);
		}
		return v;
	}
	
	public void composeWith(MACVector v)
	{
		for(int i = 0; i < v.vector.size(); ++i)
		{
			MACVectorDimension mvd = v.getDimension(i);
			double currentValue;
			try
			{
				currentValue = this.getDimension(i).value;
			}catch(Exception e){
				currentValue = 0;
			}
			this.setDimension(mvd.name, currentValue + mvd.value);
		}
	}
	
	/**
	 * 
	 */
	public void incrementDimension(DimensionName name)
	{
		for(MACVectorDimension mvd : vector)
		{
			if(mvd.name.toString().equals(name.toString()))
			{
				mvd.value = mvd.value + 1;
				return;	//stop the search
			}
		}
		//System.err.println("no dimension associated with name: " + name);	
		//name not found, add dimension
		//addDimension(name, 1);
	}
	
	
	
	//could be optimized computationally for initial enum values using enumeration's int value as index into array
	/**
	 * if DimensionName doesn't exist in vector, adds DimensionName as new dimension with value
	 */
	public void setDimension(DimensionName name, double value)
	{
		for(MACVectorDimension mvd : vector)
		{
			if(mvd.name.toString().equals(name.toString()))
			{
				mvd.value = value;
				return;	//stop the search
			}
		}
		
		//name not found, add dimension
		addDimension(name, value);
	}
	
	
	@Override
	public String toString()
	{
		String s = "( ";
		for(MACVectorDimension mvd : vector)
		{
			s += mvd.name.toString() + ":" + mvd.value + " ";
		}
		s += ")";
		return s;
	}
	
	public void addDimension(DimensionName name, double value)
	{
		for(MACVectorDimension mvd : vector)
		{
			if(mvd.name.equals(name))
			{
				return;	//don't add dimension if it already exists
			}
		}
		vector.add(new MACVectorDimension(name, value));
	}
	
	public void addDimension(MACVectorDimension mvd)
	{
		for(MACVectorDimension dim : vector)
		{
			if(mvd.name.equals(dim.name))
			{
				return;	//don't add dimension if it already exists
			}
		}
		vector.add(mvd);
	}
	
	public MACVectorDimension getDimension(int i)
	{
		return vector.get(i);
	}
	
	/**
	 * returns value of dimension from string
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public Double getDimensionValue(DimensionName name) throws Exception 
	{
		
		for(MACVectorDimension mvd : vector)
		{
			if(mvd.name.equals(name))
			{
				return mvd.value;
			}
			
		}
		
		//System.err.println("no dimension associated with name: " + name);		
		return 0.0;
	}
	
	public abstract double computeSimilarity(MACVector macVector) throws Exception;
	
	/*
	public double computeSimilarity(MACVector macVector) throws Exception
	{
		
		System.out.println(this);
		System.out.println(macVector);
		if(this.vector.size() != macVector.vector.size()){
			System.err.println("macVector dimensions unequal size!!!");
			//throw new Exception("macVector dimensions unequal size!!!");
		}
		
		double simSum = 0;
		for(int i = 0; i < vector.size(); ++i)
		{
			MACVectorDimension mvdX = this.vector.get(i);
			MACVectorDimension mvdY = macVector.vector.get(i);
			if(!mvdX.name.equals(mvdY.name))
			{
				System.err.println("macVector dimensions out of alignment order!");
				//throw new Exception ("macVector dimensions out of alignment order!");
			}
			
			double SIMi = 0;
			double min = Math.min(mvdX.value, mvdY.value);
			double max = Math.max(mvdX.value, mvdY.value);
			if(max != 0)
			{
				SIMi = min / max;
			}
			simSum += SIMi;
		}
		return simSum;
	}
	
	*/
	
	
	/**
	 * DEPRECATED, use computeSimilarity() instead!
	 * note:  vector dimensions must be in standard order for all concepts
	 * @param macVector
	 * @return
	 * @throws Exception 
	 */
	//TODO use cosine instead of normalized dot product???
	//TODO getting some NaNs from this from type -2 concepts in entityGraph, why???
	public double computeDotProduct(MACVector macVector) throws Exception
	{
		double sumAB = 0;
		double sumAA = 0;
		double sumBB = 0;
		List<MACVectorDimension> v2 = macVector.vector;
		if(vector.size() != v2.size())
		{
			System.err.println("vectors not equal length!");
		}else{
			for(int i = 0; i < vector.size(); ++i)
			{
				if(!vector.get(i).name.equals(v2.get(i).name))
				{
					System.err.println("macVector dimensions out of alignment order!");
					//throw new Exception ("macVector dimensions out of alignment order!");
				}
				double a = vector.get(i).value;
				double b = v2.get(i).value;
				sumAB += (a * b);
				sumAA += (a * a);
				sumBB += (b * b);
			}
		}
		//return Math.acos(  sumAB / ( Math.sqrt(sumAA) * Math.sqrt(sumBB) )  );
		
		double magA = Math.sqrt(sumAA);
		double magB = Math.sqrt(sumBB);
		double dotProduct = sumAB / (magA * magB);	//normalized dot product
		if(Double.isNaN(dotProduct))
		{
			//TODO why are some concepts getting Nan?  b/c they have 0's for all macVector dimensions
			//System.err.println("dotProduct is NaN for vectors: " + this + " :: " + macVector);
			return 0;
		}else{
			return dotProduct;	
		}
		//return sumAB //unnormalized dot product
		
	}

}