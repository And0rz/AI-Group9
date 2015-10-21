package class9;


import java.util.HashMap;
import java.util.List;

import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.bidding.BidDetails;
import negotiator.parties.AbstractNegotiationParty;

import negotiator.session.TimeLineInfo;
import negotiator.utility.UtilitySpace;
import negotiator.AgentID;
import negotiator.Bid;
import negotiator.BidHistory;
import negotiator.Deadline;


/**
 * This is your negotiation party.
 */
public class Group9 extends AbstractNegotiationParty {
	private Bid prevReceivedBid;
	private int acceptCount;
	private OpponentModeling opponentModels;
	private AcceptStrat acceptStrat;
	private BiddingStrat biddingStrat;
	private HashMap<Object,BidHistory> previousBidsMap;

	/**
	 * Each round this method gets called and ask you to accept or offer. The
	 * first party in the first round is a bit different, it can only propose an
	 * offer.
	 *
	 * @param validActions
	 *            Either a list containing both accept and offer or only offer.
	 * @return The chosen action.
	 */
	@Override
	public Action chooseAction(List<Class<? extends Action>> validActions) {
		Bid ourBid= biddingStrat.createBid(previousBidsMap, opponentModels.getOpponentUtilities(), this.getTimeLine());
		
		if (!validActions.contains(Accept.class)) {
			return new Offer(ourBid);
		} else {
			if(acceptStrat.determineAcceptance(prevReceivedBid, previousBidsMap, ourBid, opponentModels.getOpponentUtilities(), this.getTimeLine())){
				return new Accept();
			}else{
				return new Offer(ourBid);
			}
		}
	}
	
	
	
	/**
	 * All offers proposed by the other parties will be received as a message.
	 * You can use this information to your advantage, for example to predict
	 * their utility.
	 *
	 * @param sender
	 *            The party that did the action.
	 * @param action
	 *            The action that party did.
	 */
	@Override
	public void receiveMessage(Object sender, Action action) {
		if(action.getClass().isInstance(new Accept())){
			acceptCount=acceptCount+1;
			opponentModels.updateModel(sender, action, previousBidsMap);
		}
		else if(Action.getBidFromAction(action)!=null){
			acceptCount=0;
			prevReceivedBid=Action.getBidFromAction(action);
			updateBidhistory(sender,prevReceivedBid);
			opponentModels.updateModel(sender, action, previousBidsMap);
		}
		super.receiveMessage(sender, action);
		// Here you hear other parties' messages
	}
	
	
	//Simple helper for putting a newly received bid into the history
	private void updateBidhistory(Object sender, Bid sendBid){
		
		if(!previousBidsMap.containsKey(sender)){
			previousBidsMap.put(sender, new BidHistory());
		}
		BidDetails tempBidDetails=new BidDetails(sendBid,this.getUtility(sendBid),this.getTimeLine().getCurrentTime());
		previousBidsMap.get(sender).add(tempBidDetails);
	}
	
	
	
    @Override
    public String getDescription() {
        return "example party group 9";
    }
    
    
    //Mostly initiate the super, besides that create the BOA components
    public void init(UtilitySpace utilSpace, Deadline dl, TimeLineInfo tl, long randomSeed, AgentID agentId){
    	super.init(utilSpace, dl, tl, randomSeed, agentId);
    	acceptCount=0;
    	opponentModels = new OpponentModeling(utilSpace);
    	acceptStrat = new AcceptStrat(utilSpace);
    	biddingStrat = new BiddingStrat(utilSpace);
    	previousBidsMap = new HashMap<Object,BidHistory>();
    }
        
}
