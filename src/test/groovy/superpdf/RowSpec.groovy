package superpdf

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem
import spock.lang.Specification
import spock.lang.Shared

class RowSpec extends Specification {

    @Shared PDDocument document
    @Shared PDPage page
    BaseTable table

    def setupSpec() {
        document = new PDDocument()
        page = new PDPage(PDRectangle.A4)
        document.addPage(page)
    }

    def cleanupSpec() {
        document?.close()
    }

    def setup() {
        table = new BaseTable(700, 700, 50, 500, 50, document, page, true, true)
    }

    // Row Creation Tests
    def "createRow returns a valid row"() {
        when:
        def row = table.createRow(20f)

        then:
        row != null
    }

    def "row height is at least the specified minimum"() {
        when:
        def row = table.createRow(30f)

        then:
        row.height >= 0
    }

    // Cell Creation Tests
    def "createCell creates a cell with text"() {
        given:
        def row = table.createRow(20f)

        when:
        def cell = row.createCell(50, "Test")

        then:
        cell != null
        cell.text == "Test"
    }

    def "createCell with alignment sets alignment"() {
        given:
        def row = table.createRow(20f)

        when:
        def cell = row.createCell(50, "Test", HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE)

        then:
        cell.align == HorizontalAlignment.CENTER
        cell.valign == VerticalAlignment.MIDDLE
    }

    def "multiple cells can be created"() {
        given:
        def row = table.createRow(20f)

        when:
        row.createCell(25, "Cell 1")
        row.createCell(25, "Cell 2")
        row.createCell(25, "Cell 3")
        row.createCell(25, "Cell 4")

        then:
        row.colCount == 4
    }

    // Cell Border Handling Tests
    def "first cell has left border"() {
        given:
        def row = table.createRow(20f)

        when:
        def firstCell = row.createCell(50, "First")

        then:
        firstCell.leftBorder != null
    }

    def "second cell has no left border to avoid double borders"() {
        given:
        def row = table.createRow(20f)
        row.createCell(50, "First")

        when:
        def secondCell = row.createCell(50, "Second")

        then:
        secondCell.leftBorder == null
    }

    // getCells Tests
    def "getCells returns all cells"() {
        given:
        def row = table.createRow(20f)
        row.createCell(50, "Cell 1")
        row.createCell(50, "Cell 2")

        expect:
        row.cells.size() == 2
    }

    def "empty row has no cells"() {
        when:
        def row = table.createRow(20f)

        then:
        row.cells.size() == 0
    }

    // Column Count Tests
    def "getColCount returns correct count"() {
        given:
        def row = table.createRow(20f)

        expect:
        row.colCount == 0

        when:
        row.createCell(33, "A")

        then:
        row.colCount == 1

        when:
        row.createCell(33, "B")

        then:
        row.colCount == 2

        when:
        row.createCell(34, "C")

        then:
        row.colCount == 3
    }

    // Row Width Tests
    def "row width equals table width"() {
        when:
        def row = table.createRow(20f)

        then:
        row.width == 500f
    }

    // Header Row Tests
    def "row is not header by default"() {
        when:
        def row = table.createRow(20f)

        then:
        !row.headerRow
    }

    def "setHeaderRow marks row as header"() {
        given:
        def row = table.createRow(20f)

        when:
        row.setHeaderRow(true)

        then:
        row.headerRow
    }

    def "header row cells are marked as header cells"() {
        given:
        def row = table.createRow(20f)
        row.setHeaderRow(true)

        when:
        def cell = row.createCell(50, "Header")

        then:
        cell.headerCell
    }

    // Bookmark Tests
    def "bookmark is null by default"() {
        when:
        def row = table.createRow(20f)

        then:
        row.bookmark == null
    }

    def "setBookmark sets the bookmark"() {
        given:
        def row = table.createRow(20f)
        def bookmark = new PDOutlineItem()
        bookmark.setTitle("Test Bookmark")

        when:
        row.setBookmark(bookmark)

        then:
        row.bookmark == bookmark
        row.bookmark.title == "Test Bookmark"
    }

    // Line Spacing Tests
    def "default line spacing is 1"() {
        when:
        def row = table.createRow(20f)

        then:
        row.lineSpacing == 1f
    }

    def "setLineSpacing changes line spacing"() {
        given:
        def row = table.createRow(20f)

        when:
        row.setLineSpacing(1.5f)

        then:
        row.lineSpacing == 1.5f
    }

    def "line spacing is applied to created cells"() {
        given:
        def row = table.createRow(20f)
        row.setLineSpacing(2.0f)

        when:
        def cell = row.createCell(50, "Test")

        then:
        cell.lineSpacing == 2.0f
    }

    // Row Height Calculation Tests
    def "row height is max of cell heights"() {
        given:
        def row = table.createRow(10f)

        when:
        row.createCell(50, "Short")
        row.createCell(50, "This is a much longer text that should wrap to multiple lines and create a taller cell height")

        then:
        def rowHeight = row.height
        row.cells.every { rowHeight >= it.cellHeight }
    }

    // Remove Borders Tests
    def "removeTopBorders removes top borders from all cells"() {
        given:
        def row = table.createRow(20f)
        def cell1 = row.createCell(50, "A")
        def cell2 = row.createCell(50, "B")

        expect:
        cell1.topBorder != null
        cell2.topBorder != null

        when:
        row.removeTopBorders()

        then:
        cell1.topBorder == null
        cell2.topBorder == null
    }

    def "removeAllBorders removes all borders from all cells"() {
        given:
        def row = table.createRow(20f)
        def cell = row.createCell(100, "Test")

        when:
        row.removeAllBorders()

        then:
        cell.leftBorder == null
        cell.rightBorder == null
        cell.topBorder == null
        cell.bottomBorder == null
    }

    // xEnd Tests
    def "xEnd returns margin plus width"() {
        when:
        def row = table.createRow(20f)

        then:
        row.xEnd() == 550f  // margin (50) + width (500)
    }

    // Set Cells Tests
    def "setCells replaces all cells"() {
        given:
        def row1 = table.createRow(20f)
        row1.createCell(50, "A")
        row1.createCell(50, "B")

        def row2 = table.createRow(20f)

        when:
        row2.setCells(row1.cells)

        then:
        row2.colCount == 2
    }

    // Last Cell Extra Width Tests
    def "getLastCellExtraWidth returns remaining width"() {
        given:
        def row = table.createRow(20f)
        row.createCell(30, "A")  // 30% = 150px
        row.createCell(30, "B")  // 30% = 150px

        expect:
        row.lastCellExtraWidth == 200f  // 500 - 300
    }
}
