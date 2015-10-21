package class9;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import misc.Range;
import negotiator.Bid;
import negotiator.BidHistory;
import negotiator.bidding.BidDetails;
import negotiator.boaframework.SortedOutcomeSpace;
import negotiator.session.TimeLineInfo;
import negotiator.utility.UtilitySpace;

public class BiddingStrat {
	private UtilitySpace ourUtility;
	private SortedOutcomeSpace sortedSpace;
	private Bid ourPreviousBid;
	private boolean gahboninhoConcReactionDet;
	private boolean gahboninhoSelfishReactionDet;
	private HashMap<Object,Double> selfishnessAgainstConceding;
	private HashMap<Object,Double> selfishnessAgainstHardhead;
	private boolean hardHeadGahboninho;//learned concession strategy for Gahboninho, true=hardheaded, false=slow concession
	private Random rand;
	private double gahboninoConcActualDeadline;
	
	public BiddingStrat(UtilitySpace ownUtility){
		ourUtility=ownUtility;
		sortedSpace= new SortedOutcomeSpace(ourUtility);
		ourPreviousBid=sortedSpace.getMaxBidPossible().getBid();
		gahboninhoConcReactionDet=false;
		gahboninhoSelfishReactionDet=false;
		hardHeadGahboninho=true;
		rand= new Random(1);//Constant seed doesn't matter, not like the opponents will try to predict our bids in that way
		gahboninoConcActualDeadline=0;
	}
	
	//This is called every time an actions needs to be chosen to generate our candidate bid
	public Bid createBid(HashMap<Object,BidHistory> previousBids, HashMap<Object,UtilitySpace> opponentUtilities, TimeLineInfo timeLine){
		ourPreviousBid= getGahboninhoBid(previousBids,opponentUtilities,timeLine);
		return ourPreviousBid;
	}
	
	//Simple get the best bid method to use when exceptions are thrown in making a real bid
	public Bid getBestBid(){
		return sortedSpace.getMaxBidPossible().getBid();
	}
	
	
	
	//Very simple Gahbonino inspired tactic, checks how much the opponents are conceding, if they are both conceding already don't concede at all, if they aren't concede a little bit
	//It's basically boulware against a hardliner and hardliner against concession agents
	private Bid getGahboninhoBid(HashMap<Object,BidHistory> previousBids, HashMap<Object,UtilitySpace> opponentUtilities, TimeLineInfo timeLine){
		//Parameters
		final double concessionDeadline=0.05;
		final double concessionGoal=0.9;
		final double selfishnessDeadline=0.05;
		final double hardheadMinimumUtility=0.9;
		final double nonHardHeadFinalGoal=0.5;
		final double nonHardHeadStart=hardheadMinimumUtility;
		final double eps=0.2;
		final double rangeSize=0.2;
		double t=timeLine.getTime();
		if(t<concessionDeadline){//Concession stage
			return sortedSpace.getBidNearUtility(1-(1-concessionGoal)*(t/concessionDeadline)).getBid();//Concede linearly to concessionGoal to see how opponents react to conceders
		}
		else if(t<(concessionDeadline+selfishnessDeadline)){//Selfishness stage
			if(!gahboninhoConcReactionDet){
				gahboninhoConcReactionDet=true;
				gahboninoConcActualDeadline=timeLine.getCurrentTime();
				selfishnessAgainstConceding=computeSelfishness(previousBids,0,gahboninoConcActualDeadline);
			}
			return ourPreviousBid;//Don't concede to see how the opponents react to selfishness
		}
		else{//now after selfishnessDeadline+concessionDeadline with knowledge of how opponent reacts decide strategy
			if(!gahboninhoSelfishReactionDet){
				gahboninhoSelfishReactionDet=true;
				selfishnessAgainstHardhead=computeSelfishness(previousBids,gahboninoConcActualDeadline,timeLine.getCurrentTime());
				hardHeadGahboninho=determineGahbinoStrategy(selfishnessAgainstConceding,selfishnessAgainstHardhead);
				System.out.println("hardhead:"+hardHeadGahboninho);//TODO remove print
			}
			if(hardHeadGahboninho){
				return getBidFromTargetRange(new Range(hardheadMinimumUtility,1),opponentUtilities);//Get the bid rated best with utility above hardheadMinimumUtility
			}
			else{
				double lowerbound=nonHardHeadStart-(nonHardHeadStart-nonHardHeadFinalGoal)*(Math.pow(t, (1/eps)));
				System.out.println("lowerbound:"+lowerbound);
				return getBidFromTargetRange(new Range(lowerbound,Math.min(lowerbound+rangeSize, 1)),opponentUtilities);//Get the bid rated best with utility above hardheadMinimumUtility
			}
		}
	}
	
	
	
