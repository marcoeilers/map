// Environment code for project exercise-3.mas2j

import jason.asSyntax.*;
import jason.environment.*;
import java.util.logging.*;
import java.util.*;
import java.util.Map.*;

public class ElectronicMarketEnv extends Environment {

    private Logger logger = Logger.getLogger("exercise-3.mas2j." + ElectronicMarketEnv.class.getName());
	
	private final ItemDB itemDB;
	
	public ElectronicMarketEnv() {
		super();
		itemDB = ItemDB.getInstance();
	}

    /** Called before the MAS execution with the args informed in .mas2j */
    @Override
    public void init(String[] args) {
        super.init(args);
    }

    @Override
    public boolean executeAction(String agName, Structure action) {
		String functor = action.getFunctor();
		
		// item is a parameter and maybe an agent identifier
		int numTerms = action.getTerms().size();
		if(numTerms < 1 || numTerms > 2)
			throw new IllegalArgumentException("Expecting exactly one or two parameters instead of " + numTerms + ".");
		
		Term attributes = action.getTerm(0);
		if(!(attributes instanceof ListTerm))
			throw new IllegalArgumentException("Expecting the first parameter to be a list instead of " + attributes.getClass().getName() + ".");
		
		Term trader = action.getTerm(1);
		if(numTerms == 2 && !(trader instanceof VarTerm))
			throw new IllegalArgumentException("Expecting the second parameter to be an agent name instead of " + trader.getClass().getName() + ".");
		
		if("envAddOffer".equals(functor)) {
			itemDB.addOffer(ItemDescriptor.fromTerm(attributes), trader.toString());
			return true;
		} else if("envRemoveOffer".equals(functor)) {
			itemDB.removeOffer(ItemDescriptor.fromTerm(attributes), trader.toString());
			return true;
		} else if("envAddRequest".equals(functor)) {
			itemDB.addRequest(ItemDescriptor.fromTerm(attributes), trader.toString());
			return true;
		} else if("envRemoveRequest".equals(functor)) {
			itemDB.removeRequest(ItemDescriptor.fromTerm(attributes), trader.toString());
			return true;
		} else if("envGetBuyers".equals(functor)) {
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

    /** Called before the end of MAS execution */
    @Override
    public void stop() {
        super.stop();
    }
}

