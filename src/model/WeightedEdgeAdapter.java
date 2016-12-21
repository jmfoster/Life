package model;


public class WeightedEdgeAdapter<T> {
	public T parent;
	public T child;
	public T schemaNode;
	public double score = 0;
	
	public WeightedEdgeAdapter(T parent, T child)
	{
		this.parent = parent;
		this.child = child;
	}
}
