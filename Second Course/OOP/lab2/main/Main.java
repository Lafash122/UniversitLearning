package main;

import java.util.logging.*;
import java.io.IOException;

import tools.CalcHandler;

public class Main{
	private static final Logger logger = Logger.getLogger(Main.class.getName());

	public static void main(String[] args) {
		logger.info("the programm has been started");

		CalcHandler handler = new CalcHandler(args);
		System.out.println("Working mode: " + ((handler.getMode() == false) ? "File" : "Console"));
		if (handler.getMode() == false) {
			handler.fileHandle();
		}
		else {
			handler.cmdHandle();
		}
		
		logger.info("the program has been successfully completed");
	}
}