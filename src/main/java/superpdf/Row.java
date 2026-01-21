package superpdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

import superpdf.image.Image;

/**
 * Represents a row in a table with cells containing various types of content.
 * <p>
 * A Row is a container for cells that can hold text, images, or nested tables.
 * It manages cell positioning, borders, and row-specific properties such as
 * height and line spacing. Rows can be configured as header rows.
 * </p>
 *
 * @param <T> the type of PDPage associated with this row
 */
public class Row<T extends PDPage> {

	private final Table<T> table;
	PDOutlineItem bookmark;
	List<Cell<T>> cells;
	private boolean headerRow = false;
	float height;
	private float lineSpacing = 1;
	private boolean fixedHeight = false;

	Row(Table<T> table, List<Cell<T>> cells, float height) {
		this.table = table;
		this.cells = cells;
		this.height = height;
	}

	Row(Table<T> table, float height) {
		this.table = table;
		this.cells = new ArrayList<>();
		this.height = height;
	}

	/**
	 * <p>
	 * Creates a cell with provided width, cell value and default left top
	 * alignment
	 * </p>
	 * 
	 * @param width
	 *            Absolute width in points or in % of table width
	 * @param value
	 *            Cell's value (content)
	 * @return New {@link Cell}
	 */
	public Cell<T> createCell(float width, String value) {
		Cell<T> cell = new Cell<T>(this, width, value, true);
		if (headerRow) {
			// set all cell as header cell
			cell.setHeaderCell(true);
		}
		setBorders(cell, cells.isEmpty());
		cell.setLineSpacing(lineSpacing);
		cells.add(cell);
		return cell;
	}

	/**
	 * <p>
	 * Creates an image cell with provided width and {@link Image}
	 * </p>
	 *
	 * @param width
	 *            Cell's width
	 * @param img
	 *            {@link Image} in the cell
	 * @return {@link ImageCell}
	 */
	public ImageCell<T> createImageCell(float width, Image img) {
		ImageCell<T> cell = new ImageCell<>(this, width, img, true);
		setBorders(cell, cells.isEmpty());
		cells.add(cell);
		return cell;
	}

	/**
	 * <p>
	 * Creates an image cell with provided width, image, and alignment settings.
	 * </p>
	 *
	 * @param width
	 *            Cell's width
	 * @param img
	 *            {@link Image} in the cell
	 * @param align
	 *            {@link HorizontalAlignment} for the image
	 * @param valign
	 *            {@link VerticalAlignment} for the image
	 * @return {@link Cell} containing the image with specified alignment
	 */
	public Cell<T> createImageCell(float width, Image img, HorizontalAlignment align, VerticalAlignment valign) {
		Cell<T> cell = new ImageCell<T>(this, width, img, true, align, valign);
		setBorders(cell, cells.isEmpty());
		cells.add(cell);
		return cell;
	}

	/**
	 * <p>
	 * Creates a table cell with provided width and table data
	 * </p>
	 * 
	 * @param width
	 *            Table width
	 * @param tableData
	 *            Table's data (HTML table tags)
	 * @param doc
	 *            {@link PDDocument} where this table will be drawn
	 * @param page
	 *            {@link PDPage} where this table cell will be drawn
	 * @param yStart
	 *            Y position from which table will be drawn
	 * @param pageTopMargin
	 *            {@link TableCell}'s top margin
	 * @param pageBottomMargin
	 *            {@link TableCell}'s bottom margin
	 * @return {@link TableCell} with provided width and table data
	 */
	public TableCell<T> createTableCell(float width, String tableData, PDDocument doc, PDPage page, float yStart,
			float pageTopMargin, float pageBottomMargin) {
		TableCell<T> cell = new TableCell<T>(this, width, tableData, true, doc, page, yStart, pageTopMargin,
				pageBottomMargin);
		setBorders(cell, cells.isEmpty());
		cells.add(cell);
		return cell;
	}

