package superpdf.line

import spock.lang.Specification
import spock.lang.Subject
import java.awt.Color

class LineStyleSpec extends Specification {

    // Constructor Tests
    def "basic constructor sets color and width"() {
        when:
        def line = new LineStyle(Color.BLACK, 1f)

        then:
        line.color == Color.BLACK
        line.width == 1f
    }

    def "constructor with different color and width"() {
        when:
        def line = new LineStyle(Color.RED, 2f)

        then:
        line.color == Color.RED
        line.width == 2f
    }

    def "constructor with zero width"() {
        when:
        def line = new LineStyle(Color.BLACK, 0f)

        then:
        line.width == 0f
    }

    def "constructor with large width"() {
        when:
        def line = new LineStyle(Color.BLACK, 100f)

        then:
        line.width == 100f
    }

    // Dotted Line Tests
    def "produceDotted creates dotted line style"() {
        when:
        def dotted = LineStyle.produceDotted(Color.BLUE, 2)

        then:
        dotted.color == Color.BLUE
        dotted.width == 2f
        dotted.dashArray != null
        dotted.dashArray.length == 1
        dotted.dashArray[0] == 1.0f
        dotted.dashPhase == 0.0f
    }

    def "produceDotted with different colors"() {
        when:
        def dotted1 = LineStyle.produceDotted(Color.RED, 1)
        def dotted2 = LineStyle.produceDotted(Color.GREEN, 3)

        then:
        dotted1.color == Color.RED
        dotted2.color == Color.GREEN
    }

    // Dashed Line Tests
    def "produceDashed creates dashed line with default pattern"() {
        when:
        def dashed = LineStyle.produceDashed(Color.GREEN, 3)

        then:
        dashed.color == Color.GREEN
        dashed.width == 3f
        dashed.dashArray != null
        dashed.dashArray.length == 1
        dashed.dashArray[0] == 5.0f
        dashed.dashPhase == 0.0f
    }

    def "produceDashed with custom pattern"() {
        given:
        float[] customDashArray = [10.0f, 5.0f]

        when:
        def dashed = LineStyle.produceDashed(Color.ORANGE, 2, customDashArray, 2.5f)

        then:
        dashed.color == Color.ORANGE
        dashed.width == 2f
        dashed.dashArray.length == 2
        dashed.dashArray[0] == 10.0f
        dashed.dashArray[1] == 5.0f
        dashed.dashPhase == 2.5f
    }

    def "produceDashed with long pattern"() {
        given:
        float[] customDashArray = [15.0f, 5.0f, 5.0f, 5.0f]

        when:
        def dashed = LineStyle.produceDashed(Color.CYAN, 1, customDashArray, 0.0f)

        then:
        dashed.dashArray.length == 4
    }

    // Solid Line Tests
    def "solid line has no dash array"() {
        when:
        def solid = new LineStyle(Color.BLACK, 1f)

        then:
        solid.dashArray == null
        solid.dashPhase == 0.0f
    }

    // Equals Tests
    def "equals with same values returns true"() {
        given:
        def line1 = new LineStyle(Color.BLACK, 1f)
        def line2 = new LineStyle(Color.BLACK, 1f)

        expect:
        line1 == line2
        line2 == line1
    }

    def "equals with different colors returns false"() {
        given:
        def line1 = new LineStyle(Color.BLACK, 1f)
        def line2 = new LineStyle(Color.RED, 1f)

        expect:
        line1 != line2
    }

    def "equals with different widths returns false"() {
        given:
        def line1 = new LineStyle(Color.BLACK, 1f)
        def line2 = new LineStyle(Color.BLACK, 2f)

        expect:
        line1 != line2
    }

    def "equals with null returns false"() {
        given:
        def line = new LineStyle(Color.BLACK, 1f)

        expect:
        !line.equals(null)
    }

    def "equals with different class returns false"() {
        given:
        def line = new LineStyle(Color.BLACK, 1f)

        expect:
        line != "not a line style"
    }

    def "equals same object returns true"() {
        given:
        def line = new LineStyle(Color.BLACK, 1f)

        expect:
        line == line
    }

    // HashCode Tests
    def "hashCode is consistent"() {
        given:
        def line = new LineStyle(Color.BLACK, 1f)

        expect:
        line.hashCode() == line.hashCode()
    }

    def "hashCode for equal objects is same"() {
        given:
        def line1 = new LineStyle(Color.BLACK, 1f)
        def line2 = new LineStyle(Color.BLACK, 1f)

        expect:
        line1.hashCode() == line2.hashCode()
    }

    def "hashCode for different objects is different"() {
        given:
        def line1 = new LineStyle(Color.BLACK, 1f)
        def line2 = new LineStyle(Color.RED, 2f)

        expect:
        line1.hashCode() != line2.hashCode()
    }

    // Color Getter Tests
    def "getColor returns correct color for #color"() {
        expect:
        new LineStyle(color, 1f).color == color

        where:
        color << [Color.BLACK, Color.WHITE, Color.RED, Color.GREEN, Color.BLUE]
    }

    def "getColor with custom color"() {
        given:
        def customColor = new Color(128, 64, 32)

        when:
        def line = new LineStyle(customColor, 1f)

        then:
        line.color == customColor
    }

    // Width Getter Tests
    def "getWidth returns correct value for #width"() {
        expect:
        new LineStyle(Color.BLACK, width).width == width

        where:
        width << [0.5f, 1.0f, 2.5f, 10.0f]
    }

    // Common Border Styles Tests
    def "typical border styles work correctly"() {
        expect:
        new LineStyle(Color.BLACK, 0.5f).width == 0.5f  // thin
        new LineStyle(Color.BLACK, 1f).width == 1f      // standard
        new LineStyle(Color.BLACK, 3f).width == 3f      // thick
    }
}
