package nl.uu.cs.map.jade;

import jade.core.AID;

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
	private final Map<ItemDescriptor, AID> offeredItems, requestedItems;

	private static ItemDB instance;

	public static ItemDB getInstance() {
		if (instance == null)
			instance = new ItemDB();
		return instance;
	}

	private ItemDB() {
		offeredItems = new HashMap<ItemDescriptor, AID>();
		requestedItems = new HashMap<ItemDescriptor, AID>();
	}

	/**
	 * Registers that the specified agent offers the specified item.
	 * 
	 * @param item
	 * @param agent
	 */
	public void addOffer(ItemDescriptor item, AID agent) {
		offeredItems.put(item, agent);
	}

	/**
	 * Registers that the specified agent no longer offers the specified item.
	 * 
	 * @param item
	 * @param agent
	 */
	public void removeOffer(ItemDescriptor item, AID agent) {
		offeredItems.remove(item);
	}

	/**
	 * Registers that the specified agent requests the specified item.
	 * 
	 * @param item
	 * @param agent
	 */
	public void addRequest(ItemDescriptor item, AID agent) {
		requestedItems.put(item, agent);
	}

	/**
	 * Registers that the specified agent no longer requests the specified item.
	 * 
	 * @param item
	 * @param agent
	 */
	public void removeRequest(ItemDescriptor item, AID agent) {
		requestedItems.remove(item);
	}

	/**
	 * Returns the buyers for the specified item, returned list may be empty but
	 * never null.
	 * 
	 * @param item
	 * @return
	 */
	public List<Entry<String, AID>> getBuyers(ItemDescriptor item) {
		// find possible matching requested items
		List<ItemDescriptor> matchingItems = new ArrayList<ItemDescriptor>();
		for (ItemDescriptor matchingItem : requestedItems.keySet())
			// find all items that have similar or less properties
			// buyers would also buy items that exceed their requirements
			if (item.contains(matchingItem))
				matchingItems.add(matchingItem);

		// get all buyers for these items
		List<Entry<String, AID>> buyers = new ArrayList<Entry<String, AID>>();
		for (ItemDescriptor matchingItem : matchingItems)
			buyers.add(new SimpleEntry<String, AID>(matchingItem.getUid(),
					requestedItems.get(matchingItem)));
		return buyers;
	}

	/**
	 * Returns the sellers for the specified item, returned list may be empty
	 * but never null.
	 * 
	 * @param item
	 * @return
	 */
	public List<Entry<String, AID>> getSellers(ItemDescriptor item) {
		// find possible matching requested items
		List<ItemDescriptor> matchingItems = new ArrayList<ItemDescriptor>();
		for (ItemDescriptor matchingItem : offeredItems.keySet())
			// find all items that have similar or less properties
			// sellers would also sell items that exceed their buyers'
			// requirements
			if (matchingItem.contains(item))
				matchingItems.add(matchingItem);

		// get all sellers for these items
		List<Entry<String, AID>> sellers = new ArrayList<Entry<String, AID>>();
		for (ItemDescriptor matchingItem : matchingItems)
			sellers.add(new SimpleEntry<String, AID>(matchingItem.getUid(),
					offeredItems.get(matchingItem)));
		return sellers;
	}

}
