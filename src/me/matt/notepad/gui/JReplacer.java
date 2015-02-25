package me.matt.notepad.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class JReplacer extends JFrame {

    /**
	 *
	 */
    private static final long serialVersionUID = 6436040459323662544L;
    private final JTextArea jta;

    private JLabel searchFor;

    private JLabel direction;

    private JLabel replaceWith;

    private JTextField input;

    private JTextField output;
    private JCheckBox matchCase;
    private JRadioButton up;
    private JRadioButton down;
    private JButton find;
    private JButton cancel;
    private JButton replace;
    private JButton replaceAll;

    public JReplacer(final JTextArea jta) {
        this.jta = jta;
        this.initComponents();
    }

    private void find() {
        int baseindex = jta.getCaretPosition();
        if (up.isSelected() && baseindex > 0) {
            if (jta.getSelectedText() != null) {
                if (matchCase.isSelected()
                        && jta.getSelectedText().equals(input.getText())) {
                    baseindex -= input.getText().length();
                } else if (!matchCase.isSelected()
                        && jta.getSelectedText().toLowerCase()
                                .equals(input.getText().toLowerCase())) {
                    baseindex -= input.getText().length();
                }
            }
            final String str = jta.getText().substring(0, baseindex);
            if (matchCase.isSelected() && str.contains(input.getText())) {
                jta.select(str.lastIndexOf(input.getText()),
                        str.lastIndexOf(input.getText())
                                + input.getText().length());
            } else if (!matchCase.isSelected()
                    && str.toLowerCase()
                            .contains(input.getText().toLowerCase())) {
                jta.select(
                        str.toLowerCase().lastIndexOf(
                                input.getText().toLowerCase()),
                        str.toLowerCase().lastIndexOf(
                                input.getText().toLowerCase())
                                + input.getText().length());
            } else {
                Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(null,
                        "No matches have been found!");
            }
        } else if (down.isSelected() && baseindex < jta.getText().length()) {
            final String str = jta.getText().substring(baseindex,
                    jta.getText().length());
            if (matchCase.isSelected() && str.contains(input.getText())) {
                jta.select(jta.getText().indexOf(input.getText(), baseindex),
                        jta.getText().indexOf(input.getText(), baseindex)
                                + input.getText().length());
            } else if (!matchCase.isSelected()
                    && str.toLowerCase()
                            .contains(input.getText().toLowerCase())) {
                jta.select(
                        jta.getText()
                                .toLowerCase()
                                .indexOf(input.getText().toLowerCase(),
                                        baseindex),
                        jta.getText()
                                .toLowerCase()
                                .indexOf(input.getText().toLowerCase(),
                                        baseindex)
                                + input.getText().length());
            } else {
                Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(null,
                        "No matches have been found!");
            }
        } else {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null, "No matches have been found!");
        }
    }

    private void initComponents() {
        searchFor = new JLabel();
        direction = new JLabel();
        replaceWith = new JLabel();
        input = new JTextField();
        output = new JTextField();
        matchCase = new JCheckBox();
        up = new JRadioButton();
        down = new JRadioButton();
        find = new JButton();
        cancel = new JButton();
        replace = new JButton();
        replaceAll = new JButton();

        this.setIconImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
        this.setTitle("Replace");
        final Container contentPane = this.getContentPane();
        contentPane.setLayout(null);

        searchFor.setText("Find What:");
        contentPane.add(searchFor);
        searchFor.setBounds(10, 10, searchFor.getPreferredSize().width, 20);
        contentPane.add(input);
        input.setBounds(80, 10, 170, input.getPreferredSize().height);

        matchCase.setText("Match case");
        contentPane.add(matchCase);
        matchCase.setBounds(new Rectangle(new Point(15, 80), matchCase
                .getPreferredSize()));

        direction.setText("Direction");
        contentPane.add(direction);
        direction.setBounds(170, 60, direction.getPreferredSize().width, 20);

        up.setText("Up");
        contentPane.add(up);
        up.setBounds(new Rectangle(new Point(135, 80), up.getPreferredSize()));
        up.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent arg0) {
                if (!up.isSelected()) {
                    down.setSelected(true);
                } else {
                    down.setSelected(false);
                }
            }

        });

        down.setText("Down");
        contentPane.add(down);
        down.setBounds(new Rectangle(new Point(190, 80), down
                .getPreferredSize()));
        down.setSelected(true);
        down.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent arg0) {
                if (!down.isSelected()) {
                    up.setSelected(true);
                } else {
                    up.setSelected(false);
                }
            }

        });

        find.setText("Find Next");
        contentPane.add(find);
        find.setBounds(260, 10, 85, find.getPreferredSize().height);
        find.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                JReplacer.this.find();
            }
        });

        cancel.setText("Cancel");
        contentPane.add(cancel);
        cancel.setBounds(260, 85, 85, 23);
        cancel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                JReplacer.this.setVisible(false);
            }

        });

        replace.setText("Replace");
        contentPane.add(replace);
        replace.setBounds(260, 35, 85, 23);
        replace.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                JReplacer.this.replace();
            }

        });

        replaceAll.setText("Replace All");
        contentPane.add(replaceAll);
        replaceAll.setBounds(260, 60, 85, 23);
        replaceAll.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                if (matchCase.isSelected()) {
                    jta.setText(jta.getText().replace(input.getText(),
                            output.getText()));
                } else {
                    jta.setText(jta.getText().replaceAll(
                            "(?i)" + input.getText(), output.getText()));
                }
            }

        });

        replaceWith.setText("Replace with:");
        contentPane.add(replaceWith);
        replaceWith.setBounds(10, 35, 65, 20);
        contentPane.add(output);
        output.setBounds(80, 35, 170, 20);

        contentPane.setPreferredSize(new Dimension(370, 150));
        this.setSize(370, 150);
        this.setLocationRelativeTo(this.getOwner());
    }

    private void replace() {
        if (jta.getSelectedText() != null) {
            if (matchCase.isSelected()
                    && jta.getSelectedText().equals(input.getText())) {
                jta.replaceSelection(output.getText());
                return;
            } else if (!matchCase.isSelected()
                    && jta.getSelectedText().toLowerCase()
                            .equals(input.getText().toLowerCase())) {
                jta.replaceSelection(output.getText());
                return;
            }
        }
        final int baseindex = jta.getCaretPosition();
        if (up.isSelected() && baseindex > 0) {
            final String str = jta.getText().substring(0, baseindex);
            if (matchCase.isSelected() && str.contains(input.getText())) {
                jta.select(str.lastIndexOf(input.getText()),
                        str.lastIndexOf(input.getText())
                                + input.getText().length());
            } else if (!matchCase.isSelected()
                    && str.toLowerCase()
                            .contains(input.getText().toLowerCase())) {
                jta.select(
                        str.toLowerCase().lastIndexOf(
                                input.getText().toLowerCase()),
                        str.toLowerCase().lastIndexOf(
                                input.getText().toLowerCase())
                                + input.getText().length());
            } else {
                Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(null,
                        "No matches have been found!");
            }
        } else if (down.isSelected() && baseindex < jta.getText().length()) {
            final String str = jta.getText().substring(baseindex,
                    jta.getText().length());
            if (matchCase.isSelected() && str.contains(input.getText())) {
                jta.select(jta.getText().indexOf(input.getText(), baseindex),
                        jta.getText().indexOf(input.getText(), baseindex)
                                + input.getText().length());
            } else if (!matchCase.isSelected()
                    && str.toLowerCase()
                            .contains(input.getText().toLowerCase())) {
                jta.select(
                        jta.getText()
                                .toLowerCase()
                                .indexOf(input.getText().toLowerCase(),
                                        baseindex),
                        jta.getText()
                                .toLowerCase()
                                .indexOf(input.getText().toLowerCase(),
                                        baseindex)
                                + input.getText().length());
            } else {
                Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(null,
                        "No matches have been found!");
            }
        } else {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null, "No matches have been found!");
        }
    }
}
