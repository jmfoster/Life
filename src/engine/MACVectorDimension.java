package engine;

import java.io.Serializable;

public class MACVectorDimension implements Serializable {
	public DimensionName name;
	public double value;
	
	public MACVectorDimension(DimensionName n, double v)
	{
		this.name = n;
		this.value = v;
	}
}
