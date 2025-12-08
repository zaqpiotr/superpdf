package superpdf.datatable;

import org.apache.pdfbox.pdmodel.PDPage;

import superpdf.Cell;

/**
 * Allows changing the cell properties, while the CSV documents is written directly into the PDF tables
 */
public interface UpdateCellProperty {

    /**
     * Updates the properties of a cell at a specific column and row position.
     * This method is called during CSV-to-PDF table rendering to allow customization
     * of cell appearance and behavior based on the cell's position.
     *
     * @param cell the cell to update
     * @param column the column index of the cell
     * @param row the row index of the cell
     */
    void updateCellPropertiesAtColumn(Cell<PDPage> cell, int column, int row);
}