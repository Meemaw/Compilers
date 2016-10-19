import analyzer.Token;

public class ParseError {

	private Token token;
	private String message;
	private boolean point_after_token;

	public ParseError(String message, Token token, boolean point_after_token) {
		this.message = message;
		this.token = token;
		this.point_after_token = point_after_token;
	}

	public ParseError(String message, Token token) {
		this(message, token, false);
	}


	public Token getToken() {
		return this.token;
	}

	public String getMessage() {
		return this.message;
	}

	public boolean getPointAfterToken() {
		return this.point_after_token;
	}

}