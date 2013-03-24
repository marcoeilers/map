// Environment code for project exercise-3.mas2j

import jason.asSyntax.*;
import jason.environment.*;
import java.util.logging.*;
import java.util.*;
import java.util.Map.*;

/**
 * Environment for the electronic market that encapsulated the ItemDB
 *
 */
public class ElectronicMarketEnv extends Environment {

    private Logger logger = Logger.getLogger("exercise-3.mas2j." + ElectronicMarketEnv.class.getName());
	
	private final ItemDB itemDB;
	
	public ElectronicMarketEnv() {
		super();
		itemDB = ItemDB.getInstance();
	}

    /**
	 * Called before the MAS execution with the args informed in .mas2j.
	 */
    @Override
    public void init(String[] args) {
        super.init(args);
    }

	/**
	 * Invoked when a non-internal action is executed in Jason.
	 * 
	 * @param agName name of the agent performing the action
	 * @param action the action to be executed
	 *
	 * @return true if the action was handled, false otherwise
	 */
    @Override
    public boolean executeAction(String agName, Structure action) {
		String functor = action.getFunctor();
		
		// parameters are an item and an agent identifier
		int numTerms = action.getTerms().size();
		if(numTerms != 2)
			throw new IllegalArgumentException("Expecting exactly two parameters instead of " + numTerms + ".");
		
		// extract the product attribute list from the action parameters
		Term attributes = action.getTerm(0);
		if(!(attributes instanceof ListTerm))
			throw new IllegalArgumentException("Expecting the first parameter to be a list instead of " + attributes.getClass().getName() + ".");
		
		// extract the trader from the action parameters
		Term trader = action.getTerm(1);
		if(numTerms == 2 && !(trader instanceof VarTerm))
			throw new IllegalArgumentException("Expecting the second parameter to be an agent name instead of " + trader.getClass().getName() + ".");
		
		// match on all functions
		if("envAddOffer".equals(functor)) {
			// add an offer to the ItemDB
			itemDB.addOffer(ItemDescriptor.fromTerm(attributes), trader.toString());
			return true;
		} else if("envRemoveOffer".equals(functor)) {
			// remove an offer from the ItemDB
			itemDB.removeOffer(ItemDescriptor.fromTerm(attributes), trader.toString());
			return true;
		} else if("envAddRequest".equals(functor)) {
			// add a request to the ItemDB
			itemDB.addRequest(ItemDescriptor.fromTerm(attributes), trader.toString());
			return true;
		} else if("envRemoveRequest".equals(functor)) {
			// remove a request from the ItemDB
			itemDB.removeRequest(ItemDescriptor.fromTerm(attributes), trader.toString());
			return true;
		} else if("envGetBuyers".equals(functor)) {
			// get buyers for a specific item from the ItemDB
			// and reply to the trader with an appropriate percept
			List<String> buyers = itemDB.getBuyers(ItemDescriptor.fromTerm(attributes));
			Literal setBuyers = ASSyntax.createLiteral("setBuyers");
			ListTerm buyerTerms = new ListTermImpl();
			for(String buyer : buyers)
				buyerTerms.add(ASSyntax.createLiteral(buyer));
			setBuyers.addTerm(buyerTerms);
			setBuyers.addTerm(attributes);
			setBuyers.addTerm(trader);
			addPercept(agName, setBuyers);
			return true;
		} else if("envGetSellers".equals(functor)) {
			// get sellers for a specific item from the ItemDB
			// and reply to the trader with an appropriate percept
			List<String> sellers = itemDB.getSellers(ItemDescriptor.fromTerm(attributes));
			Literal setSellers = ASSyntax.createLiteral("setSellers");
			ListTerm sellerTerms = new ListTermImpl();
			for(String seller : sellers)
				sellerTerms.add(ASSyntax.createLiteral(seller));
			setSellers.addTerm(sellerTerms);
			setSellers.addTerm(attributes);
			setSellers.addTerm(trader);
			addPercept(agName, setSellers);
			return true;
		}
		
		// we did not handle the action
		logger.info("Agent '" + agName + "' tries to execute action '" + functor + "' which is not implemented in the environment.");
        return false;
    }

    /** 
	 * Called before the end of MAS execution.
	 */
    @Override
    public void stop() {
        super.stop();
    }
}

