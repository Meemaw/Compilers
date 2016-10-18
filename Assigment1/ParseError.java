import analyzer.Token;

public class ParseError {

	private Token token;
	private String message;

	public ParseError(String message, Token token) {
		this.message = message;
		this.token = token;
	}

	public Token getToken() {
		return this.token;
	}

	public String getMessage() {
		return this.message;
	}
}