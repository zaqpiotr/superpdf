package superpdf.text

import spock.lang.Specification

class TokenizerSpec extends Specification {

    def wrappingFunction = null

    def "tokenize creates wrap points at word boundaries"() {
        given:
        def text = "1 123 123456 12"

        when:
        def tokens = Tokenizer.tokenize(text, wrappingFunction)

        then:
        tokens == [
            Token.text(TokenType.TEXT, "1 "),
            new Token(TokenType.POSSIBLE_WRAP_POINT, ""),
            Token.text(TokenType.TEXT, "123 "),
            new Token(TokenType.POSSIBLE_WRAP_POINT, ""),
            Token.text(TokenType.TEXT, "123456 "),
            new Token(TokenType.POSSIBLE_WRAP_POINT, ""),
            Token.text(TokenType.TEXT, "12"),
            new Token(TokenType.POSSIBLE_WRAP_POINT, "")
        ]
    }

    def "tokenize handles text ending with less-than character"() {
        given:
        def text = "1 123 123456 12<"

        when:
        def tokens = Tokenizer.tokenize(text, wrappingFunction)
        def lastToken = tokens.last()

        then:
        if (lastToken.type == TokenType.CLOSE_TAG) {
            lastToken.data == "<"
        }
    }

    def "tokenize parses simple italic text"() {
        given:
        def text = "1 <i>123 123456</i> 12"

        when:
        def tokens = Tokenizer.tokenize(text, wrappingFunction)
        def italicText = extractStyledText(tokens, "i")

        then:
        italicText == "123 123456"
    }

    def "tokenize parses multiple italic sections"() {
        given:
        def text = "1 <i>123</i> <i> 123456</i> 12"

        when:
        def tokens = Tokenizer.tokenize(text, wrappingFunction)
        def italicText = extractStyledText(tokens, "i")

        then:
        italicText == "123 123456"
    }

    def "tokenize parses nested bold and italic"() {
        given:
        def text = "1 <i><b>123</b> 123456</i> 12"

        when:
        def tokens = Tokenizer.tokenize(text, wrappingFunction)
        def boldItalicText = extractBoldItalicText(tokens)

        then:
        boldItalicText == "123"
    }

    def "tokenize parses overlapping bold and italic"() {
        given:
        def text = "1 <i>123</i> <i> <b>123456</i></b> 12"

        when:
        def tokens = Tokenizer.tokenize(text, wrappingFunction)
        def boldItalicText = extractBoldItalicText(tokens)

        then:
        boldItalicText == "123456"
    }

    def "tokenize handles empty string"() {
        given:
        def text = ""

        when:
        def tokens = Tokenizer.tokenize(text, wrappingFunction)

        then:
        tokens.findAll { it.type == TokenType.TEXT && it.data == "" }.every { it.data == "" }
    }

    def "tokenize handles null string"() {
        when:
        def tokens = Tokenizer.tokenize(null, wrappingFunction)

        then:
        tokens == []
    }

    // Helper methods
    private String extractStyledText(List<Token> tokens, String tag) {
        def result = new StringBuilder()
        def inStyle = false

        tokens.each { token ->
            if (token.type == TokenType.OPEN_TAG && token.data == tag) {
                inStyle = true
            } else if (token.type == TokenType.CLOSE_TAG && token.data == tag) {
                inStyle = false
            }
            if (token.type == TokenType.TEXT && inStyle) {
                result.append(token.data)
            }
        }
        return result.toString()
    }

    private String extractBoldItalicText(List<Token> tokens) {
        def result = new StringBuilder()
        def bold = false
        def italic = false

        tokens.each { token ->
            if (token.type == TokenType.OPEN_TAG && token.data == "b") {
                bold = true
            } else if (token.type == TokenType.CLOSE_TAG && token.data == "b") {
                bold = false
            }
            if (token.type == TokenType.OPEN_TAG && token.data == "i") {
                italic = true
            } else if (token.type == TokenType.CLOSE_TAG && token.data == "i") {
                italic = false
            }
            if (token.type == TokenType.TEXT && bold && italic) {
                result.append(token.data)
            }
        }
        return result.toString()
    }
}
