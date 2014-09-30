package xlong.urlclassify.main;

import java.io.IOException;

import xlong.util.PropertiesUtil;

public class LoadProperties {
	private static String[][] defaultPropertiesList = {
		{"DBpedia_external_links.nt", "E:\\longx\\data\\external_links_en.nt"},
		{"DBpedia_instance_types.nt", "E:\\longx\\data\\instance_types_en.nt"},
		{"DBpedia_ontology.owl", "E:\\longx\\data\\dbpedia_2014.owl"},
		{"mySpliter", " |-| "},
		{"mySpliterReg", " \\|-\\| "},
		};
	public static void loadProperties() throws IOException {
		PropertiesUtil.setDefaultPropertiesList(defaultPropertiesList);
		PropertiesUtil.loadProperties();
	}
}
