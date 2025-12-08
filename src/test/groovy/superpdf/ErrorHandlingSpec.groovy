package superpdf

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.common.PDRectangle
import spock.lang.Specification
import spock.lang.Shared

class ErrorHandlingSpec extends Specification {

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

    // Cell Width Validation Tests
    def "cell width exceeding row width throws exception"() {
        given:
        def table = new BaseTable(700, 700, 50, 500, 50, document, page, true, true)
        def row = table.createRow(20f)

        when:
        row.createCell(150, "Too wide")  // 150% > 100%

        then:
        thrown(IllegalArgumentException)
    }

    def "cumulative cell width can exceed row width"() {
        given:
        def table = new BaseTable(700, 700, 50, 500, 50, document, page, true, true)
        def row = table.createRow(20f)
        row.createCell(60, "Cell 1")  // 60%

        when:
        row.createCell(60, "Cell 2")  // 60% - total 120%

        then:
        // Library allows cells to overflow - no exception thrown
        noExceptionThrown()
    }

    // Table Creation Tests
    def "table with zero width is allowed"() {
        when:
        def table = new BaseTable(700, 700, 50, 0, 50, document, page, true, true)

        then:
        table.width == 0f
    }

    // Cell Operations on Valid Table
    def "cell with zero width is allowed"() {
        given:
        def table = new BaseTable(700, 700, 50, 500, 50, document, page, true, true)
        def row = table.createRow(20f)

        when:
        def cell = row.createCell(0, "Zero width")

        then:
        cell.width == 0f
    }

    // Font Handling
    def "cell with null font throws exception on getFont"() {
        given:
        def table = new BaseTable(700, 700, 50, 500, 50, document, page, true, true)
        def row = table.createRow(20f)
        def cell = row.createCell(50, "Test")
        cell.setFont(null)

        when:
        cell.getFont()

        then:
        def e = thrown(IllegalArgumentException)
        e.message.contains("Font not set")
    }

    // Row Operations
    def "empty row has valid height"() {
        given:
        def table = new BaseTable(700, 700, 50, 500, 50, document, page, true, true)

        when:
        def row = table.createRow(20f)

        then:
        row.height >= 0
    }

    // Padding Edge Cases
    def "cell with zero padding is allowed"() {
        given:
        def table = new BaseTable(700, 700, 50, 500, 50, document, page, true, true)
        def row = table.createRow(20f)
        def cell = row.createCell(50, "Test")

        when:
        cell.setLeftPadding(0f)
        cell.setRightPadding(0f)
        cell.setTopPadding(0f)
        cell.setBottomPadding(0f)

        then:
        cell.leftPadding == 0f
        cell.rightPadding == 0f
        cell.topPadding == 0f
        cell.bottomPadding == 0f
    }

    def "cell with large padding is allowed"() {
        given:
        def table = new BaseTable(700, 700, 50, 500, 50, document, page, true, true)
        def row = table.createRow(20f)
        def cell = row.createCell(50, "Test")

        when:
        cell.setLeftPadding(100f)
        cell.setRightPadding(100f)

        then:
        // Large padding might make inner width negative but is allowed
        noExceptionThrown()
    }

    // Font Size Edge Cases
    def "cell with zero font size is allowed"() {
        given:
        def table = new BaseTable(700, 700, 50, 500, 50, document, page, true, true)
        def row = table.createRow(20f)
        def cell = row.createCell(50, "Test")

        when:
        cell.setFontSize(0f)

        then:
        cell.fontSize == 0f
    }

    def "cell with negative font size is allowed"() {
        given:
        def table = new BaseTable(700, 700, 50, 500, 50, document, page, true, true)
        def row = table.createRow(20f)
        def cell = row.createCell(50, "Test")

        when:
        cell.setFontSize(-5f)

        then:
        cell.fontSize == -5f
    }

    // Line Spacing Edge Cases
    def "cell with zero line spacing is allowed"() {
        given:
        def table = new BaseTable(700, 700, 50, 500, 50, document, page, true, true)
        def row = table.createRow(20f)
        def cell = row.createCell(50, "Test")

        when:
        cell.setLineSpacing(0f)

        then:
        cell.lineSpacing == 0f
    }

    def "cell with negative line spacing is allowed"() {
        given:
        def table = new BaseTable(700, 700, 50, 500, 50, document, page, true, true)
        def row = table.createRow(20f)
        def cell = row.createCell(50, "Test")

        when:
        cell.setLineSpacing(-1f)

        then:
        cell.lineSpacing == -1f
    }

    // Multiple rows and cells
    def "many rows and cells can be created"() {
        given:
        def table = new BaseTable(700, 700, 50, 500, 50, document, page, true, true)

        when:
        100.times { i ->
            def row = table.createRow(15f)
            5.times { j ->
                row.createCell(20, "Cell ${i},${j}")
            }
        }

        then:
        table != null
        noExceptionThrown()
    }

    // Special text content
    def "cell with very long text is handled"() {
        given:
        def table = new BaseTable(700, 700, 50, 500, 50, document, page, true, true)
        def row = table.createRow(20f)
        def longText = (1..1000).collect { "word" }.join(" ")

        when:
        def cell = row.createCell(100, longText)

        then:
        cell != null
        cell.cellHeight > 20f
    }

    def "cell with special characters is handled"() {
        given:
        def table = new BaseTable(700, 700, 50, 500, 50, document, page, true, true)
        def row = table.createRow(20f)

        when:
        def cell = row.createCell(50, 'Special: @#$%^&*()[]{}|\\')

        then:
        cell.text != null
    }

    // List Indentation Error Handling Tests
    def "paragraph with valid font does not throw exception during unordered list processing"() {
        given:
        def validFont = new org.apache.pdfbox.pdmodel.font.PDType1Font(
            org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA
        )

        when:
        def p = new Paragraph("<ul><li>Item 1</li><li>Item 2</li></ul>", validFont, 12f, 200f, HorizontalAlignment.LEFT)
        def lines = p.lines

        then:
        noExceptionThrown()
        lines != null
        lines.size() >= 2
    }

    def "paragraph with valid font does not throw exception during ordered list processing"() {
        given:
        def validFont = new org.apache.pdfbox.pdmodel.font.PDType1Font(
            org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA
        )

        when:
        def p = new Paragraph("<ol><li>First</li><li>Second</li></ol>", validFont, 12f, 200f, HorizontalAlignment.LEFT)
        def lines = p.lines

        then:
        noExceptionThrown()
        lines != null
        lines.size() >= 2
    }

    def "paragraph with mixed content and lists processes correctly"() {
        given:
        def validFont = new org.apache.pdfbox.pdmodel.font.PDType1Font(
            org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA
        )

        when:
        def p = new Paragraph("Text before <ul><li>Item 1</li><li>Item 2</li></ul>", validFont, 12f, 200f, HorizontalAlignment.LEFT)
        def lines = p.lines

        then:
        noExceptionThrown()
        lines != null
    }
}
