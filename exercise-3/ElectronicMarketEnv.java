// Environment code for project exercise-3.mas2j

import jason.asSyntax.*;
import jason.environment.*;
import java.util.logging.*;
import java.util.*;

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
		if("env_add_offer".equals(functor)) {
			itemDB.addOffer(ItemDescriptor.fromTerms(action.getTerms()), agName);
			return true;
		} else if("env_remove_offer".equals(functor)) {
			itemDB.removeOffer(ItemDescriptor.fromTerms(action.getTerms()), agName);
			return true;
		} else if("env_add_request".equals(functor)) {
			itemDB.addRequest(ItemDescriptor.fromTerms(action.getTerms()), agName);
			return true;
		} else if("env_remove_request".equals(functor)) {
			itemDB.removeRequest(ItemDescriptor.fromTerms(action.getTerms()), agName);
			return true;
		} else if("env_get_buyers".equals(functor)) {
			return true;
		} else if("env_get_sellers".equals(functor)) {
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

