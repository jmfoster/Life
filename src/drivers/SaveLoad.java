package drivers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import engine.Concept;
import engine.SchemaAlignment;

public class SaveLoad {

	public static List<Concept> restoreSerializedEntities(File ser) throws IOException
	{
		System.out.println();
		System.out.println("Restoring Serialized Entities");
		FileInputStream fis = new FileInputStream(ser);
		ObjectInputStream in = new ObjectInputStream(fis);
		List<Concept> entities = new ArrayList<Concept>();
		try{
			entities = (List<Concept>) in.readObject();
		}catch(ClassNotFoundException e){
				e.printStackTrace();
		}
		
		int size = entities.size();
		
		Concept.idTracker = entities.get(size-1).id + 1;	//start ConceptId counter back at the right place
		
		//start Concept.typeTracker back at the right place (max of concept types that exist)
		int maxType = 0;
		for(Concept c : entities)
		{
			if(c.type > maxType)
			{
				maxType = c.type;
			}
		}
		Concept.typeTracker = maxType;
		
		return entities;
	}
	
	public static List<SchemaAlignment> restoreSerializedAlignments(File serAlignments) throws IOException
	{
		System.out.println();
		System.out.println("Restoring Serialized Alignments");
		FileInputStream fis = new FileInputStream(serAlignments);
		ObjectInputStream in = new ObjectInputStream(fis);
		List<SchemaAlignment> alignments = new ArrayList<SchemaAlignment>();
		try{
			alignments = (List<SchemaAlignment>) in.readObject();
		}catch(ClassNotFoundException e){
				e.printStackTrace();
		}
		
		return alignments;
	}
	
	public static void serializeEntities(File ser, List<Concept> entities) throws IOException
	{
		System.out.println();
		System.out.println("Serializing Entities to File: " + ser.toString() + " size: " + entities.size());
		//convert set to list
		//serialize entityList to file
		FileOutputStream fos = new FileOutputStream(ser);
		ObjectOutputStream out = new ObjectOutputStream(fos);
		out.writeObject(entities);
		out.close();
	}
	
	//TODO do the Concepts within the alignments point to the same object in memory as in the restored entities list?  or are they identitcal copies but different objects? 
	public static void serializeAlignments(File serAlignments, List<SchemaAlignment> alignments) throws IOException
	{
		
		System.out.println();
		System.out.println("Serializing Alignments to File: " + serAlignments.toString() + " size: " + alignments.size());
		//convert set to list
		//serialize entityList to file
		FileOutputStream fos = new FileOutputStream(serAlignments);
		ObjectOutputStream out = new ObjectOutputStream(fos);
		out.writeObject(alignments);
		out.close();
	}

}
