package superpdf;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import superpdf.utils.PageContentStreamOptimized;
import org.apache.pdfbox.pdmodel.font.PDFont;

import superpdf.text.PipelineLayer;
import superpdf.text.Token;
import superpdf.text.TokenType;
import superpdf.text.Tokenizer;
import superpdf.text.WrappingFunction;
import superpdf.utils.FontUtils;
import superpdf.utils.PDStreamUtils;

/**
 * Represents a paragraph of text to be drawn on a PDF document.
 * <p>
 * This class handles text layout, wrapping, formatting (bold, italic, underline),
 * and rendering of paragraphs in PDF documents. It supports HTML-like tags for
 * text styling (bold, italic, paragraphs, lists) and provides configurable font,
 * size, width, alignment, color, and line spacing.
 * </p>
 */
public class Paragraph {

	private float width;
	private final String text;
	private float fontSize;
	private PDFont font;
	private final PDFont fontBold;
	private final PDFont fontItalic;
	private final PDFont fontBoldItalic;
	private final WrappingFunction wrappingFunction;
	private HorizontalAlignment align;
	private TextType textType;
	private Color color;
	private float lineSpacing;

	private final static int DEFAULT_TAB = 4;
	private final static int DEFAULT_TAB_AND_BULLET = 6;
	private final static int BULLET_SPACE = 2;

	// Cache for single-character strings to avoid repeated String.valueOf() calls
	private static final String[] CHAR_CACHE = new String[128];
	static {
		for (int i = 0; i < 128; i++) {
			CHAR_CACHE[i] = String.valueOf((char) i);
		}
	}

	private boolean drawDebug;
	private final Map<Integer, Float> lineWidths = new HashMap<>();
	private Map<Integer, List<Token>> mapLineTokens = new LinkedHashMap<>();
	private float maxLineWidth = Integer.MIN_VALUE;
	private List<Token> tokens;
	private List<String> lines;
	private Float spaceWidth;

	/**
	 * Constructs a Paragraph with basic text styling properties.
	 *
	 * @param text the text content of the paragraph
	 * @param font the {@link PDFont} to use for rendering
	 * @param fontSize the font size in points
	 * @param width the width constraint for text wrapping
	 * @param align the {@link HorizontalAlignment} for text alignment
	 */
	public Paragraph(String text, PDFont font, float fontSize, float width, final HorizontalAlignment align) {
		this(text, font, fontSize, width, align, null);
	}

	// This function exists only to preserve backwards compatibility for
	// the getWrappingFunction() method; it has been replaced with a faster implementation in the Tokenizer
	private static final WrappingFunction DEFAULT_WRAP_FUNC = new WrappingFunction() {
		@Override
		public String[] getLines(String t) {
			return t.split("(?<=\\s|-|@|,|\\.|:|;)");
		}
	};

	/**
	 * Constructs a Paragraph with integer font size and width parameters.
	 * <p>
	 * This constructor provides a simplified way to create paragraphs with default
	 * left alignment and uses integer values instead of float values for font size and width.
	 * </p>
	 *
	 * @param text the text content of the paragraph
	 * @param font the {@link PDFont} to use for rendering
	 * @param fontSize the font size in points
	 * @param width the width constraint for text wrapping
	 */
	public Paragraph(String text, PDFont font, int fontSize, int width) {
		this(text, font, fontSize, width, HorizontalAlignment.LEFT, null);
	}

	/**
	 * Creates a new Paragraph with specified wrapping function.
	 *
	 * @param text the text content
	 * @param font the font to use
	 * @param fontSize the font size in points
	 * @param width the paragraph width in points
	 * @param align the horizontal alignment
	 * @param wrappingFunction custom text wrapping function
	 */
	public Paragraph(String text, PDFont font, float fontSize, float width, final HorizontalAlignment align,
			WrappingFunction wrappingFunction) {
		this(text, font, fontSize, width, align, Color.BLACK, (TextType) null, wrappingFunction);
	}

