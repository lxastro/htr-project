package xlong.urlclassify.main;

import java.util.Collection;
import java.util.HashMap;
import java.util.TreeSet;

import xlong.urlclassify.data.Entity;
import xlong.urlclassify.data.IO.NTripleReader;
import xlong.urlclassify.data.IO.UrlEntityMapIO;
import xlong.urlclassify.data.IO.UrlMapIO;
import xlong.urlclassify.data.filter.ExistUrlFilter;
import xlong.urlclassify.data.filter.UrlMapFilter;
import xlong.urlclassify.data.processer.SimplifyProcesser;
import xlong.urlclassify.data.processer.Triple2PairProcesser;
import xlong.urlclassify.data.processer.UrlNormalizeProcesser;
import xlong.util.PropertiesUtil;
import xlong.wm.ontology.OntologyTree;

public class GetDataFile {
public static void main(String[] args) throws Exception{
		
		// ----------------------------Data process---------------------------------
		// Get properties.
		PropertiesUtil.showDefaultFile();
		PropertiesUtil.loadProperties();
		String typeFile = PropertiesUtil.getProperty("DBpedia_instance_types.nt");
		String urlFile = PropertiesUtil.getProperty("DBpedia_external_links.nt");
		String ontologyFile = PropertiesUtil.getProperty("DBpedia_ontology.owl");
		String typePairFile = "result/typePair.txt";
		String urlPairFile = "result/urlPair.txt";
		
		Collection<Entity> entities;
		HashMap<String, TreeSet<String>> urlMap;
		HashMap<String, TreeSet<Entity>> urlEntityMap;
		OntologyTree tree = OntologyTree.getTree(ontologyFile);
		
		// Read data file.
		NTripleReader typeReader = new NTripleReader(typeFile
				, new SimplifyProcesser(new UrlNormalizeProcesser(new Triple2PairProcesser())));
		NTripleReader urlReader = new NTripleReader(urlFile
				, new SimplifyProcesser(new UrlNormalizeProcesser(new Triple2PairProcesser())));
		typeReader.readAll(typePairFile);
		urlReader.readAll(urlPairFile);

		// Generate Entities.
		System.out.println("Generate Entities");
		// entities = Entity.generateEntities(typePairFile, urlPairFile);
		entities = Entity.generateEntities(typePairFile, urlPairFile, tree);
		System.out.println(entities.size());
		entities = Entity.filtEntities(entities, new ExistUrlFilter(new xlong.urlclassify.data.filter.SingleTypeFilter()));
		// entities = Entity.filtEntities(entities, new ExistUrlFilter(new xlong.data.filter.MultipleTypeFilter()));
		System.out.println(entities.size());
		Entity.write(entities, "result/entities.txt");	
		entities = null;
		entities = Entity.read("result/entities.txt");
		System.out.println(entities.size());
		
		// Get UrlEntity map. For test.
		urlEntityMap = Entity.entities2UrlEntityMap(entities);
		UrlEntityMapIO.writeOverlapUrl(urlEntityMap, "result/overlap.txt");
		
		// Get url map.
		System.out.println("Get URL Map");
		urlMap =  Entity.entities2UrlMap(entities);
		entities = null;
		System.out.println(urlMap.size());
		urlMap = UrlMapFilter.filterUrlMap(urlMap);
		System.out.println(urlMap.size());
		UrlMapIO.write(urlMap, "result/UrlMap.txt");
	}
}
