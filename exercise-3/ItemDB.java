import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The database of requested and offered items.
 *
 */
public class ItemDB {

	/**
	 * Mapping from items to traders that either offer these items or request
	 * these items.
	 */
	private final Map<ItemDescriptor, List<String>> offeredItems, requestedItems;

	private static ItemDB instance;

	public static ItemDB getInstance() {
		if (instance == null)
			instance = new ItemDB();
		return instance;
	}

	private ItemDB() {
		offeredItems = new HashMap<ItemDescriptor, List<String>>();
		requestedItems = new HashMap<ItemDescriptor, List<String>>();
	}

	/**
	 * Registers that the specified agent offers the specified item.
	 * 
	 * @param item
	 * @param agent
	 */
	public void addOffer(ItemDescriptor item, String agent) {
		List<String> sellers = offeredItems.get(item);
		if(sellers == null)
			sellers = new ArrayList<String>();
		sellers.add(agent);
		offeredItems.put(item, sellers);
	}

	/**
	 * Registers that the specified agent no longer offers the specified item.
	 * 
	 * @param item
	 * @param agent
	 */
	public void removeOffer(ItemDescriptor item, String agent) {
		offeredItems.get(item).remove(agent);
	}

	/**
	 * Registers that the specified agent requests the specified item.
	 * 
	 * @param item
	 * @param agent
	 */
	public void addRequest(ItemDescriptor item, String agent) {
		List<String> buyers = requestedItems.get(item);
		if(buyers == null)
			buyers = new ArrayList<String>();
		buyers.add(agent);
		requestedItems.put(item, buyers);
	}

	/**
	 * Registers that the specified agent no longer requests the specified item.
	 * 
	 * @param item
	 * @param agent
	 */
	public void removeRequest(ItemDescriptor item, String agent) {
		requestedItems.get(item).remove(agent);
	}

	/**
	 * Returns the buyers for the specified item, returned list may be empty but
	 * never null.
	 * 
	 * @param offer
	 * @return
	 */
	public List<String> getBuyers(ItemDescriptor offer) {
		List<String> buyers = requestedItems.get(offer);
		return buyers == null ? new ArrayList<String>() : buyers;
	}

	/**
	 * Returns the sellers for the specified item, returned list may be empty
	 * but never null.
	 * 
	 * @param request
	 * @return
	 */
	public List<String> getSellers(ItemDescriptor request) {
		List<String> sellers = offeredItems.get(request);
		return sellers == null ? new ArrayList<String>() : sellers;
	}

}
