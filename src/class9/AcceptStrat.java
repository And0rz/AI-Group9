package class9;

import java.util.HashMap;
import java.util.Iterator;

import negotiator.Bid;
import negotiator.BidHistory;
import negotiator.session.TimeLineInfo;
import negotiator.utility.UtilitySpace;

public class AcceptStrat {
	private UtilitySpace ourUtility;
	
	public AcceptStrat(UtilitySpace ownUtility){
		ourUtility=ownUtility;
	}
	
	//This is called at the end of every chooseAction to determine if we bid or accept
	public boolean determineAcceptance(Bid opponentBid, HashMap<Object,BidHistory> previousBidsMap,Bid ourBid, HashMap<Object,UtilitySpace> opponentUtilities, TimeLineInfo timeLine){
		try{
			double ourBidUtil=ourUtility.getUtility(ourBid);
			double theirBidUtil=ourUtility.getUtility(opponentBid);
			HashMap<Object,BidHistory> bidHistory = previousBidsMap;
			double curTime=timeLine.getTime();
			//Get a utility compared to our bid, time and utility compared to constant acceptance
			boolean ACNext = acceptanceRatingNext(ourBidUtil,theirBidUtil,curTime);
			boolean ACTime = acceptanceRatingTime(curTime,timeLine,theirBidUtil);
			boolean ACConst = acceptanceRatingConst(theirBidUtil, bidHistory, curTime);
			//If any strategy accepts we accept, the current parameters for the strategies mean we are basically running a next bid value strategy which also accepts anything at the very last second and accepts insanely good bids
			return (ACNext || ACTime || ACConst);
		}catch(Exception e){
			return false;
		}
	}
	
	//Function for determining acceptance mostly based on a constant. Generally we will use this to only accept the most insanely good offers
		private boolean acceptanceRatingConst(double theirBidUtil, HashMap<Object,BidHistory> previousBidsMap, double t){
				double requiredConst = 0.80; //default mninimum constant value
				double lowerbound = 83;
				double upperbound = 96;
				if (t >= lowerbound && t < upperbound) {  //decrease constant linearly in this range
					requiredConst = requiredConst - 1 * ((t - lowerbound) / (upperbound - lowerbound));  
				}
				else if (t >= upperbound) {
					double r = 1-t; //time remaining in negotation
					Iterator<BidHistory> historyIterator = previousBidsMap.values().iterator();
					double maxUtil = 0;
					while(historyIterator.hasNext()) { //retrieve max bid from every sender within remaining time window
						BidHistory bidHistory = historyIterator.next();
						double partyMax =  bidHistory.filterBetweenTime(t-r, t).getBestBidDetails().getMyUndiscountedUtil();
						if (partyMax > maxUtil) {
							maxUtil = partyMax;
						}
					}
					requiredConst = maxUtil;
				}
				return (theirBidUtil > requiredConst);			
		}
		
	//Function for determining acceptance mostly based on time. Currently this is only used to accept (nearly) any bid when we probably can't make any new bids anymore anyways.
	private boolean acceptanceRatingTime(double t, TimeLineInfo timeLine,double theirBidUtil){
		final double requiredTime=0.98;
		final double minUtil=0.3;
		return (t>=requiredTime)&(theirBidUtil>minUtil);
		
	}
	
	//This function determines acceptance mostly based on the next bid combined with some time discounting (on top of the basic time discounting of our own bids)
	private boolean acceptanceRatingNext(double ourBidUtil,double theirBidUtil, double t){
		final double a = 0.98;
		final double b = 0.00;
		return (a*theirBidUtil+b) > ourBidUtil;		
	}
}

