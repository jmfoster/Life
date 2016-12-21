package engine;

public class GenericConcept extends Concept {

	
	public GenericConcept(int type)
	{
		this.type = type;
		//setType()  
		
	}
	
	//schema nodes MACVectors get initialized late because they need to have all their roles filled
	@Override
	protected void initializeMACVector() {
		MACVectorCreator.updateMACVector(this);
		//addLegacyMACDimensions();
	}

	@Override
	protected void setType() {
		//type set in constructor
	}

}
