package xlong.instance;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Random;
import java.util.Vector;

/**
 * 
 */
public class InstanceComposite implements InstanceComponent {
	/** */
	private final Vector<Instance> instances;
	/** */
	private final Vector<InstanceComposite> composites;
	/** */
	private final Label label;
	
	/**
	 * @param label the label
	 */
	public InstanceComposite(final Label label) {
		instances = new Vector<Instance>();
		composites = new Vector<InstanceComposite>();
		this.label = label;
	}
	
	/**
	 * @param in reader 
	 * @param pFactory property factory
	 * @throws IOException IOException
	 */
	public InstanceComposite(final BufferedReader in, final Properties pFactory) throws IOException {
		instances = new Vector<Instance>();
		composites = new Vector<InstanceComposite>();
		in.readLine();
		this.label = Labels.getLabel(in.readLine());
		
		in.mark(10);
		while (in.readLine() == "{") {
			in.reset();
			composites.add(new InstanceComposite(in, pFactory));
			in.mark(10);
		}
		
		in.mark(10);
		while (in.readLine() != "}") {
			in.reset();
			this.addInstance(new Instance(in, pFactory));
			in.mark(10);
		}
	}
	
	@Override
	public final int countInstance() {
		int size = 0;
		for (InstanceComponent component:instances) {
			size += component.countInstance();
		}
		for (InstanceComponent component:composites) {
			size += component.countInstance();
		}
		return size;
	}

	@Override
	public final boolean isLeaf() {
		return false;
	}
	
	@Override
	public final Label[] getLabels() {
		return new Label[] {label};
	}
	
	/**
	 * 
	 * @param instance instance to add
	 */
	public final void addInstance(final Instance instance) {
		if (instance.containLabel(label)) {
			instances.add(instance);
		}
		for (InstanceComposite composite:composites) {
			composite.addInstance(instance);
		}
	}
	
	/**
	 * @param composite composite to add
	 * @return success or not
	 */
	public final boolean addComposite(final InstanceComposite composite) {
		return composites.add(composite);
	}

	/**
	 * @param out BufferedWriter
	 * @throws IOException IOException
	 */
	public final void save(final BufferedWriter out) throws IOException {
		out.write("{\n");
		out.write(label.getText() + "\n");
		for (InstanceComposite composite:composites) {
			composite.save(out);
		}
		for (Instance instance:instances) {
			instance.save(out);
		}
		out.write("}\n");
	}
	
	/**
	 * @param percent percent
	 * @return count
	 */
	private int[] percentToCount(final int[] percent) {
		int npart = percent.length;
		int n = countInstance();
		int[] ns = new int[npart];
		int s = 0;
		int sp = 0;
		for (int i = 0; i < npart; i++) {
			ns[i] = (int) Math.round(((double) n) * percent[i] / 100);
			s += ns[i];
			sp += percent[i];
		}
		if (sp > 100) {
			return null;
		}
		if (s != (int) Math.round(((double) n) * sp / 100)) {
			int maxid = 0;
			for (int i = 1; i < npart; i++) {
				if (percent[i] > percent[maxid]) {
					maxid = i;
				}
			}
			ns[maxid] -= (s - (int) Math.round(((double) n) * sp / 100));
		}
		return ns;
	}
	
	/**
	 * @param percent split percents
	 * @param rand random
	 * @return split composites
	 */
	public final Vector<InstanceComposite> split(final int[] percent, final Random rand) {
		int npart = percent.length;
		int[] cnt = percentToCount(percent);
		Collections.shuffle(instances, rand);
		Vector<InstanceComposite> parts = new Vector<InstanceComposite>();
		int s = 0;
		for (int i = 0; i < npart; i++) {
			InstanceComposite part = new  InstanceComposite(label);
			for (int j = s; j < s + cnt[i]; j++) {
				part.addInstance(instances.get(j));
			}
			parts.add(part);
			s += cnt[i];
		}		
		for (InstanceComposite composite:composites) {
			Vector<InstanceComposite> subParts = composite.split(percent, rand);
			for (int i = 0; i < npart; i++) {
				parts.get(i).addComposite(subParts.get(i));
			}
		}
		return parts;
	}
	
}