	//Compute variance of our utility for bids in certain timeframe, used as a measure of selfishness against our current tactic
	private HashMap<Object,Double> computeSelfishness(HashMap<Object,BidHistory> previousBids,double startTime, double endTime){
		HashMap<Object,Double> selfishMap=new HashMap<Object,Double>();
		for (Map.Entry<Object,BidHistory> entry : previousBids.entrySet()) {
		    Object key = entry.getKey();
		    double avUtil=entry.getValue().filterBetweenTime(startTime, endTime).getAverageUtility();
		    double sampleVar=0;
		    List<BidDetails> history = entry.getValue().filterBetweenTime(startTime, endTime).getHistory();
		    System.out.println("Average:"+avUtil);
		    for(BidDetails detail:history){
		    	System.out.println("Sample:"+detail.getMyUndiscountedUtil());
		    	sampleVar=sampleVar+Math.pow((detail.getMyUndiscountedUtil()-avUtil), 2.0);
		    }
		    sampleVar=sampleVar/history.size();
		    System.out.println("Variance:"+sampleVar);
		    selfishMap.put(key, sampleVar);
		}
		return selfishMap;
	}
	
	//Determine the optimal strategy against current estimated opponent strategies
	private boolean determineGahbinoStrategy(HashMap<Object,Double> varianceVSConceding,HashMap<Object,Double> varianceVSHardhead){
		final double hardheadLimit=0.001;//Variance below which opponent is considered hardhead
		final double concederLimit=0.003;//Variance above which opponent is considered conceding
		boolean competitiveFound=false;//Currently not used
		for (Map.Entry<Object,Double> entry : varianceVSConceding.entrySet()) {
			Object key=entry.getKey();
			double varianceConceding=entry.getValue();
			double varianceHardhead=varianceVSHardhead.get(key);
			if(varianceHardhead>concederLimit){//The opponent concedes to hardliners
				return true;//Abuse the conceder (or inverter) by hardlining
			}
			else if((varianceHardhead<hardheadLimit)&(varianceConceding<hardheadLimit)){
				competitiveFound=true;//If any opponent is using a competitive strategy store this information to use slow conceder unless someone else is already conceding 
			}
			else if((varianceHardhead<hardheadLimit)&(varianceConceding>concederLimit)){
				return false;//If any opponent is matching go for concession strategy to cooperate
			}
		}
		return false;//If all opponents are competitive go for slow concessions
	}
	
	private Bid getBidFromTargetRange(Range bidRange,HashMap<Object,UtilitySpace> opponentUtilities){
		final int numberBidsConsider=3;
		List<Entry<Bid,Double>> consideredBids=new ArrayList<Entry<Bid,Double>>();
		List<BidDetails> possibleBids=sortedSpace.getBidsinRange(bidRange);
		if(possibleBids.isEmpty()){//Incase something with the range went wrong
			return sortedSpace.getMaxBidPossible().getBid();
		}
		
		for(BidDetails candidate:possibleBids){
			double opponentUtilMult=1;
			for(UtilitySpace opponentSpace:opponentUtilities.values()){
				try {
					opponentUtilMult=opponentUtilMult*opponentSpace.getUtility(candidate.getBid());
				} catch (Exception e) {
					opponentUtilMult=0;
				}
			}
			for(int i=0;i<numberBidsConsider;i++){
				if((consideredBids.size()<(i+1))||(consideredBids.get(i).getValue()<opponentUtilMult)){
					consideredBids.add(i, new SimpleEntry<Bid,Double>(candidate.getBid(),opponentUtilMult));
					break;
				}
			}
			if(consideredBids.size()>numberBidsConsider){
				consideredBids.remove(numberBidsConsider);
			}
		}
		if(consideredBids.size()>0){
			int randomNum = rand.nextInt(consideredBids.size()) ;
			return consideredBids.get(randomNum).getKey();
		}
		else{
			return sortedSpace.getMaxBidPossible().getBid();//Just in case something went wrong in making the bids
		}
	}
}
