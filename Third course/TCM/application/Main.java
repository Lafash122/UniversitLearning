package application;

import javax.swing.SwingUtilities;

public class Main {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {GraphicView iface = new GraphicView();});
	}
}