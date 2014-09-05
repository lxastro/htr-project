package xlong.ontology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeSet;
import java.util.Map.Entry;

/**
 * 
 * @author longx
 *
 */
public final class OntologyTree implements Comparable<OntologyTree>  {
	/** */
	private final String name;
	/** */
	private final TreeSet<OntologyTree> sons;
	/** */
	private final TreeSet<OntologyTree> parents;
	/** */
	private static TreeSet<String> types;
	
	/**
	 * @param name name
	 */
	private OntologyTree(final String name) {
		this.name = name;
		this.parents = new TreeSet<OntologyTree>();
		this.sons = new TreeSet<OntologyTree>();
	}
	
	/**
	 * @param subClassOfMap subClassOfMap
	 */
	public static void calTypes(final Map<String, HashSet<String>> subClassOfMap) {
		types = new TreeSet<String>();
		for (Entry<String, HashSet<String>> en : subClassOfMap.entrySet()) {
			types.add(en.getKey());
			for (String type:en.getValue()) {
				types.add(type);
			}
		}
	}

	/**

	 * @param subClassOfMap subClassOfMap
	 * @return ontologyTree
	 */
	public static OntologyTree getTree(final Map<String, HashSet<String>> subClassOfMap) {
		calTypes(subClassOfMap);
		OntologyTree root = new OntologyTree("root");
		Map<String, HashSet<String>> parents = new HashMap<String, HashSet<String>>();
		for (String type:types) {
			parents.put(type, new HashSet<String>());
			parents.get(type).add("root");
		}
		for (Entry<String, HashSet<String>> en : subClassOfMap.entrySet()) {
			parents.get(en.getKey()).addAll(en.getValue());
		}
		HashSet<String> oldAdded = new HashSet<String>();
		oldAdded.add("root");
		HashSet<String> newAdded = new HashSet<String>();
		newAdded.add("root");		
		HashSet<String> oldEdge = new HashSet<String>();
		oldEdge.add("root");
		HashSet<String> newEdge = new HashSet<String>();
		Map<String, OntologyTree> htMap = new HashMap<String, OntologyTree>();
		htMap.put("root", root);
		
		while (true) {
			int cnt = 0;
			for (String type:types) {
				if ((!oldAdded.contains(type)) && oldAdded.containsAll(parents.get(type))) {
					cnt++;
					OntologyTree son = new OntologyTree(type);
					htMap.put(type, son);
					ArrayList<String> dirs = new ArrayList<String>(oldEdge);
					dirs.retainAll(parents.get(type));
					for (String dir:dirs) {
						htMap.get(dir).addSon(son);
					}
					newAdded.add(type);
					newEdge.add(type);
				}
			}
			if (cnt == 0) {
				break;
			}
			oldEdge = newEdge;
			newEdge = new HashSet<String>();
			oldAdded = newAdded;
			newAdded = new HashSet<String>(oldAdded);
		}
		
		return root;
	}
	
	/**
	 * @param son the son
	 */
	public void addSon(final OntologyTree son) {
		sons.add(son);
		son.parents.add(this);
	}

	/**
	 * @return the name
	 */
	public String getTypeName() {
		return name;
	}
	
	/**
	 * 
	 * @return sons
	 */
	public TreeSet<OntologyTree> getSons() {
		return sons;
	}
	
	/**
	 * 
	 * @param level level
	 * @return String
	 */
	private String toString(final int level) {
		String str = "";
		for (int i = 0; i < level; i++) {
			str += "    ";
		}
		str += name + "\n";
		for (OntologyTree son:sons) {
			str += son.toString(level + 1);
		}
		return str;
	}
	
	@Override
	public String toString() {
		return toString(0);
	} 
	
	@Override
	public int compareTo(final OntologyTree o) {
		return this.name.compareTo(o.name);
	}
}
