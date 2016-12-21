package engine;

public class UntypedConcept extends Concept {
	
	
	public UntypedConcept()
	{
		this.type = -1;
		//setType();
		initializeMACVector();
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
