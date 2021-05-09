package jlox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Scanner {
	private final String source;
	private final List<Token> tokens = new ArrayList<>();

	private int start = 0;
	private int current = 0;
	private int line = 1;

	private static final Map<String, TokenType> keywords;

	static {
		keywords = new HashMap<>();
		keywords.put("and", TokenType.AND);
		keywords.put("class", TokenType.CLASS);
		keywords.put("else", TokenType.ELSE);
		keywords.put("false", TokenType.FALSE);
		keywords.put("for", TokenType.FOR);
		keywords.put("fun", TokenType.FUN);
		keywords.put("if", TokenType.IF);
		keywords.put("nil", TokenType.NIL);
		keywords.put("or", TokenType.OR);
		keywords.put("print", TokenType.PRINT);
		keywords.put("return", TokenType.RETURN);
		keywords.put("super", TokenType.SUPER);
		keywords.put("this", TokenType.THIS);
		keywords.put("true", TokenType.TRUE);
		keywords.put("var", TokenType.VAR);
		keywords.put("while", TokenType.WHILE);
	}

	Scanner(String source) {
		this.source = source;
	}

	List<Token> scanTokens() {
		while (!this.isAtEnd()) {
			start = current;
			this.scanToken();
		}

		tokens.add(new Token(TokenType.EOF, "", null, line));
		return tokens;
	}

	private boolean isAtEnd() {
		return current >= source.length();
	}

	private void scanToken() {
		char curr = this.advance();
		switch (curr) {
		case '(':
			this.addToken(TokenType.LEFT_PAREN);
			break;
		case ')':
			this.addToken(TokenType.RIGHT_PAREN);
			break;
		case '{':
			this.addToken(TokenType.LEFT_BRACE);
			break;
		case '}':
			this.addToken(TokenType.RIGHT_BRACE);
			break;
		case ',':
			this.addToken(TokenType.COMMA);
			break;
		case '.':
			this.addToken(TokenType.DOT);
			break;
		case '-':
			this.addToken(TokenType.MINUS);
			break;
		case '+':
			this.addToken(TokenType.PLUS);
			break;
		case ';':
			this.addToken(TokenType.SEMICOLON);
			break;
		case '*':
			this.addToken(TokenType.STAR);
			break;
		case '!':
			this.addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
			break;
		case '=':
			this.addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
			break;
		case '<':
			this.addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
			break;
		case '>':
			this.addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
			break;
		case '/':
			if (match('/')) {
				while (peek() != '\n' && !isAtEnd())
					advance();
			} else {
				this.addToken(TokenType.SLASH);
			}
			break;
		case ' ':
		case '\r':
		case '\t':
			// Ignore whitespace.
			break;
		case '\n':
			// Increment line number.
			line++;
			break;
		case '"':
			string();
			break;
		default:
			if (isDigit(curr)) {
				number();
			} else if (isAlpha(curr)) {
				identifier();
			} else {
				Lox.error(line, "Unexpected character '" + curr + "'");
			}
			break;
		}
	}

	private boolean isDigit(char ch) {
		return ch >= '0' && ch <= '9';
	}

	private boolean isAlpha(char ch) {
		return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_';
	}

	private boolean isAlphaNumeric(char c) {
		return isAlpha(c) || isDigit(c);
	}

	private TokenType keywordType(String word) {
		return keywords.get(word);
	}

	private void identifier() {
		while (isAlphaNumeric(peek()))
			advance();
		String word = source.substring(start, current).toString();
		TokenType type;
		if ((type = keywordType(word)) != null) {
			// Keyword
			addToken(type);
		} else {
			// Identifier
			addToken(TokenType.IDENTIFIER);
		}
	}

	private void number() {
		while (isDigit(peek()))
			advance();
		if (peek() == '.' && isDigit(peekNext())) {
			advance();

			while (isDigit(peek())) {
				advance();
			}
		}
		addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
	}

	private char peekNext() {
		if (current + 1 >= source.length())
			return '\0';
		return source.charAt(current + 1);
	}

	// Only returns the current character (lookahead)
	private char peek() {
		if (isAtEnd())
			return '\0';
		return source.charAt(current);
	}

	// Returns the current character and increments current position
	private char advance() {
		return source.charAt(current++);
	}

	private void addToken(TokenType type) {
		this.addToken(type, null);
	}

	// Adds token to the list of tokens
	private void addToken(TokenType type, Object literal) {
		String text = source.substring(start, current);
		tokens.add(new Token(type, text, literal, line));
	}

	// Checks if current char is equal to the argument, if yes then increments
	// current and returns true
	private boolean match(char ch) {
		if (this.isAtEnd())
			return false;
		if (ch != source.charAt(current))
			return false;
		current++;
		return true;
	}

	private void string() {
		while (peek() != '"' && !isAtEnd()) {
			if (peek() == '\n')
				line++;
			advance();
		}
		if (isAtEnd()) {
			Lox.error(line, "Unterminated string.");
			return;
		}
		advance();
		this.addToken(TokenType.STRING, source.substring(start + 1, current - 1));
	}
}