	/**
	 * Creates a new Paragraph with color and text type.
	 *
	 * @param text the text content
	 * @param font the font to use
	 * @param fontSize the font size in points
	 * @param width the paragraph width in points
	 * @param align the horizontal alignment
	 * @param color the text color
	 * @param textType the text type
	 * @param wrappingFunction custom text wrapping function
	 */
	public Paragraph(String text, PDFont font, float fontSize, float width, final HorizontalAlignment align,
			final Color color, final TextType textType, WrappingFunction wrappingFunction) {
		this(text, font, fontSize, width, align, color, textType, wrappingFunction, 1);
	}

	/**
	 * Creates a new Paragraph with full customization including line spacing.
	 *
	 * @param text the text content
	 * @param font the font to use
	 * @param fontSize the font size in points
	 * @param width the paragraph width in points
	 * @param align the horizontal alignment
	 * @param color the text color
	 * @param textType the text type
	 * @param wrappingFunction custom text wrapping function
	 * @param lineSpacing the line spacing multiplier (1.0 = normal spacing)
	 */
	public Paragraph(String text, PDFont font, float fontSize, float width, final HorizontalAlignment align,
			final Color color, final TextType textType, WrappingFunction wrappingFunction, float lineSpacing) {
		this.color = color;
		this.text = text;
		this.font = font;
		// check if we have different default font for italic and bold text
		if (FontUtils.getDefaultfonts().isEmpty()) {
			// Fallback to using the base font for all variants if FontUtils is not initialized
			// This prevents Helvetica font warnings in Docker environments
			fontBold = font;
			fontItalic = font;
			fontBoldItalic = font;
		} else {
			fontBold = FontUtils.getDefaultfonts().get("fontBold");
			fontBoldItalic = FontUtils.getDefaultfonts().get("fontBoldItalic");
			fontItalic = FontUtils.getDefaultfonts().get("fontItalic");
		}
		this.fontSize = fontSize;
		this.width = width;
		this.textType = textType;
		this.setAlign(align);
		this.wrappingFunction = wrappingFunction;
		this.lineSpacing = lineSpacing;
	}

