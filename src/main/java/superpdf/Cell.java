package superpdf;

import java.awt.Color;
import java.net.URL;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import superpdf.line.LineStyle;
import superpdf.text.WrappingFunction;
import superpdf.utils.FontUtils;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

/**
 * Represents a table cell in a PDF document with configurable content, styling, and layout properties.
 * <p>
 * A cell contains text content and supports various formatting options including fonts, colors, padding,
 * borders, alignment, and text rotation. Cells are contained within rows and can be styled individually
 * or inherit styles from other cells.
 * </p>
 *
 * @param <T> The type of {@link PDPage} that this cell will be rendered on
 */
public class Cell<T extends PDPage> {

	private float width;
	private Float height;
	private String text;

	private URL url = null;

	private PDFont font = null;
	private PDFont fontBold = null;

	private float fontSize = 8;
	private Color fillColor;
	private Color textColor = Color.BLACK;
	private final Row<T> row;
	private WrappingFunction wrappingFunction;
	private boolean isHeaderCell = false;
	private boolean isColspanCell = false;

	// default padding
	private float leftPadding = 5f;
	private float rightPadding = 5f;
	private float topPadding = 5f;
	private float bottomPadding = 5f;

	// default border
	private LineStyle leftBorderStyle = new LineStyle(Color.BLACK, 1);
	private LineStyle rightBorderStyle = new LineStyle(Color.BLACK, 1);
	private LineStyle topBorderStyle = new LineStyle(Color.BLACK, 1);
	private LineStyle bottomBorderStyle = new LineStyle(Color.BLACK, 1);

	private Paragraph paragraph = null;
	private float lineSpacing = 1;
	private boolean textRotated = false;

	private HorizontalAlignment align;
	private VerticalAlignment valign;

	float horizontalFreeSpace = 0;
	float verticalFreeSpace = 0;

	private final List<CellContentDrawnListener<T>> contentDrawnListenerList = new ArrayList<CellContentDrawnListener<T>>();

	/**
	 * <p>
	 * Constructs a cell with the default alignment
	 * {@link VerticalAlignment#TOP} {@link HorizontalAlignment#LEFT}.
	 * </p>
	 *
	 * @param row
	 * @param width
	 * @param text
	 * @param isCalculated
	 * @see Cell#Cell(Row, float, String, boolean, HorizontalAlignment,
	 *      VerticalAlignment)
	 */
	Cell(Row<T> row, float width, String text, boolean isCalculated) {
		this(row, width, text, isCalculated, HorizontalAlignment.LEFT, VerticalAlignment.TOP);
	}

	/**
	 * <p>
	 * Constructs a cell.
	 * </p>
	 *
	 * @param row
	 *            The parent row
	 * @param width
	 *            absolute width in points or in % of table width (depending on
	 *            the parameter {@code isCalculated})
	 * @param text
	 *            The text content of the cell
	 * @param isCalculated
	 *            If {@code true}, the width is interpreted in % to the table
	 *            width
	 * @param align
	 *            The {@link HorizontalAlignment} of the cell content
	 * @param valign
	 *            The {@link VerticalAlignment} of the cell content
	 * @see Cell#Cell(Row, float, String, boolean)
	 */
	Cell(Row<T> row, float width, String text, boolean isCalculated, HorizontalAlignment align,
			VerticalAlignment valign) {
		this.row = row;
		if (isCalculated) {
			double calculatedWidth = row.getWidth() * (width / 100);
			this.width = (float) calculatedWidth;
		} else {
			this.width = width;
		}

		if (getWidth() > row.getWidth()) {
			throw new IllegalArgumentException(
					"Cell Width=" + getWidth() + " can't be bigger than row width=" + row.getWidth());
		}
		//check if we have new default font
		if(!FontUtils.getDefaultfonts().isEmpty()){
			font = FontUtils.getDefaultfonts().get("font");
			fontBold = FontUtils.getDefaultfonts().get("fontBold");
		}
		this.text = text == null ? "" : text;
		this.align = align;
		this.valign = valign;
		this.wrappingFunction = null;
	}

