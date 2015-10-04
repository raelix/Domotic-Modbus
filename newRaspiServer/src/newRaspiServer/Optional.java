package newRaspiServer;

import java.util.HashMap;

public class Optional {
private String name;
private String type;
private HashMap<String, String> map;

	
	
	public Optional(String name,String type){
		this.setName(name);
		this.setType(type);
		this.map = new HashMap<>();
	}
	
	public void setKeyValue(String key,String value){
		map.put(key, value);
	}
	
	public String getValue(String key){
	return map.get(key);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
}
