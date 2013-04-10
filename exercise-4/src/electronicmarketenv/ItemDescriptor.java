package electronicmarketenv;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import apapl.data.APLIdent;
import apapl.data.APLList;
import apapl.data.Term;

/**
 * Describes an item offered or requested in the market place.
 * 
 */
public class ItemDescriptor implements Serializable {

	private static final long serialVersionUID = -3595604261119043714L;

	private List<String> attributes = new ArrayList<String>();

	private double priceLimit;

	/**
	 * Adds an attribute to this item.
	 * 
	 * @param a
	 */
	public void addAttribute(String a) {
		attributes.add(a);
	}

	/**
	 * Checks if this item has a specific attribute.
	 * 
	 * @param a
	 * @return
	 */
	public boolean hasAttribute(String a) {
		return attributes.contains(a);
	}

	/**
	 * Returns a copy of the attributes of this item.
	 * 
	 * @return
	 */
	public List<String> getAttributes() {
		return new ArrayList<String>(attributes);
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
	 * Returns an APLList of attributes (excluding the price limit).
	 * 
	 * @return
	 */
	public APLList toAPLList() {
		APLIdent[] attributes = new APLIdent[this.attributes.size()];
		for (int i = 0; i < this.attributes.size(); ++i)
			attributes[i] = new APLIdent(this.attributes.get(i));
		return new APLList(attributes);
	}

	/**
	 * Constructs an item from the specified APLList of attributes.
	 * 
	 * @param list
	 * @return
	 */
	public static ItemDescriptor fromAPLList(APLList list) {
		ItemDescriptor id = new ItemDescriptor();
		Term head = null;
		while (list != null && (head = list.getHead()) != null) {
			if (!(head instanceof APLIdent))
				throw new IllegalArgumentException(
						"Expecting the attributes to be APLIdents.");
			id.addAttribute(((APLIdent) head).getName());
			list = (APLList) list.getTail();
		}
		return id;
	}

	/**
	 * Two items are equal if they have the same set of attributes in the same
	 * order (regardless of the price limit).
	 * 
	 * @param o
	 * @return
	 */
	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;

		if (!(o instanceof ItemDescriptor))
			return false;

		ItemDescriptor id = (ItemDescriptor) o;

		if (id.getAttributes().size() != attributes.size())
			return false;

		for (int i = 0; i < id.getAttributes().size(); ++i)
			if (!id.getAttributes().get(i).equals(attributes.get(i)))
				return false;

		return true;
	}

	/**
	 * @see equals
	 */
	@Override
	public int hashCode() {
		return toString().split("@")[0].hashCode();
	}

	/**
	 * Creates a human-readable string representation of this item.
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		String prefix = "";
		for (String attribute : attributes) {
			sb.append(prefix);
			sb.append(attribute);
			prefix = ", ";
		}
		sb.append("}@");
		sb.append(priceLimit);
		return sb.toString();
	}
}
