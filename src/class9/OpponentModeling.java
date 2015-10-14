package class9;

import java.util.HashMap;

import negotiator.Bid;
import negotiator.Domain;
import negotiator.actions.Action;
import negotiator.utility.UtilitySpace;

public class OpponentModeling {
	private HashMap<Object,UtilitySpace> opponentUtilities;//Object is other agent objects used to identify them, utilityspaces are current estimates
	private Domain currentDomain;
	private UtilitySpace ourUtility;
	
	
	
	public OpponentModeling(UtilitySpace ownUtility){
		ourUtility=ownUtility;
		currentDomain= ourUtility.getDomain();
		opponentUtilities = new HashMap<Object,UtilitySpace>();
	}

	
	//Simple getter
	public HashMap<Object,UtilitySpace> getOpponentUtilities() {
		return opponentUtilities;
	}
	
	
	
	//This is called whenever a message is received
	public void updateModel(Object agent,Action action, Bid previousBid){
		if(!opponentUtilities.containsKey(agent)){
			createNewModel(agent);
		}
		UtilitySpace updatedSpace=opponentUtilities.get(agent);
		//TODO updating spaces, both for accepting actions and new offer actions, will probably want to split those completely
	}
	
	
	
	//creates standard utilityspace model given no current information
	private void createNewModel(Object agent){
		UtilitySpace newUtilityspace = new UtilitySpace(currentDomain);
		//TODO create standard utilityspace (all weights and evaluations equal?)
		opponentUtilities.put(agent, newUtilityspace);
	}

}