package xlong.instance;

/**
 * InstanceComponent.
 */
public interface InstanceComponent {
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
	Label[] getLabels();
	
}