	/**
	 * Gets the lines of text after wrapping and layout processing.
	 * <p>
	 * This method processes the paragraph text, applying word wrapping, HTML-like
	 * formatting tags (bold, italic, lists, paragraphs), and returns the resulting
	 * lines. The result is memoized for performance.
	 * </p>
	 *
	 * @return a list of strings representing the wrapped and formatted lines
	 */
	public List<String> getLines() {
		// memoize this function because it is very expensive
		if (lines != null) {
			return lines;
		}

		final List<String> result = new ArrayList<>();

		// text and wrappingFunction are immutable, so we only ever need to compute tokens once
		if (tokens == null) {
			tokens = Tokenizer.tokenize(text, wrappingFunction);
		}

		int lineCounter = 0;
		boolean italic = false;
		boolean bold = false;
		boolean listElement = false;
		PDFont currentFont = font;
		int orderListElement = 1;
		int numberOfOrderedLists = 0;
		int listLevel = 0;
		Stack<HTMLListNode> stack= new Stack<>();

		final PipelineLayer textInLine = new PipelineLayer();
		final PipelineLayer sinceLastWrapPoint = new PipelineLayer();

		for (final Token token : tokens) {
			switch (token.getType()) {
			case OPEN_TAG:
				if (isBold(token)) {
					bold = true;
					currentFont = getFont(bold, italic);
				} else if (isItalic(token)) {
					italic = true;
					currentFont = getFont(bold, italic);
				} else if (isList(token)) {
					listLevel++;
					if (token.getData().equals("ol")) {
						numberOfOrderedLists++;
						if(listLevel > 1){
							// Use StringBuilder to avoid repeated string concatenation
							String orderingValue;
							if (stack.isEmpty()) {
								orderingValue = (orderListElement - 1) + ".";
							} else {
								String peekValue = stack.peek().getValue();
								StringBuilder sb = new StringBuilder(peekValue.length() + 10);
								sb.append(peekValue).append(orderListElement - 1).append('.');
								orderingValue = sb.toString();
							}
							stack.add(new HTMLListNode(orderListElement-1, orderingValue));
						}
						orderListElement = 1;

						textInLine.push(sinceLastWrapPoint);
						// check if you have some text before this list, if you don't then you really don't need extra line break for that
						if (textInLine.trimmedWidth() > 0) {
							// this is our line
							result.add(textInLine.trimmedText());
							lineWidths.put(lineCounter, textInLine.trimmedWidth());
							mapLineTokens.put(lineCounter, textInLine.tokens());
							maxLineWidth = Math.max(maxLineWidth, textInLine.trimmedWidth());
							textInLine.reset();
							lineCounter++;
						}
					} else if (token.getData().equals("ul")) {
						textInLine.push(sinceLastWrapPoint);
						// check if you have some text before this list, if you don't then you really don't need extra line break for that
						if (textInLine.trimmedWidth() > 0) {
							// this is our line
							result.add(textInLine.trimmedText());
							lineWidths.put(lineCounter, textInLine.trimmedWidth());
							mapLineTokens.put(lineCounter, textInLine.tokens());
							maxLineWidth = Math.max(maxLineWidth, textInLine.trimmedWidth());
							textInLine.reset();
							lineCounter++;
						}
					}
				}
				sinceLastWrapPoint.push(token);
				break;
			case CLOSE_TAG:
				if (isBold(token)) {
					bold = false;
					currentFont = getFont(bold, italic);
					sinceLastWrapPoint.push(token);
				} else if (isItalic(token)) {
					italic = false;
					currentFont = getFont(bold, italic);
					sinceLastWrapPoint.push(token);
				} else if (isList(token)) {
					listLevel--;
					if (token.getData().equals("ol")) {
						numberOfOrderedLists--;
						// reset elements
						if(numberOfOrderedLists>0){
							orderListElement = stack.peek().getOrderingNumber()+1;
							stack.pop();
						}
					}
					// ensure extra space after each lists
					// no need to worry about current line text because last closing <li> tag already done that
					if(listLevel == 0){
						result.add(" ");
						lineWidths.put(lineCounter, 0.0f);
						mapLineTokens.put(lineCounter, new ArrayList<Token>());
						lineCounter++;
					}
				} else if (isListElement(token)) {
					// wrap at last wrap point?
					if (textInLine.width() + sinceLastWrapPoint.trimmedWidth() > width) {
						// this is our line
						result.add(textInLine.trimmedText());
						lineWidths.put(lineCounter, textInLine.trimmedWidth());
						mapLineTokens.put(lineCounter, textInLine.tokens());
						maxLineWidth = Math.max(maxLineWidth, textInLine.trimmedWidth());
						textInLine.reset();
						lineCounter++;
						// wrapping at last wrap point
						if (numberOfOrderedLists>0) {
							String orderingNumber = stack.isEmpty() ? String.valueOf(orderListElement) + "." : stack.pop().getValue() + ".";
							stack.add(new HTMLListNode(orderListElement, orderingNumber));
							try {
								float tab = indentLevel(DEFAULT_TAB);
								float orderingNumberAndTab = font.getStringWidth(orderingNumber) + tab;
								textInLine.push(currentFont, fontSize, Token.padding(orderingNumberAndTab / 1000 * getFontSize()));
							} catch (IOException e) {
								throw new IllegalStateException("Unable to calculate list indentation", e);
							}
							orderListElement++;
						} else {
							try {
								// if it's not left aligned then ignore list and list element and deal with it as normal text where <li> mimic <br> behaviour
								float tabBullet = getAlign().equals(HorizontalAlignment.LEFT) ? indentLevel(DEFAULT_TAB*Math.max(listLevel - 1, 0) + DEFAULT_TAB_AND_BULLET) : indentLevel(DEFAULT_TAB);
								textInLine.push(currentFont, fontSize, Token.padding(tabBullet / 1000 * getFontSize()));
							} catch (IOException e) {
								throw new IllegalStateException("Unable to calculate list indentation", e);
							}
						}
						textInLine.push(sinceLastWrapPoint);
					}
					// wrapping at this must-have wrap point
					textInLine.push(sinceLastWrapPoint);
					// this is our line
					result.add(textInLine.trimmedText());
					lineWidths.put(lineCounter, textInLine.trimmedWidth());
					mapLineTokens.put(lineCounter, textInLine.tokens());
					maxLineWidth = Math.max(maxLineWidth, textInLine.trimmedWidth());
					textInLine.reset();
					lineCounter++;
					listElement = false;
				}
				if (isParagraph(token)) {
					if (textInLine.width() + sinceLastWrapPoint.trimmedWidth() > width) {
						// this is our line
						result.add(textInLine.trimmedText());
						lineWidths.put(lineCounter, textInLine.trimmedWidth());
						maxLineWidth = Math.max(maxLineWidth, textInLine.trimmedWidth());
						mapLineTokens.put(lineCounter, textInLine.tokens());
						lineCounter++;
						textInLine.reset();
					}
					// wrapping at this must-have wrap point
					textInLine.push(sinceLastWrapPoint);
					// this is our line
					result.add(textInLine.trimmedText());
					lineWidths.put(lineCounter, textInLine.trimmedWidth());
					mapLineTokens.put(lineCounter, textInLine.tokens());
					maxLineWidth = Math.max(maxLineWidth, textInLine.trimmedWidth());
					textInLine.reset();
					lineCounter++;

					// extra spacing because it's a paragraph
					result.add(" ");
					lineWidths.put(lineCounter, 0.0f);
					mapLineTokens.put(lineCounter, new ArrayList<Token>());
					lineCounter++;
				}
				break;
			case POSSIBLE_WRAP_POINT:
				if (textInLine.width() + sinceLastWrapPoint.trimmedWidth() > width) {
					// this is our line
					if (!textInLine.isEmpty()) {
						result.add(textInLine.trimmedText());
						lineWidths.put(lineCounter, textInLine.trimmedWidth());
						maxLineWidth = Math.max(maxLineWidth, textInLine.trimmedWidth());
						mapLineTokens.put(lineCounter, textInLine.tokens());
						lineCounter++;
						textInLine.reset();
					}
					// wrapping at last wrap point
					if (listElement) {
						if (numberOfOrderedLists>0) {
							try {
								float tab = getAlign().equals(HorizontalAlignment.LEFT) ? indentLevel(DEFAULT_TAB*Math.max(listLevel - 1, 0) + DEFAULT_TAB) : indentLevel(DEFAULT_TAB);
								String orderingNumber = stack.isEmpty() ? String.valueOf(orderListElement) + "." : stack.peek().getValue() + "." + String.valueOf(orderListElement-1) + ".";
								textInLine.push(currentFont, fontSize, Token.padding((tab + font.getStringWidth(orderingNumber)) / 1000 * getFontSize()));
							} catch (IOException e) {
								throw new IllegalStateException("Unable to calculate list indentation", e);
							}
						} else {
							try {
								// if it's not left aligned then ignore list and list element and deal with it as normal text where <li> mimic <br> behavior
								float tabBullet = getAlign().equals(HorizontalAlignment.LEFT) ? indentLevel(DEFAULT_TAB*Math.max(listLevel - 1, 0) + DEFAULT_TAB_AND_BULLET)  : indentLevel(DEFAULT_TAB);
								textInLine.push(currentFont, fontSize, Token.padding(tabBullet / 1000 * getFontSize()));
							} catch (IOException e) {
								throw new IllegalStateException("Unable to calculate list indentation", e);
							}
						}
					}
					textInLine.push(sinceLastWrapPoint);
				} else {
					textInLine.push(sinceLastWrapPoint);
				}
				break;
			case WRAP_POINT:
				// wrap at last wrap point?
				if (textInLine.width() + sinceLastWrapPoint.trimmedWidth() > width) {
					// this is our line
					result.add(textInLine.trimmedText());
					lineWidths.put(lineCounter, textInLine.trimmedWidth());
					mapLineTokens.put(lineCounter, textInLine.tokens());
					maxLineWidth = Math.max(maxLineWidth, textInLine.trimmedWidth());
					textInLine.reset();
					lineCounter++;
					// wrapping at last wrap point
					if (listElement) {
						if(!getAlign().equals(HorizontalAlignment.LEFT)) {
							listLevel = 0;
						}
						if (numberOfOrderedLists>0) {
//							String orderingNumber = String.valueOf(orderListElement) + ". ";
							String orderingNumber = stack.isEmpty() ? String.valueOf("1") + "." : stack.pop().getValue() + ". ";
							try {
								float tab = indentLevel(DEFAULT_TAB);
								float orderingNumberAndTab = font.getStringWidth(orderingNumber) + tab;
								textInLine.push(currentFont, fontSize, Token.padding(orderingNumberAndTab / 1000 * getFontSize()));
							} catch (IOException e) {
								throw new IllegalStateException("Unable to calculate list indentation", e);
							}
						} else {
							try {
								// if it's not left aligned then ignore list and list element and deal with it as normal text where <li> mimic <br> behaviour
								float tabBullet = getAlign().equals(HorizontalAlignment.LEFT) ? indentLevel(DEFAULT_TAB*Math.max(listLevel - 1, 0) + DEFAULT_TAB_AND_BULLET) : indentLevel(DEFAULT_TAB);
								textInLine.push(currentFont, fontSize, Token.padding(tabBullet / 1000 * getFontSize()));
							} catch (IOException e) {
								throw new IllegalStateException("Unable to calculate list indentation", e);
							}
						}
					}
					textInLine.push(sinceLastWrapPoint);
				}
				if (isParagraph(token)) {
					// check if you have some text before this paragraph, if you don't then you really don't need extra line break for that
					if (textInLine.trimmedWidth() > 0) {
						// extra spacing because it's a paragraph
						result.add(" ");
						lineWidths.put(lineCounter, 0.0f);
						mapLineTokens.put(lineCounter, new ArrayList<Token>());
						lineCounter++;
					}
				} else if (isListElement(token)) {
					listElement = true;
					// token padding, token bullet
					try {
						// if it's not left aligned then ignore list and list element and deal with it as normal text where <li> mimic <br> behaviour
						float tab = getAlign().equals(HorizontalAlignment.LEFT) ? indentLevel(DEFAULT_TAB*Math.max(listLevel - 1, 0) + DEFAULT_TAB) : indentLevel(DEFAULT_TAB);
						textInLine.push(currentFont, fontSize, Token.padding(tab / 1000 * getFontSize()));
						if (numberOfOrderedLists>0) {
							// if it's ordering list then move depending on your: ordering number + ". "
							String orderingNumber;
							if(listLevel > 1){
								orderingNumber = stack.peek().getValue() + String.valueOf(orderListElement) + ". ";
							} else {
								orderingNumber = String.valueOf(orderListElement) + ". ";
							}
							textInLine.push(currentFont, fontSize, Token.text(TokenType.ORDERING, orderingNumber));
							orderListElement++;
						} else {
							// if it's unordered list then just move by bullet character (take care of alignment!)
							textInLine.push(currentFont, fontSize, Token.text(TokenType.BULLET, " "));
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					// wrapping at this must-have wrap point
					textInLine.push(sinceLastWrapPoint);
					result.add(textInLine.trimmedText());
					lineWidths.put(lineCounter, textInLine.trimmedWidth());
					mapLineTokens.put(lineCounter, textInLine.tokens());
					maxLineWidth = Math.max(maxLineWidth, textInLine.trimmedWidth());
					textInLine.reset();
					lineCounter++;
					if(listLevel>0){
						// preserve current indent
						try {
							if (numberOfOrderedLists>0) {
								float tab = getAlign().equals(HorizontalAlignment.LEFT) ? indentLevel(DEFAULT_TAB*Math.max(listLevel - 1, 0)) : indentLevel(DEFAULT_TAB);
								// if it's ordering list then move depending on your: ordering number + ". "
								String orderingNumber;
								if(listLevel > 1){
									orderingNumber = stack.peek().getValue() + String.valueOf(orderListElement) + ". ";
								} else {
									orderingNumber = String.valueOf(orderListElement) + ". ";
								}
								float tabAndOrderingNumber = tab + font.getStringWidth(orderingNumber);
								textInLine.push(currentFont, fontSize, Token.padding(tabAndOrderingNumber / 1000 * getFontSize()));
								orderListElement++;
							} else {
								if(getAlign().equals(HorizontalAlignment.LEFT)){
									float tab = indentLevel(DEFAULT_TAB*Math.max(listLevel - 1, 0) + DEFAULT_TAB + BULLET_SPACE);
									textInLine.push(currentFont, fontSize, Token.padding(tab / 1000 * getFontSize()));
								}
							}
						} catch (IOException e) {
							throw new IllegalStateException("Unable to calculate list indentation", e);
						}
					}
				}
				break;
			case TEXT:
				try {
					String word = token.getData();
					float wordWidth = token.getWidth(currentFont);
					if(wordWidth / 1000f * fontSize > width && width > font.getAverageFontWidth() / 1000f * fontSize) {
						// you need to check if you have already something in your line
						boolean alreadyTextInLine = false;
						if(textInLine.trimmedWidth()>0){
							alreadyTextInLine = true;
						}
						while (wordWidth / 1000f * fontSize > width) {
						float width = 0;
						float firstPartWordWidth = 0;
						float restOfTheWordWidth = 0;
						String lastTextToken = word;
						StringBuilder firstPartOfWord = new StringBuilder();
						StringBuilder restOfTheWord = new StringBuilder();
						for (int i = 0; i < lastTextToken.length(); i++) {
							char c = lastTextToken.charAt(i);
							try {
								// Use cached string for ASCII characters, otherwise create new string
								String charStr = (c < 128) ? CHAR_CACHE[c] : String.valueOf(c);
								width += (currentFont.getStringWidth(charStr) / 1000f * fontSize);
							} catch (IOException e) {
								throw new IllegalStateException("Unable to calculate text width", e);
							}
							if(alreadyTextInLine){
								if (width < this.width - textInLine.trimmedWidth()) {
									firstPartOfWord.append(c);
									firstPartWordWidth = Math.max(width, firstPartWordWidth);
								} else {
									restOfTheWord.append(c);
									restOfTheWordWidth = Math.max(width, restOfTheWordWidth);
								}
							} else {
								if (width < this.width) {
									firstPartOfWord.append(c);
									firstPartWordWidth = Math.max(width, firstPartWordWidth);
								} else {
									if(i==0){
										firstPartOfWord.append(c);
										for (int j = 1; j< lastTextToken.length(); j++){
											restOfTheWord.append(lastTextToken.charAt(j));
										}
										break;
									} else {
										restOfTheWord.append(c);
										restOfTheWordWidth = Math.max(width, restOfTheWordWidth);

									}
								}
							}
						}
						// reset
						alreadyTextInLine = false;
						sinceLastWrapPoint.push(currentFont, fontSize,
								Token.text(TokenType.TEXT, firstPartOfWord.toString()));
						textInLine.push(sinceLastWrapPoint);
						// this is our line
						result.add(textInLine.trimmedText());
						lineWidths.put(lineCounter, textInLine.trimmedWidth());
						mapLineTokens.put(lineCounter, textInLine.tokens());
						maxLineWidth = Math.max(maxLineWidth, textInLine.trimmedWidth());
						textInLine.reset();
						lineCounter++;
						word = restOfTheWord.toString();
						wordWidth = currentFont.getStringWidth(word);
						}
						sinceLastWrapPoint.push(currentFont, fontSize, Token.text(TokenType.TEXT, word));
					} else {
						sinceLastWrapPoint.push(currentFont, fontSize, token);
					}

				} catch (IOException e) {
					throw new IllegalStateException("Unable to calculate text width", e);
				}
				break;
			}
		}
		if (sinceLastWrapPoint.trimmedWidth() + textInLine.trimmedWidth() > 0)

		{
			textInLine.push(sinceLastWrapPoint);
			result.add(textInLine.trimmedText());
			lineWidths.put(lineCounter, textInLine.trimmedWidth());
			mapLineTokens.put(lineCounter, textInLine.tokens());
			maxLineWidth = Math.max(maxLineWidth, textInLine.trimmedWidth());
		}

		lines = result;
		return result;

	}

	private static boolean isItalic(final Token token) {
		return "i".equals(token.getData());
	}

	private static boolean isBold(final Token token) {
		return "b".equals(token.getData());
	}

	private static boolean isParagraph(final Token token) {
		return "p".equals(token.getData());
	}

	private static boolean isListElement(final Token token) {
		return "li".equals(token.getData());
	}

	private static boolean isList(final Token token) {
		return "ul".equals(token.getData()) || "ol".equals(token.getData());
	}

	private float indentLevel(int numberOfSpaces) throws IOException {
		if (spaceWidth == null) {
			spaceWidth = font.getSpaceWidth();
		}
		return numberOfSpaces * spaceWidth;
	}

	/**
	 * Gets the appropriate font variant based on bold and italic flags.
	 *
	 * @param isBold whether bold formatting is applied
	 * @param isItalic whether italic formatting is applied
	 * @return the PDFont instance corresponding to the formatting flags
	 */
	public PDFont getFont(boolean isBold, boolean isItalic) {
		if (isBold) {
			if (isItalic) {
				return fontBoldItalic;
			} else {
				return fontBold;
			}
		} else if (isItalic) {
			return fontItalic;
		} else {
			return font;
		}
	}

	/**
	 * Writes the paragraph to the PDF page at the specified position.
	 * <p>
	 * This method renders all lines of the paragraph to the page content stream,
	 * applying alignment, text formatting, and optional decorations (underline, etc.).
	 * Debug mode can draw visual guides for font metrics and width constraints.
	 * </p>
	 *
	 * @param stream the page content stream to write to
	 * @param cursorX the x-coordinate of the starting position
	 * @param cursorY the y-coordinate of the starting position
	 * @return the y-coordinate after writing all lines (cursor position for next element)
	 */
	public float write(final PageContentStreamOptimized stream, float cursorX, float cursorY) {
		if (drawDebug) {
			PDStreamUtils.rectFontMetrics(stream, cursorX, cursorY, font, fontSize);

			// width
			PDStreamUtils.rect(stream, cursorX, cursorY, width, 1, Color.RED);
		}

		for (String line : getLines()) {
			line = line.trim();

			float textX = cursorX;
			switch (align) {
			case CENTER:
				textX += getHorizontalFreeSpace(line) / 2;
				break;
			case LEFT:
				break;
			case RIGHT:
				textX += getHorizontalFreeSpace(line);
				break;
			}

			PDStreamUtils.write(stream, line, font, fontSize, textX, cursorY, color);

			if (textType != null) {
				switch (textType) {
				case HIGHLIGHT:
				case SQUIGGLY:
				case STRIKEOUT:
					throw new UnsupportedOperationException("Not implemented.");
				case UNDERLINE:
					float y = (float) (cursorY - FontUtils.getHeight(font, fontSize)
							- FontUtils.getDescent(font, fontSize) - 1.5);
					try {
						float titleWidth = font.getStringWidth(line) / 1000 * fontSize;
						stream.moveTo(textX, y);
						stream.lineTo(textX + titleWidth, y);
						stream.stroke();
					} catch (final IOException e) {
						throw new IllegalStateException("Unable to underline text", e);
					}
					break;
				default:
					break;
				}
			}

			// move one "line" down
			cursorY -= getFontHeight();
		}

		return cursorY;
	}

	/**
	 * Calculates the total height of the paragraph in points.
	 * <p>
	 * The height is calculated based on the number of lines, font height,
	 * and line spacing. For paragraphs with multiple lines, spacing is applied
	 * between lines but not after the last line.
	 * </p>
	 *
	 * @return the total height of the paragraph in points, or 0 if there are no lines
	 */
	public float getHeight() {
		if (getLines().size() == 0) {
			return 0;
		} else {
			return (getLines().size() - 1) * getLineSpacing() * getFontHeight() + getFontHeight();
		}
	}

	/**
	 * Gets the height of a single line of text using the current font and font size.
	 *
	 * @return the font height in points
	 */
	public float getFontHeight() {
		return FontUtils.getHeight(font, fontSize);
	}

	private float getHorizontalFreeSpace(final String text) {
		try {
			final float tw = font.getStringWidth(text.trim()) / 1000 * fontSize;
			return width - tw;
		} catch (IOException e) {
			throw new IllegalStateException("Unable to calculate text width", e);
		}
	}

	/**
	 * Gets the width constraint for this paragraph.
	 *
	 * @return the width in points
	 */
	public float getWidth() {
		return width;
	}

	/**
	 * Gets the original text content of this paragraph.
	 *
	 * @return the text content (may contain HTML-like formatting tags)
	 */
	public String getText() {
		return text;
	}

	/**
	 * Gets the font size for this paragraph.
	 *
	 * @return the font size in points
	 */
	public float getFontSize() {
		return fontSize;
	}

	/**
	 * Gets the base font used for this paragraph.
	 *
	 * @return the PDFont instance
	 */
	public PDFont getFont() {
		return font;
	}

	/**
	 * Gets the horizontal alignment of this paragraph.
	 *
	 * @return the horizontal alignment
	 */
	public HorizontalAlignment getAlign() {
		return align;
	}

	/**
	 * Sets the horizontal alignment for this paragraph.
	 * <p>
	 * Changing the alignment invalidates the line wrapping cache, forcing
	 * the lines to be recalculated on the next call to {@link #getLines()}.
	 * </p>
	 *
	 * @param align the horizontal alignment to set
	 */
	public void setAlign(HorizontalAlignment align) {
		lines = null; // invalidate line wrapping cache
		this.align = align;
	}

	/**
	 * Checks if debug mode is enabled for this paragraph.
	 * <p>
	 * When debug mode is enabled, the {@link #write(PageContentStreamOptimized, float, float)}
	 * method will draw visual guides showing font metrics and width constraints.
	 * </p>
	 *
	 * @return true if debug mode is enabled, false otherwise
	 */
	public boolean isDrawDebug() {
		return drawDebug;
	}

	/**
	 * Sets the debug mode for this paragraph.
	 * <p>
	 * When debug mode is enabled, the {@link #write(PageContentStreamOptimized, float, float)}
	 * method will draw visual guides showing font metrics and width constraints.
	 * </p>
	 *
	 * @param drawDebug true to enable debug mode, false to disable
	 */
	public void setDrawDebug(boolean drawDebug) {
		this.drawDebug = drawDebug;
	}

	/**
	 * Gets the wrapping function used for text wrapping.
	 * <p>
	 * If no custom wrapping function was provided during construction,
	 * returns the default wrapping function that splits on whitespace,
	 * hyphens, and common punctuation.
	 * </p>
	 *
	 * @return the wrapping function instance
	 */
	public WrappingFunction getWrappingFunction() {
		return wrappingFunction == null ? DEFAULT_WRAP_FUNC : wrappingFunction;
	}

	/**
	 * Gets the maximum width among all lines in this paragraph.
	 * <p>
	 * This value is calculated during line wrapping and represents the actual
	 * width of the widest line, which may be less than the paragraph width constraint.
	 * </p>
	 *
	 * @return the maximum line width in points
	 */
	public float getMaxLineWidth() {
		return maxLineWidth;
	}

	/**
	 * Gets the width of a specific line by its index.
	 * <p>
	 * Line indices are zero-based and correspond to the lines returned by {@link #getLines()}.
	 * The width is calculated during line wrapping and represents the actual rendered width.
	 * </p>
	 *
	 * @param key the zero-based line index
	 * @return the width of the specified line in points
	 */
	public float getLineWidth(int key) {
		return lineWidths.get(key);
	}

	/**
	 * Gets a map of line indices to their corresponding tokens.
	 * <p>
	 * Each entry in the map represents a line (by zero-based index) and its
	 * associated list of tokens that compose that line. This is useful for
	 * advanced rendering or analysis of the paragraph structure.
	 * </p>
	 *
	 * @return a map where keys are line indices and values are lists of tokens
	 */
	public Map<Integer, List<Token>> getMapLineTokens() {
		return mapLineTokens;
	}

	/**
	 * Gets the line spacing multiplier for this paragraph.
	 * <p>
	 * The line spacing determines the vertical space between lines.
	 * A value of 1.0 represents normal spacing, values greater than 1.0
	 * increase spacing, and values less than 1.0 decrease spacing.
	 * </p>
	 *
	 * @return the line spacing multiplier
	 */
	public float getLineSpacing() {
		return lineSpacing;
	}

	/**
	 * Sets the line spacing multiplier for this paragraph.
	 * <p>
	 * The line spacing determines the vertical space between lines.
	 * A value of 1.0 represents normal spacing, values greater than 1.0
	 * increase spacing, and values less than 1.0 decrease spacing.
	 * </p>
	 *
	 * @param lineSpacing the line spacing multiplier to set
	 */
	public void setLineSpacing(float lineSpacing) {
		this.lineSpacing = lineSpacing;
	}

}
