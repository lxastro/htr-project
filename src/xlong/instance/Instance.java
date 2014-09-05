package xlong.instance;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * Instance to store property and labels.
 */
public class Instance implements InstanceComponent {
	/** Property of a instance. */
	private final Property property;
	/** Labels of a instance. */
	private final TreeSet<Label> labels;
	/** Labels pool. */
	private static Map<String, TreeSet<Label>> labelsPool = new HashMap<String, TreeSet<Label>>();
	/**
	 * @param property the property
	 * @param labels the labels
	 */
	public Instance(final Property property, final Collection<Label> labels) {
		this.property = property;
		
		String str = Labels.labelsToString(labels);
		TreeSet<Label> labelSet = labelsPool.get(str);
		if (labelSet != null) {
			this.labels = labelSet;
		} else {
			this.labels = new TreeSet<Label>(labels);
			labelsPool.put(str, this.labels);
		}
	}
	/**
	 * @param in buffered reader
	 * @param pFactory a property factory
	 * @throws IOException IOException
	 */
	public Instance(final BufferedReader in, final Properties pFactory) throws IOException {
		property = pFactory.getProperty(in.readLine());
		labels = Labels.loadFromString(in.readLine());
	}
	@Override
	public final int countInstance() {
		return 1;
	}

	@Override
	public final boolean isLeaf() {
		return true;
	}

	/**
	 * @return the property
	 */
	public final Property getProperty() {
		return property;
	}

	@Override
	public final Label[] getLabels() {
		return labels.toArray(new Label[0]);
	}
	
	/**
	 * @param label the label
	 * @return contain or not
	 */
	public final boolean containLabel(final Label label) {
		return labels.contains(label);
	}

	/**
	 * @param out buffered writer
	 * @throws IOException IOException
	 */
	public final void save(final BufferedWriter out) throws IOException {
		out.write(property.getOneLineString() + "\n");
		out.write(Labels.labelsToString(labels) + "\n");
	}

}
