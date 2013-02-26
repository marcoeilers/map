package nl.uu.cs.map.jade;

import jade.core.AID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemDB {

	/**
	 * Mapping from items to traders that either offer these items or request
	 * these items.
	 */
	private final Map<ItemDescriptor, List<AID>> offeredItems, requestedItems;

	private static ItemDB instance;

	public static ItemDB getInstance() {
		if (instance == null)
			instance = new ItemDB();
		return instance;
	}

	private ItemDB() {
		offeredItems = new HashMap<ItemDescriptor, List<AID>>();
		requestedItems = new HashMap<ItemDescriptor, List<AID>>();
	}

	/**
	 * Registers that the specified agent offers the specified item.
	 * 
	 * @param item
	 * @param agent
	 */
	public void addOffer(ItemDescriptor item, AID agent) {
		List<AID> sellers = offeredItems.get(item);
		if (sellers == null)
			sellers = new ArrayList<AID>();
		sellers.add(agent);
	}

	/**
	 * Registers that the specified agent no longer offers the specified item.
	 * 
	 * @param item
	 * @param agent
	 * @throws IllegalArgumentException
	 *             if the item or the seller is not registered
	 */
	public void removeOffer(ItemDescriptor item, AID agent) {
		List<AID> sellers = offeredItems.get(item);
		if (sellers != null)
			if (!sellers.remove(agent))
				throw new IllegalArgumentException(
						"The trader is not registered as a seller for this item");
			else
				throw new IllegalArgumentException(
						"This item is not registered");
	}

	/**
	 * Registers that the specified agent requests the specified item.
	 * 
	 * @param item
	 * @param agent
	 */
	public void addRequest(ItemDescriptor item, AID agent) {
		List<AID> buyers = requestedItems.get(item);
		if (buyers == null)
			buyers = new ArrayList<AID>();
		buyers.add(agent);
	}

	/**
	 * Registers that the specified agent no longer requests the specified item.
	 * 
	 * @param item
	 * @param agent
	 * @throws IllegalArgumentException
	 *             if the item or the buyer is not registered
	 */
	public void removeRequest(ItemDescriptor item, AID agent) {
		List<AID> buyers = requestedItems.get(item);
		if (buyers != null)
			if (!buyers.remove(agent))
				throw new IllegalArgumentException(
						"The trader is not registered as a buyer for this item");
			else
				throw new IllegalArgumentException(
						"This item is not registered");
	}

	/**
	 * Returns the buyers for the specified item, returned list may be empty but
	 * never null.
	 * 
	 * @param item
	 * @return
	 */
	public List<AID> getBuyers(ItemDescriptor item) {
		List<AID> buyers = requestedItems.get(item);
		return buyers != null ? buyers : new ArrayList<AID>();
	}

	/**
	 * Returns the sellers for the specified item, returned list may be empty
	 * but never null.
	 * 
	 * @param item
	 * @return
	 */
	public List<AID> getSellers(ItemDescriptor item) {
		List<AID> sellers = offeredItems.get(item);
		return sellers != null ? sellers : new ArrayList<AID>();
	}

}
