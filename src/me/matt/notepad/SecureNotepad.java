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
import javax.swing.WindowConstants;
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

    public static void main(final String[] args) {
        final URL resource = SecureNotepad.class.getClassLoader().getResource(
                SecureNotepad.icon);
        if (resource != null) {
            SecureNotepad.RUNNING_FROM_JAR = true;
        }
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (final Exception e) {
        }
        JFrame.setDefaultLookAndFeelDecorated(true);
        new SecureNotepad();
    }

    private static final long serialVersionUID = 8628585234724001989L;
    private final JTextArea jta;
    private final JMenuBar jmb;
    private final JMenu jmfile;
    private final JMenu jmedit;
    private final JMenu jmformat;
    private final JMenu jmhelp;
    private final JScrollPane jsp;
    private File fnameContainer;
    private final UndoManager um = new UndoManager();
    private final JFinder finder;
    private final JReplacer replacer;

    private boolean changed = false;

    public static boolean RUNNING_FROM_JAR = false;

    private static String icon = "resources/icon.png";

    public SecureNotepad() {
        final Font fnt = new Font("Tahoma", Font.PLAIN, 12);
        final Container con = this.getContentPane();

        jta = new JTextArea();
        jmb = new JMenuBar();
        jmfile = new JMenu("File");
        jmedit = new JMenu("Edit");
        jmformat = new JMenu("Format");
        jmhelp = new JMenu("Help");
        jsp = new JScrollPane(jta);
        replacer = new JReplacer(jta);
        finder = new JFinder(jta);
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        con.setLayout(new BorderLayout());
        jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jsp.setVisible(true);

        jta.setFont(fnt);
        jta.setLineWrap(false);
        jta.setWrapStyleWord(false);

        jta.getDocument().addUndoableEditListener(new UndoableEditListener() {
            @Override
            public void undoableEditHappened(final UndoableEditEvent e) {
                um.addEdit(e.getEdit());
            }
        });

        con.add(jsp);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                if (jta.getText().length() > 0) {
                    if (changed) {
                        if (SecureNotepad.this
                                .confirm(fnameContainer != null ? fnameContainer
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
            public void keyReleased(final KeyEvent e) {
                SecureNotepad.this.keyActionPerformed(e);
            }

            @Override
            public void keyTyped(final KeyEvent e) {
                if (!e.isControlDown()) {
                    changed = true;
                }
            }
        });

        this.createMenuItem(jmfile, "New               Ctrl+N");
        this.createMenuItem(jmfile, "Open...           Ctrl+O");
        this.createMenuItem(jmfile, "Save                Ctrl+S");
        this.createMenuItem(jmfile, "Save As...");
        jmfile.addSeparator();
        this.createMenuItem(jmfile, "Print                Ctrl+P");
        jmfile.addSeparator();
        this.createMenuItem(jmfile, "Exit");

        this.createMenuItem(jmedit, "Undo             Ctrl+Z");
        jmedit.addSeparator();
        this.createMenuItem(jmedit, "Cut                Ctrl+X");
        this.createMenuItem(jmedit, "Copy             Ctrl+C");
        this.createMenuItem(jmedit, "Paste             Ctrl+V");
        this.createMenuItem(jmedit, "Delete                 Del");
        this.createMenuItem(jmedit, "Select All      Ctrl+A");
        jmedit.addSeparator();
        this.createMenuItem(jmedit, "Find               Ctrl+F");
        this.createMenuItem(jmedit, "Replace        Ctrl+H");

        this.createCheckMenuItem(jmformat, "Word Wrap");
        this.createMenuItem(jmformat, "Font...");

        this.createMenuItem(jmhelp, "About Secure Notepad");

        jmb.add(jmfile);
        jmb.add(jmedit);
        jmb.add(jmformat);
        jmb.add(jmhelp);
        new FileDrop(this, new FileDrop.Listener() {

            @Override
            public void filesDropped(final File[] files) {
                boolean found = false;
                for (final File f : files) {
                    if (f.getName().endsWith(".stxt")) {
                        try {
                            final String loaded = SecureTextFile.load(
                                    f,
                                    JOptionPane
                                            .showInputDialog("What is the password for "
                                                    + f.getName() + "?"));
                            if (loaded != null) {
                                jta.setText(loaded);
                                SecureNotepad.this.setTitle(f.getName()
                                        .substring(0, f.getName().length() - 5)
                                        + " - Notepad");
                                fnameContainer = f;
                                changed = false;
                            } else {
                                JOptionPane.showMessageDialog(null,
                                        "Invalid password");
                            }
                            found = true;
                            break;
                        } catch (final Exception ex) {
                        }
                    }
                }
                if (!found) {
                    JOptionPane.showMessageDialog(null,
                            "No supported files found!");
                }
            }
        });

        this.setJMenuBar(jmb);
        this.setIconImage(ImageUtil.getImage(SecureNotepad.icon));
        this.setSize(1450, 750);
        this.setTitle("Untitled - Notepad");
        this.setVisible(true);

    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final JFileChooser jfc = new JFileChooser();
        jfc.setFileFilter(new SecureFileFilter());
        jfc.setAcceptAllFileFilterUsed(false);
        if (e.getActionCommand().startsWith("New")) {
            if (jta.getText().length() > 0) {
                if (changed) {
                    if (!this.confirm(fnameContainer != null ? fnameContainer
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
                    if (!this.confirm(fnameContainer != null ? fnameContainer
                            .getName() : "Untitled")) {
                        return;
                    }
                }
            }
            final int opt = jfc.showDialog(null, "Open");
            if (opt == JFileChooser.APPROVE_OPTION) {
                try {
                    final File file = jfc.getSelectedFile();
                    final String loaded = SecureTextFile.load(file, JOptionPane
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
                } catch (final Exception ex) {
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
                } catch (final IOException ex) {
                }
                changed = false;
            } else {
                this.actionPerformed(new ActionEvent(e.getSource(), e.getID(),
                        "Save As"));
            }
        } else if (e.getActionCommand().startsWith("Save As")) {
            if (fnameContainer != null) {
                jfc.setCurrentDirectory(fnameContainer);
                jfc.setSelectedFile(fnameContainer);
            } else {
                jfc.setSelectedFile(new File("Untitled.stxt"));
            }

            final int ret = jfc.showSaveDialog(null);

            if (ret == JFileChooser.APPROVE_OPTION) {
                try {
                    final File file = jfc.getSelectedFile().getName()
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
                } catch (final Exception ex) {
                }
            }
        } else if (e.getActionCommand().startsWith("Print")) {
            PrintUtility.print(jta);
        } else if (e.getActionCommand().startsWith("Exit")) {
            if (jta.getText().length() > 0) {
                if (changed) {
                    if (this.confirm(fnameContainer != null ? fnameContainer
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
            final JFontChooser jfont = new JFontChooser();
            jfont.setVisible(true);
            final int opt = jfont.showDialog(this);
            if (opt == JFontChooser.OK_OPTION) {
                jta.setFont(jfont.getSelectedFont());
            }
        } else if (e.getActionCommand().startsWith("About")) {
            JOptionPane.showMessageDialog(this, "Created by: Matt Langlois",
                    "Notepad", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private boolean confirm(final String file) {
        final int opt = JOptionPane.showConfirmDialog(this,
                "Do you wish to save changes to " + file + "?",
                "Secure Notepad", JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);
        return opt != JOptionPane.CANCEL_OPTION;
    }

    public void createCheckMenuItem(final JMenu jm, final String txt) {
        final JCheckBoxMenuItem jmi = new JCheckBoxMenuItem(txt);
        jmi.addActionListener(this);
        jm.add(jmi);
    }

    public void createMenuItem(final JMenu jm, final String txt) {
        final JMenuItem jmi = new JMenuItem(txt);
        jmi.addActionListener(this);
        jm.add(jmi);
    }

    private void keyActionPerformed(final KeyEvent e) {
        if (e.isControlDown()) {
            if (e.getKeyCode() == KeyEvent.VK_N) {
                this.actionPerformed(new ActionEvent(e.getSource(), -1, "New"));
            } else if (e.getKeyCode() == KeyEvent.VK_O) {
                this.actionPerformed(new ActionEvent(e.getSource(), -1, "Open"));
            } else if (e.getKeyCode() == KeyEvent.VK_S) {
                this.actionPerformed(new ActionEvent(e.getSource(), -1,
                        "Save  "));
            } else if (e.getKeyCode() == KeyEvent.VK_P) {
                this.actionPerformed(new ActionEvent(e.getSource(), -1, "Print"));
            } else if (e.getKeyCode() == KeyEvent.VK_Z) {
                this.actionPerformed(new ActionEvent(e.getSource(), -1, "Undo"));
            } else if (e.getKeyCode() == KeyEvent.VK_F) {
                this.actionPerformed(new ActionEvent(e.getSource(), -1, "Find"));
            } else if (e.getKeyCode() == KeyEvent.VK_H) {
                this.actionPerformed(new ActionEvent(e.getSource(), -1,
                        "Replace"));
            }
        }
    }

}
