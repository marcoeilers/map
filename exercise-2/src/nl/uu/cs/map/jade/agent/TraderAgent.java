package nl.uu.cs.map.jade.agent;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import nl.uu.cs.map.jade.ItemDescriptor;

public class TraderAgent extends Agent {
	private static final long serialVersionUID = 3698872544683250437L;

	private List<ItemDescriptor> offers;
	private List<ItemDescriptor> requests;

	@Override
	protected void setup() {
		addBehaviour(new ContactMatchmakerBehaviour(this));
		addBehaviour(new MessageRespondBehaviour(this));
	}

	/**
	 * Registers all offered and requested items with the matchmaker service.
	 * Subsequently requests all relevant information from the matchmaker
	 * service.
	 * 
	 */
	private class ContactMatchmakerBehaviour extends Behaviour {
		private static final long serialVersionUID = -2214859914596959774L;
		private boolean done = false;
		private Agent agent;

		private ContactMatchmakerBehaviour(Agent a) {
			this.agent = a;
		}

		@Override
		public void action() {
			// get the Matchmaker agent from DF
			DFAgentDescription mmdesc = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("matchmaking");
			mmdesc.addServices(sd);

			try {
				DFAgentDescription[] matchmakers = DFService.search(agent,
						mmdesc);
				if (matchmakers.length != 1)
					throw new IllegalStateException(
							"There is less or more than one MatchmakerAgent in the system");

				// register offered and requested items
				ACLMessage registerOffersMsg = new ACLMessage(ACLMessage.INFORM);
				registerOffersMsg.addReceiver(matchmakers[0].getName());
				registerOffersMsg.setSender(getAID());
				registerOffersMsg.setProtocol("registerOffers"); // TODO:
																	// change?
				try {
					registerOffersMsg.setContentObject((Serializable) offers);
					send(registerOffersMsg);
				} catch (IOException e) {
					e.printStackTrace();
				}

				ACLMessage registerRequestsMsg = new ACLMessage(
						ACLMessage.INFORM);
				registerRequestsMsg.addReceiver(matchmakers[0].getName());
				registerRequestsMsg.setSender(getAID());
				registerRequestsMsg.setProtocol("registerRequests"); // TODO:
																		// change?
				try {
					registerRequestsMsg
							.setContentObject((Serializable) requests);
					send(registerRequestsMsg);
				} catch (IOException e) {
					e.printStackTrace();
				}

			} catch (FIPAException e) {
				e.printStackTrace();
			}

			done = true;
		}

		@Override
		public boolean done() {
			return done;
		}

	}

	/**
	 * Responds to matchmaker messages by contacting other trade agents, to
	 * trade agent messages by accepting their offer or proposing a modified
	 * deal.
	 * 
	 */
	private class MessageRespondBehaviour extends Behaviour {
		private static final long serialVersionUID = -3467911981451330057L;
		private Agent agent;

		private MessageRespondBehaviour(Agent a) {
			this.agent = a;
		}

		@Override
		public void action() {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return false;
		}

	}
}
