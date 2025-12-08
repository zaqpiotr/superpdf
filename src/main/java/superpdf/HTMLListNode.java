package superpdf;

/**
 * Data container for HTML ordered list elements.
 */
public class HTMLListNode {

	/**
	 * <p>
	 * Element's current ordering number (e.g third element in the current list)
	 * </p>
	 */
	private int orderingNumber;

	/**
	 * <p>
	 * Element's whole ordering number value (e.g 1.1.2.1)
	 * </p>
	 */
	private String value;

	/**
	 * Gets the element's current ordering number within its list level.
	 *
	 * @return the ordering number (e.g., 3 for the third element)
	 */
	public int getOrderingNumber() {
		return orderingNumber;
	}

	/**
	 * Sets the element's ordering number within its list level.
	 *
	 * @param orderingNumber the ordering number to set
	 */
	public void setOrderingNumber(int orderingNumber) {
		this.orderingNumber = orderingNumber;
	}

	/**
	 * Gets the element's complete hierarchical ordering value.
	 *
	 * @return the full ordering value (e.g., "1.1.2.1")
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the element's complete hierarchical ordering value.
	 *
	 * @param value the full ordering value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Constructs a new HTMLListNode with the specified ordering number and value.
	 *
	 * @param orderingNumber the element's ordering number within its list level
	 * @param value the element's complete hierarchical ordering value
	 */
	public HTMLListNode(int orderingNumber, String value) {
		this.orderingNumber = orderingNumber;
		this.value = value;
	}

}
