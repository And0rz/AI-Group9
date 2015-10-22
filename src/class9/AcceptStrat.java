package class9;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import negotiator.Bid;
import negotiator.BidHistory;
import negotiator.bidding.BidDetails;
import negotiator.session.TimeLineInfo;
import negotiator.utility.UtilitySpace;

public class AcceptStrat {
	private UtilitySpace ourUtility;
	private double maxAvarageUtil = 0;
	
	public AcceptStrat(UtilitySpace ownUtility){
		ourUtility=ownUtility;
	}
	
	//This is called at the end of every chooseAction to determine if we bid or accept
	public boolean determineAcceptance(Bid opponentBid, HashMap<Object,BidHistory> previousBidsMap,Bid ourBid, HashMap<Object,UtilitySpace> opponentUtilities, TimeLineInfo timeLine){
		//TODO generally examine the current acceptance functions
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
			return ((ACNext || ACTime) && ACConst);
		}catch(Exception e){
			return false;
		}
	}
	
	//Function for determining acceptance mostly based on a constant. Generally we will use this to only accept the most insanely good offers
		private boolean acceptanceRatingConst(double theirBidUtil, HashMap<Object,BidHistory> previousBidsMap, double t){
			//TODO parameter tuning
			
			/*
			final double requiredUtil=0.85;//The constant value required
			return theirBidUtil>requiredUtil;
			*/
			final double margin = 1.15;
			if (t <= 0.97) { //wait for better deal or accept if theirBidUtil is really high
				double requiredUtil = 0.85;
				return theirBidUtil >= requiredUtil;
			}
			else //when near the deadline, base acceptance on recent history of bids
			{
				if(maxAvarageUtil == 0) {
					double r = 0.85; //time remaining in negotation
					Iterator<BidHistory> historyIterator = previousBidsMap.values().iterator();
					//double maxUtil = 0;
					while(historyIterator.hasNext()) { //retrieve max bid from every sender within remaining time window
						BidHistory bidHistory = historyIterator.next();
						//sumAvarageUtil += bidHistory.filterBetweenTime(t-r, t).getAverageUtility();
						//sumAvarageUtil += bidHistory.filterBetweenTime(t-r, t).getBestBidDetails().getMyUndiscountedUtil();
						double avarageUtil = bidHistory.filterBetweenTime(r, t).getAverageUtility();
						if (avarageUtil > maxAvarageUtil) {
							maxAvarageUtil = avarageUtil;
						}
						//if (util > maxUtil) {
						//	maxUtil = util;
						//}
						//List<BidDetails> utils = bidHistory.filterBetweenTime(t-r, t).getNBestBids(5);
						/*
						List<BidDetails> utils = bidHistory.getNBestBids(10);
						Iterator<BidDetails> utilsIterator = utils.iterator();
						while(utilsIterator.hasNext()) {
							double util = utilsIterator.next().getMyUndiscountedUtil();
							if (util > maxUtil) {
								maxUtil = util;
							}
						} */
					}
					//avarageUtil /= previousBidsMap.size();
					//System.out.println(avarageUtil + "* " + margin + "= " + avarageUtil*margin);
					if(maxAvarageUtil*margin > 0.9) //upper bound on required utility
						maxAvarageUtil = 0.9;
					else
						maxAvarageUtil *= margin; //multiply maximum avarage with an estimated margin
				}
				//true if theirBidUtil is larger than the avarage maxBid within remaining time window
				//return theirBidUtil > (sumAvarageUtil / previousBidsMap.size());
				return theirBidUtil > maxAvarageUtil;		
			}
			
		}
		
	//Function for determining acceptance mostly based on time. Currently this is only used to accept (nearly) any bid when we probably can't make any new bids anymore anyways.
	private boolean acceptanceRatingTime(double t, TimeLineInfo timeLine,double theirBidUtil){
		//TODO parameter tuning
		/*
		final double requiredTime=0.995;
		final double absoluteReservationValue=0;
		return (t>=requiredTime)&&(theirBidUtil>absoluteReservationValue);
		*/
	
		final double requiredTime=0.98;
		return t>=requiredTime;
		
	}
	
	//This function determines acceptance mostly based on the next bid combined with some time discounting (on top of the basic time discounting of our own bids)
	private boolean acceptanceRatingNext(double ourBidUtil,double theirBidUtil, double t){
		//TODO parameter tuning
		/*
		final double timeFactor=0.05;//How much higher the opponents bid is effectively rated, this value is the factor for t=1
		final double leniency=0.03;//This is how much higher the opponents bid is always multiplied by, additive to timeFactor
		final double allowedGap=0.01;//This is an additive factor which represents an allowed gap
		return (allowedGap+(1+leniency+timeFactor*t*t*t)*(theirBidUtil/ourBidUtil))>1;
		*/
		
		final double a = 1.00;
		final double b = 0.00;
		return (a*theirBidUtil+b) > ourBidUtil;		
	}
}

