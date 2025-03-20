package tools;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.logging.Level;

import commands.*;
import exceptions.*;

public class CalcHandler {
	private static final Logger logger = Logger.getLogger(CalcHandler.class.getName());
	private boolean mode;
	private File input;
	
	public CalcHandler(String[] args) {
		if (args.length == 0)
			mode = true;
		else {
			if (args.length > 1) 
				logger.warning("too much arguments");
			input = new File(args[0]);
			if (!input.exists()) {
				logger.warning("file: '" + args[0] + "' does not exist");
				mode = true;
			}
			else
				mode = false;
		}
	}

	private void lineHandle(String line, Context context) {
		String[] parcedLine = line.split("\\s+");
		if (parcedLine[0].equals(""))
			return;

		try {
			Command com = CommandFactory.getCommand(parcedLine[0]);
			List<String> arguments = Arrays.asList(parcedLine).subList(1, parcedLine.length);
			com.process(context, arguments);
		}
		catch (CalculatorException e) {
			logger.severe(e.getClass().getName() + " : " + e.getMessage());
		}
	}

	public void fileHandle() {
		logger.info("working with: " + input.getName());
		try (BufferedReader reader = new BufferedReader(new FileReader(input))) {
			Context context = new Context();

			String line;
			while ((line = reader.readLine()) != null ) {
				lineHandle(line, context);
			}
		}
		catch (IOException e) {
			logger.severe(e.getClass().getName() + " : " + e.getMessage());
		}
		
	}

	public void cmdHandle() {
		logger.info("working with Console");
		Scanner scaner = new Scanner(System.in);
		Context context = new Context();

		String line = "";
		while (!line.equals("EXIT")) {
			line = scaner.nextLine();
			lineHandle(line, context);
		}
		scaner.close();
	}

	public boolean getMode() {
		return mode;
	}
}