package me.matt.notepad;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoManager;

import me.matt.notepad.gui.JFinder;
import me.matt.notepad.gui.JFontChooser;
import me.matt.notepad.gui.JReplacer;
import me.matt.notepad.secure.SecureTextFile;
import me.matt.notepad.util.FileDrop;
import me.matt.notepad.util.ImageUtil;
import me.matt.notepad.util.PrintUtility;
import me.matt.notepad.util.SecureFileFilter;

public class SecureNotepad extends JFrame implements ActionListener {

	private static final long serialVersionUID = 8628585234724001989L;

	private JTextArea jta;
	private JMenuBar jmb;
	private JMenu jmfile;
	private JMenu jmedit;
	private JMenu jmformat;
	private JMenu jmhelp;
	private JScrollPane jsp;
	private File fnameContainer;
	private UndoManager um = new UndoManager();
	private JFinder finder;
	private JReplacer replacer;
	private boolean changed = false;

	public SecureNotepad() {
		Font fnt = new Font("Tahoma", Font.PLAIN, 12);
		Container con = getContentPane();

		jta = new JTextArea();
		jmb = new JMenuBar();
		jmfile = new JMenu("File");
		jmedit = new JMenu("Edit");
		jmformat = new JMenu("Format");
		jmhelp = new JMenu("Help");
		jsp = new JScrollPane(jta);
		replacer = new JReplacer(jta);
		finder = new JFinder(jta);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		con.setLayout(new BorderLayout());
		jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		jsp.setVisible(true);

		jta.setFont(fnt);
		jta.setLineWrap(false);
		jta.setWrapStyleWord(false);

		jta.getDocument().addUndoableEditListener(new UndoableEditListener() {
			@Override
			public void undoableEditHappened(UndoableEditEvent e) {
				um.addEdit(e.getEdit());
			}
		});

		con.add(jsp);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (jta.getText().length() > 0) {
					if (changed) {
						if (confirm(fnameContainer != null ? fnameContainer
								.getName() : "Untitled")) {
							System.exit(0);
						}
					} else {
						System.exit(0);
					}
				} else {
					System.exit(0);
				}
			}
		});

		jta.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				keyActionPerformed(e);
			}

			public void keyTyped(KeyEvent e) {
				if (!e.isControlDown()) {
					changed = true;
				}
			}
		});

		createMenuItem(jmfile, "New               Ctrl+N");
		createMenuItem(jmfile, "Open...           Ctrl+O");
		createMenuItem(jmfile, "Save                Ctrl+S");
		createMenuItem(jmfile, "Save As...");
		jmfile.addSeparator();
		createMenuItem(jmfile, "Print                Ctrl+P");
		jmfile.addSeparator();
		createMenuItem(jmfile, "Exit");

		createMenuItem(jmedit, "Undo             Ctrl+Z");
		jmedit.addSeparator();
		createMenuItem(jmedit, "Cut                Ctrl+X");
		createMenuItem(jmedit, "Copy             Ctrl+C");
		createMenuItem(jmedit, "Paste             Ctrl+V");
		createMenuItem(jmedit, "Delete                 Del");
		createMenuItem(jmedit, "Select All      Ctrl+A");
		jmedit.addSeparator();
		createMenuItem(jmedit, "Find               Ctrl+F");
		createMenuItem(jmedit, "Replace        Ctrl+H");

		createCheckMenuItem(jmformat, "Word Wrap");
		createMenuItem(jmformat, "Font...");

		createMenuItem(jmhelp, "About Secure Notepad");

		jmb.add(jmfile);
		jmb.add(jmedit);
		jmb.add(jmformat);
		jmb.add(jmhelp);
		new FileDrop(this, new FileDrop.Listener() {

			@Override
			public void filesDropped(File[] files) {
				boolean found = false;
				for (File f : files) {
					if (f.getName().endsWith(".stxt")) {
						try {
							String loaded = SecureTextFile.load(
									f,
									JOptionPane
											.showInputDialog("What is the password for "
													+ f.getName() + "?"));
							if (loaded != null) {
								jta.setText(loaded);
								setTitle(f.getName().substring(0,
										f.getName().length() - 5)
										+ " - Notepad");
								fnameContainer = f;
								changed = false;
							} else {
								JOptionPane.showMessageDialog(null,
										"Invalid password");
							}
							found = true;
							break;
						} catch (Exception ex) {
						}
					}
				}
				if (!found) {
					JOptionPane.showMessageDialog(null,
							"No supported files found!");
				}
			}
		});

		setJMenuBar(jmb);
		setIconImage(ImageUtil.getImage(icon));
		setSize(1450, 750);
		setTitle("Untitled - Notepad");
		setVisible(true);

	}

	public void createMenuItem(JMenu jm, String txt) {
		JMenuItem jmi = new JMenuItem(txt);
		jmi.addActionListener(this);
		jm.add(jmi);
	}

	public void createCheckMenuItem(JMenu jm, String txt) {
		JCheckBoxMenuItem jmi = new JCheckBoxMenuItem(txt);
		jmi.addActionListener(this);
		jm.add(jmi);
	}

	private void keyActionPerformed(KeyEvent e) {
		if (e.isControlDown()) {
			if (e.getKeyCode() == KeyEvent.VK_N) {
				actionPerformed(new ActionEvent(e.getSource(), -1, "New"));
			} else if (e.getKeyCode() == KeyEvent.VK_O) {
				actionPerformed(new ActionEvent(e.getSource(), -1, "Open"));
			} else if (e.getKeyCode() == KeyEvent.VK_S) {
				actionPerformed(new ActionEvent(e.getSource(), -1, "Save  "));
			} else if (e.getKeyCode() == KeyEvent.VK_P) {
				actionPerformed(new ActionEvent(e.getSource(), -1, "Print"));
			} else if (e.getKeyCode() == KeyEvent.VK_Z) {
				actionPerformed(new ActionEvent(e.getSource(), -1, "Undo"));
			} else if (e.getKeyCode() == KeyEvent.VK_F) {
				actionPerformed(new ActionEvent(e.getSource(), -1, "Find"));
			} else if (e.getKeyCode() == KeyEvent.VK_H) {
				actionPerformed(new ActionEvent(e.getSource(), -1, "Replace"));
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser jfc = new JFileChooser();
		jfc.setFileFilter(new SecureFileFilter());
		jfc.setAcceptAllFileFilterUsed(false);
		if (e.getActionCommand().startsWith("New")) {
			if (jta.getText().length() > 0) {
				if (changed) {
					if (!confirm(fnameContainer != null ? fnameContainer
							.getName() : "Untitled")) {
						return;
					}
				}
			}
			this.setTitle("Untitled - Notepad");
			jta.setText("");
			fnameContainer = null;
			changed = false;
		} else if (e.getActionCommand().startsWith("Open")) {
			if (jta.getText().length() > 0) {
				if (changed) {
					if (!confirm(fnameContainer != null ? fnameContainer
							.getName() : "Untitled")) {
						return;
					}
				}
			}
			int opt = jfc.showDialog(null, "Open");
			if (opt == JFileChooser.APPROVE_OPTION) {
				try {
					File file = jfc.getSelectedFile();
					String loaded = SecureTextFile.load(file, JOptionPane
							.showInputDialog("What is the password for "
									+ file.getName() + "?"));
					if (loaded != null) {
						jta.setText(loaded);
						this.setTitle(file.getName().substring(0,
								file.getName().length() - 5)
								+ " - Notepad");
						fnameContainer = file;
						changed = false;
					} else {
						JOptionPane.showMessageDialog(this, "Invalid password");
					}
				} catch (Exception ex) {
				}
			}
		} else if (e.getActionCommand().startsWith("Save  ")) {
			if (fnameContainer != null) {
				try {
					SecureTextFile
							.save(fnameContainer,
									jta.getText(),
									JOptionPane
											.showInputDialog("What would you like the password to be?"));
				} catch (IOException ex) {
				}
				changed = false;
			} else {
				actionPerformed(new ActionEvent(e.getSource(), e.getID(),
						"Save As"));
			}
		} else if (e.getActionCommand().startsWith("Save As")) {
			if (fnameContainer != null) {
				jfc.setCurrentDirectory(fnameContainer);
				jfc.setSelectedFile(fnameContainer);
			} else {
				jfc.setSelectedFile(new File("Untitled.stxt"));
			}

			int ret = jfc.showSaveDialog(null);

			if (ret == JFileChooser.APPROVE_OPTION) {
				try {
					File file = jfc.getSelectedFile().getName()
							.endsWith(".stxt") ? jfc.getSelectedFile()
							: new File(jfc.getSelectedFile() + ".stxt");
					SecureTextFile
							.save(file,
									jta.getText(),
									JOptionPane
											.showInputDialog("What would you like the password to be?"));
					this.setTitle(file.getName().substring(0,
							file.getName().length() - 5)
							+ " - Notepad");
					fnameContainer = file;
					changed = false;
				} catch (Exception ex) {
				}
			}
		} else if (e.getActionCommand().startsWith("Print")) {
			PrintUtility.print(jta);
		} else if (e.getActionCommand().startsWith("Exit")) {
			if (jta.getText().length() > 0) {
				if (changed) {
					if (confirm(fnameContainer != null ? fnameContainer
							.getName() : "Untitled")) {
						System.exit(0);
					}
				} else {
					System.exit(0);
				}
			} else {
				System.exit(0);
			}
		} else if (e.getActionCommand().startsWith("Undo")) {
			if (um.canUndo()) {
				um.undo();
			}
		} else if (e.getActionCommand().startsWith("Cut")) {
			jta.cut();
		} else if (e.getActionCommand().startsWith("Copy")) {
			jta.copy();
		} else if (e.getActionCommand().startsWith("Paste")) {
			jta.paste();
		} else if (e.getActionCommand().startsWith("Delete")) {
			jta.setText(jta.getText().substring(0, jta.getSelectionStart())
					+ jta.getText().substring(jta.getSelectionEnd()));
		} else if (e.getActionCommand().startsWith("Select")) {
			jta.selectAll();
		} else if (e.getActionCommand().startsWith("Find")) {
			if (!finder.isVisible()) {
				finder.setVisible(true);
			}
			finder.requestFocus();
		} else if (e.getActionCommand().startsWith("Replace")) {
			if (!replacer.isVisible()) {
				replacer.setVisible(true);
			}
			replacer.requestFocus();
		} else if (e.getActionCommand().startsWith("Word")) {
			if (!jta.getWrapStyleWord()) {
				jta.setLineWrap(true);
				jta.setWrapStyleWord(true);
				jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			} else {
				jta.setLineWrap(false);
				jta.setWrapStyleWord(false);
				jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
			}
		} else if (e.getActionCommand().startsWith("Font")) {
			JFontChooser jfont = new JFontChooser();
			jfont.setVisible(true);
			int opt = jfont.showDialog(this);
			if (opt == JFontChooser.OK_OPTION) {
				jta.setFont(jfont.getSelectedFont());
			}
		} else if (e.getActionCommand().startsWith("About")) {
			JOptionPane.showMessageDialog(this, "Created by: Matt Langlois",
					"Notepad", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private boolean confirm(String file) {
		int opt = JOptionPane.showConfirmDialog(this,
				"Do you wish to save changes to " + file + "?",
				"Secure Notepad", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE);
		return opt != JOptionPane.CANCEL_OPTION;
	}

	public static void main(String[] args) {
		final URL resource = SecureNotepad.class.getClassLoader().getResource(
				icon);
		if (resource != null) {
			RUNNING_FROM_JAR = true;
		}
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		JFrame.setDefaultLookAndFeelDecorated(true);
		new SecureNotepad();
	}

	public static boolean RUNNING_FROM_JAR = false;
	private static String icon = "resources/icon.png";

}
