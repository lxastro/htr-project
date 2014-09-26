package xlong.main;

import java.util.HashMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import xlong.data.IO.UrlMapIO;
import xlong.ontology.OntologyTree;
import xlong.util.MyWriter;
import xlong.util.PropertiesUtil;

public class FindMissingOntologyType {
	public static void main(String[] args) throws Exception{
		PropertiesUtil.init();
		PropertiesUtil.loadProperties();
		
		String ontologyFile = PropertiesUtil.getProperty("DBpedia_ontology.owl");
		OntologyTree tree = OntologyTree.getTree(ontologyFile);
		MyWriter.setFile("result/ontology", false);
		MyWriter.writeln(tree.toString());
		MyWriter.close();
		
		TreeSet<String> types = OntologyTree.getTypes();
		
		HashMap<String, TreeSet<String>> urlMap = UrlMapIO.read("result/UrlMap.txt");
		TreeSet<String> labels = new TreeSet<String>();
		for (Entry<String, TreeSet<String>> en:urlMap.entrySet()) {
			labels.addAll(en.getValue());
		}
		
		labels.removeAll(types);
		for (String label:labels) {
			System.out.println(label);
		}
	}
}
