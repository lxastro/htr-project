package xlong.instance;

/**
 * 
 * @author longx
 *
 */
public class SparseVectors implements Properties {

	@Override
	public final Property getProperty(final String s) {
		return new SparseVector(s);
	}

}
