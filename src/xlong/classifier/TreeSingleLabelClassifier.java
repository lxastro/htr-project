//package xlong.classifier;
//
//import xlong.classifier.converter.SparseVectorSampleToWekaInstanceConverter;
//import xlong.sample.Composite;
//import xlong.sample.Property;
//import xlong.sample.Sample;
//
//public class TreeSingleLabelClassifier extends AbstractClassifier  {
//
//	private weka.classifiers.Classifier wekaClassifier;
//	private SparseVectorSampleToWekaInstanceConverter converter;
//	
//	public TreeSingleLabelClassifier(int numOfAtt, int numOfClass) {
//		wekaClassifier = new weka.classifiers.bayes.NaiveBayesMultinomial();
//		//wekaClassifier = new weka.classifiers.functions.SMO();
//		converter = new SparseVectorSampleToWekaInstanceConverter(numOfAtt, numOfClass);
//	}
//	@Override
//	public void train(Composite composite) throws Exception {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public double[] getDistribution(Sample sample) throws Exception {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public double[] getDistribution(Property property) throws Exception {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//}
