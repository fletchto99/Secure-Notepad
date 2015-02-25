/************************************************************
 * Copyright 2004-2005,2007-2008 Masahiko SAWAI All Rights Reserved.
 ************************************************************/
package me.matt.notepad.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;

/**
 * The <code>JFontChooser</code> class is a swing component for font selection.
 * This class has <code>JFileChooser</code> like APIs. The following code pops
 * up a font chooser dialog.
 *
 * <pre>
 *   JFontChooser fontChooser = new JFontChooser();
 *   int result = fontChooser.showDialog(parent);
 *   if (result == JFontChooser.OK_OPTION)
 *   {
 *   	Font font = fontChooser.getSelectedFont();
 *   	System.out.println("Selected Font : " + font);
 * }
 *
 * <pre>
 **/
public class JFontChooser extends JComponent {
    protected class DialogCancelAction extends AbstractAction {
        /**
		 *
		 */
        private static final long serialVersionUID = 1L;
        protected static final String ACTION_NAME = "Cancel";
        private final JDialog dialog;

        protected DialogCancelAction(final JDialog dialog) {
            this.dialog = dialog;
            this.putValue(Action.DEFAULT, DialogCancelAction.ACTION_NAME);
            this.putValue(Action.ACTION_COMMAND_KEY,
                    DialogCancelAction.ACTION_NAME);
            this.putValue(Action.NAME,
                    JFontChooser.this.myString(DialogCancelAction.ACTION_NAME));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            dialogResultValue = JFontChooser.CANCEL_OPTION;
            dialog.setVisible(false);
        }
    }

    protected class DialogOKAction extends AbstractAction {
        /**
		 *
		 */
        private static final long serialVersionUID = 1L;
        protected static final String ACTION_NAME = "OK";
        private final JDialog dialog;

        protected DialogOKAction(final JDialog dialog) {
            this.dialog = dialog;
            this.putValue(Action.DEFAULT, DialogOKAction.ACTION_NAME);
            this.putValue(Action.ACTION_COMMAND_KEY, DialogOKAction.ACTION_NAME);
            this.putValue(Action.NAME,
                    JFontChooser.this.myString(DialogOKAction.ACTION_NAME));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            dialogResultValue = JFontChooser.OK_OPTION;
            dialog.setVisible(false);
        }
    }

    protected class ListSearchTextFieldDocumentHandler implements
            DocumentListener {
        public class ListSelector implements Runnable {
            private final int index;

            public ListSelector(final int index) {
                this.index = index;
            }

            @Override
            public void run() {
                targetList.setSelectedIndex(index);
            }
        }

        JList<?> targetList;

        public ListSearchTextFieldDocumentHandler(final JList<?> targetList) {
            this.targetList = targetList;
        }

        @Override
        public void changedUpdate(final DocumentEvent e) {
            this.update(e);
        }

        @Override
        public void insertUpdate(final DocumentEvent e) {
            this.update(e);
        }

        @Override
        public void removeUpdate(final DocumentEvent e) {
            this.update(e);
        }

        private void update(final DocumentEvent event) {
            String newValue = "";
            try {
                final Document doc = event.getDocument();
                newValue = doc.getText(0, doc.getLength());
            } catch (final BadLocationException e) {
                e.printStackTrace();
            }

            if (newValue.length() > 0) {
                int index = targetList.getNextMatch(newValue, 0,
                        Position.Bias.Forward);
                if (index < 0) {
                    index = 0;
                }
                targetList.ensureIndexIsVisible(index);

                final String matchedName = targetList.getModel()
                        .getElementAt(index).toString();
                if (newValue.equalsIgnoreCase(matchedName)) {
                    if (index != targetList.getSelectedIndex()) {
                        SwingUtilities.invokeLater(new ListSelector(index));
                    }
                }
            }
        }
    }

    protected class ListSelectionHandler implements ListSelectionListener {
        private final JTextComponent textComponent;

        ListSelectionHandler(final JTextComponent textComponent) {
            this.textComponent = textComponent;
        }

