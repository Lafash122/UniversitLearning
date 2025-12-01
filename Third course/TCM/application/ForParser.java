package application;

import java.util.*;
import java.util.regex.*;

public class ForParser {
	public static class Token {
		public final String token;
		public final String type;
		public final int line;
		public final int col;

		Token(String token, String type, int line, int col) {
			this.token = token;
			this.type = type;
			this.line = line;
			this.col = col;
		}
	}

	private final static Pattern pattern = Pattern.compile(
		"\\G(?:" +
			"(?<WS>\\s+)" +
			"|(?<FOR>for)" +
			"|(?<LBRACE>\\{)" +
			"|(?<RBRACE>\\})" +
			"|(?<LPAR>\\()" +
			"|(?<RPAR>\\))" +
			"|(?<SEMICOLON>;)" +
			"|(?<INCREMENT>\\+\\+)" +
			"|(?<DECREMENT>--)" +
			"|(?<NAME>[a-zA-Z_][a-zA-Z0-9_]*)" +
			"|(?<NUMBER>-?\\d+)" +
			"|(?<EQ>==)" +
			"|(?<NE>!=)" +
			"|(?<LE><=)" +
			"|(?<GE>>=)" +
			"|(?<LT><)" +
			"|(?<GT>>)" +
			"|(?<ASSIGN>=))"
	);

	private ArrayList<Message> messages = new ArrayList<>();
	private boolean noBadTokens = true;

	private List<Token> getTokens(String code) {
		if (code.length() == 0) {
			Message msg = new Message(1, 1, "Пустой файл", "NO_ERRORS", 1);
			messages.add(msg);

			return null;
		}

		List<Token> tokens = new ArrayList<>();
		Matcher matcher = pattern.matcher(code);

		int codePos = 0;
		int lineNumber = 1;
		int colNumber = 1;

		noBadTokens = true;
		while (codePos < code.length()) {
			matcher.region(codePos, code.length());

			if (!matcher.lookingAt()) {
				Message msg = new Message(
					lineNumber,
					colNumber,
					"Неизвестный токен: " + code.substring(codePos, codePos + 1),
					"UNKNOWN_TOKEN",
					0
				);
				messages.add(msg);

				noBadTokens = false;

				codePos++;
				colNumber++;
				continue;
			}

			String tokenText = matcher.group(0);
			int startLine = lineNumber;
			int startCol = colNumber;

			String type = null;
			if (matcher.group("WS") != null) type = "WS";
			else if (matcher.group("FOR") != null) type = "FOR";
			else if (matcher.group("LBRACE") != null) type = "LBRACE";
			else if (matcher.group("RBRACE") != null) type = "RBRACE";
			else if (matcher.group("LPAR") != null) type = "LPAR";
			else if (matcher.group("RPAR") != null) type = "RPAR";
			else if (matcher.group("SEMICOLON") != null) type = "SEMICOLON";
			else if (matcher.group("INCREMENT") != null) type = "INCREMENT";
			else if (matcher.group("DECREMENT") != null) type = "DECREMENT";
			else if (matcher.group("EQ") != null) type = "EQ";
			else if (matcher.group("NE") != null) type = "NE";
			else if (matcher.group("LE") != null) type = "LE";
			else if (matcher.group("GE") != null) type = "GE";
			else if (matcher.group("LT") != null) type = "LT";
			else if (matcher.group("GT") != null) type = "GT";
			else if (matcher.group("ASSIGN") != null) type = "ASSIGN";
			else if (matcher.group("NUMBER") != null) type = "NUMBER";
			else if (matcher.group("NAME") != null) type = "NAME";

			tokens.add(new Token(tokenText, type, startLine, startCol));

			for (char c : tokenText.toCharArray())
				if (c == '\n') {
					lineNumber++;
					colNumber = 1;
				}
				else
					colNumber++;

			codePos = matcher.end();
		}

		return tokens;
	}

	private int tokenNum;
	private List<Token> tokens;
	private String recoveredType = null;

	public ArrayList<Message> parseCode(String code) {
		messages.clear();
		tokens = getTokens(code);

		if (tokens == null)
			return messages;

		boolean noSymbols = true;
		tokenNum = 0;
		for ( ; tokenNum < tokens.size(); tokenNum++) {
			if (!tokens.get(tokenNum).type.equals("WS")) {
				noSymbols = false;
				break;
			}
		}

		if (noSymbols) {
			if (noBadTokens) {
				Message msg = new Message(
					1,
					1,
					"Файл содержит только пробельные символы",
					"NO_ERRORS",
					1
				);
				messages.add(msg);

				return messages;
			}

			return messages;
		}

		while (tokenNum < tokens.size())
			parseFor();

		if (messages.isEmpty()) {
			Message msg = new Message(1, 1, "Успешное завершение", "NO_ERRORS", 1);
			messages.add(msg);
		}

		return messages;
	}

