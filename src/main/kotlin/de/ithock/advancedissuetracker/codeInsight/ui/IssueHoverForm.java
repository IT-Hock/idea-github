/*
 * Created by JFormDesigner on Sat Dec 31 12:02:01 CET 2022
 */

package de.ithock.advancedissuetracker.codeInsight.ui;

import java.awt.*;
import javax.swing.*;
import com.intellij.ui.components.*;
import com.intellij.uiDesigner.core.*;
import org.jdesktop.swingx.*;

/**
 * @author subtixx
 */
public class IssueHoverForm extends JPanel {
    public IssueHoverForm() {
        initComponents();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        panel2 = new JPanel();
        label4 = new JLabel();
        label5 = new JLabel();
        panel1 = new JPanel();
        label1 = new JLabel();
        label2 = new JLabel();
        panel4 = new JPanel();
        scrollPane1 = new JBScrollPane();
        label3 = new JBTextArea();
        panel3 = new JPanel();
        actionLink1 = new ActionLink();
        actionLink2 = new ActionLink();
        actionLink3 = new ActionLink();
        actionLink4 = new ActionLink();
        actionLink5 = new ActionLink();

        //======== this ========
        setLayout(new GridLayoutManager(4, 1, new Insets(10, 10, 10, 10), 0, 0));

        //======== panel2 ========
        {
            panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), 10, 0));

            //---- label4 ----
            label4.setText("ITH-1");
            panel2.add(label4, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));

            //---- label5 ----
            label5.setText("Lorem Ipsum Dolor Sit Aemt");
            panel2.add(label5, new GridConstraints(0, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_GROW | GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
        }
        add(panel2, new GridConstraints(0, 0, 1, 1,
            GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
            GridConstraints.SIZEPOLICY_FIXED,
            null, null, null));

        //======== panel1 ========
        {
            panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), 5, 0));

            //---- label1 ----
            label1.setText(null);
            label1.setIcon(UIManager.getIcon("FileChooser.homeFolderIcon"));
            label1.setMaximumSize(new Dimension(32, 32));
            label1.setMinimumSize(new Dimension(32, 32));
            label1.setHorizontalAlignment(SwingConstants.CENTER);
            panel1.add(label1, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));

            //---- label2 ----
            label2.setText("Dominic Hock <d.hock@it-hock.de>");
            panel1.add(label2, new GridConstraints(0, 1, 1, 1,
                GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null, null, null));
        }
        add(panel1, new GridConstraints(1, 0, 1, 1,
            GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
            GridConstraints.SIZEPOLICY_FIXED,
            null, null, null));

        //======== panel4 ========
        {
            panel4.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), 0, 0));

            //======== scrollPane1 ========
            {

                //---- label3 ----
                label3.setText("text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text text ");
                label3.setLineWrap(true);
                label3.setWrapStyleWord(true);
                scrollPane1.setViewportView(label3);
            }
            panel4.add(scrollPane1, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
        }
        add(panel4, new GridConstraints(2, 0, 1, 1,
            GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
            null, null, null));

        //======== panel3 ========
        {
            panel3.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));

            //---- actionLink1 ----
            actionLink1.setText("text");
            actionLink1.setHorizontalAlignment(SwingConstants.CENTER);
            panel3.add(actionLink1, new GridConstraints(0, 0, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));

            //---- actionLink2 ----
            actionLink2.setText("text");
            actionLink2.setHorizontalAlignment(SwingConstants.CENTER);
            panel3.add(actionLink2, new GridConstraints(0, 1, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));

            //---- actionLink3 ----
            actionLink3.setText("text");
            actionLink3.setHorizontalAlignment(SwingConstants.CENTER);
            panel3.add(actionLink3, new GridConstraints(0, 2, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));

            //---- actionLink4 ----
            actionLink4.setText("text");
            actionLink4.setHorizontalAlignment(SwingConstants.CENTER);
            panel3.add(actionLink4, new GridConstraints(0, 3, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));

            //---- actionLink5 ----
            actionLink5.setText("text");
            actionLink5.setHorizontalAlignment(SwingConstants.CENTER);
            panel3.add(actionLink5, new GridConstraints(0, 4, 1, 1,
                GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                null, null, null));
        }
        add(panel3, new GridConstraints(3, 0, 1, 1,
            GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
            GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
            null, null, null));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JPanel panel2;
    private JLabel label4;
    private JLabel label5;
    private JPanel panel1;
    private JLabel label1;
    private JLabel label2;
    private JPanel panel4;
    private JBScrollPane scrollPane1;
    private JBTextArea label3;
    private JPanel panel3;
    private ActionLink actionLink1;
    private ActionLink actionLink2;
    private ActionLink actionLink3;
    private ActionLink actionLink4;
    private ActionLink actionLink5;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
