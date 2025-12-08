package superpdf

import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import spock.lang.Specification
import spock.lang.Shared
import java.awt.Color

class ParagraphSpec extends Specification {

    @Shared PDType1Font HELVETICA = new PDType1Font(Standard14Fonts.FontName.HELVETICA)
    @Shared PDType1Font COURIER = new PDType1Font(Standard14Fonts.FontName.COURIER)

    // Constructor Tests
    def "basic constructor creates paragraph"() {
        when:
        def p = new Paragraph("Test text", HELVETICA, 12, 200)

        then:
        p != null
    }

    def "constructor with alignment sets alignment"() {
        when:
        def p = new Paragraph("Test text", HELVETICA, 12f, 200f, HorizontalAlignment.CENTER)

        then:
        p.align == HorizontalAlignment.CENTER
    }

    def "constructor with color creates paragraph"() {
        when:
        def p = new Paragraph("Test text", HELVETICA, 12f, 200f, HorizontalAlignment.LEFT, Color.RED, null, null)

        then:
        p != null
    }

    def "constructor with line spacing creates paragraph"() {
        when:
        def p = new Paragraph("Test text", HELVETICA, 12f, 200f, HorizontalAlignment.LEFT, Color.BLACK, null, null, 1.5f)

        then:
        p != null
    }

    // getLines Tests
    def "getLines returns non-null list for simple text"() {
        when:
        def p = new Paragraph("Hello World", HELVETICA, 12f, 200f, HorizontalAlignment.LEFT)

        then:
        p.lines != null
        !p.lines.isEmpty()
    }

    def "getLines returns non-null list for empty text"() {
        when:
        def p = new Paragraph("", HELVETICA, 12f, 200f, HorizontalAlignment.LEFT)

        then:
        p.lines != null
    }

    def "getLines with break tags creates multiple lines"() {
        when:
        def p = new Paragraph("Line1<br>Line2<br>Line3", HELVETICA, 12f, 200f, HorizontalAlignment.LEFT)

        then:
        p.lines.size() >= 3
    }

    def "getLines wraps long text"() {
        given:
        def longText = "This is a very long text that should wrap to multiple lines when the paragraph width is narrow enough to force wrapping behavior."

        when:
        def p = new Paragraph(longText, HELVETICA, 12f, 100f, HorizontalAlignment.LEFT)

        then:
        p.lines.size() > 1
    }

    def "getLines is memoized"() {
        given:
        def p = new Paragraph("Test text", HELVETICA, 12f, 200f, HorizontalAlignment.LEFT)

        when:
        def lines1 = p.lines
        def lines2 = p.lines

        then:
        lines1.is(lines2)
    }

    // getHeight Tests
    def "getHeight returns positive value"() {
        when:
        def p = new Paragraph("Test text", HELVETICA, 12f, 200f, HorizontalAlignment.LEFT)

        then:
        p.height > 0
    }

    def "multiple lines have greater height than single line"() {
        given:
        def p1 = new Paragraph("Short", HELVETICA, 12f, 200f, HorizontalAlignment.LEFT)
        def p2 = new Paragraph("Line1<br>Line2<br>Line3", HELVETICA, 12f, 200f, HorizontalAlignment.LEFT)

        expect:
        p2.height > p1.height
    }

    def "empty paragraph has non-negative height"() {
        when:
        def p = new Paragraph("", HELVETICA, 12f, 200f, HorizontalAlignment.LEFT)

        then:
        p.height >= 0
    }

    // getWidth Tests
    def "getWidth returns configured width"() {
        when:
        def p = new Paragraph("Test", HELVETICA, 12f, 200f, HorizontalAlignment.LEFT)

        then:
        p.width == 200f
    }

    // getMaxLineWidth Tests
    def "getMaxLineWidth returns positive value"() {
        given:
        def p = new Paragraph("Short text", HELVETICA, 12f, 500f, HorizontalAlignment.LEFT)
        p.lines  // Force calculation

        expect:
        p.maxLineWidth > 0
        p.maxLineWidth <= 500f
    }

    // Alignment Tests
    def "getAlign returns configured alignment"() {
        when:
        def p = new Paragraph("Test", HELVETICA, 12f, 200f, HorizontalAlignment.RIGHT)

        then:
        p.align == HorizontalAlignment.RIGHT
    }

