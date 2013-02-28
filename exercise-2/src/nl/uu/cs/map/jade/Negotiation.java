package nl.uu.cs.map.jade;

import jade.core.AID;

/**
 * Contains all information concerning one agent's negotiation with another
 * agent for a specific item.
 * 
 */
public class Negotiation {
	private String uid; /* unique identifier of the item */
	private AID aid; /* name of the other agent */
	private double lastOffer; /* last price offered by this agent */
	private boolean needsResponse; /*
									 * specifies if the other agent has to be
									 * responded to
									 */
	private boolean initialSent = false; /*
										 * specifies if at least one message has
										 * been exchanged in this negotiation
										 */

	public Negotiation(String uid, AID aid, double lastOffer,
			boolean needsResponse) {
		super();
		this.uid = uid;
		this.aid = aid;
		this.lastOffer = lastOffer;
		this.needsResponse = needsResponse;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public AID getAid() {
		return aid;
	}

	public void setAid(AID aid) {
		this.aid = aid;
	}

	public double getLastOffer() {
		return lastOffer;
	}

	public void setLastOffer(double lastOffer) {
		this.lastOffer = lastOffer;
	}

	public boolean isNeedsResponse() {
		return needsResponse;
	}

	public void setNeedsResponse(boolean needsResponse) {
		this.needsResponse = needsResponse;
	}

	public boolean isInitialSent() {
		return initialSent;
	}

	public void setInitialSent(boolean initialSent) {
		this.initialSent = initialSent;
	}
}