	/**
	 * <p>
	 * Creates a cell with provided width, cell value, horizontal and vertical
	 * alignment
	 * </p>
	 * 
	 * @param width
	 *            Absolute width in points or in % of table width
	 * @param value
	 *            Cell's value (content)
	 * @param align
	 *            Cell's {@link HorizontalAlignment}
	 * @param valign
	 *            Cell's {@link VerticalAlignment}
	 * @return New {@link Cell}
	 */
	public Cell<T> createCell(float width, String value, HorizontalAlignment align, VerticalAlignment valign) {
		Cell<T> cell = new Cell<T>(this, width, value, true, align, valign);
		if (headerRow) {
			// set all cell as header cell
			cell.setHeaderCell(true);
		}
		setBorders(cell, cells.isEmpty());
		cell.setLineSpacing(lineSpacing);
		cells.add(cell);
		return cell;
	}

	/**
	 * <p>
	 * Creates a cell with the same width as the corresponding header cell
	 * </p>
	 *
	 * @param value
	 *            Cell's value (content)
	 * @return new {@link Cell}
	 */
	public Cell<T> createCell(String value) {
		float headerCellWidth = table.getHeader().getCells().get(cells.size()).getWidth();
		Cell<T> cell = new Cell<T>(this, headerCellWidth, value, false);
		setBorders(cell, cells.isEmpty());
		cells.add(cell);
		return cell;
	}

	/**
	 * <p>
	 * Remove left border to avoid double borders from previous cell's right
	 * border. In most cases left border will be removed.
	 * </p>
	 * 
	 * @param cell
	 *            {@link Cell}
	 * @param leftBorder
	 *            boolean for drawing cell's left border. If {@code true} then
	 *            the left cell's border will be drawn.
	 */
	private void setBorders(final Cell<T> cell, final boolean leftBorder) {
		if (!leftBorder) {
			cell.setLeftBorderStyle(null);
		}
	}

	/**
	 * <p>
	 * remove top borders of cells to avoid double borders from cells in
	 * previous row
	 * </p>
	 */
	void removeTopBorders() {
		for (final Cell<T> cell : cells) {
			cell.setTopBorderStyle(null);
		}
	}

	/**
	 * <p>
	 * Remove all borders of cells.
	 * </p>
	 */
	void removeAllBorders() {
		for (final Cell<T> cell : cells) {
			cell.setBorderStyle(null);
			;
		}
	}

	/**
	 * <p>
	 * Gets maximal height of the cells in current row therefore row's height.
	 * </p>
	 * <p>
	 * If {@link #isFixedHeight()} is true, returns the configured height without
	 * adjusting based on cell content. Otherwise, adjusts the height to accommodate
	 * the tallest cell.
	 * </p>
	 *
	 * @return Row's height
	 */
	public float getHeight() {
		if (fixedHeight) {
			return height;
		}
		float maxheight = 0.0f;
		for (Cell<T> cell : this.cells) {
			float cellHeight = cell.getCellHeight();

			if (cellHeight > maxheight) {
				maxheight = cellHeight;
			}
		}

		if (maxheight > height) {
			this.height = maxheight;
		}
		return height;
	}

	/**
	 * <p>
	 * Gets the line height of the row.
	 * </p>
	 *
	 * @return the line height in points
	 * @throws IOException if an I/O error occurs
	 */
	public float getLineHeight() throws IOException {
		return height;
	}

	/**
	 * <p>
	 * Sets the height of the row.
	 * </p>
	 *
	 * @param height the height to set in points
	 */
	public void setHeight(float height) {
		this.height = height;
	}

	/**
	 * <p>
	 * Gets the list of cells in this row.
	 * </p>
	 *
	 * @return list of cells
	 */
	public List<Cell<T>> getCells() {
		return cells;
	}

	/**
	 * <p>
	 * Gets the number of columns (cells) in this row.
	 * </p>
	 *
	 * @return the number of cells in the row
	 */
	public int getColCount() {
		return cells.size();
	}

