package superpdf.text;

import org.apache.pdfbox.pdmodel.font.PDFont;

import java.io.IOException;
import java.util.Objects;

/**
 * Represents a token produced during text tokenization.
 *
 * Token itself is thread safe, so you can reuse shared instances;
 * however, subclasses may have additional methods which are not thread safe.
 * Tokens encapsulate a type and associated data string, and provide methods
 * for accessing token properties and calculating width metrics.
 */
public class Token {

	private final TokenType type;

	private final String data;

	/**
	 * Constructs a new Token with the specified type and data.
	 *
	 * @param type the type of the token
	 * @param data the data associated with the token
	 */
	public Token(TokenType type, String data) {
		this.type = type;
		this.data = data;
	}

	/**
	 * Gets the data content of this token.
	 *
	 * @return the token data as a string
	 */
	public String getData() {
		return data;
	}

	/**
	 * Gets the type of this token.
	 *
	 * @return the TokenType of this token
	 */
	public TokenType getType() {
		return type;
	}

	/**
	 * Gets the width of this token's data when rendered with the specified font.
	 *
	 * @param font the PDF font to use for width calculation
	 * @return the width of the token data
	 * @throws IOException if an error occurs while reading font metrics
	 */
	public float getWidth(PDFont font) throws IOException {
		return font.getStringWidth(getData());
	}

	/**
	 * Returns the padding value efficiently, avoiding Float.parseFloat() for PaddingToken instances.
	 * For PaddingToken instances, returns the cached padding value.
	 * For regular Token instances with PADDING type, parses the data string to float.
	 *
	 * @return the padding value as a float
	 * @throws NumberFormatException if the token data cannot be parsed as a float
	 */
	public float getPaddingValue() {
		if (this instanceof PaddingToken) {
			return ((PaddingToken) this).getPaddingValue();
		}
		return Float.parseFloat(getData());
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + type + "/" + data + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Token token = (Token) o;
		return getType() == token.getType() &&
				Objects.equals(getData(), token.getData());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getType(), getData());
	}

	/**
	 * Creates a non-thread safe token instance optimized for renderable text.
	 * The returned token caches width calculations for improved performance.
	 *
	 * @param type the token type
	 * @param data the token data
	 * @return a TextToken instance optimized for text rendering
	 */
	public static Token text(TokenType type, String data) {
		return new TextToken(type, data);
	}

	/**
	 * Creates a token instance optimized for padding values.
	 * Avoids repeated Float.parseFloat() calls by caching the padding value.
	 *
	 * @param paddingValue the padding value as a float
	 * @return a PaddingToken instance optimized for padding operations
	 */
	public static Token padding(float paddingValue) {
		return new PaddingToken(paddingValue);
	}
}

// Optimized token for padding values - avoids repeated Float.parseFloat() calls
class PaddingToken extends Token {
	private final float paddingValue;

	PaddingToken(float paddingValue) {
		super(TokenType.PADDING, String.valueOf(paddingValue));
		this.paddingValue = paddingValue;
	}

	public float getPaddingValue() {
		return paddingValue;
	}
}

// Non-thread safe subclass with caching to optimize tokens containing renderable text
class TextToken extends Token {
	private PDFont cachedWidthFont;
	private float cachedWidth;

	TextToken(TokenType type, String data) {
		super(type, data);
	}

	@Override
	public float getWidth(PDFont font) throws IOException {
		if (font == cachedWidthFont) {
			return cachedWidth;
		}
		cachedWidth = super.getWidth(font);
		// must come after super call, in case it throws
		cachedWidthFont = font;
		return cachedWidth;
	}
}