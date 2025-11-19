package application;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.undo.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;

import java.io.*;
import java.util.*;

public class GraphicView extends JFrame {
	private static final int DEFAULT_TEXT_SIZE = 18;

	private static final Color mainColor = new Color(209, 213, 222);	// RGB:209,213,222
	private static final Color auxiliaryColor = new Color(96, 110, 140);	// Pigeon Blue
	private static final Color backColor = new Color(176, 183, 198);	// Cadet Blue Crayola
	private static final Color fontColor = new Color(0, 33, 55);		// Blackish Blue
	private static final Color selectionColor = new Color(255, 155, 170);	// Salmon Crayola
	private static final Color noErrorColor = new Color(93, 161, 48);	// Grass Green
	private static final Color hasErrorColor = new Color(255, 36, 0);	// Scarlet
	private static final Font menuFont = new Font("Ubuntu", Font.BOLD, DEFAULT_TEXT_SIZE);
	private static final Font textFont = new Font("Ubuntu", Font.PLAIN, DEFAULT_TEXT_SIZE);

	private JMenu fileMenu;
	private JMenuItem createFile;
	private JMenuItem openFile;
	private JMenuItem saveFile;
	private JMenuItem saveAsFile;
	private JMenuItem exitFile;

	private JMenu editMenu;
	private JMenuItem undoEdit;
	private JMenuItem redoEdit;
	private JMenuItem cutEdit;
	private JMenuItem copyEdit;
	private JMenuItem pasteEdit;
	private JMenuItem deleteEdit;
	private JMenuItem selectAllEdit;

	private JMenu textMenu;
	private JMenuItem taskSetting;
	private JMenuItem grammar;
	private JMenuItem сlassification;
	private JMenuItem method;
	private JMenuItem diagnostics;
	private JMenuItem testing;

	private JButton startButton;

	private JMenu infoMenu;
	private JMenuItem showInfo;
	private JMenuItem aboutInfo;

	private JSlider textSizeSlider;

	private UndoManager undoManager;
	private JTextArea originalText;
	private JPanel diagnosticsArea;
	private JTable diagnosticsTable;
	private DefaultTableModel table;

	private JButton createFileButton;
	private JButton openFileButton;
	private JButton saveCurrentButton;
	private JButton undoEditButton;
	private JButton redoEditButton;
	private JButton startParceButton;
	private JButton antlerButton;
	private JButton showInfoButton;
	private JButton aboutButton;

	private JLabel caretPosInfo;

	private File openedFile = null;

	private void setGlobalStyle() {
		UIManager.put("Button.background", backColor);
		UIManager.put("Button.foreground", fontColor);
		UIManager.put("Button.font", menuFont);
		UIManager.put("Menu.foreground", fontColor);
		UIManager.put("Menu.font", menuFont);
		UIManager.put("PopupMenu.background", auxiliaryColor);
		UIManager.put("MenuItem.background", auxiliaryColor);
		UIManager.put("MenuItem.foreground", fontColor);
		UIManager.put("MenuItem.font", menuFont);
		UIManager.put("TextArea.background", mainColor);
		UIManager.put("TextArea.foreground", fontColor);
		UIManager.put("TextArea.font", textFont);
		UIManager.put("ScrollPane.background", backColor);
		UIManager.put("MenuItem.acceleratorFont", menuFont);
		UIManager.put("MenuItem.acceleratorForeground", fontColor);
		UIManager.put("MenuItem.acceleratorSelectionForeground", fontColor);
	}