	/**
	 * <p>
	 * Sets the list of cells for this row.
	 * </p>
	 *
	 * @param cells the cells to set
	 */
	public void setCells(List<Cell<T>> cells) {
		this.cells = cells;
	}

	/**
	 * <p>
	 * Gets the width of the row (same as the table width).
	 * </p>
	 *
	 * @return the width of the row in points
	 */
	public float getWidth() {
		return table.getWidth();
	}

	/**
	 * <p>
	 * Gets the bookmark associated with this row.
	 * </p>
	 *
	 * @return the {@link PDOutlineItem} bookmark, or null if not set
	 */
	public PDOutlineItem getBookmark() {
		return bookmark;
	}

	/**
	 * <p>
	 * Sets the bookmark for this row.
	 * </p>
	 *
	 * @param bookmark the {@link PDOutlineItem} to set as this row's bookmark
	 */
	public void setBookmark(PDOutlineItem bookmark) {
		this.bookmark = bookmark;
	}

	/**
	 * Calculates the extra width available for the last cell in the row.
	 * This is the difference between the row's width and the sum of all cell widths.
	 *
	 * @return the extra width available for the last cell
	 */
	protected float getLastCellExtraWidth() {
		float cellWidth = 0;
		for (Cell<T> cell : cells) {
			cellWidth += cell.getWidth();
		}

		float lastCellExtraWidth = this.getWidth() - cellWidth;
		return lastCellExtraWidth;
	}

	/**
	 * <p>
	 * Gets the x-coordinate of the right edge (end) of the row.
	 * </p>
	 *
	 * @return the x-coordinate of the row's right edge in points
	 */
	public float xEnd() {
		return table.getMargin() + getWidth();
	}

	/**
	 * <p>
	 * Checks whether this row is a header row.
	 * </p>
	 *
	 * @return true if this is a header row, false otherwise
	 */
	public boolean isHeaderRow() {
		return headerRow;
	}

	/**
	 * <p>
	 * Sets whether this row is a header row.
	 * </p>
	 *
	 * @param headerRow true to mark this as a header row, false otherwise
	 */
	public void setHeaderRow(boolean headerRow) {
		this.headerRow = headerRow;
	}

	/**
	 * <p>
	 * Gets the line spacing factor for this row.
	 * </p>
	 *
	 * @return the line spacing value
	 */
	public float getLineSpacing() {
		return lineSpacing;
	}

	/**
	 * <p>
	 * Sets the line spacing for this row.
	 * </p>
	 *
	 * @param lineSpacing the line spacing value to set
	 */
	public void setLineSpacing(float lineSpacing) {
		this.lineSpacing = lineSpacing;
	}

	/**
	 * <p>
	 * Checks whether this row has a fixed height.
	 * </p>
	 * <p>
	 * When fixed height is enabled, the row height remains constant and
	 * text content will be shrunk to fit within the available space.
	 * </p>
	 *
	 * @return {@code true} if this row has a fixed height, {@code false} otherwise
	 */
	public boolean isFixedHeight() {
		return fixedHeight;
	}

	/**
	 * <p>
	 * Sets whether this row should have a fixed height.
	 * </p>
	 * <p>
	 * When set to {@code true}, the row height will not expand to accommodate
	 * content that exceeds the configured height. Instead, text content will be
	 * automatically shrunk to fit within the available space.
	 * </p>
	 *
	 * @param fixedHeight {@code true} to enable fixed height mode, {@code false} otherwise
	 */
	public void setFixedHeight(boolean fixedHeight) {
		this.fixedHeight = fixedHeight;
	}

	/**
	 * <p>
	 * Fits the text content of all cells to the row height by adjusting font sizes.
	 * </p>
	 * <p>
	 * This method is called automatically when fixed height is enabled and ensures
	 * that text content is scaled down if necessary to fit within the row.
	 * </p>
	 */
	void fitTextToHeight() {
		if (!fixedHeight) {
			return;
		}
		for (Cell<T> cell : cells) {
			cell.fitFontSizeToHeight(height);
		}
	}
}
