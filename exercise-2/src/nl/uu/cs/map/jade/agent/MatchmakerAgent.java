package nl.uu.cs.map.jade.agent;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

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
		itemDB = ItemDB.getInstance();

		// register at the DF
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("matchmaking");
		sd.setName("Market-Matchmaker");
		dfd.addServices(sd);

		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void takeDown() {
		try {
			DFService.deregister(this);
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