        @Override
        public void valueChanged(final ListSelectionEvent e) {
            if (e.getValueIsAdjusting() == false) {
                final JList<?> list = (JList<?>) e.getSource();
                final String selectedValue = (String) list.getSelectedValue();

                final String oldValue = textComponent.getText();
                textComponent.setText(selectedValue);
                if (!oldValue.equalsIgnoreCase(selectedValue)) {
                    textComponent.selectAll();
                    textComponent.requestFocus();
                }

                JFontChooser.this.updateSampleFont();
            }
        }
    }

    protected class TextFieldFocusHandlerForTextSelection extends FocusAdapter {
        private final JTextComponent textComponent;

        public TextFieldFocusHandlerForTextSelection(
                final JTextComponent textComponent) {
            this.textComponent = textComponent;
        }

        @Override
        public void focusGained(final FocusEvent e) {
            textComponent.selectAll();
        }

        @Override
        public void focusLost(final FocusEvent e) {
            textComponent.select(0, 0);
            JFontChooser.this.updateSampleFont();
        }
    }

    protected class TextFieldKeyHandlerForListSelectionUpDown extends
            KeyAdapter {
        private final JList<?> targetList;

        public TextFieldKeyHandlerForListSelectionUpDown(final JList<?> list) {
            targetList = list;
        }

        @Override
        public void keyPressed(final KeyEvent e) {
            int i = targetList.getSelectedIndex();
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP:
                    i = targetList.getSelectedIndex() - 1;
                    if (i < 0) {
                        i = 0;
                    }
                    targetList.setSelectedIndex(i);
                    break;
                case KeyEvent.VK_DOWN:
                    final int listSize = targetList.getModel().getSize();
                    i = targetList.getSelectedIndex() + 1;
                    if (i >= listSize) {
                        i = listSize - 1;
                    }
                    targetList.setSelectedIndex(i);
                    break;
                default:
                    break;
            }
        }
    }

    /**
	 *
	 */
    private static final long serialVersionUID = 1L;
    // class variables
    /**
     * Return value from <code>showDialog()</code>.
     * 
     * @see #showDialog
     **/
    public static final int OK_OPTION = 0;

    /**
     * Return value from <code>showDialog()</code>.
     * 
     * @see #showDialog
     **/
    public static final int CANCEL_OPTION = 1;
    /**
     * Return value from <code>showDialog()</code>.
     * 
     * @see #showDialog
     **/
    public static final int ERROR_OPTION = -1;

    private static final Font DEFAULT_SELECTED_FONT = new Font("Serif",
            Font.PLAIN, 12);

    private static final Font DEFAULT_FONT = new Font("Dialog", Font.PLAIN, 10);
    private static final int[] FONT_STYLE_CODES = { Font.PLAIN, Font.BOLD,
            Font.ITALIC, Font.BOLD | Font.ITALIC };
    private static final String[] DEFAULT_FONT_SIZE_STRINGS = { "8", "9", "10",
            "11", "12", "14", "16", "18", "20", "22", "24", "26", "28", "36",
            "48", "72", };
    // instance variables
    protected int dialogResultValue = JFontChooser.ERROR_OPTION;
    private final ResourceBundle messageCatalog = ResourceBundle.getBundle(
            JFontChooser.class.getName() + "Messages", this.getLocale());
    private String[] fontStyleNames = null;
    private String[] fontFamilyNames = null;
    private String[] fontSizeStrings = null;
    private JTextField fontFamilyTextField = null;
    private JTextField fontStyleTextField = null;
    private JTextField fontSizeTextField = null;
    private JList<?> fontNameList = null;
    private JList<?> fontStyleList = null;
    private JList<?> fontSizeList = null;

    private JPanel fontNamePanel = null;

    private JPanel fontStylePanel = null;

    private JPanel fontSizePanel = null;

    private JPanel samplePanel = null;

    private JTextField sampleText = null;

    /**
     * Constructs a <code>JFontChooser</code> object.
     **/
    public JFontChooser() {
        this(JFontChooser.DEFAULT_FONT_SIZE_STRINGS);
    }

    /**
     * Constructs a <code>JFontChooser</code> object using the given font size
     * array.
     * 
     * @param fontSizeStrings
     *            the array of font size string.
     **/
    public JFontChooser(String[] fontSizeStrings) {
        if (fontSizeStrings == null) {
            fontSizeStrings = JFontChooser.DEFAULT_FONT_SIZE_STRINGS;
        }
        this.fontSizeStrings = fontSizeStrings;

        final JPanel selectPanel = new JPanel();
        selectPanel.setLayout(new BoxLayout(selectPanel, BoxLayout.X_AXIS));
        selectPanel.add(this.getFontFamilyPanel());
        selectPanel.add(this.getFontStylePanel());
        selectPanel.add(this.getFontSizePanel());

        final JPanel contentsPanel = new JPanel();
        contentsPanel.setLayout(new GridLayout(2, 1));
        contentsPanel.add(selectPanel, BorderLayout.NORTH);
        contentsPanel.add(this.getSamplePanel(), BorderLayout.CENTER);

        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.add(contentsPanel);
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.setSelectedFont(JFontChooser.DEFAULT_SELECTED_FONT);
    }

    protected JDialog createDialog(final Component parent) {
        final Frame frame = new Frame();
        frame.setIconImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
        final JDialog dialog = new JDialog(frame, this.myString("SelectFont"),
                true);

        final Action okAction = new DialogOKAction(dialog);
        final Action cancelAction = new DialogCancelAction(dialog);

        final JButton okButton = new JButton(okAction);
        okButton.setFont(JFontChooser.DEFAULT_FONT);
        final JButton cancelButton = new JButton(cancelAction);
        cancelButton.setFont(JFontChooser.DEFAULT_FONT);

        final JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(2, 1));
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(25, 0, 10, 10));

        final ActionMap actionMap = buttonsPanel.getActionMap();
        actionMap.put(cancelAction.getValue(Action.DEFAULT), cancelAction);
        actionMap.put(okAction.getValue(Action.DEFAULT), okAction);
        final InputMap inputMap = buttonsPanel
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke("ESCAPE"),
                cancelAction.getValue(Action.DEFAULT));
        inputMap.put(KeyStroke.getKeyStroke("ENTER"),
                okAction.getValue(Action.DEFAULT));

        final JPanel dialogEastPanel = new JPanel();
        dialogEastPanel.setLayout(new BorderLayout());
        dialogEastPanel.add(buttonsPanel, BorderLayout.NORTH);

        dialog.getContentPane().add(this, BorderLayout.CENTER);
        dialog.getContentPane().add(dialogEastPanel, BorderLayout.EAST);
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        return dialog;
    }

    protected String[] getFontFamilies() {
        if (fontFamilyNames == null) {
            final GraphicsEnvironment env = GraphicsEnvironment
                    .getLocalGraphicsEnvironment();
            fontFamilyNames = env.getAvailableFontFamilyNames();
        }
        return fontFamilyNames;
    }

    public JList<?> getFontFamilyList() {
        if (fontNameList == null) {
            fontNameList = new JList<Object>(this.getFontFamilies());
            fontNameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            fontNameList.addListSelectionListener(new ListSelectionHandler(this
                    .getFontFamilyTextField()));
            fontNameList.setSelectedIndex(0);
            fontNameList.setFont(JFontChooser.DEFAULT_FONT);
            fontNameList.setFocusable(false);
        }
        return fontNameList;
    }

    protected JPanel getFontFamilyPanel() {
        if (fontNamePanel == null) {
            fontNamePanel = new JPanel();
            fontNamePanel.setLayout(new BorderLayout());
            fontNamePanel
                    .setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            fontNamePanel.setPreferredSize(new Dimension(180, 130));

            final JScrollPane scrollPane = new JScrollPane(
                    this.getFontFamilyList());
            scrollPane.getVerticalScrollBar().setFocusable(false);
            scrollPane
                    .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

            final JPanel p = new JPanel();
            p.setLayout(new BorderLayout());
            p.add(this.getFontFamilyTextField(), BorderLayout.NORTH);
            p.add(scrollPane, BorderLayout.CENTER);

            final JLabel label = new JLabel(this.myString("FontName"));
            label.setHorizontalAlignment(SwingConstants.LEFT);
            label.setHorizontalTextPosition(SwingConstants.LEFT);
            label.setLabelFor(this.getFontFamilyTextField());
            label.setDisplayedMnemonic('F');

            fontNamePanel.add(label, BorderLayout.NORTH);
            fontNamePanel.add(p, BorderLayout.CENTER);

        }
        return fontNamePanel;
    }

    public JTextField getFontFamilyTextField() {
        if (fontFamilyTextField == null) {
            fontFamilyTextField = new JTextField();
            fontFamilyTextField
                    .addFocusListener(new TextFieldFocusHandlerForTextSelection(
                            fontFamilyTextField));
            fontFamilyTextField
                    .addKeyListener(new TextFieldKeyHandlerForListSelectionUpDown(
                            this.getFontFamilyList()));
            fontFamilyTextField.getDocument().addDocumentListener(
                    new ListSearchTextFieldDocumentHandler(this
                            .getFontFamilyList()));
            fontFamilyTextField.setFont(JFontChooser.DEFAULT_FONT);

        }
        return fontFamilyTextField;
    }

    public JList<?> getFontSizeList() {
        if (fontSizeList == null) {
            fontSizeList = new JList<Object>(fontSizeStrings);
            fontSizeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            fontSizeList.addListSelectionListener(new ListSelectionHandler(this
                    .getFontSizeTextField()));
            fontSizeList.setSelectedIndex(0);
            fontSizeList.setFont(JFontChooser.DEFAULT_FONT);
            fontSizeList.setFocusable(false);
        }
        return fontSizeList;
    }

    protected JPanel getFontSizePanel() {
        if (fontSizePanel == null) {
            fontSizePanel = new JPanel();
            fontSizePanel.setLayout(new BorderLayout());
            fontSizePanel.setPreferredSize(new Dimension(70, 130));
            fontSizePanel
                    .setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            final JScrollPane scrollPane = new JScrollPane(
                    this.getFontSizeList());
            scrollPane.getVerticalScrollBar().setFocusable(false);
            scrollPane
                    .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

            final JPanel p = new JPanel();
            p.setLayout(new BorderLayout());
            p.add(this.getFontSizeTextField(), BorderLayout.NORTH);
            p.add(scrollPane, BorderLayout.CENTER);

            final JLabel label = new JLabel(this.myString("FontSize"));
            label.setHorizontalAlignment(SwingConstants.LEFT);
            label.setHorizontalTextPosition(SwingConstants.LEFT);
            label.setLabelFor(this.getFontSizeTextField());
            label.setDisplayedMnemonic('S');

            fontSizePanel.add(label, BorderLayout.NORTH);
            fontSizePanel.add(p, BorderLayout.CENTER);
        }
        return fontSizePanel;
    }

    public JTextField getFontSizeTextField() {
        if (fontSizeTextField == null) {
            fontSizeTextField = new JTextField();
            fontSizeTextField
                    .addFocusListener(new TextFieldFocusHandlerForTextSelection(
                            fontSizeTextField));
            fontSizeTextField
                    .addKeyListener(new TextFieldKeyHandlerForListSelectionUpDown(
                            this.getFontSizeList()));
            fontSizeTextField.getDocument().addDocumentListener(
                    new ListSearchTextFieldDocumentHandler(this
                            .getFontSizeList()));
            fontSizeTextField.setFont(JFontChooser.DEFAULT_FONT);
        }
        return fontSizeTextField;
    }

    public JList<?> getFontStyleList() {
        if (fontStyleList == null) {
            fontStyleList = new JList<Object>(this.getFontStyleNames());
            fontStyleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            fontStyleList.addListSelectionListener(new ListSelectionHandler(
                    this.getFontStyleTextField()));
            fontStyleList.setSelectedIndex(0);
            fontStyleList.setFont(JFontChooser.DEFAULT_FONT);
            fontStyleList.setFocusable(false);
        }
        return fontStyleList;
    }

    protected String[] getFontStyleNames() {
        if (fontStyleNames == null) {
            int i = 0;
            fontStyleNames = new String[4];
            fontStyleNames[i++] = this.myString("Plain");
            fontStyleNames[i++] = this.myString("Bold");
            fontStyleNames[i++] = this.myString("Italic");
            fontStyleNames[i++] = this.myString("BoldItalic");
        }
        return fontStyleNames;
    }

    protected JPanel getFontStylePanel() {
        if (fontStylePanel == null) {
            fontStylePanel = new JPanel();
            fontStylePanel.setLayout(new BorderLayout());
            fontStylePanel.setBorder(BorderFactory
                    .createEmptyBorder(5, 5, 5, 5));
            fontStylePanel.setPreferredSize(new Dimension(140, 130));

            final JScrollPane scrollPane = new JScrollPane(
                    this.getFontStyleList());
            scrollPane.getVerticalScrollBar().setFocusable(false);
            scrollPane
                    .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

            final JPanel p = new JPanel();
            p.setLayout(new BorderLayout());
            p.add(this.getFontStyleTextField(), BorderLayout.NORTH);
            p.add(scrollPane, BorderLayout.CENTER);

            final JLabel label = new JLabel(this.myString("FontStyle"));
            label.setHorizontalAlignment(SwingConstants.LEFT);
            label.setHorizontalTextPosition(SwingConstants.LEFT);
            label.setLabelFor(this.getFontStyleTextField());
            label.setDisplayedMnemonic('Y');

            fontStylePanel.add(label, BorderLayout.NORTH);
            fontStylePanel.add(p, BorderLayout.CENTER);
        }
        return fontStylePanel;
    }

    public JTextField getFontStyleTextField() {
        if (fontStyleTextField == null) {
            fontStyleTextField = new JTextField();
            fontStyleTextField
                    .addFocusListener(new TextFieldFocusHandlerForTextSelection(
                            fontStyleTextField));
            fontStyleTextField
                    .addKeyListener(new TextFieldKeyHandlerForListSelectionUpDown(
                            this.getFontStyleList()));
            fontStyleTextField.getDocument().addDocumentListener(
                    new ListSearchTextFieldDocumentHandler(this
                            .getFontStyleList()));
            fontStyleTextField.setFont(JFontChooser.DEFAULT_FONT);
        }
        return fontStyleTextField;
    }

    protected JPanel getSamplePanel() {
        if (samplePanel == null) {
            final Border titledBorder = BorderFactory
                    .createTitledBorder(BorderFactory.createEtchedBorder(),
                            this.myString("Sample"));
            final Border empty = BorderFactory.createEmptyBorder(5, 10, 10, 10);
            final Border border = BorderFactory.createCompoundBorder(
                    titledBorder, empty);

            samplePanel = new JPanel();
            samplePanel.setLayout(new BorderLayout());
            samplePanel.setBorder(border);

            samplePanel.add(this.getSampleTextField(), BorderLayout.CENTER);
        }
        return samplePanel;
    }

    protected JTextField getSampleTextField() {
        if (sampleText == null) {
            final Border lowered = BorderFactory.createLoweredBevelBorder();

            sampleText = new JTextField(this.myString("SampleString"));
            sampleText.setBorder(lowered);
            sampleText.setPreferredSize(new Dimension(300, 100));
        }
        return sampleText;
    }

    /**
     * Get the selected font.
     * 
     * @return the selected font
     * 
     * @see #setSelectedFont
     * @see java.awt.Font
     **/
    public Font getSelectedFont() {
        final Font font = new Font(this.getSelectedFontFamily(),
                this.getSelectedFontStyle(), this.getSelectedFontSize());
        return font;
    }

    /**
     * Get the family name of the selected font.
     * 
     * @return the font family of the selected font.
     * 
     * @see #setSelectedFontFamily
     **/
    public String getSelectedFontFamily() {
        final String fontName = (String) this.getFontFamilyList()
                .getSelectedValue();
        return fontName;
    }

    /**
     * Get the size of the selected font.
     * 
     * @return the size of the selected font
     * 
     * @see #setSelectedFontSize
     **/
    public int getSelectedFontSize() {
        int fontSize = 1;
        String fontSizeString = this.getFontSizeTextField().getText();
        while (true) {
            try {
                fontSize = Integer.parseInt(fontSizeString);
                break;
            } catch (final NumberFormatException e) {
                fontSizeString = (String) this.getFontSizeList()
                        .getSelectedValue();
                this.getFontSizeTextField().setText(fontSizeString);
            }
        }

        return fontSize;
    }

    /**
     * Get the style of the selected font.
     * 
     * @return the style of the selected font. <code>Font.PLAIN</code>, <code>Font.BOLD</code>, <code>Font.ITALIC</code>,
     *         <code>Font.BOLD|Font.ITALIC</code>
     * 
     * @see java.awt.Font#PLAIN
     * @see java.awt.Font#BOLD
     * @see java.awt.Font#ITALIC
     * @see #setSelectedFontStyle
     **/
    public int getSelectedFontStyle() {
        final int index = this.getFontStyleList().getSelectedIndex();
        return JFontChooser.FONT_STYLE_CODES[index];
    }

    public String getVersionString() {
        return this.myString("Version");
    }

    protected String myString(final String key) {
        String value = key;
        try {
            value = messageCatalog.getString(key);
        } catch (final MissingResourceException e) {
        }
        return value;
    }

    /**
     * Set the selected font.
     * 
     * @param font
     *            the selected font
     * 
     * @see #getSelectedFont
     * @see java.awt.Font
     **/
    public void setSelectedFont(final Font font) {
        this.setSelectedFontFamily(font.getFamily());
        this.setSelectedFontStyle(font.getStyle());
        this.setSelectedFontSize(font.getSize());
    }

    /**
     * Set the family name of the selected font.
     * 
     * @param name
     *            the family name of the selected font.
     * 
     * @see getSelectedFontFamily
     **/
    public void setSelectedFontFamily(final String name) {
        final String[] names = this.getFontFamilies();
        for (int i = 0; i < names.length; i++) {
            if (names[i].toLowerCase().equals(name.toLowerCase())) {
                this.getFontFamilyList().setSelectedIndex(i);
                break;
            }
        }
        this.updateSampleFont();
    }

    /**
     * Set the size of the selected font.
     * 
     * @param size
     *            the size of the selected font
     * 
     * @see #getSelectedFontSize
     **/
    public void setSelectedFontSize(final int size) {
        final String sizeString = String.valueOf(size);
        for (int i = 0; i < fontSizeStrings.length; i++) {
            if (fontSizeStrings[i].equals(sizeString)) {
                this.getFontSizeList().setSelectedIndex(i);
                break;
            }
        }
        this.getFontSizeTextField().setText(sizeString);
        this.updateSampleFont();
    }

    /**
     * Set the style of the selected font.
     * 
     * @param style
     *            the size of the selected font. <code>Font.PLAIN</code>, <code>Font.BOLD</code>, <code>Font.ITALIC</code>, or
     *            <code>Font.BOLD|Font.ITALIC</code>.
     * 
     * @see java.awt.Font#PLAIN
     * @see java.awt.Font#BOLD
     * @see java.awt.Font#ITALIC
     * @see #getSelectedFontStyle
     **/
    public void setSelectedFontStyle(final int style) {
        for (int i = 0; i < JFontChooser.FONT_STYLE_CODES.length; i++) {
            if (JFontChooser.FONT_STYLE_CODES[i] == style) {
                this.getFontStyleList().setSelectedIndex(i);
                break;
            }
        }
        this.updateSampleFont();
    }

    /**
     * Show font selection dialog.
     * 
     * @param parent
     *            Dialog's Parent component.
     * @return OK_OPTION, CANCEL_OPTION or ERROR_OPTION
     * 
     * @see #OK_OPTION
     * @see #CANCEL_OPTION
     * @see #ERROR_OPTION
     **/
    public int showDialog(final Component parent) {
        dialogResultValue = JFontChooser.ERROR_OPTION;
        JDialog dialog = this.createDialog(parent);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                dialogResultValue = JFontChooser.CANCEL_OPTION;
            }
        });

        dialog.setVisible(true);
        dialog.dispose();
        dialog = null;

        return dialogResultValue;
    }

    protected void updateSampleFont() {
        final Font font = this.getSelectedFont();
        this.getSampleTextField().setFont(font);
    }
}
