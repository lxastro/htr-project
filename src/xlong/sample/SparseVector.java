package xlong.sample;

import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 * @author longx
 *
 */
public class SparseVector implements Property {

	/** */
	private int[] indexs;
	/** */
	private double[] values;
	
	/**
	 * 
	 * @param vector vector
	 */
	public SparseVector(final Map<Integer, Double> vector) {
		int size = vector.size();
		indexs = new int[size];
		values = new double[size];
		int i = 0;
		for (Entry<Integer, Double> en:vector.entrySet()) {
			indexs[i] = en.getKey();
			values[i] = en.getValue();
			i++;
		}
	}
	
	/**
	 * @param line one line string
	 */
	public SparseVector(final String line) {
		String[] parts = line.split(" ");
		int size = parts.length / 2;
		indexs = new int[size];
		values = new double[size];
		for (int i = 0; i < size; i++) {
			indexs[i] = Integer.parseInt(parts[2 * i]);
			values[i] = Double.parseDouble(parts[2 * i + 1]);
		}
	}
	
	/**
	 * @return indexs
	 */
	public final int[] getIndexs() {
		return indexs;
	}
	
	/**
	 * @return values
	 */
	public final double[] getValues() {
		return values;
	}
	
	@Override
	public final String toString() {
		String str = indexs[0] + ":" + values[0];
		for (int i = 1; i < indexs.length; i++) {
			str += " " + indexs[i] + ":" + values[i];
		}
		return str;
	}
	
	@Override
	public final String getOneLineString() {
		String str = indexs[0] + " " + values[0];
		for (int i = 1; i < indexs.length; i++) {
			str += " " + indexs[i] + " " + values[i];
		}
		return str;	
	}

}
