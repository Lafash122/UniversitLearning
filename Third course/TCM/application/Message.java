package application;

public record Message(
	int lineNumber,
	int colNumber,
	String errorLine,
	String errorType,
	int messageType
) {}