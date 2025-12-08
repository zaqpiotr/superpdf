package superpdf.text;

/**
 * Types of tokens produced during text tokenization.
 * Represents different categories of tokens that are used in the text rendering pipeline.
 */
public enum TokenType {
	/** Regular text content */
	TEXT,
	/** Possible point where text can be wrapped */
	POSSIBLE_WRAP_POINT,
	/** Point where text wrapping should occur */
	WRAP_POINT,
	/** HTML opening tag */
	OPEN_TAG,
	/** HTML closing tag */
	CLOSE_TAG,
	/** Padding token for spacing */
	PADDING,
	/** Bullet point token */
	BULLET,
	/** Ordering/numbering token */
	ORDERING
}
