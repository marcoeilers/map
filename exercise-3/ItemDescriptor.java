import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.*;
import java.util.UUID;
import jason.asSyntax.*;

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
	
	public void setUid(String uid) {
		this.uid = uid;
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
		sb.append("@");
		sb.append(priceLimit);
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
	
	public List<Term> toTerms() {
		List<Term> terms = new ArrayList<Term>();
		terms.add(new StringTermImpl(getUid()));
		terms.add(new NumberTermImpl(getPriceLimit()));
		for(Entry<String, String> attribute : getAttributes()) {
			terms.add(new StringTermImpl(attribute.getKey()));
			terms.add(new StringTermImpl(attribute.getValue()));
		}
		return terms;
	}
	
	public static ItemDescriptor fromTerms(List<Term> terms) {
		if(terms.size() < 2 || !(terms.get(0) instanceof StringTerm) || !(terms.get(1) instanceof NumberTerm))
			throw new IllegalArgumentException("Expecting the first term to be the Uid and the second term to be the price limit.");
		if(terms.size() % 2 == 1)
			throw new IllegalArgumentException("Expecting an even number of strings as key-value pairs.");
		
		ItemDescriptor id = new ItemDescriptor();
		id.setUid(((StringTerm) terms.get(0)).getString());
		id.setPriceLimit(((NumberTerm) terms.get(1)).solve());
		for(int i = 2; i < terms.size(); i += 2) {
			if(!(terms.get(i) instanceof StringTerm) || !(terms.get(i+1) instanceof StringTerm))
				throw new IllegalArgumentException("Expecting the key-value pairs to consist of strings.");
			String key = ((StringTerm) terms.get(i)).getString(), value = ((StringTerm) terms.get(i+1)).getString();
			id.setAttribute(key, value);
		}
		
		return id;
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof ItemDescriptor))
			return false;
		
		ItemDescriptor id = (ItemDescriptor) o;
		if(!id.getUid().equals(uid))
			return false;
		
		if(Double.compare(id.getPriceLimit(), priceLimit) != 0)
			return false;
		
		if(id.getAttributes().size() != attributes.size())
			return false;
		
		for(Entry<String, String> attribute : id.getAttributes())
			if(!hasAttribute(attribute.getKey()))
				return false;
			else if(!getAttribute(attribute.getKey()).equals(attribute.getValue()))
				return false;
			
		return true;
	}
}
