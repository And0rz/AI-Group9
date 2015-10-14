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
		//TODO actual acceptance strategy, which isn't just accept it if it's really good
		try{
			if(ourUtility.getUtility(opponentBid)>0.9){
				return true;
			}else{
				return false;
			}
		}catch(Exception e){
			return false;
		}
	}
}