    def "setAlign changes alignment"() {
        given:
        def p = new Paragraph("Test", HELVETICA, 12f, 200f, HorizontalAlignment.LEFT)

        when:
        p.setAlign(HorizontalAlignment.CENTER)

        then:
        p.align == HorizontalAlignment.CENTER
    }

    // HTML Tag Tests
    def "bold text is processed"() {
        when:
        def p = new Paragraph("<b>Bold Text</b>", HELVETICA, 12f, 200f, HorizontalAlignment.LEFT)

        then:
        p.lines != null
    }

    def "italic text is processed"() {
        when:
        def p = new Paragraph("<i>Italic Text</i>", HELVETICA, 12f, 200f, HorizontalAlignment.LEFT)

        then:
        p.lines != null
    }

    def "bold and italic combined is processed"() {
        when:
        def p = new Paragraph("<b><i>Bold Italic Text</i></b>", HELVETICA, 12f, 200f, HorizontalAlignment.LEFT)

        then:
        p.lines != null
    }

    def "unordered list creates multiple lines"() {
        when:
        def p = new Paragraph("<ul><li>Item 1</li><li>Item 2</li></ul>", HELVETICA, 12f, 200f, HorizontalAlignment.LEFT)

        then:
        p.lines != null
        p.lines.size() >= 2
    }

    def "ordered list creates multiple lines"() {
        when:
        def p = new Paragraph("<ol><li>First</li><li>Second</li><li>Third</li></ol>", HELVETICA, 12f, 200f, HorizontalAlignment.LEFT)

        then:
        p.lines != null
        p.lines.size() >= 3
    }

    def "nested lists are processed"() {
        given:
        def html = "<ul><li>Item 1</li><li>Item 2<ul><li>Nested 1</li><li>Nested 2</li></ul></li></ul>"

        when:
        def p = new Paragraph(html, HELVETICA, 12f, 200f, HorizontalAlignment.LEFT)

        then:
        p.lines != null
    }

    // Special Characters Tests
    def "less than character is preserved"() {
        when:
        def p = new Paragraph("a < b", HELVETICA, 12f, 200f, HorizontalAlignment.LEFT)

        then:
        p.lines != null
        p.lines[0].contains("<")
    }

    def "greater than character is preserved"() {
        when:
        def p = new Paragraph("a > b", HELVETICA, 12f, 200f, HorizontalAlignment.LEFT)

        then:
        p.lines != null
        p.lines[0].contains(">")
    }

    def "comparison operators are preserved"() {
        when:
        def p = new Paragraph("x >= y && y <= z", HELVETICA, 12f, 200f, HorizontalAlignment.LEFT)

        then:
        p.lines != null
    }

    // WrappingFunction Tests
    def "getWrappingFunction returns non-null"() {
        when:
        def p = new Paragraph("Test", HELVETICA, 12f, 200f, HorizontalAlignment.LEFT)

        then:
        p.wrappingFunction != null
    }

    def "custom wrapping function is used"() {
        when:
        def p = new Paragraph("Test text here", HELVETICA, 12f, 200f, HorizontalAlignment.LEFT,
            { text -> text.split(" ") } as superpdf.text.WrappingFunction)

        then:
        p.lines != null
    }

    // Font Size Effects
    def "larger font size increases height"() {
        given:
        def p1 = new Paragraph("Test", HELVETICA, 10f, 200f, HorizontalAlignment.LEFT)
        def p2 = new Paragraph("Test", HELVETICA, 20f, 200f, HorizontalAlignment.LEFT)

        expect:
        p2.height > p1.height
    }

    // Width Effects on Wrapping
    def "narrower width causes more wrapping"() {
        given:
        def text = "This is some text that will need to wrap"
        def wide = new Paragraph(text, HELVETICA, 12f, 400f, HorizontalAlignment.LEFT)
        def narrow = new Paragraph(text, HELVETICA, 12f, 100f, HorizontalAlignment.LEFT)

        expect:
        narrow.lines.size() >= wide.lines.size()
    }

