package nl.uu.cs.map.jade;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Describes an item offered or requested in the market place.
 *
 */
public class ItemDescriptor implements Serializable {
	
	private Map<String, String> attributes = new HashMap<String, String>();
	private String uid;
	
	public String getAttribute(String key){
		return attributes.get(key);
	}
	
	public boolean hasAttribute(String key){
		return attributes.containsKey(key);
	}
	
	public void setAttribute(String key, String value){
		attributes.put(key, value);
	}
	
	public ItemDescriptor(String... strings){
		this();
		if (strings.length%2 != 0)
			throw new IllegalArgumentException("Missing value for a key: "+strings);
		for(int i=0; i<strings.length; i+=2){
			attributes.put(strings[i], strings[i+1]);
		}
	}
	
	public String getUid(){
		return uid;
	}
	
	public ItemDescriptor(){
		UUID id = UUID.randomUUID();
		uid=id.toString();
	}
}
