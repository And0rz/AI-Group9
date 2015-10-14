package class9;

import java.util.HashMap;

import negotiator.Bid;
import negotiator.session.TimeLineInfo;
import negotiator.utility.UtilitySpace;

public class AcceptStrat {
	private UtilitySpace ourUtility;
	
	public AcceptStrat(UtilitySpace ownUtility){
		ourUtility=ownUtility;
	}
	
	//This is called at the end of every chooseAction to determine if we bid or accept
	public boolean determineAcceptance(Bid opponentBid, Bid ourBid, HashMap<Object,UtilitySpace> opponentUtilities, TimeLineInfo timeLine){
		//TODO generally examine the current acceptance functions
		try{
			double ourBidUtil=ourUtility.getUtility(ourBid);
			double theirBidUtil=ourUtility.getUtility(opponentBid);
			double curTime=timeLine.getTime();
			//Get a utility compared to our bid, time and utility compared to constant acceptance
			boolean ACNext = acceptanceRatingNext(ourBidUtil,theirBidUtil,curTime);
			boolean ACTime = acceptanceRatingTime(curTime,timeLine,theirBidUtil);
			boolean ACConst = acceptanceRatingConst(theirBidUtil);
			//If any strategy accepts we accept, the current parameters for the strategies mean we are basically running a next bid value strategy which also accepts anything at the very last second and accepts insanely good bids
			return (ACNext||ACTime||ACConst);
		}catch(Exception e){
			return false;
		}
	}
	
	//Function for determining acceptance mostly based on a constant. Generally we will use this to only accept the most insanely good offers
		private boolean acceptanceRatingConst(double theirBidUtil){
			//TODO parameter tuning
			final double requiredUtil=0.97;//The constant value required
			return theirBidUtil>requiredUtil;
		}
	
	//Function for determining acceptance mostly based on time. Currently this is only used to accept (nearly) any bid when we probably can't make any new bids anymore anyways.
	private boolean acceptanceRatingTime(double t, TimeLineInfo timeLine,double theirBidUtil){
		//TODO parameter tuning
		final double requiredTime=0.995;
		final double absoluteReservationValue=0;
		return (t>=requiredTime)&&(theirBidUtil>absoluteReservationValue);
	}
	
	//This function determines acceptance mostly based on the next bid combined with some time discounting (on top of the basic time discounting of our own bids)
	private boolean acceptanceRatingNext(double ourBidUtil,double theirBidUtil, double t){
		//TODO parameter tuning
		final double timeFactor=0.05;//How much higher the opponents bid is effectively rated, this value is the factor for t=1
		final double leniency=0.03;//This is how much higher the opponents bid is always multiplied by, additive to timeFactor
		final double allowedGap=0.01;//This is an additive factor which represents an allowed gap
		return (allowedGap+(1+leniency+timeFactor*t*t*t)*(theirBidUtil/ourBidUtil))>1;
	}
}

