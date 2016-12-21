package engine;

public class ConsolidatedConcept extends Concept {

	public ConsolidatedConcept()
	{
		//initializeMACVector();  initialized manually by PredicationEngine so all roles can be filled first
		
	}
	
	//schema nodes MACVectors get initialized late because they need to have all their roles filled
	@Override
	protected void initializeMACVector() {
		MACVectorCreator.updateMACVector(this);
		//addLegacyMACDimensions();
	}

	@Override
	protected void setType() {
		this.type = Concept.typeTracker + 1;
	}

}
