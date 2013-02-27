package nl.uu.cs.map.jade.agent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import nl.uu.cs.map.jade.ItemDescriptor;
import nl.uu.cs.map.jade.Negotiation;

public class TraderAgent extends Agent {
	private static final long serialVersionUID = 3698872544683250437L;

	private List<ItemDescriptor> offers;
	private List<ItemDescriptor> requests;

	@Override
	protected void setup() {

		// get the Matchmaker agent from DF
		DFAgentDescription mmdesc = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("matchmaking");
		mmdesc.addServices(sd);

		try {
			DFAgentDescription[] matchmakers = DFService.search(this, mmdesc);
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

			ACLMessage registerRequestsMsg = new ACLMessage(ACLMessage.INFORM);
			registerRequestsMsg.addReceiver(matchmakers[0].getName());
			registerRequestsMsg.setSender(getAID());
			registerRequestsMsg.setProtocol("registerRequests"); // TODO:
																	// change?
			try {
				registerRequestsMsg.setContentObject((Serializable) requests);
				send(registerRequestsMsg);
			} catch (IOException e) {
				e.printStackTrace();
			}

			// get other traders which offer requested items and start a
			// negotiation
			for (ItemDescriptor i : requests) {
				ACLMessage findOffersMsg = new ACLMessage(ACLMessage.REQUEST);
				findOffersMsg.addReceiver(matchmakers[0].getName());
				findOffersMsg.setSender(getAID());
				findOffersMsg.setProtocol("getOffers");
				findOffersMsg.setReplyWith(i.getUid());
				try {
					findOffersMsg.setContentObject(i);
					send(findOffersMsg);
					addBehaviour(new NegotiationBehaviour(i, false));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// same for offered items
			for (ItemDescriptor i : requests) {
				ACLMessage findRequestsMsg = new ACLMessage(ACLMessage.REQUEST);
				findRequestsMsg.addReceiver(matchmakers[0].getName());
				findRequestsMsg.setSender(getAID());
				findRequestsMsg.setProtocol("getRequests");
				findRequestsMsg.setReplyWith(i.getUid());
				try {
					findRequestsMsg.setContentObject(i);
					send(findRequestsMsg);
					addBehaviour(new NegotiationBehaviour(i, true));
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		} catch (FIPAException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Responds to matchmaker messages by contacting other trade agents, to
	 * trade agent messages by accepting their offer or proposing a modified
	 * deal.
	 * 
	 */
	private class NegotiationBehaviour extends CyclicBehaviour {
		private static final long serialVersionUID = -3467911981451330057L;
		private ItemDescriptor item;
		private boolean buying;
		private String id;
		private List<Entry<String, AID>> partners;
		private boolean done = false;

		private Set<Negotiation> negotiations = new HashSet<Negotiation>();

		private NegotiationBehaviour(ItemDescriptor item, boolean buying) {
			this.item = item;
			this.buying = buying;
			this.id = item.getUid();
		}

		@SuppressWarnings("unchecked")
		@Override
		public void action() {
			if (!done) {
				MessageTemplate tmpl = MessageTemplate.MatchReplyWith(id);
				ACLMessage msg = TraderAgent.this.blockingReceive(tmpl);

				// if response to getOffers/getRequests
				if (msg.getProtocol().equals("setOffers")
						|| msg.getProtocol().equals("setRequests")) {
					try {
						partners = (List<Entry<String, AID>>) msg
								.getContentObject();

						// create negotiation information
						for (Entry<String, AID> e : partners) {
							Negotiation n = new Negotiation(e.getKey(),
									e.getValue(),
									buying ? 0.5 * item.getPriceLimit()
											: 2.0 * item.getPriceLimit(), false);
							negotiations.add(n);
						}

						// start by messaging one of them
						Negotiation best = null;
						double bestPrice = buying ? Double.MAX_VALUE
								: Double.MIN_VALUE;
						for (Negotiation n : negotiations) {
							if (buying) {
								if (n.getLastOffer() < bestPrice) {
									best = n;
									bestPrice = n.getLastOffer();
								}
							} else {
								if (n.getLastOffer() > bestPrice) {
									best = n;
									bestPrice = n.getLastOffer();
								}
							}
						}

						if (best != null) {
							// TODO check if new offer would violate limit
							
							// if not, send message 
						}

					} catch (UnreadableException e) {
						e.printStackTrace();
					}

					// else if (counter)proposal
				} else if (msg.getProtocol().equals("proposeDeal")) {
					// get Negotiation object
					Negotiation n = getNegotiation(msg.getConversationId());
					if (n != null){
						// TODO
						// check if acceptable
						
						// otherwise, check if counterproposal possible
						
						// otherwise reject
					}else{
						// TODO create new Negotiation object
					}
				} else if (msg.getProtocol().equals("acceptDeal")) {
					// TODO
					done = true;
				} else if (msg.getProtocol().equals("rejectDeal")) {
					// TODO
					done = true;
				}
			}
		}
		
		private Negotiation getNegotiation(String uid){
			Negotiation result = null;
			for (Negotiation n : negotiations){
				if (n.getUid().equals(uid)){
					result = n;
					break;
				}
			}
			return result;
		}
	}
}
