package xlong.main;

import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.TreeSet;

import weka.core.Instances;
import xlong.classifier.converter.SparseVectorSampleToWekaInstanceConverter;
import xlong.data.Entity;
import xlong.data.IO.NTripleReader;
import xlong.data.IO.UrlEntityMapIO;
import xlong.data.IO.UrlMapIO;
import xlong.data.filter.ExistTypeFilter;
import xlong.data.filter.ExistUrlFilter;
import xlong.data.processer.SimplifyProcesser;
import xlong.data.processer.Triple2PairProcesser;
import xlong.data.processer.UrlNormalizeProcesser;
import xlong.ontology.OntologyTree;
import xlong.sample.Composite;
import xlong.sample.Sample;
import xlong.sample.Labels;
import xlong.sample.SparseVectors;
import xlong.sample.Text;
import xlong.sample.Texts;
import xlong.sample.converter.TextToSparseVectorConverter;
import xlong.sample.tokenizer.SingleWordTokenizer;
import xlong.util.MyWriter;
import xlong.util.PropertiesUtil;

public class BuildInstances {
	public static void main(String[] args) throws Exception{
		
		// ----------------------------Data process---------------------------------
		// Get properties.
		PropertiesUtil.init();
		PropertiesUtil.loadProperties();
		String typeFile = PropertiesUtil.getProperty("DBpedia_instance_types.nt");
		String urlFile = PropertiesUtil.getProperty("DBpedia_external_links.nt");
		String ontologyFile = PropertiesUtil.getProperty("DBpedia_ontology.owl");
		String typePairFile = "result/typePair.txt";
		String urlPairFile = "result/urlPair.txt";
		
		Collection<Entity> entities;
		HashMap<String, TreeSet<String>> urlMap;
		HashMap<String, TreeSet<Entity>> urlEntityMap;
		Composite treeComposite;
		Composite flatComposite;
		TextToSparseVectorConverter converter = new TextToSparseVectorConverter(new SingleWordTokenizer());
		
		// Read data file.
		NTripleReader typeReader = new NTripleReader(typeFile
				, new SimplifyProcesser(new UrlNormalizeProcesser(new Triple2PairProcesser())));
		NTripleReader urlReader = new NTripleReader(urlFile
				, new SimplifyProcesser(new UrlNormalizeProcesser(new Triple2PairProcesser())));
		typeReader.readAll(typePairFile);
		urlReader.readAll(urlPairFile);

		// Generate Entities.
		System.out.println("Generate Entities");
		entities = Entity.generateEntities(typePairFile, urlPairFile);
		System.out.println(entities.size());
		entities = Entity.filtEntities(entities, new ExistUrlFilter(new ExistTypeFilter()));
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
		UrlMapIO.write(urlMap, "result/UrlMap.txt");
		urlMap = null;
		urlMap = UrlMapIO.read("result/UrlMap.txt");
		System.out.println(urlMap.size());
		
		
		// ----------------------------Samples---------------------------------
		OntologyTree tree = OntologyTree.getTree(ontologyFile);
		MyWriter.setFile("result/ontology", false);
		MyWriter.writeln(tree.toString());
		MyWriter.close();
		treeComposite = new Composite(tree);
		treeComposite.save("result/treeEmpty");
		
		for (Entry<String, TreeSet<String>> en:urlMap.entrySet()) {
			Sample instance = new Sample(new Text(en.getKey()), Labels.getLabels(en.getValue()));
			treeComposite.addSample(instance);
		}	
		System.out.println(treeComposite.countSample());
		treeComposite.save("result/treeOri");
		
		treeComposite = new Composite("result/treeOri", new Texts());
		System.out.println(treeComposite.countSample());
		
		converter.buildDictionary(treeComposite);
		converter.determineDictionary();
		treeComposite = converter.convert(treeComposite);	
		System.out.println(treeComposite.countSample());
		treeComposite.save("result/tree");
		

		treeComposite = new Composite("result/tree", new SparseVectors());
		System.out.println(treeComposite.countSample());
		treeComposite.save("result/tree2");	
		
		Vector<Composite> trees = treeComposite.split(new int[]{10, 20, 30, 40}, new Random());
		System.out.println(trees.get(0).countSample());
		System.out.println(trees.get(1).countSample());
		System.out.println(trees.get(2).countSample());
		System.out.println(trees.get(3).countSample());
		trees = null;		
		treeComposite = null;
		
		flatComposite = new Composite();
		for (Entry<String, TreeSet<String>> en:urlMap.entrySet()) {
			Sample instance = new Sample(new Text(en.getKey()), Labels.getLabels(en.getValue()));
			flatComposite.addSample(instance);
		}
		System.out.println(flatComposite.countSample());
		
		
		converter.buildDictionary(flatComposite);
		converter.determineDictionary();
		flatComposite = converter.convert(flatComposite);
		System.out.println(flatComposite.countSample());
		flatComposite.save("result/flat");
		converter.save("result/dictionary");
		
		converter = TextToSparseVectorConverter.load("result/dictionary");
		flatComposite = new Composite("result/flat", new SparseVectors());
		System.out.println(flatComposite.countSample());
		System.out.println(converter.getDictionary().size());
		
		SparseVectorSampleToWekaInstanceConverter converter2 = new SparseVectorSampleToWekaInstanceConverter(converter.getDictionary().size());
		for (Sample sample:flatComposite.getSamples()) {
			converter2.addSample(sample);
		}
		Instances instances = converter2.getInstances();
		System.out.println(instances.numClasses());
		System.out.println(instances.sumOfWeights());
		System.out.println(instances.numAttributes());
		System.out.println(instances.get(2000000).toString());
		
	}
}
