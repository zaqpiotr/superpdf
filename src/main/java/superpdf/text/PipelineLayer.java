package superpdf.text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.font.PDFont;

/**
 * Represents a layer in the text rendering pipeline.
 *
 * This class manages text tokens and their associated metrics (width, position)
 * for a text rendering pipeline. It accumulates tokens, tracks text content and
 * rendered widths, and provides methods to query the current state of the pipeline layer.
 *
 * The layer maintains:
 * - A list of tokens (text, bullets, orderings, padding)
 * - Accumulated text content and width metrics
 * - The last text token and its trimmed version
 * - Width calculations for rendered and trimmed text
 */
public class PipelineLayer {

	/**
	 * Constructs a new PipelineLayer with default initialization.
	 */
	public PipelineLayer() {
	}

	private static String rtrim(String s) {
		int len = s.length();
		while ((len > 0) && (s.charAt(len - 1) <= ' ')) {
			len--;
		}
		if (len == s.length()) {
			return s;
		}
		if (len == 0) {
			return "";
		}
		return s.substring(0, len);
	}

	private final StringBuilder text = new StringBuilder();

	private String lastTextToken = "";

	private List<Token> tokens = new ArrayList<>();

	private String trimmedLastTextToken = "";

	private float width;

	private float widthLastToken;

	private float widthTrimmedLastToken;

	private float widthCurrentText;

	/**
	 * Checks whether this pipeline layer contains no tokens.
	 *
	 * @return {@code true} if this layer has no tokens, {@code false} otherwise
	 */
	public boolean isEmpty() {
		return tokens.isEmpty();
	}

	/**
	 * Adds a token to this pipeline layer.
	 *
	 * @param token the token to add to this layer
	 */
	public void push(final Token token) {
		tokens.add(token);
	}

	/**
	 * Adds a token to this pipeline layer with font metrics.
	 *
	 * Processes the token based on its type (PADDING, BULLET, ORDERING, or TEXT),
	 * updating the accumulated text and width metrics accordingly. Text tokens are
	 * processed with width calculations based on the provided font and font size.
	 *
	 * @param font the PDF font for calculating text width
	 * @param fontSize the font size for width calculations
	 * @param token the token to add to this layer
	 * @throws IOException if an error occurs while reading font metrics
	 */
	public void push(final PDFont font, final float fontSize, final Token token) throws IOException {
		if (token.getType().equals(TokenType.PADDING)) {
			width += token.getPaddingValue();
		}
		if (token.getType().equals(TokenType.BULLET)) {
			// just appending one space because our bullet width will be wide as one character of current font
			text.append(token.getData());
			width += (token.getWidth(font) / 1000f * fontSize);
		}

		if (token.getType().equals(TokenType.ORDERING)) {
			// just appending one space because our bullet width will be wide as one character of current font
			text.append(token.getData());
			width += (token.getWidth(font) / 1000f * fontSize);
		}

		if (token.getType().equals(TokenType.TEXT)) {
			text.append(lastTextToken);
			width += widthLastToken;
			lastTextToken = token.getData();
			trimmedLastTextToken = rtrim(lastTextToken);
			widthLastToken = token.getWidth(font) / 1000f * fontSize;

			if (trimmedLastTextToken.length() == lastTextToken.length()) {
				widthTrimmedLastToken = widthLastToken;
			} else {
				widthTrimmedLastToken = (font.getStringWidth(trimmedLastTextToken) / 1000f * fontSize);
			}

			widthCurrentText = text.length() == 0 ? 0 :
					(font.getStringWidth(text.toString()) / 1000f * fontSize);
		}

		push(token);
	}

	/**
	 * Merges another pipeline layer into this layer.
	 *
	 * Incorporates all tokens, text content, and width metrics from the provided
	 * pipeline layer into this layer. The source pipeline is reset after merging.
	 *
	 * @param pipeline the pipeline layer to merge into this layer
	 */
	public void push(final PipelineLayer pipeline) {
		text.append(lastTextToken);
		width += widthLastToken;
		text.append(pipeline.text);
		if (pipeline.text.length() > 0) {
			width += pipeline.widthCurrentText;
		}
		lastTextToken = pipeline.lastTextToken;
		trimmedLastTextToken = pipeline.trimmedLastTextToken;
		widthLastToken = pipeline.widthLastToken;
		widthTrimmedLastToken = pipeline.widthTrimmedLastToken;
		tokens.addAll(pipeline.tokens);

		pipeline.reset();
	}

	/**
	 * Clears all content from this pipeline layer.
	 *
	 * Resets all text, tokens, and width metrics to their initial state.
	 */
	public void reset() {
		text.delete(0, text.length());
		width = 0.0f;
		lastTextToken = "";
		trimmedLastTextToken = "";
		widthLastToken = 0.0f;
		widthTrimmedLastToken = 0.0f;
		tokens.clear();
	}

	/**
	 * Returns the accumulated text content with the trimmed last text token appended.
	 *
	 * @return the trimmed text content of this pipeline layer
	 */
	public String trimmedText() {
		return text.toString() + trimmedLastTextToken;
	}

	/**
	 * Returns the total rendered width of the content in this pipeline layer.
	 *
	 * @return the width of all accumulated text and tokens
	 */
	public float width() {
		return width + widthLastToken;
	}

	/**
	 * Returns the rendered width of the trimmed content in this pipeline layer.
	 *
	 * @return the width of all accumulated text and tokens with trailing whitespace removed
	 */
	public float trimmedWidth() {
		return width + widthTrimmedLastToken;
	}

	/**
	 * Returns a copy of all tokens in this pipeline layer.
	 *
	 * @return a new list containing all tokens in this layer
	 */
	public List<Token> tokens() {
		return new ArrayList<>(tokens);
	}

	@Override
	public String toString() {
		return text.toString() + "(" + lastTextToken + ") [width: " + width() + ", trimmed: " + trimmedWidth() + "]";
	}
}
