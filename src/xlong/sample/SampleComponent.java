package xlong.sample;

import java.util.Set;

/**
 * InstanceComponent.
 */
public interface SampleComponent {
	/**
	 * @return number of instances in this component.
	 */
	int countInstance();
	
	/**
	 * @return true if this component is a leaf.
	 */
	boolean isLeaf();
	
	/**
	 * @return the labels.
	 */
	Set<Label> getLabels();
	
}
