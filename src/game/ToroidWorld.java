package game;



import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ToroidWorld extends World {
	
	public ToroidWorld(File file, Integer iteration) throws NumberFormatException, IOException {
		super(file, iteration);		
	}
	
	public ToroidWorld(Integer size, Integer[][] cellArray, Integer iteration)
	{
		super(size, cellArray, iteration);
	}

	@Override
	public List<Cell> getNeighbors(Integer i, Integer j) //assumes toroid world (i.e., wraparound at edges)
	{
		List<Cell> neighbors = new ArrayList<Cell>();
		
		for(int m = -1; m <=1; ++m)
		{
			for(int n= -1; n <= 1; ++n)
			{
				int neighborX=(i+m)%size;
				int neighborY=(j+n)%size;
				if(neighborX < 0)
				{
					neighborX = neighborX + size;
				}
				if(neighborY < 0)
				{
					neighborY = neighborY + size;
				}				
			
				if(m == 0 && n ==0)	//coordinates for this cell
				{
					//don't include this cell in neighbors					
				}else{
					neighbors.add(this.getCell(neighborX, neighborY));
				}											
			}
		}
		return neighbors;
	}
}
