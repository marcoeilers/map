package electronicmarketenv;

import java.util.List;
import java.util.logging.Logger;

import apapl.Environment;
import apapl.ExternalActionFailedException;
import apapl.data.APLIdent;
import apapl.data.APLList;
import apapl.data.Term;

/**
 * Environment for the electronic market that encapsulates the ItemDB.
 * 
 * @author robert
 * 
 */
public class ElectronicMarketEnv extends Environment {

	private final Logger logger = Logger.getLogger(ElectronicMarketEnv.class
			.getName());

	private final ItemDB itemDB;

	public ElectronicMarketEnv() {
		super();
		itemDB = ItemDB.getInstance();
	}

	public static void main(String args[]) {
		// to generate the MANIFEST.MF appropriately during Runnable JAR File
		// Export
	}

	@Override
	protected void addAgent(String agent) {
		logger.info("Adding agent " + agent);
	}

	@Override
	protected void removeAgent(String agent) {
		logger.info("Removing agent " + agent);
	}

	/* --- External Actions --- */

	/**
	 * Adds the agent's offer to the ItemDB.
	 * 
	 * @param matchmaker
	 * @param agent
	 * @param offer
	 * @return
	 * @throws ExternalActionFailedException
	 */
	public Term addOffer(String matchmaker, APLIdent agent, APLList offer)
			throws ExternalActionFailedException {
		checkInvokingAgentIsMatchmaker(matchmaker);
		itemDB.addOffer(ItemDescriptor.fromAPLList(offer), agent.getName());
		return wrapBoolean(true);
	}

	/**
	 * Removes the agent's offer from the ItemDB.
	 * 
	 * @param matchmaker
	 * @param agent
	 * @param offer
	 * @return
	 * @throws ExternalActionFailedException
	 */
	public Term removeOffer(String matchmaker, APLIdent agent, APLList offer)
			throws ExternalActionFailedException {
		checkInvokingAgentIsMatchmaker(matchmaker);
		itemDB.removeOffer(ItemDescriptor.fromAPLList(offer), agent.getName());
		return wrapBoolean(true);
	}

	/**
	 * Adds the agent's request to the ItemDB.
	 * 
	 * @param matchmaker
	 * @param agent
	 * @param request
	 * @return
	 * @throws ExternalActionFailedException
	 */
	public Term addRequest(String matchmaker, APLIdent agent, APLList request)
			throws ExternalActionFailedException {
		checkInvokingAgentIsMatchmaker(matchmaker);
		itemDB.addRequest(ItemDescriptor.fromAPLList(request), agent.getName());
		return wrapBoolean(true);
	}

	/**
	 * Removes the agent's request from the ItemDB.
	 * 
	 * @param matchmaker
	 * @param agent
	 * @param request
	 * @return
	 * @throws ExternalActionFailedException
	 */
	public Term removeRequest(String matchmaker, APLIdent agent, APLList request)
			throws ExternalActionFailedException {
		checkInvokingAgentIsMatchmaker(matchmaker);
		itemDB.removeRequest(ItemDescriptor.fromAPLList(request),
				agent.getName());
		return wrapBoolean(true);
	}

	/**
	 * Gets the buyers for the specified offer.
	 * 
	 * @param matchmaker
	 * @param agent
	 * @param offer
	 * @return
	 * @throws ExternalActionFailedException
	 */
	public Term getBuyers(String matchmaker, APLIdent agent, APLList offer)
			throws ExternalActionFailedException {
		checkInvokingAgentIsMatchmaker(matchmaker);
		APLList buyers = wrapStringList(itemDB.getBuyers(ItemDescriptor
				.fromAPLList(offer)));
		logger.info("Agent " + agent + " got " + buyers
				+ " as buyers for his offer " + offer);
		return buyers;
	}

	/**
	 * Gets the sellers for the specified request.
	 * 
	 * @param matchmaker
	 * @param agent
	 * @param request
	 * @return
	 * @throws ExternalActionFailedException
	 */
	public Term getSellers(String matchmaker, APLIdent agent, APLList request)
			throws ExternalActionFailedException {
		checkInvokingAgentIsMatchmaker(matchmaker);
		APLList sellers = wrapStringList(itemDB.getSellers(ItemDescriptor
				.fromAPLList(request)));
		logger.info("Agent " + agent + " got " + sellers
				+ " as sellers for his request " + request);
		return sellers;
	}

	/**
	 * Prints the argument list separated by whitespace.
	 * 
	 * @param agent
	 * @param args
	 * @return
	 * @throws ExternalActionFailedException
	 */
	public Term printList(String agent, APLList args)
			throws ExternalActionFailedException {
		StringBuilder sb = new StringBuilder();
		sb.append(agent);
		sb.append(": ");

		String separator = "";
		Term head = null;
		while (args != null && (head = args.getHead()) != null) {
			sb.append(separator);
			sb.append(head.toString());
			args = (APLList) args.getTail();
			separator = " ";
		}
		logger.info(sb.toString());
		return wrapBoolean(true);
	}

	/**
	 * Dummy action that simply prints hello to the logger.
	 * 
	 * @param agent
	 * @return
	 * @throws ExternalActionFailedException
	 */
	public Term sayHello(String agent) throws ExternalActionFailedException {
		logger.info(agent + " says Hello");
		return wrapBoolean(true);
	}

	/* --- Utils --- */

	/**
	 * Wraps a boolean in a result list as a return value of external action
	 * calls.
	 * 
	 * @param b
	 * @return
	 */
	private APLList wrapBoolean(boolean b) {
		return new APLList(new APLIdent[] { b ? new APLIdent("true")
				: new APLIdent("false") });
	}

	/**
	 * Wraps a list of Strings in a result list as a return value of external
	 * action calls.
	 * 
	 * @param l
	 * @return
	 */
	private APLList wrapStringList(List<String> l) {
		APLIdent[] t = new APLIdent[l.size()];
		for (int i = 0; i < l.size(); ++i)
			t[i] = new APLIdent(l.get(i));
		return new APLList(t);
	}

	/**
	 * Throws an exception if the invoking agent is not the matchmaker.
	 * 
	 * @param agent
	 * @throws ExternalActionFailedException
	 */
	private void checkInvokingAgentIsMatchmaker(String agent)
			throws ExternalActionFailedException {
		if (!"matchmaker".equals(agent))
			throw new ExternalActionFailedException(
					"Only the matchmaker can invoke this action.");
	}

}