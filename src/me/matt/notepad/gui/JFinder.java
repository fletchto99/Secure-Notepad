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

public class JFinder extends JFrame {

    /**
	 *
	 */
    private static final long serialVersionUID = 3912492046525133163L;
    private final JTextArea jta;

    private JLabel searchfor;

    private JLabel direction;

    private JTextField input;

    private JCheckBox matchCase;
    private JRadioButton up;
    private JRadioButton down;
    private JButton find;
    private JButton cancel;

    public JFinder(final JTextArea jta) {
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
        searchfor = new JLabel();
        direction = new JLabel();
        input = new JTextField();
        matchCase = new JCheckBox();
        up = new JRadioButton();
        down = new JRadioButton();
        find = new JButton();
        cancel = new JButton();

        this.setIconImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
        this.setTitle("Find");
        final Container contentPane = this.getContentPane();
        contentPane.setLayout(null);

        searchfor.setText("Find What:");
        contentPane.add(searchfor);
        searchfor.setBounds(10, 10, searchfor.getPreferredSize().width, 20);
        contentPane.add(input);
        input.setBounds(70, 10, 180, input.getPreferredSize().height);

        matchCase.setText("Match case");
        contentPane.add(matchCase);
        matchCase.setBounds(new Rectangle(new Point(15, 70), matchCase
                .getPreferredSize()));

        direction.setText("Direction");
        contentPane.add(direction);
        direction.setBounds(170, 50, direction.getPreferredSize().width, 20);

        up.setText("Up");
        contentPane.add(up);
        up.setBounds(new Rectangle(new Point(135, 70), up.getPreferredSize()));
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
        down.setText("Down");
        contentPane.add(down);
        down.setBounds(new Rectangle(new Point(190, 70), down
                .getPreferredSize()));

        find.setText("Find Next");
        contentPane.add(find);
        find.setBounds(265, 10, 80, find.getPreferredSize().height);
        find.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                JFinder.this.find();
            }

        });

        cancel.setText("Cancel");
        contentPane.add(cancel);
        cancel.setBounds(265, 40, 80, 23);
        cancel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                JFinder.this.setVisible(false);
            }

        });

        contentPane.setPreferredSize(new Dimension(370, 140));
        this.setSize(370, 140);
        this.setLocationRelativeTo(this.getOwner());
    }
}