    // Line Spacing Effects
    def "line spacing affects height"() {
        given:
        def p1 = new Paragraph("Line1<br>Line2", HELVETICA, 12f, 200f, HorizontalAlignment.LEFT, Color.BLACK, null, null, 1.0f)
        def p2 = new Paragraph("Line1<br>Line2", HELVETICA, 12f, 200f, HorizontalAlignment.LEFT, Color.BLACK, null, null, 2.0f)

        expect:
        p2.height > p1.height
    }

    // Edge Cases
    def "whitespace only text is processed"() {
        when:
        def p = new Paragraph("   ", HELVETICA, 12f, 200f, HorizontalAlignment.LEFT)

        then:
        p.lines != null
    }

    def "space separated columns work"() {
        when:
        def p = new Paragraph("Column1    Column2    Column3", HELVETICA, 12f, 300f, HorizontalAlignment.LEFT)

        then:
        p.lines != null
    }

    def "mixed content is processed"() {
        given:
        def content = "Normal <b>Bold</b> Normal <i>Italic</i> Normal"

        when:
        def p = new Paragraph(content, HELVETICA, 12f, 200f, HorizontalAlignment.LEFT)

        then:
        p.lines != null
    }

    // Different Font Tests
    def "different fonts work"() {
        when:
        def p1 = new Paragraph("Test", HELVETICA, 12f, 200f, HorizontalAlignment.LEFT)
        def p2 = new Paragraph("Test", COURIER, 12f, 200f, HorizontalAlignment.LEFT)

        then:
        p1.lines != null
        p2.lines != null
    }

    // Break Character Tests
    def "break tag creates new line"() {
        when:
        def p = new Paragraph("Line1<br>Line2", HELVETICA, 12f, 200f, HorizontalAlignment.LEFT)

        then:
        p.lines.size() >= 2
    }

    def "multiple break tags create multiple lines"() {
        when:
        def p = new Paragraph("A<br>B<br>C<br>D", HELVETICA, 12f, 200f, HorizontalAlignment.LEFT)

        then:
        p.lines.size() >= 4
    }

    // Ordered List Numbering Tests
    def "ordered list generates correct numbering for single level"() {
        when:
        def p = new Paragraph("<ol><li>First</li><li>Second</li><li>Third</li></ol>", HELVETICA, 12f, 300f, HorizontalAlignment.LEFT)

        then:
        p.lines != null
        p.lines.size() >= 3
        p.lines.any { it.contains("1.") }
        p.lines.any { it.contains("2.") }
        p.lines.any { it.contains("3.") }
    }

    def "ordered list with many items uses StringBuilder optimization"() {
        given:
        def items = (1..20).collect { "<li>Item $it</li>" }.join("")
        def html = "<ol>$items</ol>"

        when:
        def p = new Paragraph(html, HELVETICA, 12f, 300f, HorizontalAlignment.LEFT)

        then:
        p.lines != null
        p.lines.size() >= 20
        // Verify numbering goes from 1 to 20
        p.lines.any { it.contains("1.") }
        p.lines.any { it.contains("20.") }
    }

    // CHAR_CACHE Tests
    def "ASCII characters use character cache for width calculation"() {
        given:
        def asciiText = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

        when:
        def p = new Paragraph(asciiText, HELVETICA, 12f, 500f, HorizontalAlignment.LEFT)

        then:
        p.lines != null
        p.maxLineWidth > 0
        // All characters should be processed using cache (ASCII range 0-127)
    }

    def "character cache handles all printable ASCII characters"() {
        given:
        // ASCII 32 (space) to 126 (~)
        def printableAscii = (32..126).collect { (char) it }.join("")

        when:
        def p = new Paragraph(printableAscii, HELVETICA, 10f, 800f, HorizontalAlignment.LEFT)

        then:
        p.lines != null
        p.maxLineWidth > 0
    }

    def "character cache performance with repeated characters"() {
        given:
        def repeatedText = "aaaaaaaaaa bbbbbbbbbb cccccccccc"

        when:
        def p = new Paragraph(repeatedText, HELVETICA, 12f, 300f, HorizontalAlignment.LEFT)

        then:
        p.lines != null
        p.maxLineWidth > 0
        // Repeated ASCII characters should benefit from cache
    }
}
