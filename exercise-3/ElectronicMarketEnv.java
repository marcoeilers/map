// Environment code for project exercise-3.mas2j

import jason.asSyntax.*;
import jason.environment.*;
import java.util.logging.*;

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
		if("addOffer".equals(agName)) {
			return true;
		} else if("removeOffer".equals(agName)) {
			return true;
		} else if("addRequest".equals(agName)) {
			return true;
		} else if("removeRequest".equals(agName)) {
			return true;
		} else if("getBuyers".equals(agName)) {
			return true;
		} else if("getSellers".equals(agName)) {
			return true;
		}
		
		// we did not handle the action
		logger.info("Action '" + action + "' is not implemented in the environment.");
        return false;
    }

    /** Called before the end of MAS execution */
    @Override
    public void stop() {
        super.stop();
    }
}

