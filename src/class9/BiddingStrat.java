package class9;

import java.util.HashMap;

import negotiator.Bid;
import negotiator.BidHistory;
import negotiator.boaframework.SortedOutcomeSpace;
import negotiator.session.TimeLineInfo;
import negotiator.utility.UtilitySpace;

public class BiddingStrat {
	private UtilitySpace ourUtility;
	private SortedOutcomeSpace sortedSpace;
	
	public BiddingStrat(UtilitySpace ownUtility){
		ourUtility=ownUtility;
		sortedSpace= new SortedOutcomeSpace(ourUtility);
	}
	
	//This is called every time an actions needs to be chosen to generate our candidate bid
	public Bid createBid(HashMap<Object,BidHistory> previousBids, HashMap<Object,UtilitySpace> opponentUtilities, TimeLineInfo timeLine){
		//TODO implement actual strategy
		return sortedSpace.getMaxBidPossible().getBid();
	}
	
	//Simple get the best bid method to use when exceptions are thrown in making a real bid
	public Bid getBestBid(){
		return sortedSpace.getMaxBidPossible().getBid();
	}

}
