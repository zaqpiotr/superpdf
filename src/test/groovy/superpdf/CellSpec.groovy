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

class CellSpec extends Specification {

    @Shared PDDocument document
    @Shared PDPage page
    BaseTable table
    Row<PDPage> row

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
        row = table.createRow(20f)
    }

    // Text and Content Tests
    def "cell text content is set correctly"() {
        when:
        def cell = row.createCell(50, "Test Content")

        then:
        cell.text == "Test Content"
    }

    def "cell setText updates content"() {
        given:
        def cell = row.createCell(50, "Initial")

        when:
        cell.setText("Updated")

        then:
        cell.text == "Updated"
    }

    def "cell with null text becomes empty string"() {
        when:
        def cell = row.createCell(50, null)

        then:
        cell.text == ""
    }

    def "cell with empty text stays empty"() {
        when:
        def cell = row.createCell(50, "")

        then:
        cell.text == ""
    }

    // Font Tests
    def "cell has default font"() {
        when:
        def cell = row.createCell(50, "Test")

        then:
        cell.font != null
    }

    def "setFont changes the font"() {
        given:
        def cell = row.createCell(50, "Test")
        def courier = new PDType1Font(Standard14Fonts.FontName.COURIER)

        when:
        cell.setFont(courier)

        then:
        cell.font == courier
    }

    def "default font size is 8"() {
        when:
        def cell = row.createCell(50, "Test")

        then:
        cell.fontSize == 8f
    }

    def "setFontSize changes the font size"() {
        given:
        def cell = row.createCell(50, "Test")

        when:
        cell.setFontSize(12f)

        then:
        cell.fontSize == 12f
    }

    def "header cell uses bold font"() {
        given:
        def cell = row.createCell(50, "Test")
        def boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
        cell.setFontBold(boldFont)

        when:
        cell.setHeaderCell(true)

        then:
        cell.font == boldFont
    }

    // Color Tests
    def "default text color is black"() {
        when:
        def cell = row.createCell(50, "Test")

        then:
        cell.textColor == Color.BLACK
    }

    def "setTextColor changes text color"() {
        given:
        def cell = row.createCell(50, "Test")

        when:
        cell.setTextColor(Color.RED)

        then:
        cell.textColor == Color.RED
    }

    def "default fill color is null"() {
        when:
        def cell = row.createCell(50, "Test")

        then:
        cell.fillColor == null
    }

    def "setFillColor changes background color"() {
        given:
        def cell = row.createCell(50, "Test")

        when:
        cell.setFillColor(Color.YELLOW)

        then:
        cell.fillColor == Color.YELLOW
    }

    // Padding Tests
    def "default padding is 5 for all sides"() {
        when:
        def cell = row.createCell(50, "Test")

        then:
        cell.leftPadding == 5f
        cell.rightPadding == 5f
        cell.topPadding == 5f
        cell.bottomPadding == 5f
    }

    def "individual padding can be set"() {
        given:
        def cell = row.createCell(50, "Test")

        when:
        cell.setLeftPadding(10f)
        cell.setRightPadding(15f)
        cell.setTopPadding(20f)
        cell.setBottomPadding(25f)

        then:
        cell.leftPadding == 10f
        cell.rightPadding == 15f
        cell.topPadding == 20f
        cell.bottomPadding == 25f
    }

    // Border Tests
    def "first cell has all borders"() {
        when:
        def cell = row.createCell(50, "Test")

        then:
        cell.topBorder != null
        cell.bottomBorder != null
        cell.rightBorder != null
    }

    def "setBorderStyle sets all borders"() {
        given:
        def cell = row.createCell(50, "Test")
        def redBorder = new LineStyle(Color.RED, 2f)

        when:
        cell.setBorderStyle(redBorder)

        then:
        cell.leftBorder == redBorder
        cell.rightBorder == redBorder
        cell.topBorder == redBorder
        cell.bottomBorder == redBorder
    }

    def "individual borders can be set"() {
        given:
        def cell = row.createCell(50, "Test")
        def leftBorder = new LineStyle(Color.RED, 1f)
        def rightBorder = new LineStyle(Color.GREEN, 2f)
        def topBorder = new LineStyle(Color.BLUE, 3f)
        def bottomBorder = new LineStyle(Color.YELLOW, 4f)

        when:
        cell.setLeftBorderStyle(leftBorder)
        cell.setRightBorderStyle(rightBorder)
        cell.setTopBorderStyle(topBorder)
        cell.setBottomBorderStyle(bottomBorder)

        then:
        cell.leftBorder == leftBorder
        cell.rightBorder == rightBorder
        cell.topBorder == topBorder
        cell.bottomBorder == bottomBorder
    }

    def "setBorderStyle(null) removes all borders"() {
        given:
        def cell = row.createCell(50, "Test")

        when:
        cell.setBorderStyle(null)

        then:
        cell.leftBorder == null
        cell.rightBorder == null
        cell.topBorder == null
        cell.bottomBorder == null
    }

    // Alignment Tests
    def "default alignment is LEFT and TOP"() {
        when:
        def cell = row.createCell(50, "Test")

        then:
        cell.align == HorizontalAlignment.LEFT
        cell.valign == VerticalAlignment.TOP
    }

    def "alignment can be changed"() {
        given:
        def cell = row.createCell(50, "Test")

        when:
        cell.setAlign(HorizontalAlignment.CENTER)
        cell.setValign(VerticalAlignment.MIDDLE)

        then:
        cell.align == HorizontalAlignment.CENTER
        cell.valign == VerticalAlignment.MIDDLE
    }

    def "cell can be created with specific alignment"() {
        when:
        def cell = row.createCell(50, "Test", HorizontalAlignment.RIGHT, VerticalAlignment.BOTTOM)

        then:
        cell.align == HorizontalAlignment.RIGHT
        cell.valign == VerticalAlignment.BOTTOM
    }

    // Width Tests
    def "cell width is calculated as percentage of table width"() {
        when:
        def cell = row.createCell(50, "Test")

        then:
        cell.width == 250f  // 50% of 500
    }

    def "setWidth changes cell width"() {
        given:
        def cell = row.createCell(50, "Test")

        when:
        cell.setWidth(100f)

        then:
        cell.width == 100f
    }

    def "cell width exceeding row width throws exception"() {
        when:
        row.createCell(150, "Test")  // 150% > 100%

        then:
        thrown(IllegalArgumentException)
    }

    // Text Rotation Tests
    def "text is not rotated by default"() {
        when:
        def cell = row.createCell(50, "Test")

        then:
        !cell.textRotated
    }

    def "setTextRotated enables rotation"() {
        given:
        def cell = row.createCell(50, "Test")

        when:
        cell.setTextRotated(true)

        then:
        cell.textRotated
    }

    // Header Cell Tests
    def "cell is not header by default"() {
        when:
        def cell = row.createCell(50, "Test")

        then:
        !cell.headerCell
    }

    def "setHeaderCell marks cell as header"() {
        given:
        def cell = row.createCell(50, "Test")

        when:
        cell.setHeaderCell(true)

        then:
        cell.headerCell
    }

    // Colspan Tests
    def "cell is not colspan by default"() {
        when:
        def cell = row.createCell(50, "Test")

        then:
        !cell.colspanCell
    }

    def "setColspanCell marks cell for colspan"() {
        given:
        def cell = row.createCell(50, "Test")

        when:
        cell.setColspanCell(true)

        then:
        cell.colspanCell
    }

    // Line Spacing Tests
    def "default line spacing is 1"() {
        when:
        def cell = row.createCell(50, "Test")

        then:
        cell.lineSpacing == 1f
    }

    def "setLineSpacing changes line spacing"() {
        given:
        def cell = row.createCell(50, "Test")

        when:
        cell.setLineSpacing(1.5f)

        then:
        cell.lineSpacing == 1.5f
    }

    // URL Tests
    def "url is null by default"() {
        when:
        def cell = row.createCell(50, "Test")

        then:
        cell.url == null
    }

    def "setUrl sets the hyperlink"() {
        given:
        def cell = row.createCell(50, "Test")
        def url = new URL("https://example.com")

        when:
        cell.setUrl(url)

        then:
        cell.url == url
    }

    // Paragraph Tests
    def "getParagraph returns valid paragraph"() {
        when:
        def cell = row.createCell(50, "Test Content")

        then:
        cell.paragraph != null
    }

    def "paragraph is invalidated on text change"() {
        given:
        def cell = row.createCell(50, "Original")
        def p1 = cell.paragraph

        when:
        cell.setText("Updated")
        def p2 = cell.paragraph

        then:
        !p1.is(p2)
    }

    def "paragraph is invalidated on font change"() {
        given:
        def cell = row.createCell(50, "Test")
        def p1 = cell.paragraph

        when:
        cell.setFont(new PDType1Font(Standard14Fonts.FontName.COURIER))
        def p2 = cell.paragraph

        then:
        !p1.is(p2)
    }

    // Copy Style Tests
    def "copyCellStyle copies styling from source cell"() {
        given:
        def sourceCell = row.createCell(25, "Source")
        sourceCell.setFillColor(Color.CYAN)
        sourceCell.setTextColor(Color.MAGENTA)
        sourceCell.setFontSize(14f)
        sourceCell.setAlign(HorizontalAlignment.CENTER)
        sourceCell.setValign(VerticalAlignment.BOTTOM)

        def targetCell = row.createCell(25, "Target")

        when:
        targetCell.copyCellStyle(sourceCell)

        then:
        targetCell.fillColor == Color.CYAN
        targetCell.textColor == Color.MAGENTA
        targetCell.align == HorizontalAlignment.CENTER
        targetCell.valign == VerticalAlignment.BOTTOM
    }

    // Content Drawn Listener Tests
    def "addContentDrawnListener adds listener"() {
        given:
        def cell = row.createCell(50, "Test")
        def listener = { c, doc, p, rect -> } as CellContentDrawnListener<PDPage>

        when:
        cell.addContentDrawnListener(listener)

        then:
        cell.cellContentDrawnListeners.size() == 1
        cell.cellContentDrawnListeners.contains(listener)
    }

    def "multiple listeners can be added"() {
        given:
        def cell = row.createCell(50, "Test")
        def listener1 = { c, doc, p, rect -> } as CellContentDrawnListener<PDPage>
        def listener2 = { c, doc, p, rect -> } as CellContentDrawnListener<PDPage>

        when:
        cell.addContentDrawnListener(listener1)
        cell.addContentDrawnListener(listener2)

        then:
        cell.cellContentDrawnListeners.size() == 2
    }

    def "notifyContentDrawnListeners calls all listeners"() {
        given:
        def cell = row.createCell(50, "Test")
        def callCount = 0
        def listener = { c, doc, p, rect -> callCount++ } as CellContentDrawnListener<PDPage>
        cell.addContentDrawnListener(listener)

        when:
        cell.notifyContentDrawnListeners(document, page, new PDRectangle(0, 0, 100, 100))

        then:
        callCount == 1
    }

    // Height Tests
    def "setHeight sets cell height"() {
        given:
        def cell = row.createCell(50, "Test")

        when:
        cell.setHeight(50f)

        then:
        cell.cellHeight == 50f
    }

    def "getTextHeight returns positive value"() {
        when:
        def cell = row.createCell(50, "Test")

        then:
        cell.textHeight > 0
    }

    def "getTextWidth returns positive value"() {
        when:
        def cell = row.createCell(50, "Test")

        then:
        cell.textWidth > 0
    }
}
