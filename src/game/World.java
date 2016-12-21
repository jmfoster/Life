package game;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import model.CellSpaceRelation;
import model.Model;

public abstract class World{
	//protected List<Cell> cellList;
	protected Integer size;		//assumes square world sizeXsize
	private Cell[][] cellArray;
	protected Integer iteration;
	private List<CellSpaceRelation> cellSpaceRelations = new ArrayList<CellSpaceRelation>();
	
	public World(Integer size, Integer[][] stateArray, Integer iteration)
	{
		this.size = size;
		this.cellArray = new Cell[size][size];
		this.iteration = iteration;
		for(int i = 0; i < size; ++i)
		{
			for(int j = 0; j < size; ++j)
			{
				int state = stateArray[i][j];
				Cell cell = new Cell(i, j, state, iteration);
				this.cellArray[i][j] = cell;
			}
		}		
	}
	
	public void addCellSpaceRelation(CellSpaceRelation csr)
	{
		if(Model.includeCells)
		{
			cellSpaceRelations.add(csr);

		}
	}
	
	public List<CellSpaceRelation> getCellSpaceRelations()
	{
		return cellSpaceRelations;
	}
	
	public Integer getSize()
	{
		return size;
	}
	
	public World(File file, Integer iteration) throws NumberFormatException, IOException
	{
		this.iteration = iteration;
		//read world from file	
		FileReader fileReader = new FileReader(file);
		BufferedReader reader = new BufferedReader(fileReader);
		
		
		
		if(reader.ready())
		{			
			this.size = Integer.parseInt(reader.readLine());		
			System.out.println("size = " + size);
		}
		
		this.cellArray = new Cell[size][size];
		
		for (int i = 0; i < size; ++i)
		{
			String line = reader.readLine();
			String[] cells = line.split(" ");
			
			for(int j = 0; j < cells.length; ++j)
			{
				int state = Integer.parseInt(cells[j]);
				Cell cell = new Cell(i, j, state, iteration);
				cellArray[i][j] = cell;
			}
		}
	}
	
	public Cell getCell(int i, int j)
	{
		return cellArray[i][j];
	}
	
	
	public List<Cell> getLiveCellList()	
	{
		List<Cell> liveCellList = new LinkedList<Cell>();
		for(int i = 0; i < size; ++i)
		{
			for(int j = 0; j < size; ++j)
			{
				Cell cell = this.getCell(i, j);
				if(cell.state == 1)
				{
					liveCellList.add(cell);
				}
			}
		}
		return liveCellList;
	}
	
	public Integer getState(Integer i, Integer j)
	{
		return cellArray[i][j].state;
	}
	
	public abstract List<Cell> getNeighbors(Integer i, Integer j); 

	public List<Cell> getNeighbors(Cell cell)
	{
		return this.getNeighbors(cell.i, cell.j);
	}	
	
	public Integer getNeighborsSum(Integer i, Integer j)
	{
		Integer sum = 0;
		List<Cell> neighbors = this.getNeighbors(i, j);
		for(Cell c : neighbors)
		{
			sum += c.state;
		}
		return sum;
	}
	
	@Override
	public String toString()
	{	
		String s = "";
		for(int i=0; i< size; ++i)
		{
			
			for(int j=0; j< size; ++j)
			{
					s += cellArray[i][j].state + " ";	
					//System.out.println(cellArray[i][j] + " ");
			}
			s+= "\n";
			//System.out.println(s);
		}
		return s;
	}
}
