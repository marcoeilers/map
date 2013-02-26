package nl.uu.cs.map.jade.agent;

import jade.core.AID;
import jade.core.Agent;

import java.util.List;

import nl.uu.cs.map.jade.ItemDB;
import nl.uu.cs.map.jade.ItemDescriptor;

public class MatchmakerAgent extends Agent {

	private static final long serialVersionUID = 8758414317735123415L;

	/**
	 * Result of a matchmaking request containing the specified item and one
	 * list of buyers and sellers each.
	 * 
	 * @author robert
	 * 
	 */
	public static class MatchMakingResult {
		public ItemDescriptor item;
		public List<AID> buyers, sellers;

		public MatchMakingResult(ItemDescriptor item, List<AID> buyers,
				List<AID> sellers) {
			this.item = item;
			this.buyers = buyers;
			this.sellers = sellers;
		}
	}

	private ItemDB itemDB;

	@Override
	protected void setup() {
		// get the arguments passed to this agent like this:
		// java -cp jade.jar:. jade.Boot -gui -host localhost
		// "matchmaker:MatchmakerAgent(arg1,arg2)"
		Object[] args = getArguments();
		for (Object arg : args)
			System.out.println(arg);

		itemDB = ItemDB.getInstance();
	}

	/**
	 * Returns the matchmaking result that contains buyers and sellers for the
	 * specified item.
	 * 
	 * @param item
	 * @return
	 */
	private MatchMakingResult makeMatch(ItemDescriptor item) {
		return new MatchMakingResult(item, itemDB.getBuyers(item),
				itemDB.getSellers(item));
	}

}