	public GraphicView() {
		setGlobalStyle();
		setTitle("Графический интерфейс парсера");
		setSize(960, 540);
		this.setMinimumSize(new Dimension(800, 490));
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent e) {
				if (openedFile == null)
					showSaveAndExitDialog();
				else {
					writeToFile(openedFile);
					dispose();
				}
			}
		});

		setJMenuBar(createMenu());

		JScrollPane toolPanel = createToolPanel();
		JPanel mainPanel = createMainPanel();
		JPanel infoPanel = createInfoPanel();	

		add(toolPanel, BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);
		add(infoPanel, BorderLayout.SOUTH);

		setVisible(true);
	}

	private void showSaveAndExitDialog() {
		Object[] options = {"Да", "Нет", "Отмена"};

		JOptionPane pane = new JOptionPane(
			"Сохранить перед закрытием?",
			JOptionPane.WARNING_MESSAGE,
			JOptionPane.YES_NO_CANCEL_OPTION,
			null,
			options,
			options[0]
		);

		JDialog dialog = pane.createDialog(this, "Графический интерфейс парсера");

		for (Component compo : pane.getComponents())
			if (compo instanceof JPanel)
				for (Component btn : ((JPanel) compo).getComponents())
					if (btn instanceof JButton) {
						((JButton) btn).setBorderPainted(false);
						((JButton) btn).setFocusPainted(false);
					}

		dialog.setVisible(true);

		Object selected = pane.getValue();
		int option = -1;
		if (selected instanceof Integer)
			option = (Integer) selected;
		else if (selected instanceof String)
			for (int i = 0; i < options.length; ++i)
				if (options[i].equals(selected)) {
					option = i;
					break;
				}

		if (option == 0) {
			int chose = saveFile();
			if (chose == 0)
				dispose();
		}
		else if (option == 1)
			dispose();
	}

	private void showAboutDialog() {
		Object message = "Графический интерфейс парсера\nФИТ, ПИиКН, Третий курс\nВерсия: v0.1 (22102025)\n\nПрограмма представляет из себя текстовый редактор парсера,\nвыполненного в целях освоения курса \"Методы трансляции и верификации\".";
		Object[] options = { "Ок" };

		JOptionPane aboutInfoPane = new JOptionPane(
			message,
			JOptionPane.INFORMATION_MESSAGE,
			JOptionPane.OK_OPTION,
			null,
			options				
		);

		JDialog aboutInfoDialog = aboutInfoPane.createDialog(this, "Графический интерфейс парсера: Справка");

		for (Component compo : aboutInfoPane.getComponents())
		if (compo instanceof JPanel)
			for (Component btn : ((JPanel) compo).getComponents())
				if (btn instanceof JButton) {
					((JButton) btn).setBorderPainted(false);
					((JButton) btn).setFocusPainted(false);
				}

		aboutInfoDialog.setVisible(true);
	}

	private void writeToFile(File file) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			writer.write(originalText.getText());
			writer.flush();
			JOptionPane.showMessageDialog(this,
				"Файл сохранен",
				"Сохранение файла",
				JOptionPane.INFORMATION_MESSAGE);
		}
		catch (IOException i) {
			JOptionPane.showMessageDialog(this,
				"Ошибка сохранения:\n" + i.getMessage(),
				"Сохранение файла",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	private int saveFile() {
		if (openedFile == null)
			return saveAsFile();
		else {
			writeToFile(openedFile);
			return 0;
		}
			
	}

	private int saveAsFile() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Сохранить файл");

		int selected = fileChooser.showSaveDialog(this);

		if (selected == JFileChooser.APPROVE_OPTION) {
			openedFile = fileChooser.getSelectedFile();

			writeToFile(openedFile);

			return 0;
		}

		return 1;
	}

	private void openFile() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Открыть файл");

		FileNameExtensionFilter filter = new FileNameExtensionFilter("Текстовые файлы (*.txt)", "txt");
		fileChooser.setFileFilter(filter);
		fileChooser.setAcceptAllFileFilterUsed(false);

		int selected = fileChooser.showOpenDialog(this);

		if (selected == JFileChooser.APPROVE_OPTION) {
			openedFile = fileChooser.getSelectedFile();

			try (BufferedReader reader = new BufferedReader(new FileReader(openedFile))) {
				StringBuilder str = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null)
					str.append(line).append("\n");
				originalText.setText(str.toString());
			}
			catch (IOException i) {
				JOptionPane.showMessageDialog(this,
					"Ошибка открытия:\n" + i.getMessage(),
					"Открытие файла",
					JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void openWordDoc(String docName) {
		try {
			File doc = new File("documentations/" + docName + ".docx");
			Desktop.getDesktop().open(doc);
		}
		catch (IOException i) {
			JOptionPane.showMessageDialog(this, "Не удалось открыть документ: " + docName);
		}
	}

	private void createFile() {
		saveFile();
		originalText.setText("");
		openedFile = null;
	}

	private void parsing() {
		String code = originalText.getText();

		DefaultTableModel model = (DefaultTableModel) diagnosticsTable.getModel();
		model.setRowCount(0);

		Parser p = new Parser();

		ArrayList<Message> messages = p.parseCode(code);
		for (Message msg : messages) {
			table.addRow(new Object[] {
				msg.lineNumber(),
				msg.colNumber(),
				msg.errorLine(),
				msg.errorType()
			});
		}

		if (messages.isEmpty())
			return;

		if (messages.get(0).messageType() == 0)
			diagnosticsTable.setForeground(hasErrorColor);
		else
			diagnosticsTable.setForeground(noErrorColor);
	}

	private JMenuBar createMenu() {
		JMenuBar res = new JMenuBar();

		fileMenu = createFileMenu();
		editMenu = createEditMenu();
		textMenu = createTextMenu();
		startButton = createStartButton();
		infoMenu = createInfoMenu();

		res.setBackground(auxiliaryColor);
		res.setBorder(null);
		res.setBorderPainted(false);

		res.add(fileMenu);
		res.add(editMenu);
		res.add(textMenu);
		res.add(startButton);
		res.add(infoMenu);

		return res;
	}

	private JMenu createFileMenu() {
		JMenu res = new JMenu("Файл");

		res.setBorderPainted(false);
		res.getPopupMenu().setOpaque(true);
		res.getPopupMenu().setBorderPainted(false);

		createFile = new JMenuItem("Создать");
		createFile.setBorderPainted(false);
		createFile.addActionListener(e -> createFile());
		createFile.setAccelerator(KeyStroke.getKeyStroke("control N"));

		openFile = new JMenuItem("Открыть");
		openFile.setBorderPainted(false);
		openFile.addActionListener(e -> openFile());
		openFile.setAccelerator(KeyStroke.getKeyStroke("control O"));

		saveFile = new JMenuItem("Сохранить");
		saveFile.setBorderPainted(false);
		saveFile.addActionListener(e -> saveFile());
		saveFile.setAccelerator(KeyStroke.getKeyStroke("control S"));

		saveAsFile = new JMenuItem("Сохранить как");
		saveAsFile.setBorderPainted(false);
		saveAsFile.addActionListener(e -> saveAsFile());
		saveAsFile.setAccelerator(KeyStroke.getKeyStroke("control shift S"));

		exitFile = new JMenuItem("Выход");
		exitFile.setBorderPainted(false);
		exitFile.addActionListener(e -> showSaveAndExitDialog());

		res.add(createFile);
		res.add(openFile);
		res.add(saveFile);
		res.add(saveAsFile);
		res.add(exitFile);

		return res;
	}

	private JMenu createEditMenu() {
		JMenu res = new JMenu("Правка");

		res.setBorderPainted(false);
		res.getPopupMenu().setOpaque(true);
		res.getPopupMenu().setBorderPainted(false);

		undoEdit = new JMenuItem("Отменить");
		undoEdit.setBorderPainted(false);
		undoEdit.addActionListener(e -> {
			if (undoManager.canUndo())
				undoManager.undo();
		});
		undoEdit.setAccelerator(KeyStroke.getKeyStroke("control Z"));

		redoEdit = new JMenuItem("Повторить");
		redoEdit.setBorderPainted(false);
		redoEdit.addActionListener(e -> {
			if (undoManager.canRedo())
				undoManager.redo();
		});
		redoEdit.setAccelerator(KeyStroke.getKeyStroke("control Y"));

		cutEdit = new JMenuItem("Вырезать");
		cutEdit.setBorderPainted(false);
		cutEdit.addActionListener(e -> originalText.cut());
		cutEdit.setAccelerator(KeyStroke.getKeyStroke("control X"));

		copyEdit = new JMenuItem("Копировать");
		copyEdit.setBorderPainted(false);
		copyEdit.addActionListener(e -> originalText.copy());
		copyEdit.setAccelerator(KeyStroke.getKeyStroke("control C"));

		pasteEdit = new JMenuItem("Вставить");
		pasteEdit.setBorderPainted(false);
		pasteEdit.addActionListener(e -> originalText.paste());
		pasteEdit.setAccelerator(KeyStroke.getKeyStroke("control V"));

		deleteEdit = new JMenuItem("Удалить");
		deleteEdit.setBorderPainted(false);
		deleteEdit.addActionListener(e -> originalText.replaceSelection(""));
		deleteEdit.setAccelerator(KeyStroke.getKeyStroke("DELETE"));

		selectAllEdit = new JMenuItem("Выделить все");
		selectAllEdit.setBorderPainted(false);
		selectAllEdit.addActionListener(e -> originalText.selectAll());
		selectAllEdit.setAccelerator(KeyStroke.getKeyStroke("control A"));

		res.add(undoEdit);
		res.add(redoEdit);
		res.add(cutEdit);
		res.add(copyEdit);
		res.add(pasteEdit);
		res.add(deleteEdit);
		res.add(selectAllEdit);

		return res;
	}

	private JMenu createTextMenu() {
		JMenu res = new JMenu("Текст");

		res.setBorderPainted(false);
		res.getPopupMenu().setOpaque(true);
		res.getPopupMenu().setBorderPainted(false);

		taskSetting = new JMenuItem("Постановка задачи");
		taskSetting.setBorderPainted(false);
		taskSetting.addActionListener(e -> openWordDoc("Постановка задачи"));

		grammar = new JMenuItem("Грамматика языка");
		grammar.setBorderPainted(false);
		grammar.addActionListener(e -> openWordDoc("Грамматика языка"));

		сlassification = new JMenuItem("Классификация по Хомскому");
		сlassification.setBorderPainted(false);
		сlassification.addActionListener(e -> openWordDoc("Классификация по Хомскому"));

		method = new JMenuItem("Метод анализа");
		method.setBorderPainted(false);
		method.addActionListener(e -> openWordDoc("Метод анализа"));

		diagnostics = new JMenuItem("Диагностика");
		diagnostics.setBorderPainted(false);
		diagnostics.addActionListener(e -> openWordDoc("Диагностика"));

		testing = new JMenuItem("Тестирование");
		testing.setBorderPainted(false);
		testing.addActionListener(e -> openWordDoc("Тестирование"));

		res.add(taskSetting);
		res.add(grammar);
		res.add(сlassification);
		res.add(method);
		res.add(diagnostics);
		res.add(testing);

		return res;
	}

	private JButton createStartButton() {
		JButton res = new JButton("Пуск");

		res.setBackground(auxiliaryColor);
		res.setBorderPainted(false);
		res.setFocusPainted(false);
		res.setToolTipText("Нажмите для запуска");

		res.addActionListener(e -> {
			parsing();
		});

		return res;
	}

	private JMenu createInfoMenu() {
		JMenu res = new JMenu("Справка");

		res.setBorderPainted(false);
		res.getPopupMenu().setOpaque(true);
		res.getPopupMenu().setBorderPainted(false);

		showInfo = new JMenuItem("Вызов справки");
		showInfo.setBorderPainted(false);
		showInfo.addActionListener(e -> openWordDoc("Справка"));

		aboutInfo = new JMenuItem("О программе");
		aboutInfo.setBorderPainted(false);
		aboutInfo.addActionListener(e -> showAboutDialog());

		res.add(showInfo);
		res.add(aboutInfo);

		return res;
	}

	private JSlider createTextSizeSlider() {
		JSlider res = new JSlider(JSlider.HORIZONTAL, 8, 28, DEFAULT_TEXT_SIZE);

		res.setMajorTickSpacing(2);
		res.setMinorTickSpacing(1);
		res.setPaintTicks(false);
		res.setPaintLabels(true);

		res.setBackground(backColor);
		res.setForeground(fontColor);
		res.setUI(new BasicSliderUI(res) {
			@Override
			public void paintThumb(Graphics g) {
				g.setColor(fontColor);
				g.fillRect(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height);
			}

			@Override
			public void paintTrack(Graphics g) {
				g.setColor(mainColor);
				g.fillRect(trackRect.x, trackRect.y + trackRect.height / 2 - 5, trackRect.width, 10);
			}

			@Override
			protected Dimension getThumbSize() {
				return new Dimension(5, 20);
			}
		});
		res.setFocusable(false);

		res.setMinimumSize(new Dimension(300, 42));
		res.setPreferredSize(new Dimension(300, 42));
		res.setMaximumSize(new Dimension(300, 42));

		res.addChangeListener(e -> {
			int textSize = res.getValue();

			Font newFont = new Font("Ubuntu", Font.PLAIN, textSize);

			originalText.setFont(newFont);

			diagnosticsTable.getTableHeader().setFont(newFont);
			diagnosticsTable.setFont(newFont);
			diagnosticsTable.setRowHeight(textSize + 2);
		});

		return res;
	}

	private JButton createButton(String icoName, String tipeMessage) {
		ImageIcon orig = new ImageIcon("resourses/" + icoName);
		Image scaled = orig.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);

		JButton res = new JButton(new ImageIcon(scaled));

		res.setBorderPainted(false);
		res.setFocusPainted(false);
		res.setToolTipText(tipeMessage);

		return res;
	}

	private JScrollPane createToolPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
		panel.setBackground(backColor);

		createFileButton = createButton("newfile.png", "Нажмите для создания нового файла");
		createFileButton.addActionListener(e -> createFile());

		openFileButton = createButton("openfile.png", "Нажмите для открытия файла");
		openFileButton.addActionListener(e -> openFile());

		saveCurrentButton = createButton("savefile.png", "Нажмите для сохранения файла");
		saveCurrentButton.addActionListener(e -> saveFile());

		undoEditButton = createButton("undo.png", "Нажмите для отмены");
		undoEditButton.addActionListener(e -> {
			if (undoManager.canUndo())
				undoManager.undo();
		});

		redoEditButton = createButton("redo.png", "Нажмите для повторения");
		redoEditButton.addActionListener(e -> {
			if (undoManager.canRedo())
				undoManager.redo();
		});

		startParceButton = createButton("start.png", "Нажмите для запуска парсера");
		startParceButton.addActionListener(e -> {
			parsing();
		});

		antlerButton = createButton("antler.png", "Нажмите для запуска Антлера");
		antlerButton.addActionListener(e -> {});

		showInfoButton = createButton("info.png", "Нажмите для вызова справки");
		showInfoButton.addActionListener(e -> openWordDoc("Справка"));

		aboutButton = createButton("about.png", "Нажмите для вызова информационного окна");
		aboutButton.addActionListener(e -> showAboutDialog());

		textSizeSlider = createTextSizeSlider();

		panel.add(createFileButton);
		panel.add(openFileButton);
		panel.add(saveCurrentButton);
		panel.add(undoEditButton);
		panel.add(redoEditButton);
		panel.add(startParceButton);
		panel.add(antlerButton);
		panel.add(showInfoButton);
		panel.add(aboutButton);
		panel.add(textSizeSlider);

		JScrollPane res = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		res.setBorder(null);
		res.getHorizontalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
			@Override
			protected void configureScrollBarColors() {
				this.thumbColor = auxiliaryColor;
				this.trackColor = backColor;
			}
		});

		return res;
	}

	private JPanel createMainPanel() {
		undoManager = new UndoManager();
		originalText = new JTextArea();
		originalText.setSelectionColor(selectionColor);
		originalText.getDocument().addUndoableEditListener(undoManager);
		originalText.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent e) {
				int pos = e.getDot();
				try {
					int line = originalText.getLineOfOffset(pos);
					int col = pos - originalText.getLineStartOffset(line);

					caretPosInfo.setText("Строка " + (line + 1) + ", столбец " + (col + 1));
				}
				catch (Exception exeption) {	}
			}
		});

		JScrollPane textScroll = new JScrollPane(originalText, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		textScroll.setBorder(null);
		textScroll.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
			@Override
			protected void configureScrollBarColors() {
				this.thumbColor = auxiliaryColor;
				this.trackColor = backColor;
			}
		});
		textScroll.getHorizontalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
			@Override
			protected void configureScrollBarColors() {
				this.thumbColor = auxiliaryColor;
				this.trackColor = backColor;
			}
		});

		table = new DefaultTableModel(
			new Object[][]{},
			new Object[] { "Строка", "Столбец", "Ошибка", "Название ошибки" }
		) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		diagnosticsTable = new JTable(table);
		diagnosticsTable.getTableHeader().setBackground(mainColor);
		diagnosticsTable.getTableHeader().setForeground(fontColor);
		diagnosticsTable.getTableHeader().setReorderingAllowed(false);
		diagnosticsTable.getTableHeader().setResizingAllowed(false);
		diagnosticsTable.getTableHeader().setFont(textFont);
		diagnosticsTable.setBackground(mainColor);
		diagnosticsTable.setForeground(fontColor);
		diagnosticsTable.setFont(textFont);
		diagnosticsTable.setRowHeight(DEFAULT_TEXT_SIZE + 2);

		JScrollPane diagnosticsScroll = new JScrollPane(diagnosticsTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		diagnosticsScroll.setBorder(null);
		diagnosticsScroll.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
			@Override
			protected void configureScrollBarColors() {
				this.thumbColor = auxiliaryColor;
				this.trackColor = backColor;
			}
		});
		diagnosticsScroll.getHorizontalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
			@Override
			protected void configureScrollBarColors() {
				this.thumbColor = auxiliaryColor;
				this.trackColor = backColor;
			}
		});
		
		diagnosticsScroll.addComponentListener(new java.awt.event.ComponentAdapter() {
			@Override
			public void componentResized(java.awt.event.ComponentEvent e) {
				int totalWidth = diagnosticsScroll.getViewport().getWidth();
				diagnosticsTable.getColumnModel().getColumn(0).setMinWidth(110);
				diagnosticsTable.getColumnModel().getColumn(0).setMaxWidth(110);
				diagnosticsTable.getColumnModel().getColumn(0).setPreferredWidth(110);

				diagnosticsTable.getColumnModel().getColumn(1).setMinWidth(120);
				diagnosticsTable.getColumnModel().getColumn(1).setMaxWidth(120);
				diagnosticsTable.getColumnModel().getColumn(1).setPreferredWidth(120);

				diagnosticsTable.getColumnModel().getColumn(2).setMinWidth(200);
				diagnosticsTable.getColumnModel().getColumn(2).setPreferredWidth((int)(totalWidth * 0.50));

				diagnosticsTable.getColumnModel().getColumn(3).setMinWidth(180);
				diagnosticsTable.getColumnModel().getColumn(3).setPreferredWidth((int)(totalWidth * 0.30));
			}
		});

		diagnosticsArea = new JPanel(new BorderLayout());
		diagnosticsArea.add(diagnosticsScroll, BorderLayout.CENTER);

		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, textScroll, diagnosticsArea);
		split.setResizeWeight(0.7);
		split.setDividerSize(5);
		split.setOneTouchExpandable(false);
		split.setBorder(null);
		split.setDividerSize(5);

		JPanel res = new JPanel(new BorderLayout());
		res.setBackground(backColor);
		res.add(split, BorderLayout.CENTER);

		return res;
	}

	private JPanel createInfoPanel() {
		caretPosInfo = new JLabel("Строка 1, столбец 1");
		caretPosInfo.setForeground(fontColor);
		caretPosInfo.setFont(menuFont);

		JPanel res = new JPanel(new BorderLayout());
		res.setBackground(backColor);
		res.add(caretPosInfo, BorderLayout.WEST);

		return res;
	}
}