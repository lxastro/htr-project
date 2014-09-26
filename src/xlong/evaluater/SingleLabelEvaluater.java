//package xlong.evaluater;
//
//import xlong.classifier.Classifier;
//import xlong.sample.Sample;
//
//public final class SingleLabelEvaluater extends Evaluater {
//	
//	
//	private int[] tp;
//	private int[] tn;
//	private int[] fp;
//	private int n;
//	
//	public SingleLabelEvaluater(Classifier classifier) {
//		super(classifier);
//		tp = new int [numClass];
//		tn = new int [numClass];
//		fp = new int [numClass];
//		n = 0;
//	}
//
//	@Override
//	public void evaluate(Sample sample) throws Exception {
//		double[] result = classifier.getDistribution(sample);
//		int testClass = argmax(result);
//		
//		int trueClass = classifier.label2id(sample.getLabels().iterator().next().getText());
//		if (testClass == trueClass) {
//			tp[trueClass] += 1;
//		} else {
//			tn[trueClass] += 1;
//			fp[testClass] += 1;
//		}
//		n += 1;
//	}
//	
//	@Override
//	public double getAccuracy() {
//		int t = 0;
//		for (int i = 0; i < numClass; i++) {
//			t += tp[i];
//		}
//		return ((double) t) / n;
//	}
//	
//	public int argmax(double[] a) {
//		double max = -Double.MIN_VALUE;
//		int maxid = -1;
//		int n = a.length;
//		for (int i = 0; i < n; i++) {
//			if (a[i] > max) {
//				max = a[i];
//				maxid = i;
//			}
//		}
//		return maxid;
//	}
//
//}
