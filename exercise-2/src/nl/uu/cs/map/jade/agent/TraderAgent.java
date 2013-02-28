package nl.uu.cs.map.jade.agent;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import nl.uu.cs.map.jade.ItemDescriptor;
import nl.uu.cs.map.jade.Negotiation;

public class TraderAgent extends Agent {
	private static final long serialVersionUID = 3698872544683250437L;

	private List<ItemDescriptor> offers;
	private List<ItemDescriptor> requests;

	@Override
	protected void setup() {

		// retrieve the file name of the initialization file
		Object[] args = getArguments();
		if (args.length != 1)
			throw new IllegalArgumentException(
					"Expecting exactly one argument: the name of the initialization file");

		// read the properties file
		Properties properties = new Properties();
		Reader reader;
		try {
			reader = new FileReader((String) args[0]);
			properties.load(reader);
			reader.close();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

		// construct offers and requests
		this.offers = new ArrayList<ItemDescriptor>();
		String offersString = properties.getProperty("items.offered");
		if (offersString != null) {
			String[] offers = offersString.split("\\|");
			for (String offer : offers) {
				ItemDescriptor item = new ItemDescriptor();
				String[] attributes = offer.split(";");
				for (String attribute : attributes) {
					String[] keyValue = attribute.split(":");
					item.setAttribute(keyValue[0].trim(), keyValue[1].trim());
				}
				this.offers.add(item);
			}
		}

		this.requests = new ArrayList<ItemDescriptor>();
		String requestsString = properties.getProperty("items.requested");
		if (requestsString != null) {
			String[] requests = requestsString.split("\\|");
			for (String request : requests) {
				ItemDescriptor item = new ItemDescriptor();
				String[] attributes = request.split(";");
				for (String attribute : attributes) {
					String[] keyValue = attribute.split(":");
					item.setAttribute(keyValue[0].trim(), keyValue[1].trim());
				}
				this.requests.add(item);
			}
		}

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
				System.out.println("Registering offers: "+getAID());
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
				System.out.println("Registering requests: "+getAID());
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
					System.out.println("Getting offers for item "+i+": "+getAID());
					addBehaviour(new NegotiationBehaviour(i, false));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// same for offered items
			for (ItemDescriptor i : offers) {
				ACLMessage findRequestsMsg = new ACLMessage(ACLMessage.REQUEST);
				findRequestsMsg.addReceiver(matchmakers[0].getName());
				findRequestsMsg.setSender(getAID());
				findRequestsMsg.setProtocol("getRequests");
				findRequestsMsg.setReplyWith(i.getUid());
				try {
					findRequestsMsg.setContentObject(i);
					send(findRequestsMsg);
					System.out.println("Getting requests for item "+i+": "+getAID());
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
	private class NegotiationBehaviour extends Behaviour {
		private static final long serialVersionUID = -3467911981451330057L;
		private ItemDescriptor item;
		private boolean buying;
		private String id;
		private List<Entry<String, AID>> partners;
		private boolean done = false;
		private AID waitFor = null; // AID of the partner for who we are
									// currently waiting for.
									// No other messages can be sent out until
									// we have a response from this one.

		private Set<Negotiation> negotiations = new HashSet<Negotiation>();

		private NegotiationBehaviour(ItemDescriptor item, boolean buying) {
			this.item = item;
			this.buying = buying;
			this.id = item.getUid();
		}

		@Override
		public boolean done() {
			return done;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void action() {
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
					initiateNextRound();

				} catch (UnreadableException e) {
					e.printStackTrace();
				}

				// else if (counter)proposal
			} else if (msg.getProtocol().equals("proposeDeal")) {
				// get Negotiation object
				Negotiation n = getNegotiation(msg.getConversationId());
				if (n == null) {
					n = new Negotiation(msg.getConversationId(),
							msg.getSender(),
							buying ? 0.5 * item.getPriceLimit() : 2.0 * item
									.getPriceLimit(), true);
					negotiations.add(n);
				}

				if (waitFor == null || waitFor.equals(msg.getSender())) {
					waitFor = null;

					double offeredPrice = Double.parseDouble(msg.getContent());
					n.setNeedsResponse(true);

					Negotiation bestN = getBestNegotiation();

					// check if counterproposal possible
					if (buying) {
						double newProposal = bestN.getLastOffer() + 1.0;
						if (newProposal < offeredPrice
								&& newProposal < item.getPriceLimit()) {
							proposeDeal(bestN.getAid(), bestN.getUid(),
									newProposal);
							bestN.setLastOffer(newProposal);
							bestN.setNeedsResponse(false);
							waitFor = bestN.getAid();
							return;
						}
					} else {
						double newProposal = bestN.getLastOffer() - 2.0;
						if (newProposal > offeredPrice
								&& newProposal > item.getPriceLimit()) {
							proposeDeal(bestN.getAid(), bestN.getUid(),
									newProposal);
							bestN.setLastOffer(newProposal);
							bestN.setNeedsResponse(false);
							waitFor = bestN.getAid();
							return;
						}
					}

					// otherwise, check if acceptable
					if ((buying && offeredPrice < item.getPriceLimit())
							|| (!buying && offeredPrice > item.getPriceLimit())) {
						acceptDeal(n.getAid(), n.getUid(), offeredPrice);
						done = true;
						for (Negotiation toReject : negotiations) {
							if (toReject != n && toReject.isNeedsResponse()) {
								rejectDeal(toReject.getAid(), toReject.getUid());
							}
						}
						return;
					}

					// otherwise reject
					rejectDeal(n.getAid(), n.getUid());
					negotiations.remove(n);
				}
			} else if (msg.getProtocol().equals("acceptDeal")) {
				if (!msg.getSender().equals(waitFor))
					throw new IllegalStateException(
							"Got accept from someone I'm not waiting for.");
				waitFor = null;
				Negotiation n = getNegotiation(msg.getConversationId());
				if (n == null)
					throw new IllegalStateException(
							"Got accept without offering anything.");
				for (Negotiation toReject : negotiations) {
					if (toReject != n && toReject.isNeedsResponse()) {
						rejectDeal(toReject.getAid(), toReject.getUid());
					}
				}
				double offeredPrice = Double.parseDouble(msg.getContent());
				System.out.println("Deal closed! " + getAID()
						+ (buying ? " bought item from " : " sold item to ")
						+ msg.getSender() + " for " + offeredPrice
						+ " money units. Item was " + item.toString());
				done = true;
			} else if (msg.getProtocol().equals("rejectDeal")) {
				if (!msg.getSender().equals(waitFor))
					throw new IllegalStateException(
							"Got rejection from someone I'm not waiting for.");
				waitFor = null;
				Negotiation n = getNegotiation(msg.getConversationId());
				if (n == null)
					throw new IllegalStateException(
							"Got rejection without offering anything.");
				negotiations.remove(n);
				initiateNextRound();
			}

		}

		private Negotiation getNegotiation(String uid) {
			Negotiation result = null;
			for (Negotiation n : negotiations) {
				if (n.getUid().equals(uid)) {
					result = n;
					break;
				}
			}
			return result;
		}

		private void proposeDeal(AID recipient, String uid, double price) {
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.setReplyWith(uid);
			msg.setConversationId(id);
			msg.addReceiver(recipient);
			msg.setSender(getAID());
			msg.setContent("" + price);
			msg.setProtocol("proposeDeal");
			send(msg);
			System.out.println("Proposing to "+(buying?"buy":"sell")+" item "+item+": "+getAID()+". Partner: "+recipient+". Price: "+price);
		}

		private void acceptDeal(AID recipient, String uid, double price) {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setReplyWith(uid);
			msg.setConversationId(id);
			msg.addReceiver(recipient);
			msg.setSender(getAID());
			msg.setContent("" + price);
			msg.setProtocol("acceptDeal");
			System.out.println("Accepting proposal from "+recipient+" for item "+item+": "+getAID()+". Price: "+price);
			send(msg);
		}

		private void rejectDeal(AID recipient, String uid) {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setReplyWith(uid);
			msg.setConversationId(id);
			msg.addReceiver(recipient);
			msg.setSender(getAID());
			msg.setProtocol("rejectDeal");
			System.out.println("Rejecting proposal from "+recipient+" for item "+item+": "+getAID());
			send(msg);
		}

		private void initiateNextRound() {
			Negotiation bestN = getBestNegotiation();
			if (buying) {
				double newProposal = bestN.getLastOffer() + 1.0;
				if (newProposal < item.getPriceLimit()) {
					proposeDeal(bestN.getAid(), bestN.getUid(), newProposal);
					bestN.setLastOffer(newProposal);
					bestN.setNeedsResponse(false);
					waitFor = bestN.getAid();
					return;
				}
			} else {
				double newProposal = bestN.getLastOffer() - 2.0;
				if (newProposal > item.getPriceLimit()) {
					proposeDeal(bestN.getAid(), bestN.getUid(), newProposal);
					bestN.setLastOffer(newProposal);
					bestN.setNeedsResponse(false);
					waitFor = bestN.getAid();
					return;
				}
			}
		}

		private Negotiation getBestNegotiation() {
			if (buying) {
				double best = Double.MAX_VALUE;
				Negotiation result = null;
				for (Negotiation n : negotiations) {
					if (n.getLastOffer() < best) {
						best = n.getLastOffer();
						result = n;
					}
				}
				return result;
			} else {
				double best = Double.MIN_VALUE;
				Negotiation result = null;
				for (Negotiation n : negotiations) {
					if (n.getLastOffer() > best) {
						best = n.getLastOffer();
						result = n;
					}
				}
				return result;
			}
		}
	}

}
