package nl.uu.cs.map.jade.agent;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class MatchmakerAgent extends Agent {

	private static final long serialVersionUID = 8758414317735123415L;

	@Override
	protected void setup() {
		// get the arguments passed to this agent like this:
		// java -cp jade.jar:. jade.Boot -gui -host localhost
		// "matchmaker:MatchmakerAgent(arg1,arg2)"
		Object[] args = getArguments();
		for (Object arg : args)
			System.out.println(arg);
		super.setup();
		
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

}
