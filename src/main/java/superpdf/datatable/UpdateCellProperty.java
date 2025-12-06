package superpdf.datatable;

import org.apache.pdfbox.pdmodel.PDPage;

import superpdf.Cell;

/**
 * Allows changing the cell properties, while the CSV documents is written directly into the PDF tables
 */
public interface UpdateCellProperty {

    void updateCellPropertiesAtColumn(Cell<PDPage> cell, int column, int row);
}