import jason.asSemantics.*;
import jason.*;
import java.util.*;
import java.io.*;
import java.util.logging.*;

public class TraderAgClass extends Agent {
	
	private Logger logger = Logger.getLogger("exercise-3.mas2j." + TraderAgClass.class.getName());
	
	@Override
	public void initAg() {
		super.initAg();
		
		String agentName = ts.getUserAgArch().getAgName();
		
		// read the properties file
		Properties properties = new Properties();
		Reader reader;
		try {
			reader = new FileReader("resource/" + agentName + ".properties");
			properties.load(reader);
			reader.close();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

		// construct offers and requests
		List<ItemDescriptor> offers = parseItems(properties.getProperty("items.offered"));
		List<ItemDescriptor> requests = parseItems(properties.getProperty("items.requested"));
		
		logger.info("Agent '" + agentName + "' offers " + offers.size() + " items and requests " + requests.size() + " items.");
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

}
