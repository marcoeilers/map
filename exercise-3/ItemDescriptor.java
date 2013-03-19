import java.io.Serializable;
import java.util.*;
import java.util.UUID;
import jason.asSyntax.*;

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
	 * Checks if this item has a specific attribute
	 * 
	 * @param a
	 * @return
	 */
	public boolean hasAttribute(String a) {
		return attributes.contains(a);
	}
	
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
	
	public Term toTerm() {
		ListTerm attributeTerms = new ListTermImpl();
		for(String attribute : attributes)
			attributeTerms.add(new StringTermImpl(attribute));
		return attributeTerms;
	}
	
	public static ItemDescriptor fromTerm(Term term) {
		if(!(term instanceof ListTerm))
			throw new IllegalArgumentException("The term has to be an attribute list instead of " + term.getClass().getName() + ".");
		
		ItemDescriptor id = new ItemDescriptor();
		for(Term attribute : ((ListTerm) term)) {
			if(!(attribute instanceof StringTerm))
				throw new IllegalArgumentException("Expecting the attributes to be strings.");
			id.addAttribute(((StringTerm) attribute).getString());
		}
		return id;
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof ItemDescriptor))
			return false;
		
		ItemDescriptor id = (ItemDescriptor) o;
		
		if(Double.compare(id.getPriceLimit(), priceLimit) != 0)
			return false;
		
		if(id.getAttributes().size() != attributes.size())
			return false;
		
		for(String attribute : id.getAttributes())
			if(!hasAttribute(attribute))
				return false;
			
		return true;
	}
	
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
