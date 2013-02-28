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
		if (args.length != 2)
			throw new IllegalArgumentException(
					"Expecting exactly two arguments: the name of the initialization file and the time when to enter the market place in ms");

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
		offers = parseItems(properties.getProperty("items.offered"));
		requests = parseItems(properties.getProperty("items.requested"));

		try {
			Thread.sleep(Integer.parseInt((String)args[1]));
		} catch (NumberFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
						"Expecting exactly one matchmaker but found "
								+ matchmakers.length);

			// register offered and requested items
			ACLMessage registerOffersMsg = new ACLMessage(ACLMessage.INFORM);
			registerOffersMsg.addReceiver(matchmakers[0].getName());
			registerOffersMsg.setSender(getAID());
			registerOffersMsg.setProtocol("registerOffers"); // TODO:
																// change?
			try {
				registerOffersMsg.setContentObject((Serializable) offers);
				send(registerOffersMsg);
				//System.out.println("Registering offers: "
				//		+ getAID().getLocalName());
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
				//System.out.println("Registering requests: "
				//		+ getAID().getLocalName());
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
					//System.out.println("Getting offers for item " + i.getType()
					//		+ ": " + getAID().getLocalName());
					addBehaviour(new NegotiationBehaviour(i, true));
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
					//System.out.println("Getting requests for item "
					//		+ i.getType() + ": " + getAID().getLocalName());
					addBehaviour(new NegotiationBehaviour(i, false));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (FIPAException e) {
			e.printStackTrace();
		}

	}

	private List<ItemDescriptor> parseItems(String itemsString) {
		List<ItemDescriptor> items = new ArrayList<ItemDescriptor>();
		if (itemsString != null) {
			// items are separated by vertical bars
			String[] splitItems = itemsString.split("\\|");
			for (String itemString : splitItems) {
				ItemDescriptor item = new ItemDescriptor();
				boolean hasPriceLimit = false;

				// attributes are separated by semicolons
				String[] attributes = itemString.split(";");
				for (String attribute : attributes) {

					// attribute key and value are separated by colons
					String[] keyValue = attribute.split(":");
					if ("priceLimit".equals(keyValue[0])) {
						// handle the price limit property
						item.setPriceLimit(Double.parseDouble(keyValue[1]));
						hasPriceLimit = true;
					} else
						item.setAttribute(keyValue[0].trim(),
								keyValue[1].trim());
				}
				if (!hasPriceLimit)
					throw new IllegalArgumentException(
							"Missing priceLimit property for item");
				items.add(item);
			}
		}
		return items;
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
			ACLMessage msg = TraderAgent.this.receive(tmpl);
			if (msg != null) {
				// if response to getOffers/getRequests
				if (msg.getProtocol().equals("setOffers")
						|| msg.getProtocol().equals("setRequests")) {
					try {
						List<Entry<String, AID>> partners = (List<Entry<String, AID>>) msg
								.getContentObject();

						// create negotiation information
						for (Entry<String, AID> e : partners) {
							Negotiation n = new Negotiation(e.getKey(),
									e.getValue(),
									buying ? 0.5 * item.getPriceLimit()
											: 2.0 * item.getPriceLimit(), false);
							if (getNegotiation(e.getKey()) == null)
								negotiations.add(n);
						}

						// start by messaging one of them
						if (waitFor == null)
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
								buying ? 0.5 * item.getPriceLimit()
										: 2.0 * item.getPriceLimit(), true);
						negotiations.add(n);
					}
					// System.out.println("Got proposeDeal for "+ item.getType()
					// +": "+getAID().getLocalName());

					if (waitFor == null || waitFor.equals(msg.getSender())) {
						waitFor = null;

						double offeredPrice = Double.parseDouble(msg
								.getContent());
						n.setNeedsResponse(true);

						Negotiation bestN = getBestNegotiation();

						// check if counterproposal possible
						if (buying) {
							double newProposal = bestN.getLastOffer() + 1.0;
							if (newProposal < offeredPrice
									&& newProposal <= item.getPriceLimit()) {
								proposeDeal(bestN.getAid(), bestN.getUid(),
										newProposal);
								// System.out.println("Counterproposing to " +
								// (buying ? "buy" : "sell")
								// + " item " + item.getType() + ": " +
								// getAID().getLocalName() + ". Partner: "
								// + bestN.getAid().getLocalName() + ". Price: "
								// + newProposal);
								bestN.setLastOffer(newProposal);
								bestN.setNeedsResponse(false);
								waitFor = bestN.getAid();
								return;
							}
						} else {
							double newProposal = bestN.getLastOffer() - 1.0;
							if (newProposal > offeredPrice
									&& newProposal >= item.getPriceLimit()) {
								proposeDeal(bestN.getAid(), bestN.getUid(),
										newProposal);
								// System.out.println("Counterproposing to " +
								// (buying ? "buy" : "sell")
								// + " item " + item.getType() + ": " +
								// getAID().getLocalName() + ". Partner: "
								// + bestN.getAid().getLocalName() + ". Price: "
								// + newProposal);
								bestN.setLastOffer(newProposal);
								bestN.setNeedsResponse(false);
								waitFor = bestN.getAid();
								return;
							}
						}

						// otherwise, check if acceptable
						if ((buying && offeredPrice <= item.getPriceLimit())
								|| (!buying && offeredPrice >= item
										.getPriceLimit())) {
							acceptDeal(n.getAid(), n.getUid(), offeredPrice);
							done = true;
							for (Negotiation toReject : negotiations) {
								if (toReject != n && toReject.isNeedsResponse()) {
									rejectDeal(toReject.getAid(),
											toReject.getUid());
									System.out
											.println("Rejecting proposal from "
													+ toReject.getAid()
															.getLocalName()
													+ " for item "
													+ item.getType()
													+ ": "
													+ getAID().getLocalName()
													+ " because different offer was accepted.");
								}
							}
							return;
						}

						// otherwise reject
						rejectDeal(n.getAid(), n.getUid());
						System.out.println("Rejecting proposal from "
								+ n.getAid().getLocalName() + " for item "
								+ item.getType() + ": "
								+ getAID().getLocalName()
								+ " because otherwise condition; "
								+ (buying ? "wasBuying" : "wasSelling")
								+ ", last offer was " + n.getLastOffer());
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
					System.out
							.println("Deal closed! "
									+ getAID().getLocalName()
									+ (buying ? " bought item from "
											: " sold item to ")
									+ msg.getSender().getLocalName() + " for "
									+ offeredPrice + " money units. Item was "
									+ item.getType());
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
			}else{
				block();
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
		}

		private void acceptDeal(AID recipient, String uid, double price) {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setReplyWith(uid);
			msg.setConversationId(id);
			msg.addReceiver(recipient);
			msg.setSender(getAID());
			msg.setContent("" + price);
			msg.setProtocol("acceptDeal");
			System.out.println("Accepting proposal from "
					+ recipient.getLocalName() + " for item " + item.getType()
					+ ": " + getAID().getLocalName() + ". Price: " + price);
			send(msg);
		}

		private void rejectDeal(AID recipient, String uid) {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setReplyWith(uid);
			msg.setConversationId(id);
			msg.addReceiver(recipient);
			msg.setSender(getAID());
			msg.setProtocol("rejectDeal");
			send(msg);
		}

		private void initiateNextRound() {
			Negotiation bestN = getBestNegotiation();
			if (bestN != null) {
				if (buying) {
					double newProposal = bestN.getLastOffer() + 1.0;
					if (newProposal <= item.getPriceLimit()) {
						proposeDeal(bestN.getAid(), bestN.getUid(), newProposal);
						// System.out.println("Initiative: Proposing to " +
						// (buying ? "buy" : "sell")
						// + " item " + item.getType() + ": " +
						// getAID().getLocalName() + ". Partner: "
						// + bestN.getAid().getLocalName() + ". Price: " +
						// newProposal);
						bestN.setLastOffer(newProposal);
						bestN.setNeedsResponse(false);
						waitFor = bestN.getAid();
						return;
					}
				} else {
					double newProposal = bestN.getLastOffer() - 1.0;
					if (newProposal >= item.getPriceLimit()) {
						proposeDeal(bestN.getAid(), bestN.getUid(), newProposal);
						// System.out.println("Initiative: Proposing to " +
						// (buying ? "buy" : "sell")
						// + " item " + item.getType() + ": " +
						// getAID().getLocalName() + ". Partner: "
						// + bestN.getAid().getLocalName() + ". Price: " +
						// newProposal);
						bestN.setLastOffer(newProposal);
						bestN.setNeedsResponse(false);
						waitFor = bestN.getAid();
						return;
					}
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
