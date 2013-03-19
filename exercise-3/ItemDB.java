import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ItemDB {

	/**
	 * Mapping from items to traders that either offer these items or request
	 * these items.
	 */
	private final Map<ItemDescriptor, String> offeredItems, requestedItems;

	private static ItemDB instance;

	public static ItemDB getInstance() {
		if (instance == null)
			instance = new ItemDB();
		return instance;
	}

	private ItemDB() {
		offeredItems = new HashMap<ItemDescriptor, String>();
		requestedItems = new HashMap<ItemDescriptor, String>();
	}

	/**
	 * Registers that the specified agent offers the specified item.
	 * 
	 * @param item
	 * @param agent
	 */
	public void addOffer(ItemDescriptor item, String agent) {
		offeredItems.put(item, agent);
	}

	/**
	 * Registers that the specified agent no longer offers the specified item.
	 * 
	 * @param item
	 * @param agent
	 */
	public void removeOffer(ItemDescriptor item, String agent) {
		offeredItems.remove(item);
	}

	/**
	 * Registers that the specified agent requests the specified item.
	 * 
	 * @param item
	 * @param agent
	 */
	public void addRequest(ItemDescriptor item, String agent) {
		requestedItems.put(item, agent);
	}

	/**
	 * Registers that the specified agent no longer requests the specified item.
	 * 
	 * @param item
	 * @param agent
	 */
	public void removeRequest(ItemDescriptor item, String agent) {
		requestedItems.remove(item);
	}

	/**
	 * Returns the buyers for the specified item, returned list may be empty but
	 * never null.
	 * 
	 * @param offer
	 * @return
	 */
	public List<String> getBuyers(ItemDescriptor offer) {
		// find possible matching requested items
		List<ItemDescriptor> matchingItems = new ArrayList<ItemDescriptor>();
		for (ItemDescriptor request : requestedItems.keySet())
			if (offer.equals(request))
				matchingItems.add(request);

		// get all buyers for these items
		List<String> buyers = new ArrayList<String>();
		for (ItemDescriptor matchingItem : matchingItems)
			buyers.add(requestedItems.get(matchingItem));
		return buyers;
	}

	/**
	 * Returns the sellers for the specified item, returned list may be empty
	 * but never null.
	 * 
	 * @param request
	 * @return
	 */
	public List<String> getSellers(ItemDescriptor request) {
		// find possible matching requested items
		List<ItemDescriptor> matchingItems = new ArrayList<ItemDescriptor>();
		for (ItemDescriptor offer : offeredItems.keySet())
			if (offer.equals(request))
				matchingItems.add(offer);

		// get all sellers for these items
		List<String> sellers = new ArrayList<String>();
		for (ItemDescriptor matchingItem : matchingItems)
			sellers.add(offeredItems.get(matchingItem));
		return sellers;
	}

}
