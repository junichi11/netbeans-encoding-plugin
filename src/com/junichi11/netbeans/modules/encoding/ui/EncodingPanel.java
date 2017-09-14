/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 */
package com.junichi11.netbeans.modules.encoding.ui;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.nio.charset.Charset;
import java.util.Collection;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.windows.WindowManager;

/**
 *
 * @author junichi11
 */
final class EncodingPanel extends JPanel {

    private static final Collection<? extends Charset> CHARSETS = Charset.availableCharsets().values();
    private static final long serialVersionUID = -7350298167289629517L;

    private DefaultListModel<String> encodingListModel = new DefaultListModel<>();
    private KeyListener encodingFilterKeyListener = new EncodingFilterKeyListener();
    private DocumentListener encodingFilterDocumentListener = new EncodingFilterDocumentListener();

    /**
     * Creates new form EncodingPanel
     */
    public EncodingPanel(Charset encoding) {
        initComponents();
        CHARSETS.forEach((charset) -> {
            encodingListModel.addElement(charset.name());
        });
        encodingList.setModel(encodingListModel);
        encodingList.setSelectedValue(encoding.name(), true);
        encodingFilterTextField.getDocument().addDocumentListener(encodingFilterDocumentListener);
        encodingFilterTextField.addKeyListener(encodingFilterKeyListener);

        // set Preferred size
        int preferredWidth = encodingListScrollPane.getPreferredSize().width;
        int preferredHeight = WindowManager.getDefault().getMainWindow().getSize().height / 4;
        encodingListScrollPane.setPreferredSize(new Dimension(preferredWidth, preferredHeight));

    }

    void shutdown() {
        // remove listeners
        encodingFilterTextField.removeKeyListener(encodingFilterKeyListener);
        encodingFilterTextField.getDocument().removeDocumentListener(encodingFilterDocumentListener);
        encodingFilterKeyListener = null;
        encodingFilterDocumentListener = null;
    }

    void fireChange() {
        encodingListModel.clear();
        String filter = encodingFilterTextField.getText();
        CHARSETS.stream()
                .filter(charset -> charset.name().toLowerCase().contains(filter.toLowerCase()))
                .forEachOrdered(charset -> encodingListModel.addElement(charset.name()));
        if (encodingListModel.size() > 0) {
            String element = encodingListModel.getElementAt(0);
            encodingList.setSelectedValue(element, true);
        }
    }

    void addEncodingListMouseListener(MouseListener listener) {
        encodingList.addMouseListener(listener);
    }

    void removeEncodingListMouseListener(MouseListener listener) {
        encodingList.removeMouseListener(listener);
    }

    boolean isEncodingPanelComponent(Object object) {
        return object == this
                || isEncodingList(object)
                || isEncodingListScrollBar(object)
                || isEncodingFilterField(object);
    }

    boolean isEncodingListScrollBar(Object object) {
        return encodingListScrollPane.getVerticalScrollBar() == object;
    }

    boolean isEncodingList(Object object) {
        return encodingList == object;
    }

    boolean isEncodingFilterField(Object object) {
        return encodingFilterTextField == object;
    }

    void requrestForcusEncodingFilter() {
        encodingFilterTextField.requestFocusInWindow();
    }

    public String getSelectedEncoding() {
        return encodingList.getSelectedValue();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        encodingFilterTextField = new javax.swing.JTextField();
        encodingListScrollPane = new javax.swing.JScrollPane();
        encodingList = new javax.swing.JList<>();

        encodingFilterTextField.setText(org.openide.util.NbBundle.getMessage(EncodingPanel.class, "EncodingPanel.encodingFilterTextField.text")); // NOI18N

        encodingListScrollPane.setViewportView(encodingList);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(encodingFilterTextField)
            .addComponent(encodingListScrollPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(encodingListScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(encodingFilterTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField encodingFilterTextField;
    private javax.swing.JList<String> encodingList;
    private javax.swing.JScrollPane encodingListScrollPane;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes
    private class EncodingFilterKeyListener implements KeyListener {

        public EncodingFilterKeyListener() {
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            // change a selected item
            int size = encodingListModel.getSize();
            if (size > 0) {
                int selectedIndex = encodingList.getSelectedIndex();
                String element;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        if (selectedIndex == 0) {
                            element = encodingListModel.getElementAt(size - 1);
                        } else {
                            element = encodingListModel.getElementAt(selectedIndex - 1);
                        }
                        break;
                    case KeyEvent.VK_DOWN:
                        if (selectedIndex == size - 1) {
                            element = encodingListModel.getElementAt(0);
                        } else {
                            element = encodingListModel.getElementAt(selectedIndex + 1);
                        }
                        break;
                    default:
                        element = null;
                        break;
                }

                if (element != null) {
                    encodingList.setSelectedValue(element, true);
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }
    }

    private class EncodingFilterDocumentListener implements DocumentListener {

        public EncodingFilterDocumentListener() {
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            processUpdate();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            processUpdate();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            processUpdate();
        }

        private void processUpdate() {
            fireChange();
        }
    }
}
