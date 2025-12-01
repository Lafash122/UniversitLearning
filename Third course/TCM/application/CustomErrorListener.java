package application;

import org.antlr.v4.runtime.*;

import java.util.*;

import application.antlr.*;

public class CustomErrorListener extends BaseErrorListener {
	private ArrayList<Message> messages = new ArrayList<>();

	@Override
	public void syntaxError(Recognizer<?, ?> recognizer,
							Object offendingSymbol,
							int line,
							int charPositionInLine,
							String msg,
							RecognitionException e)
	{
		messages.add(new Message(
			line,
			charPositionInLine,
			"Error " + msg,
			"ANTLR_ERROR",
			0
		));
	}

	public ArrayList<Message> getMessages() {
		return messages;
	}
}