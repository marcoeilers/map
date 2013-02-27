package nl.uu.cs.map.jade;

import jade.core.AID;

public class Negotiation {
	private String uid;
	private AID aid;
	private double lastOffer;
	private boolean needsResponse;

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
}
