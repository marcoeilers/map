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

	public String getAttribute(String key) {
		return attributes.get(key);
	}

	public void setAttribute(String key, String value) {
		attributes.put(key, value);
	}

	public boolean hasAttribute(String key) {
		return attributes.containsKey(key);
	}

	public Set<Entry<String, String>> getAttributes() {
		return attributes.entrySet();
	}

	public void setPriceLimit(double limit) {
		this.priceLimit = limit;
	}

	public double getPriceLimit() {
		return priceLimit;
	}

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
