package superpdf

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import spock.lang.Specification
import spock.lang.Shared
import superpdf.line.LineStyle
import java.awt.Color

class TableSpec extends Specification {

    @Shared PDDocument document
    @Shared PDPage page

    def setupSpec() {
        document = new PDDocument()
        page = new PDPage(PDRectangle.A4)
        document.addPage(page)
    }

    def cleanupSpec() {
        document?.close()
    }

    // Table Creation Tests
    def "table can be created with basic parameters"() {
        when:
        def table = new BaseTable(700, 700, 50, 500, 50, document, page, true, true)

        then:
        table != null
        table.width == 500f
    }

    def "table can create rows"() {
        given:
        def table = new BaseTable(700, 700, 50, 500, 50, document, page, true, true)

        when:
        def row = table.createRow(20f)

        then:
        row != null
    }

    def "table can create multiple rows with cells"() {
        given:
        def table = new BaseTable(700, 700, 50, 500, 50, document, page, true, true)

        when:
        def headerRow = table.createRow(15f)
        headerRow.createCell(25, "Column 1")
        headerRow.createCell(25, "Column 2")
        headerRow.createCell(25, "Column 3")
        headerRow.createCell(25, "Column 4")

        def dataRow = table.createRow(12f)
        dataRow.createCell(25, "Data 1")
        dataRow.createCell(25, "Data 2")
        dataRow.createCell(25, "Data 3")
        dataRow.createCell(25, "Data 4")

        then:
        headerRow.colCount == 4
        dataRow.colCount == 4
    }

    // Header Row Tests
    def "table can have header row"() {
        given:
        def table = new BaseTable(700, 700, 50, 500, 50, document, page, true, true)
        def row = table.createRow(15f)
        row.setHeaderRow(true)
        row.createCell(50, "Header 1")
        row.createCell(50, "Header 2")

        expect:
        row.headerRow
        row.cells.every { it.headerCell }
    }

    // Cell Styling Tests
    def "cells can be styled with colors"() {
        given:
        def table = new BaseTable(700, 700, 50, 500, 50, document, page, true, true)
        def row = table.createRow(20f)
        def cell = row.createCell(100, "Styled Cell")

        when:
        cell.setFillColor(Color.YELLOW)
        cell.setTextColor(Color.BLUE)

        then:
        cell.fillColor == Color.YELLOW
        cell.textColor == Color.BLUE
    }

    def "cells can have custom borders"() {
        given:
        def table = new BaseTable(700, 700, 50, 500, 50, document, page, true, true)
        def row = table.createRow(20f)
        def cell = row.createCell(100, "Bordered Cell")
        def redBorder = new LineStyle(Color.RED, 2f)

        when:
        cell.setBorderStyle(redBorder)

        then:
        cell.topBorder == redBorder
        cell.bottomBorder == redBorder
        cell.leftBorder == redBorder
        cell.rightBorder == redBorder
    }

    def "cells can have different fonts"() {
        given:
        def table = new BaseTable(700, 700, 50, 500, 50, document, page, true, true)
        def row = table.createRow(20f)
        def cell = row.createCell(100, "Custom Font")
        def courier = new PDType1Font(Standard14Fonts.FontName.COURIER)

        when:
        cell.setFont(courier)
        cell.setFontSize(12f)

        then:
        cell.font == courier
        cell.fontSize == 12f
    }

    // Alignment Tests
    def "cells can have different alignments"() {
        given:
        def table = new BaseTable(700, 700, 50, 500, 50, document, page, true, true)
        def row = table.createRow(20f)

        when:
        def leftCell = row.createCell(33, "Left", HorizontalAlignment.LEFT, VerticalAlignment.TOP)
        def centerCell = row.createCell(33, "Center", HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE)
        def rightCell = row.createCell(34, "Right", HorizontalAlignment.RIGHT, VerticalAlignment.BOTTOM)

        then:
        leftCell.align == HorizontalAlignment.LEFT
        leftCell.valign == VerticalAlignment.TOP
        centerCell.align == HorizontalAlignment.CENTER
        centerCell.valign == VerticalAlignment.MIDDLE
        rightCell.align == HorizontalAlignment.RIGHT
        rightCell.valign == VerticalAlignment.BOTTOM
    }

    // Text Rotation Tests
    def "cells can have rotated text"() {
        given:
        def table = new BaseTable(700, 700, 50, 500, 50, document, page, true, true)
        def row = table.createRow(50f)
        def cell = row.createCell(100, "Rotated")

        when:
        cell.setTextRotated(true)

        then:
        cell.textRotated
    }

    // Line Spacing Tests
    def "table line spacing propagates to cells"() {
        given:
        def table = new BaseTable(700, 700, 50, 500, 50, document, page, true, true)
        def row = table.createRow(20f)
        row.setLineSpacing(1.5f)

        when:
        def cell = row.createCell(100, "Spaced Text")

        then:
        cell.lineSpacing == 1.5f
    }

    // Borderless Table Tests
    def "table can be borderless"() {
        given:
        def table = new BaseTable(700, 700, 50, 500, 50, document, page, true, true)
        def row = table.createRow(20f)
        def cell = row.createCell(100, "No Borders")

        when:
        row.removeAllBorders()

        then:
        cell.leftBorder == null
        cell.rightBorder == null
        cell.topBorder == null
        cell.bottomBorder == null
    }

    // HTML Content Tests
    def "cells can contain HTML formatted text"() {
        given:
        def table = new BaseTable(700, 700, 50, 500, 50, document, page, true, true)
        def row = table.createRow(30f)

        when:
        def cell = row.createCell(100, "<b>Bold</b> and <i>Italic</i> text")

        then:
        cell.text == "<b>Bold</b> and <i>Italic</i> text"
        cell.paragraph != null
    }

    // URL Tests
    def "cells can have URLs"() {
        given:
        def table = new BaseTable(700, 700, 50, 500, 50, document, page, true, true)
        def row = table.createRow(20f)
        def cell = row.createCell(100, "Click me")
        def url = new URL("https://example.com")

        when:
        cell.setUrl(url)

        then:
        cell.url == url
    }

    // Content Drawn Listener Tests
    def "cells can have content drawn listeners"() {
        given:
        def table = new BaseTable(700, 700, 50, 500, 50, document, page, true, true)
        def row = table.createRow(20f)
        def cell = row.createCell(100, "Listener Cell")
        def listenerCalled = false

        when:
        cell.addContentDrawnListener({ c, doc, p, rect -> listenerCalled = true } as CellContentDrawnListener)
        cell.notifyContentDrawnListeners(document, page, new PDRectangle(0, 0, 100, 100))

        then:
        listenerCalled
    }

    // Draw Tests (Integration)
    def "table can be drawn without exceptions"() {
        given:
        def doc = new PDDocument()
        def pg = new PDPage(PDRectangle.A4)
        doc.addPage(pg)
        def table = new BaseTable(700, 700, 50, 500, 50, doc, pg, true, true)

        def headerRow = table.createRow(15f)
        headerRow.createCell(50, "Header 1")
        headerRow.createCell(50, "Header 2")

        def dataRow = table.createRow(12f)
        dataRow.createCell(50, "Data 1")
        dataRow.createCell(50, "Data 2")

        when:
        table.draw()

        then:
        noExceptionThrown()

        cleanup:
        doc?.close()
    }
}
