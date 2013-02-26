package nl.uu.cs.map.jade.agent;

import java.util.List;

import nl.uu.cs.map.jade.ItemDescriptor;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;

public class TraderAgent extends Agent {
	private static final long serialVersionUID = 3698872544683250437L;

	private List<ItemDescriptor> offers;
	private List<ItemDescriptor> requests;
	
	@Override
	protected void setup() {
		addBehaviour(new ContactMatchmakerBehaviour());
		addBehaviour(new MessageRespondBehaviour());
	}
	
	/**
	 * Registers all offered and requested items with the matchmaker service.
	 * Subsequently requests all relevant information from the matchmaker service.
	 *
	 */
	private class ContactMatchmakerBehaviour extends Behaviour {
		private static final long serialVersionUID = -2214859914596959774L;

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
	
	/**
	 * Responds to matchmaker messages by contacting other trade agents,
	 * to trade agent messages by accepting their offer or proposing a modified deal.
	 *
	 */
	private class MessageRespondBehaviour extends Behaviour {
		private static final long serialVersionUID = -3467911981451330057L;

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