	private boolean parseFor() {
		boolean matchRes;
		boolean innerParseRes;

		matchRes = matching("for", new String[] { "FOR" }, "FOR_ERR:FOR_EXPECTED", new String[] { "LPAR", "FOR" });
		if (matchRes)
			return true;
		if ("FOR".equals(recoveredType))
			return false;

		matchRes = matching("(", new String[] { "LPAR" }, "FOR_ERR:LPAR_EXPECTED", new String[] { "NAME", "FOR" });
		if (matchRes)
			return true;
		if ("FOR".equals(recoveredType))
			return false;

		innerParseRes = parseInit();
		if (innerParseRes)
			return true;
		if ("FOR".equals(recoveredType))
			return false;

		matchRes = matching(";", new String[] { "SEMICOLON" }, "FOR_ERR:SEMICOLON_EXPECTED", new String[] { "NAME", "FOR" });
		if (matchRes)
			return true;
		if ("FOR".equals(recoveredType))
			return false;

		innerParseRes = parseCond();
		if (innerParseRes)
			return true;
		if ("FOR".equals(recoveredType))
			return false;

		matchRes = matching(";", new String[] { "SEMICOLON" }, "FOR_ERR:SEMICOLON_EXPECTED", new String[] { "NAME", "FOR" });
		if (matchRes)
			return true;
		if ("FOR".equals(recoveredType))
			return false;

		innerParseRes = parseLoop();
		if (innerParseRes)
			return true;
		if ("FOR".equals(recoveredType))
			return false;

		matchRes = matching(")", new String[] { "RPAR" }, "FOR_ERR:RPAR_EXPECTED", new String[] { "LBRACE", "FOR" });
		if (matchRes)
			return true;
		if ("FOR".equals(recoveredType))
			return false;

		matchRes = matching("{", new String[] { "LBRACE" }, "FOR_ERR:LBRACE_EXPECTED", new String[] { "RBRACE", "FOR" });
		if (matchRes)
			return true;
		if ("FOR".equals(recoveredType))
			return false;

		innerParseRes = parseBody();
		if (innerParseRes)
			return true;
		if ("FOR".equals(recoveredType))
			return false;

		matchRes = matching("}", new String[] { "RBRACE" }, "FOR_ERR:RBRACE_EXPECTED", new String[] { "WS" , "FOR" });
		if (matchRes)
			return true;

		return false;
	}

	private boolean parseInit() {
		if (skipWSToken(";"))
			return true;
		if (tokens.get(tokenNum).type.equals("SEMICOLON"))
			return false;

		boolean matchRes;

		matchRes = matching("Имя переменной", new String[] { "NAME" }, "INIT_ERR:NAME_EXPECTED", new String[] { "ASSIGN", "SEMICOLON", "FOR" });
		if (matchRes)
			return true;
		if ("SEMICOLON".equals(recoveredType) || "FOR".equals(recoveredType))
			return false;

		matchRes = matching("=", new String[] { "ASSIGN" }, "INIT_ERR:ASSIGN_EXPECTED", new String[] { "NUMBER", "SEMICOLON", "FOR" });
		if (matchRes)
			return true;
		if ("SEMICOLON".equals(recoveredType) || "FOR".equals(recoveredType))
			return false;

		matchRes = matching("Число", new String[] { "NUMBER" }, "INIT_ERR:NUMBER_EXPECTED", new String[] { "SEMICOLON", "FOR" });
		if (matchRes)
			return true;

		return false;
	}

	private boolean parseCond() {
		if (skipWSToken(";"))
			return true;
		if (tokens.get(tokenNum).type.equals("SEMICOLON"))
			return false;

		boolean matchRes;

		matchRes = matching(
			"Имя переменной",
			new String[] { "NAME" },
			"COND_ERR:NAME_EXPECTED",
			new String[] { "EQ", "NE", "LE", "GE", "LT", "GT", "SEMICOLON", "FOR" }
		);
		if (matchRes)
			return true;
		if ("SEMICOLON".equals(recoveredType) || "FOR".equals(recoveredType))
			return false;

		matchRes = matching(
			"Оператор сравнения",
			new String[] { "EQ", "NE", "LE", "GE", "LT", "GT" },
			"COND_ERR:COM_OP_EXPECTED",
			new String[] { "NUMBER", "SEMICOLON", "FOR" }
		);
		if (matchRes)
			return true;
		if ("SEMICOLON".equals(recoveredType) || "FOR".equals(recoveredType))
			return false;

		matchRes = matching("Число", new String[] { "NUMBER" }, "COND_ERR:NUMBER_EXPECTED", new String[] { "SEMICOLON", "FOR" });
		if (matchRes)
			return true;

		return false;
	}

