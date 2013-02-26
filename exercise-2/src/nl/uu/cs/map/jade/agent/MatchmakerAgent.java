package nl.uu.cs.map.jade.agent;

import jade.core.Agent;

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
	}

}
