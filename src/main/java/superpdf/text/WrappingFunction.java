package superpdf.text;

/**
 * Interface for custom text wrapping functions.
 *
 * Implementations define how text should be wrapped into separate lines.
 * This allows for custom line-breaking algorithms to be used in the text tokenization process.
 */
public interface WrappingFunction {

	/**
	 * Splits the given text into multiple lines using the wrapping algorithm.
	 *
	 * @param text the text to wrap into lines
	 * @return an array of strings, each representing a line of wrapped text
	 */
	String[] getLines(String text);
}
