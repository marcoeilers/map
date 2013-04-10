package electronicmarketenv;

import apapl.Environment;
import apapl.ExternalActionFailedException;
import apapl.data.APLIdent;
import apapl.data.APLList;
import apapl.data.APLListVar;
import apapl.data.Term;

public class ElectronicMarketEnv extends Environment {

	public static void main(String args[]) {
		// to generate the MANIFEST.MF appropriately during Runnable JAR File
		// Export
	}

	@Override
	protected void addAgent(String agent) {
		System.out.println("Adding " + agent);
	}

	@Override
	protected void removeAgent(String agent) {
		System.out.println("Removing " + agent);
	}

	/* --- External Actions --- */

	public Term sayHello(String agent) throws ExternalActionFailedException {
		System.out.println(agent + " says Hello");
		return wrapBoolean(true);
	}

	/* --- Utils --- */

	private APLListVar wrapBoolean(boolean b) {
		return new APLList(new Term[] { b ? new APLIdent("true")
				: new APLIdent("false") });
	}

}
