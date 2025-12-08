package superpdf.text

import spock.lang.Specification

class TokenSpec extends Specification {

    def "getPaddingValue returns cached value for PaddingToken"() {
        given:
        def paddingValue = 15.5f
        def token = Token.padding(paddingValue)

        when:
        def result = token.getPaddingValue()

        then:
        result == paddingValue
        token instanceof PaddingToken
    }

    def "getPaddingValue parses string data for regular Token with PADDING type"() {
        given:
        def paddingValue = 20.75f
        def token = new Token(TokenType.PADDING, String.valueOf(paddingValue))

        when:
        def result = token.getPaddingValue()

        then:
        result == paddingValue
        !(token instanceof PaddingToken)
    }

    def "getPaddingValue handles zero padding for PaddingToken"() {
        given:
        def token = Token.padding(0f)

        when:
        def result = token.getPaddingValue()

        then:
        result == 0f
    }

    def "getPaddingValue handles negative padding for PaddingToken"() {
        given:
        def token = Token.padding(-5.5f)

        when:
        def result = token.getPaddingValue()

        then:
        result == -5.5f
    }

    def "getPaddingValue handles large padding values for PaddingToken"() {
        given:
        def largeValue = 999999.99f
        def token = Token.padding(largeValue)

        when:
        def result = token.getPaddingValue()

        then:
        result == largeValue
    }

    def "getPaddingValue parses integer padding from regular Token"() {
        given:
        def token = new Token(TokenType.PADDING, "42")

        when:
        def result = token.getPaddingValue()

        then:
        result == 42f
    }

    def "getPaddingValue parses decimal padding from regular Token"() {
        given:
        def token = new Token(TokenType.PADDING, "3.14159")

        when:
        def result = token.getPaddingValue()

        then:
        Math.abs(result - 3.14159f) < 0.0001f
    }

    def "PaddingToken factory method creates correct type"() {
        when:
        def token = Token.padding(10f)

        then:
        token.getType() == TokenType.PADDING
        token.getData() == "10.0"
        token instanceof PaddingToken
    }

    def "PaddingToken avoids Float.parseFloat on repeated calls"() {
        given:
        def token = Token.padding(25.5f)

        when:
        def result1 = token.getPaddingValue()
        def result2 = token.getPaddingValue()
        def result3 = token.getPaddingValue()

        then:
        result1 == result2
        result2 == result3
        result1 == 25.5f
    }

    def "regular Token with non-PADDING type throws NumberFormatException on getPaddingValue"() {
        given:
        def token = new Token(TokenType.TEXT, "not a number")

        when:
        token.getPaddingValue()

        then:
        thrown(NumberFormatException)
    }

    def "PaddingToken preserves precision for small values"() {
        given:
        def smallValue = 0.001f
        def token = Token.padding(smallValue)

        when:
        def result = token.getPaddingValue()

        then:
        Math.abs(result - smallValue) < 0.00001f
    }

    def "PaddingToken equality based on type and data"() {
        given:
        def token1 = Token.padding(10.5f)
        def token2 = new Token(TokenType.PADDING, "10.5")

        expect:
        token1.getType() == token2.getType()
        token1.getData() == token2.getData()
    }
}