	private boolean parseLoop() {
		if (skipWSToken(")"))
			return true;
		if (tokens.get(tokenNum).type.equals("RPAR"))
			return false;

		boolean matchRes;

		matchRes = matching(
			"Имя переменной",
			new String[] { "NAME" },
			"LOOP_ERR:NAME_EXPECTED",
			new String[] { "INCREMENT", "DECREMENT", "RPAR", "FOR" }
		);
		if (matchRes)
			return true;
		if ("RPAR".equals(recoveredType) || "FOR".equals(recoveredType))
			return false;

		matchRes = matching(
			"Оператор счетчика",
			new String[] { "INCREMENT", "DECREMENT" },
			"COND_ERR:COM_OP_EXPECTED",
			new String[] { "RPAR", "FOR" }
		);
		if (matchRes)
			return true;

		return false;
	}

	private boolean parseBody() {
		while ((tokenNum < tokens.size())) {
			if (skipWSToken("}"))
				return true;
			if ("RBRACE".equals(tokens.get(tokenNum).type))
				return false;

			boolean matchRes;

			if ("FOR".equals(tokens.get(tokenNum).type)) {
				if (parseFor())
					return true;
				continue;
			}

			matchRes = matching(
				"Имя переменной",
				new String[] { "NAME" },
				"BODY_ERR:NAME_EXPECTED",
				new String[] { "SEMICOLON", "RBRACE", "ASSIGN", "FOR" }
			);
			if ("RBRACE".equals(recoveredType))
				return false;
			if ("SEMICOLON".equals(recoveredType)) {
				tokenNum++;
				continue;
			}
			if ("FOR".equals(tokens.get(tokenNum).type)) {
				if (parseFor())
					return true;
				continue;
			}

			matchRes = matching(
				"=",
				new String[] { "ASSIGN" },
				"BODY_ERR:ASSIGN_EXPECTED",
				new String[] { "SEMICOLON", "RBRACE", "NUMBER", "FOR" }
			);
			if ("RBRACE".equals(recoveredType))
				return false;
			if ("SEMICOLON".equals(recoveredType)) {
				tokenNum++;
				continue;
			}
			if ("FOR".equals(tokens.get(tokenNum).type)) {
				if (parseFor())
					return true;
				continue;
			}

			matchRes = matching(
				"Число",
				new String[] { "NUMBER" },
				"BODY_ERR:NUMBER_EXPECTED",
				new String[] { "SEMICOLON", "RBRACE", "FOR" }
			);
			if ("RBRACE".equals(recoveredType))
				return false;
			if ("SEMICOLON".equals(recoveredType)) {
				tokenNum++;
				continue;
			}
			if ("FOR".equals(tokens.get(tokenNum).type)) {
				if (parseFor())
					return true;
				continue;
			}

			matchRes = matching(
				";",
				new String[] { "SEMICOLON" },
				"BODY_ERR:SEMICOLON_EXPECTED",
				new String[] { "RBRACE", "NAME", "FOR" }
			);
			if ("RBRACE".equals(recoveredType))
				return false;				
		}

		return true;
	}

	private boolean skipWSToken(String expected) {
		for ( ; tokenNum < tokens.size(); tokenNum++)
			if (!tokens.get(tokenNum).type.equals("WS"))
				return false;

		if (!"for".equals(expected)) {
			Token t = tokens.get(tokens.size() - 1);
			Message msg = new Message(
				t.line,
				t.col,
				"Ожидалось " + expected + " вместо конца файла",
				"UNEXPECTED_END_OF_FILE",
				0
			);
			messages.add(msg);
		}

		return true;
	}

	private boolean matching(String expected, String[] types, String errorType, String[] recTokens) {
		if (skipWSToken(expected))
			return true;

		Token t = tokens.get(tokenNum);
		Set<String> expectedTypes = new HashSet<>(Arrays.asList(types));
		recoveredType = null;

		if (!expectedTypes.contains(t.type)) {
			Message msg = new Message(
				t.line,
				t.col,
				"Ожидалось " + expected + " вместо " + t.token,
				errorType,
				0
			);
			messages.add(msg);

			return recover(recTokens);
		}
		tokenNum++;

		return false;
	}

	private boolean recover(String[] recTokens) {
		Set<String> recovering = new HashSet<>(Arrays.asList(recTokens));

		while (tokenNum < tokens.size()) {
			Token t = tokens.get(tokenNum);
			if (recovering.contains(t.type)) {
				recoveredType = t.type;
				return false;
			}
			tokenNum++;
		}

		return true;
	}
}