package superpdf

import spock.lang.Specification
import spock.lang.Unroll

class AlignmentSpec extends Specification {

    // HorizontalAlignment Tests
    def "HorizontalAlignment has three values"() {
        expect:
        HorizontalAlignment.values().length == 3
        HorizontalAlignment.LEFT != null
        HorizontalAlignment.CENTER != null
        HorizontalAlignment.RIGHT != null
    }

    @Unroll
    def "HorizontalAlignment.get('#input') returns #expected"() {
        expect:
        HorizontalAlignment.get(input) == expected

        where:
        input      | expected
        "left"     | HorizontalAlignment.LEFT
        "LEFT"     | HorizontalAlignment.LEFT
        "Left"     | HorizontalAlignment.LEFT
        "center"   | HorizontalAlignment.CENTER
        "CENTER"   | HorizontalAlignment.CENTER
        "Center"   | HorizontalAlignment.CENTER
        "right"    | HorizontalAlignment.RIGHT
        "RIGHT"    | HorizontalAlignment.RIGHT
        "Right"    | HorizontalAlignment.RIGHT
    }

    @Unroll
    def "HorizontalAlignment.get with whitespace '#input' returns #expected"() {
        expect:
        HorizontalAlignment.get(input) == expected

        where:
        input       | expected
        "  left  "  | HorizontalAlignment.LEFT
        " center "  | HorizontalAlignment.CENTER
        "right "    | HorizontalAlignment.RIGHT
    }

    def "HorizontalAlignment.get with null returns LEFT"() {
        expect:
        HorizontalAlignment.get(null) == HorizontalAlignment.LEFT
    }

    @Unroll
    def "HorizontalAlignment.get with invalid input '#input' returns LEFT"() {
        expect:
        HorizontalAlignment.get(input) == HorizontalAlignment.LEFT

        where:
        input << ["invalid", "", "middle"]
    }

    def "HorizontalAlignment.valueOf works correctly"() {
        expect:
        HorizontalAlignment.valueOf("LEFT") == HorizontalAlignment.LEFT
        HorizontalAlignment.valueOf("CENTER") == HorizontalAlignment.CENTER
        HorizontalAlignment.valueOf("RIGHT") == HorizontalAlignment.RIGHT
    }

    // VerticalAlignment Tests
    def "VerticalAlignment has three values"() {
        expect:
        VerticalAlignment.values().length == 3
        VerticalAlignment.TOP != null
        VerticalAlignment.MIDDLE != null
        VerticalAlignment.BOTTOM != null
    }

    @Unroll
    def "VerticalAlignment.get('#input') returns #expected"() {
        expect:
        VerticalAlignment.get(input) == expected

        where:
        input      | expected
        "top"      | VerticalAlignment.TOP
        "TOP"      | VerticalAlignment.TOP
        "Top"      | VerticalAlignment.TOP
        "middle"   | VerticalAlignment.MIDDLE
        "MIDDLE"   | VerticalAlignment.MIDDLE
        "Middle"   | VerticalAlignment.MIDDLE
        "bottom"   | VerticalAlignment.BOTTOM
        "BOTTOM"   | VerticalAlignment.BOTTOM
        "Bottom"   | VerticalAlignment.BOTTOM
    }

    @Unroll
    def "VerticalAlignment.get with whitespace '#input' returns #expected"() {
        expect:
        VerticalAlignment.get(input) == expected

        where:
        input       | expected
        "  top  "   | VerticalAlignment.TOP
        " middle "  | VerticalAlignment.MIDDLE
        "bottom "   | VerticalAlignment.BOTTOM
    }

    def "VerticalAlignment.get with null returns TOP"() {
        expect:
        VerticalAlignment.get(null) == VerticalAlignment.TOP
    }

    @Unroll
    def "VerticalAlignment.get with invalid input '#input' returns TOP"() {
        expect:
        VerticalAlignment.get(input) == VerticalAlignment.TOP

        where:
        input << ["invalid", "", "center"]
    }

    def "VerticalAlignment.valueOf works correctly"() {
        expect:
        VerticalAlignment.valueOf("TOP") == VerticalAlignment.TOP
        VerticalAlignment.valueOf("MIDDLE") == VerticalAlignment.MIDDLE
        VerticalAlignment.valueOf("BOTTOM") == VerticalAlignment.BOTTOM
    }

    // Combined Alignment Tests
    def "all 9 alignment combinations exist"() {
        given:
        def horizontals = HorizontalAlignment.values()
        def verticals = VerticalAlignment.values()

        expect:
        horizontals.length == 3
        verticals.length == 3
        horizontals.every { h -> verticals.every { v -> h != null && v != null } }
    }

    def "alignment ordinals are correct"() {
        expect:
        HorizontalAlignment.LEFT.ordinal() == 0
        HorizontalAlignment.CENTER.ordinal() == 1
        HorizontalAlignment.RIGHT.ordinal() == 2

        VerticalAlignment.TOP.ordinal() == 0
        VerticalAlignment.MIDDLE.ordinal() == 1
        VerticalAlignment.BOTTOM.ordinal() == 2
    }

    def "alignment names are correct"() {
        expect:
        HorizontalAlignment.LEFT.name() == "LEFT"
        HorizontalAlignment.CENTER.name() == "CENTER"
        HorizontalAlignment.RIGHT.name() == "RIGHT"

        VerticalAlignment.TOP.name() == "TOP"
        VerticalAlignment.MIDDLE.name() == "MIDDLE"
        VerticalAlignment.BOTTOM.name() == "BOTTOM"
    }
}
