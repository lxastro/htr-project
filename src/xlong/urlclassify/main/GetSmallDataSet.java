package xlong.urlclassify.main;

import java.util.Random;
import java.util.Vector;

import xlong.wm.sample.Composite;
import xlong.wm.sample.Texts;

public class GetSmallDataSet {
	public static void main(String[] args) throws Exception {
		Composite treeComposite;
		treeComposite = new Composite("result/treeParsed", new Texts());
		treeComposite.flatComposite(1);
		System.out.println(treeComposite.countSample());
		System.out.println(treeComposite.getComposites().size());
		Vector<Composite> composites;
		
		composites = treeComposite.split(new int[] {1, 90}, new Random(123));
		treeComposite = composites.firstElement();
		System.out.println(treeComposite.countSample());
		treeComposite.cutBranch(10);
		System.out.println(treeComposite.countSample());
		
		treeComposite.save("result/treeSmall");
	}
}
