package nl.uu.cs.map.jade;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Describes an item offered or requested in the market place.
 *
 */
public class ItemDescriptor implements Serializable {
	
	private Map<String, String> attributes = new HashMap<String, String>();
	
	public String getAttribute(String key){
		return attributes.get(key);
	}
	
	public boolean hasAttribute(String key){
		return attributes.containsKey(key);
	}
	
	public ItemDescriptor(String... strings){
		if (strings.length%2 != 0)
			throw new IllegalArgumentException("Missing value for a key: "+strings);
		for(int i=0; i<strings.length; i+=2){
			attributes.put(strings[i], strings[i+1]);
		}
	}
}
