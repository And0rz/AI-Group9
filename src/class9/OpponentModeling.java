package class9;

import java.util.HashMap;
import java.util.Map.Entry;

import negotiator.Bid;
import negotiator.BidHistory;
import negotiator.Domain;
import negotiator.actions.Action;
import negotiator.bidding.BidDetails;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.Objective;
import negotiator.issue.ValueDiscrete;
import negotiator.utility.Evaluator;
import negotiator.utility.EvaluatorDiscrete;
import negotiator.utility.UtilitySpace;

public class OpponentModeling {
	private HashMap<Object,UtilitySpace> opponentUtilities;//Object is other agent objects used to identify them, utilityspaces are current estimates
	private Domain currentDomain;
	private UtilitySpace ourUtility;
	private double learnCoef = 0.2;
	private int learnValueAddition = 1;
	private int amountOfIssues;
	
	
	public OpponentModeling(UtilitySpace ownUtility){
		ourUtility=ownUtility;
		currentDomain= ourUtility.getDomain();
		opponentUtilities = new HashMap<Object,UtilitySpace>();
	}

	
	
	//Simple getter
	public HashMap<Object,UtilitySpace> getOpponentUtilities() {
		return opponentUtilities;
	}
	
	
	//Determines the difference between bids given the opponent's prevoius Utility Space
	private HashMap<Integer, Integer> determineDifference(UtilitySpace thisSpace, BidDetails first,
			BidDetails second) {

		HashMap<Integer, Integer> diff = new HashMap<Integer, Integer>();
		try {
			for (Issue i : thisSpace.getDomain().getIssues()) {
				diff.put(i.getNumber(), (((ValueDiscrete) first.getBid()
						.getValue(i.getNumber())).equals((ValueDiscrete) second
						.getBid().getValue(i.getNumber()))) ? 0 : 1);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return diff;
	}
	
	//This is called whenever a message is received
	public void updateModel(Object agent, Action action, HashMap<Object,BidHistory> previousBidsMap){
		if(!opponentUtilities.containsKey(agent)){
			createNewModel(agent);
		}
		UtilitySpace updatedSpace = opponentUtilities.get(agent);
		//updating Utility space, both for accepting actions and new offer 
		//actions, will probably want to split those completely
		if (previousBidsMap.get(agent).size()<2) {
			return;
		}
		int numberOfUnchanged = 0;
		BidDetails oppBid = previousBidsMap.get(agent).getHistory().get(previousBidsMap.get(agent).size() - 1);
		BidDetails prevOppBid = previousBidsMap.get(agent).getHistory().get(previousBidsMap.get(agent).size() - 2);
		HashMap<Integer, Integer> lastDiffSet = determineDifference(updatedSpace, prevOppBid, oppBid);
		
		// count the number of changes in value
		for (Integer i : lastDiffSet.keySet()) {
			if (lastDiffSet.get(i) == 0)
				numberOfUnchanged++;
		}
		// This is the value to be added to weights of unchanged issues before
		// normalization.
		// Also the value that is taken as the minimum possible weight,
		// (therefore defining the maximum possible also).
		double goldenValue = learnCoef / (double) amountOfIssues;
		// The total sum of weights before normalization.
		double totalSum = 1D + goldenValue * (double) numberOfUnchanged;
		// The maximum possible weight
		double maximumWeight = 1D - ((double) amountOfIssues) * goldenValue
				/ totalSum;

		// re-weighing issues while making sure that the sum remains 1
		for (Integer i : lastDiffSet.keySet()) {
			if (lastDiffSet.get(i) == 0
					&& updatedSpace.getWeight(i) < maximumWeight)
				updatedSpace.setWeight(updatedSpace.getDomain()
						.getObjective(i),
						(updatedSpace.getWeight(i) + goldenValue)
								/ totalSum);
			else
				updatedSpace.setWeight(updatedSpace.getDomain()
						.getObjective(i), updatedSpace.getWeight(i)
						/ totalSum);
		}
		// Then for each issue value that has been offered last time, a constant
		// value is added to its corresponding ValueDiscrete.
		try {
			for (Entry<Objective, Evaluator> e : updatedSpace
					.getEvaluators()) {
				// cast issue to discrete and retrieve value. Next, add constant
				// learnValueAddition to the current preference of the value to
				// make
				// it more important
				((EvaluatorDiscrete) e.getValue())
						.setEvaluation(
								oppBid.getBid().getValue(
										((IssueDiscrete) e.getKey())
												.getNumber()),
								(learnValueAddition + ((EvaluatorDiscrete) e.
										getValue())
										.getEvaluationNotNormalized(((ValueDiscrete) oppBid
												.getBid().getValue(
														((IssueDiscrete) e
																.getKey())
																.getNumber())))));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		opponentUtilities.put(agent, updatedSpace);
	}
	
	
	
	//creates standard UtilitySpace model given no current information
	private void createNewModel(Object agent){
		//UtilitySpace newUtilitySpace = new UtilitySpace(currentDomain);
		UtilitySpace newUtilitySpace = new UtilitySpace(ourUtility);
		//set all issue weights to be equal and evaluations 1)
		amountOfIssues = newUtilitySpace.getDomain().getIssues().size();
		double commonWeight = 1D / (double) amountOfIssues;
		
		//initialize the weights
		for (Entry<Objective, Evaluator> e : newUtilitySpace
				.getEvaluators()) {
			// set the issue weights
			newUtilitySpace.unlock(e.getKey());
			e.getValue().setWeight(commonWeight);
			try {
				// set all value weights to one (they are normalized when
				// calculating the utility)
				for (ValueDiscrete vd : ((IssueDiscrete) e.getKey())
						.getValues())
					((EvaluatorDiscrete) e.getValue()).setEvaluation(vd, 1);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
				
		opponentUtilities.put(agent, newUtilitySpace);
	}

}