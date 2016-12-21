package engine;


public class DimensionNameString implements DimensionName {
	public String name;
	
	public DimensionNameString(String name)
	{
		this.name = name;
	}
	
	@Override
	public String toString()
	{
		return name;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(o instanceof DimensionNameString)
		{
			DimensionNameString dns = (DimensionNameString) o;
			return dns.name.equals(this.name);
		}else if(o instanceof DimensionName)
		{
			String name = ((DimensionName) o).toString();
			return this.name.equals(name);
		}
		else{
			return false;
		}
	}
}