	/**
	 * <p>
	 * Retrieves cell's text {@link Color}. Default color is black.
	 * </p>
	 *
	 * @return {@link Color} of the cell's text
	 */
	public Color getTextColor() {
		return textColor;
	}

	/**
	 * <p>
	 * Sets cell's text {@link Color}.
	 * </p>
	 *
	 * @param textColor
	 *            designated text {@link Color}
	 */
	public void setTextColor(Color textColor) {
		this.textColor = textColor;
	}

	/**
	 * <p>
	 * Gets fill (background) {@link Color} for the current cell.
	 * </p>
	 *
	 * @return Fill {@link Color} for the cell
	 */
	public Color getFillColor() {
		return fillColor;
	}

	/**
	 * <p>
	 * Sets fill (background) {@link Color} for the current cell.
	 * </p>
	 *
	 * @param fillColor
	 *            Fill {@link Color} for the cell
	 */
	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
	}

	/**
	 * <p>
	 * Gets cell's width.
	 * </p>
	 *
	 * @return Cell's width
	 */
	public float getWidth() {
		return width;
	}

	/**
	 * <p>
	 * Gets cell's width without (left,right) padding.
	 *
	 * @return Inner cell's width
	 */
	public float getInnerWidth() {
		return getWidth() - getLeftPadding() - getRightPadding()
				- (leftBorderStyle == null ? 0 : leftBorderStyle.getWidth())
				- (rightBorderStyle == null ? 0 : rightBorderStyle.getWidth());
	}

	/**
	 * <p>
	 * Gets cell's height without (top,bottom) padding.
	 *
	 * @return Inner cell's height
	 */
	public float getInnerHeight() {
		return getHeight() - getBottomPadding() - getTopPadding()
				- (topBorderStyle == null ? 0 : topBorderStyle.getWidth())
				- (bottomBorderStyle == null ? 0 : bottomBorderStyle.getWidth());
	}

	/**
	 * <p>
	 * Retrieves text from current cell
	 * </p>
	 *
	 * @return cell's text
	 */
	public String getText() {
		return text;
	}

	/**
	 * <p>
	 * Sets cell's text value
	 * </p>
	 *
	 * @param text
	 *            Text value of the cell
	 */
	public void setText(String text) {
		this.text = text;

		// paragraph invalidated
		paragraph = null;
	}

	/**
	 * <p>
	 * Gets appropriate {@link PDFont} for current cell.
	 * </p>
	 *
	 * @return {@link PDFont} for current cell
	 * @throws IllegalArgumentException
	 *             if <code>font</code> is not set.
	 */
	public PDFont getFont() {
		if (font == null) {
			throw new IllegalArgumentException("Font not set.");
		}
		if (isHeaderCell) {
			return fontBold;
		} else {
			return font;
		}
	}

	/**
	 * <p>
	 * Sets appropriate {@link PDFont} for current cell.
	 * </p>
	 *
	 * @param font
	 *            {@link PDFont} for current cell
	 */
	public void setFont(PDFont font) {
		this.font = font;

		// paragraph invalidated
		paragraph = null;
	}

	/**
	 * <p>
	 * Gets {@link PDFont} size for current cell (in points).
	 * </p>
	 *
	 * @return {@link PDFont} size for current cell (in points).
	 */
	public float getFontSize() {
		return fontSize;
	}

	/**
	 * <p>
	 * Sets {@link PDFont} size for current cell (in points).
	 * </p>
	 *
	 * @param fontSize
	 *            {@link PDFont} size for current cell (in points).
	 */
	public void setFontSize(float fontSize) {
		this.fontSize = fontSize;

		// paragraph invalidated
		paragraph = null;
	}

	/**
	 * <p>
	 * Retrieves a valid {@link Paragraph} depending of cell's {@link PDFont}
	 * and value rotation.
	 * </p>
	 *
	 * <p>
	 * If cell has rotated value then {@link Paragraph} width is depending of
	 * {@link Cell#getInnerHeight()} otherwise {@link Cell#getInnerWidth()}
	 * </p>
	 *
	 *
	 * @return Cell's {@link Paragraph}
	 */
	public Paragraph getParagraph() {
		if (paragraph == null) {
			// if it is header cell then use font bold
			if (isHeaderCell) {
				if (isTextRotated()) {
					paragraph = new Paragraph(text, fontBold, fontSize, getInnerHeight(), align, textColor, null,
							wrappingFunction, lineSpacing);
				} else {
					paragraph = new Paragraph(text, fontBold, fontSize, getInnerWidth(), align, textColor, null,
							wrappingFunction, lineSpacing);
				}
			} else {
				if (isTextRotated()) {
					paragraph = new Paragraph(text, font, fontSize, getInnerHeight(), align, textColor, null,
							wrappingFunction, lineSpacing);
				} else {
					paragraph = new Paragraph(text, font, fontSize, getInnerWidth(), align, textColor, null,
							wrappingFunction, lineSpacing);
				}
			}
		}
		return paragraph;
	}

	/**
	 * <p>
	 * Gets the extra width for this cell, which includes any extra width from the previous cell in the row.
	 * </p>
	 *
	 * @return The cumulative extra width from the row's last cell plus this cell's width
	 */
	public float getExtraWidth() {
		return this.row.getLastCellExtraWidth() + getWidth();
	}

	/**
	 * <p>
	 * Gets the cell's height according to {@link Row}'s height
	 * </p>
	 *
	 * @return {@link Row}'s height
	 */
	public float getHeight() {
		return row.getHeight();
	}

	/**
	 * <p>
	 * Gets the height of the single cell, opposed to {@link #getHeight()},
	 * which returns the row's height.
	 * </p>
	 * <p>
	 * Depending of rotated/normal cell's value there is two cases for
	 * calculation:
	 * </p>
	 * <ol>
	 * <li>Rotated value - cell's height is equal to overall text length in the
	 * cell with necessery paddings (top,bottom)</li>
	 * <li>Normal value - cell's height is equal to {@link Paragraph}'s height
	 * with necessery paddings (top,bottom)</li>
	 * </ol>
	 *
	 * @return Cell's height
	 * @throws IllegalStateException
	 *             if <code>font</code> is not set.
	 */
	public float getCellHeight() {
		if (height != null) {
			return height;
		}

		if (isTextRotated()) {
			return FontUtils.getStringWidth(getFont(), getText(), getFontSize()) + getTopPadding()
					+ (getTopBorder() == null ? 0 : getTopBorder().getWidth()) + getBottomPadding()
					+ (getBottomBorder() == null ? 0 : getBottomBorder().getWidth());
		} else {
			return getTextHeight() + getTopPadding() + getBottomPadding()
					+ (getTopBorder() == null ? 0 : getTopBorder().getWidth())
					+ (getBottomBorder() == null ? 0 : getBottomBorder().getWidth());
		}
	}

	/**
	 * <p>
	 * Sets the height of the single cell.
	 * </p>
	 *
	 * @param height
	 *            Cell's height
	 */
	public void setHeight(final Float height) {
		this.height = height;
	}

	/**
	 * <p>
	 * Gets {@link Paragraph}'s height
	 * </p>
	 *
	 * @return {@link Paragraph}'s height
	 */
	public float getTextHeight() {
		return getParagraph().getHeight();
	}

	/**
	 * <p>
	 * Adjusts the font size to fit the text within the specified row height.
	 * </p>
	 * <p>
	 * Uses binary search to find the optimal font size that allows the text
	 * to fit within the available height (row height minus padding and borders).
	 * The minimum font size is 1pt.
	 * </p>
	 *
	 * @param rowHeight the row height to fit the text into
	 */
	void fitFontSizeToHeight(float rowHeight) {
		if (isTextRotated() || text == null || text.isEmpty()) {
			return;
		}

		float availableHeight = rowHeight - getTopPadding() - getBottomPadding()
				- (topBorderStyle == null ? 0 : topBorderStyle.getWidth())
				- (bottomBorderStyle == null ? 0 : bottomBorderStyle.getWidth());

		if (availableHeight <= 0) {
			return;
		}

		float requiredHeight = getRequiredTextHeight();
		if (requiredHeight <= availableHeight) {
			return;
		}

		// Binary search for optimal font size
		float minFontSize = 1f;
		float maxFontSize = fontSize;
		float originalFontSize = fontSize;

		for (int i = 0; i < 10; i++) {
			float midFontSize = (minFontSize + maxFontSize) / 2;
			setFontSize(midFontSize);

			requiredHeight = getRequiredTextHeight();
			if (requiredHeight <= availableHeight) {
				minFontSize = midFontSize;
			} else {
				maxFontSize = midFontSize;
			}
		}

		// Use the smaller font size to ensure it fits
		if (getRequiredTextHeight() > availableHeight) {
			setFontSize(minFontSize);
		}
	}

	/**
	 * <p>
	 * Gets the required text height including font descent.
	 * </p>
	 *
	 * @return the required height for rendering the text
	 */
	private float getRequiredTextHeight() {
		float paragraphHeight = getParagraph().getHeight();
		float descent = FontUtils.getDescent(getFont(), getFontSize());
		return paragraphHeight + Math.abs(descent);
	}

	/**
	 * <p>
	 * Gets {@link Paragraph}'s width
	 * </p>
	 *
	 * @return {@link Paragraph}'s width
	 */
	public float getTextWidth() {
		return getParagraph().getWidth();
	}

	/**
	 * <p>
	 * Gets cell's left padding (in points).
	 * </p>
	 *
	 * @return Cell's left padding (in points).
	 */
	public float getLeftPadding() {
		return leftPadding;
	}

	/**
	 * <p>
	 * Sets cell's left padding (in points)
	 * </p>
	 *
	 * @param cellLeftPadding
	 *            Cell's left padding (in points).
	 */
	public void setLeftPadding(float cellLeftPadding) {
		this.leftPadding = cellLeftPadding;

		// paragraph invalidated
		paragraph = null;
	}

	/**
	 * <p>
	 * Gets cell's right padding (in points).
	 * </p>
	 *
	 * @return Cell's right padding (in points).
	 */
	public float getRightPadding() {
		return rightPadding;
	}

	/**
	 * <p>
	 * Sets cell's right padding (in points)
	 * </p>
	 *
	 * @param cellRightPadding
	 *            Cell's right padding (in points).
	 */
	public void setRightPadding(float cellRightPadding) {
		this.rightPadding = cellRightPadding;

		// paragraph invalidated
		paragraph = null;
	}

	/**
	 * <p>
	 * Gets cell's top padding (in points).
	 * </p>
	 *
	 * @return Cell's top padding (in points).
	 */
	public float getTopPadding() {
		return topPadding;
	}

	/**
	 * <p>
	 * Sets cell's top padding (in points)
	 * </p>
	 *
	 * @param cellTopPadding
	 *            Cell's top padding (in points).
	 */
	public void setTopPadding(float cellTopPadding) {
		this.topPadding = cellTopPadding;
	}

	/**
	 * <p>
	 * Gets cell's bottom padding (in points).
	 * </p>
	 *
	 * @return Cell's bottom padding (in points).
	 */
	public float getBottomPadding() {
		return bottomPadding;
	}

	/**
	 * <p>
	 * Sets cell's bottom padding (in points)
	 * </p>
	 *
	 * @param cellBottomPadding
	 *            Cell's bottom padding (in points).
	 */
	public void setBottomPadding(float cellBottomPadding) {
		this.bottomPadding = cellBottomPadding;
	}

	/**
	 * <p>
	 * Gets free vertical space of cell.
	 * </p>
	 *
	 * <p>
	 * If cell has rotated value then free vertical space is equal inner cell's
	 * height ({@link #getInnerHeight()}) subtracted to the longest line of
	 * rotated {@link Paragraph} otherwise it's just cell's inner height (
	 * {@link #getInnerHeight()}) subtracted with width of the normal
	 * {@link Paragraph}.
	 * </p>
	 *
	 * @return Free vertical space of the cell's.
	 */
	public float getVerticalFreeSpace() {
		if (isTextRotated()) {
			// need to calculate max line width so we just iterating through
			// lines
			for (String line : getParagraph().getLines()) {
			}
			return getInnerHeight() - getParagraph().getMaxLineWidth();
		} else {
			return getInnerHeight() - getTextHeight();
		}
	}

	/**
	 * <p>
	 * Gets free horizontal space of cell.
	 * </p>
	 *
	 * <p>
	 * If cell has rotated value then free horizontal space is equal cell's
	 * inner width ({@link #getInnerWidth()}) subtracted to the
	 * {@link Paragraph}'s height otherwise it's just cell's
	 * {@link #getInnerWidth()} subtracted with width of longest line in normal
	 * {@link Paragraph}.
	 * </p>
	 *
	 * @return Free vertical space of the cell's.
	 */
	public float getHorizontalFreeSpace() {
		if (isTextRotated()) {
			return getInnerWidth() - getTextHeight();
		} else {
			return getInnerWidth() - getParagraph().getMaxLineWidth();
		}
	}

	/**
	 * <p>
	 * Gets the horizontal alignment of the cell's content.
	 * </p>
	 *
	 * @return The {@link HorizontalAlignment} of the cell's content
	 */
	public HorizontalAlignment getAlign() {
		return align;
	}

	/**
	 * <p>
	 * Gets the vertical alignment of the cell's content.
	 * </p>
	 *
	 * @return The {@link VerticalAlignment} of the cell's content
	 */
	public VerticalAlignment getValign() {
		return valign;
	}

	/**
	 * <p>
	 * Checks whether this cell is a header cell.
	 * </p>
	 *
	 * @return {@code true} if this is a header cell, {@code false} otherwise
	 */
	public boolean isHeaderCell() {
		return isHeaderCell;
	}

	/**
	 * <p>
	 * Sets whether this cell is a header cell.
	 * </p>
	 *
	 * @param isHeaderCell {@code true} to mark this as a header cell, {@code false} otherwise
	 */
	public void setHeaderCell(boolean isHeaderCell) {
		this.isHeaderCell = isHeaderCell;
	}

	/**
	 * <p>
	 * Gets the wrapping function used for text wrapping in this cell.
	 * </p>
	 *
	 * @return The {@link WrappingFunction} used for text wrapping
	 */
	public WrappingFunction getWrappingFunction() {
		return getParagraph().getWrappingFunction();
	}

	/**
	 * <p>
	 * Sets the wrapping function used for text wrapping in this cell.
	 * </p>
	 *
	 * @param wrappingFunction The {@link WrappingFunction} to use for text wrapping
	 */
	public void setWrappingFunction(WrappingFunction wrappingFunction) {
		this.wrappingFunction = wrappingFunction;

		// paragraph invalidated
		paragraph = null;
	}

	/**
	 * <p>
	 * Gets the line style for the left border of this cell.
	 * </p>
	 *
	 * @return The {@link LineStyle} of the left border
	 */
	public LineStyle getLeftBorder() {
		return leftBorderStyle;
	}

	/**
	 * <p>
	 * Gets the line style for the right border of this cell.
	 * </p>
	 *
	 * @return The {@link LineStyle} of the right border
	 */
	public LineStyle getRightBorder() {
		return rightBorderStyle;
	}

	/**
	 * <p>
	 * Gets the line style for the top border of this cell.
	 * </p>
	 *
	 * @return The {@link LineStyle} of the top border
	 */
	public LineStyle getTopBorder() {
		return topBorderStyle;
	}

	/**
	 * <p>
	 * Gets the line style for the bottom border of this cell.
	 * </p>
	 *
	 * @return The {@link LineStyle} of the bottom border
	 */
	public LineStyle getBottomBorder() {
		return bottomBorderStyle;
	}

	/**
	 * <p>
	 * Sets the line style for the left border of this cell.
	 * </p>
	 *
	 * @param leftBorder The {@link LineStyle} to apply to the left border
	 */
	public void setLeftBorderStyle(LineStyle leftBorder) {
		this.leftBorderStyle = leftBorder;
	}

	/**
	 * <p>
	 * Sets the line style for the right border of this cell.
	 * </p>
	 *
	 * @param rightBorder The {@link LineStyle} to apply to the right border
	 */
	public void setRightBorderStyle(LineStyle rightBorder) {
		this.rightBorderStyle = rightBorder;
	}

	/**
	 * <p>
	 * Sets the line style for the top border of this cell.
	 * </p>
	 *
	 * @param topBorder The {@link LineStyle} to apply to the top border
	 */
	public void setTopBorderStyle(LineStyle topBorder) {
		this.topBorderStyle = topBorder;
	}

	/**
	 * <p>
	 * Sets the line style for the bottom border of this cell.
	 * </p>
	 *
	 * @param bottomBorder The {@link LineStyle} to apply to the bottom border
	 */
	public void setBottomBorderStyle(LineStyle bottomBorder) {
		this.bottomBorderStyle = bottomBorder;
	}

	/**
	 * <p>
	 * Easy setting for cell border style.
	 *
	 * @param border
	 *            It is {@link LineStyle} for all borders
	 * @see LineStyle Rendering line attributes
	 */
	public void setBorderStyle(LineStyle border) {
		this.leftBorderStyle = border;
		this.rightBorderStyle = border;
		this.topBorderStyle = border;
		this.bottomBorderStyle = border;
	}

	/**
	 * <p>
	 * Checks whether the text in this cell is rotated.
	 * </p>
	 *
	 * @return {@code true} if the text is rotated, {@code false} otherwise
	 */
	public boolean isTextRotated() {
		return textRotated;
	}

	/**
	 * <p>
	 * Sets whether the text in this cell should be rotated.
	 * </p>
	 *
	 * @param textRotated {@code true} to rotate the text, {@code false} otherwise
	 */
	public void setTextRotated(boolean textRotated) {
		this.textRotated = textRotated;
	}

	/**
	 * <p>
	 * Gets the bold font used for this cell.
	 * </p>
	 *
	 * @return The {@link PDFont} used for bold text
	 */
	public PDFont getFontBold() {
		return fontBold;
	}

	/**
	 * <p>
	 * Sets the {@linkplain PDFont font} used for bold text, for example in
	 * {@linkplain #isHeaderCell() header cells}.
	 * </p>
	 *
	 * @param fontBold
	 *            The {@linkplain PDFont font} to use for bold text
	 */
	public void setFontBold(final PDFont fontBold) {
		this.fontBold = fontBold;
	}

	/**
	 * <p>
	 * Checks whether this cell spans multiple columns.
	 * </p>
	 *
	 * @return {@code true} if this cell spans multiple columns, {@code false} otherwise
	 */
	public boolean isColspanCell() {
		return isColspanCell;
	}

	/**
	 * <p>
	 * Sets whether this cell spans multiple columns.
	 * </p>
	 *
	 * @param isColspanCell {@code true} to make this cell span multiple columns, {@code false} otherwise
	 */
	public void setColspanCell(boolean isColspanCell) {
		this.isColspanCell = isColspanCell;
	}

	/**
	 * <p>
	 * Sets the horizontal alignment of the cell's content.
	 * </p>
	 *
	 * @param align The {@link HorizontalAlignment} to apply to the cell's content
	 */
	public void setAlign(HorizontalAlignment align) {
		this.align = align;
	}

	/**
	 * <p>
	 * Sets the vertical alignment of the cell's content.
	 * </p>
	 *
	 * @param valign The {@link VerticalAlignment} to apply to the cell's content
	 */
	public void setValign(VerticalAlignment valign) {
		this.valign = valign;
	}

	/**
	 * <p>
	 * Copies the style of an existing cell to this cell
	 * </p>
	 *
	 * @param sourceCell Source {@link Cell} from which cell style will be copied.
	 */
	public void copyCellStyle(Cell sourceCell) {
		Boolean leftBorder = this.leftBorderStyle == null;
		setBorderStyle(sourceCell.getTopBorder());
		if (leftBorder) {
			this.leftBorderStyle = null;// if left border wasn't set, don't set
										// it now
		}
		this.font = sourceCell.getFont();// otherwise paragraph gets invalidated
		this.fontBold = sourceCell.getFontBold();
		this.fontSize = sourceCell.getFontSize();
		setFillColor(sourceCell.getFillColor());
		setTextColor(sourceCell.getTextColor());
		setAlign(sourceCell.getAlign());
		setValign(sourceCell.getValign());
	}

	/**
	 * <p>
	 * Compares the style of a cell with another cell
	 * </p>
	 *
	 * @param sourceCell Source {@link Cell} which will be used for style comparation
	 * @return boolean if source cell has the same style
	 */
	public Boolean hasSameStyle(Cell sourceCell) {
		if (!sourceCell.getTopBorder().equals(getTopBorder())) {
			return false;
		}
		if (!sourceCell.getFont().equals(getFont())) {
			return false;
		}
		if (!sourceCell.getFontBold().equals(getFontBold())) {
			return false;
		}
		if (!sourceCell.getFillColor().equals(getFillColor())) {
			return false;
		}
		if (!sourceCell.getTextColor().equals(getTextColor())) {
			return false;
		}
		if (!sourceCell.getAlign().equals(getAlign())) {
			return false;
		}
		if (!sourceCell.getValign().equals(getValign())) {
			return false;
		}
		return true;
	}

	/**
	 * <p>
	 * Sets the width of this cell.
	 * </p>
	 *
	 * @param width The width of the cell in points
	 */
	public void setWidth(float width) {
		this.width = width;
	}

	/**
	 * <p>
	 * Gets the line spacing used for multi-line text in this cell.
	 * </p>
	 *
	 * @return The line spacing multiplier (e.g., 1.0 for single spacing, 1.5 for 1.5 spacing)
	 */
	public float getLineSpacing() {
		return lineSpacing;
	}

	/**
	 * <p>
	 * Sets the line spacing used for multi-line text in this cell.
	 * </p>
	 *
	 * @param lineSpacing The line spacing multiplier (e.g., 1.0 for single spacing, 1.5 for 1.5 spacing)
	 */
	public void setLineSpacing(float lineSpacing) {
		this.lineSpacing = lineSpacing;
	}

	/**
	 * <p>
	 * Adds a listener that will be notified when the cell's content is drawn.
	 * </p>
	 *
	 * @param listener The {@link CellContentDrawnListener} to add
	 */
	public void addContentDrawnListener(CellContentDrawnListener<T> listener) {
		contentDrawnListenerList.add(listener);
	}

	/**
	 * <p>
	 * Gets the list of listeners that will be notified when the cell's content is drawn.
	 * </p>
	 *
	 * @return A {@link List} of {@link CellContentDrawnListener} instances
	 */
	public List<CellContentDrawnListener<T>> getCellContentDrawnListeners() {
		return contentDrawnListenerList;
	}

	/**
	 * <p>
	 * Notifies all registered listeners that the cell's content has been drawn.
	 * </p>
	 *
	 * @param document The {@link PDDocument} in which the content was drawn
	 * @param page The {@link PDPage} on which the content was drawn
	 * @param rectangle The {@link PDRectangle} representing the area where the content was drawn
	 */
	public void notifyContentDrawnListeners(PDDocument document, PDPage page, PDRectangle rectangle) {
		for(CellContentDrawnListener<T> listener : getCellContentDrawnListeners()) {
			listener.onContentDrawn(this, document, page, rectangle);
		}
	}

	/**
	 * <p>
	 * Gets the URL associated with this cell, if any.
	 * </p>
	 *
	 * @return The {@link URL} associated with this cell, or {@code null} if no URL is set
	 */
	public URL getUrl() {
		return url;
	}

	/**
	 * <p>
	 * Sets the URL to be associated with this cell, making it a clickable link.
	 * </p>
	 *
	 * @param url The {@link URL} to associate with this cell
	 */
	public void setUrl(URL url) {
		this.url = url;
	}


}
