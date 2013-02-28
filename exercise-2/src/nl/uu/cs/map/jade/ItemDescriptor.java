package nl.uu.cs.map.jade;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

/**
 * Describes an item offered or requested in the market place.
 * 
 */
public class ItemDescriptor implements Serializable {

	private static final long serialVersionUID = -3595604261119043714L;

	private Map<String, String> attributes = new HashMap<String, String>();
	private String uid;

	private double priceLimit;

	public ItemDescriptor() {
		UUID id = UUID.randomUUID();
		uid = id.toString();
	}

	public ItemDescriptor(String... strings) {
		this();
		if (strings.length % 2 != 0)
			throw new IllegalArgumentException("Missing value for a key: "
					+ strings);
		for (int i = 0; i < strings.length; i += 2) {
			attributes.put(strings[i], strings[i + 1]);
		}
	}

	public ItemDescriptor(double limit) {
		this();
		this.priceLimit = limit;
	}

	public ItemDescriptor(double limit, String... strings) {
		this(strings);
		this.priceLimit = limit;
	}

	/**
	 * Returns an attribute of this item, if available
	 * 
	 * @param key
	 *            the name of the attribute
	 * @return
	 */
	public String getAttribute(String key) {
		return attributes.get(key);
	}

	/**
	 * Sets an attribute of this item.
	 * 
	 * @param key
	 * @param value
	 */
	public void setAttribute(String key, String value) {
		attributes.put(key, value);
	}

	/**
	 * Checks if this item has a specific attribute
	 * 
	 * @param key
	 * @return
	 */
	public boolean hasAttribute(String key) {
		return attributes.containsKey(key);
	}

	/**
	 * Returns a set consisting all attributes of this item as Entries.
	 * 
	 * @return
	 */
	public Set<Entry<String, String>> getAttributes() {
		return attributes.entrySet();
	}

	/**
	 * Sets the maximal/minimal price for this item.
	 * 
	 * @param limit
	 */
	public void setPriceLimit(double limit) {
		this.priceLimit = limit;
	}

	/**
	 * Gets the maximal/minimal price for this item, respectively, if this item
	 * is requested/offered.
	 * 
	 * @return
	 */
	public double getPriceLimit() {
		return priceLimit;
	}

	/**
	 * Gets a string containing a unique identifier of this item descriptor.
	 * 
	 * @return
	 */
	public String getUid() {
		return uid;
	}

	/**
	 * Returns true iff this ItemDescriptor contains all of the other one's
	 * values.
	 * 
	 * @param item
	 * @return
	 */
	public boolean contains(ItemDescriptor item) {
		boolean contains = true;
		for (Entry<String, String> entry : item.getAttributes()) {
			if (hasAttribute(entry.getKey()))
				contains &= entry.getValue().equals(
						getAttribute(entry.getKey()));
			else
				contains = false;
		}
		return contains;
	}

	/**
	 * Returns the contents of the "type" attribute. Can be used as a shorthand
	 * identifier for this item. Returns an empty string if the attribute does
	 * not exist.
	 * 
	 * @return
	 */
	public String getType() {
		String result = attributes.get("type");
		return result == null ? "" : result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(uid);
		sb.append(" { ");
		for (Entry<String, String> entry : attributes.entrySet()) {
			sb.append(" > ");
			sb.append(entry.getKey());
			sb.append(" : ");
			sb.append(entry.getValue());
		}
		sb.append(" }");
		return sb.toString();
	}
}
