// --------------------------------------------------------
// Code generated by Papyrus Java
// --------------------------------------------------------

package RootElement;

import java.lang.String;

/************************************************************/
/**
 * 
 */
public class Supply {
	/**
	 * 
	 */
	private double size;
	/**
	 * 
	 */
	private String supplyName;

	/**
	 * 
	 * @param supplyName 
	 * @param size 
	 */
	public Supply(String supplyName, double size) {

		this.size = size;
		this.supplyName = supplyName;
	}

	/**
	 * 
	 * @return 
	 */
	public double getSize() {

		return size;
	}

	/**
	 * 
	 * @return 
	 */
	public String getName() {

		return supplyName;
	}
};