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

	public String getAttribute(String key) {
		return attributes.get(key);
	}

	public boolean hasAttribute(String key) {
		return attributes.containsKey(key);
	}

	public void setAttribute(String key, String value) {
		attributes.put(key, value);
	}

	public Set<Entry<String, String>> getAttributes() {
		return attributes.entrySet();
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

	public String getUid() {
		return uid;
	}

	public ItemDescriptor() {
		UUID id = UUID.randomUUID();
		uid = id.toString();
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
}
