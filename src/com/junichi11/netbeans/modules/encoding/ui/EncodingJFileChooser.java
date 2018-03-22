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

import java.awt.Component;
import java.awt.Dimension;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.Box;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListDataListener;
import org.openide.util.NbBundle;

/**
 *
 * @author Chris2011
 */
public class EncodingJFileChooser extends JFileChooser {
    private JComboBox encodingComboBox;

    public EncodingJFileChooser() {
        this.encodingComboBox = new JComboBox();

        this.encodingComboBox.setModel(new EncodingJFileChooser.EncodingModel());
        this.encodingComboBox.setRenderer(new EncodingJFileChooser.EncodingRenderer());

        addEncodingComboBox(this, this.encodingComboBox, new JLabel(NbBundle.getMessage(EncodingJFileChooser.class, "EncodingLabel")));
    }

    public Charset getEncoding() {
        return ((EncodingJFileChooser.EncodingKey) this.encodingComboBox.getSelectedItem()).getCharset();
    }

    public void setEncoding(final Charset encoding) {
        EncodingJFileChooser.EncodingKey key = (encoding == null ? EncodingJFileChooser.EncodingKey.DEFAULT : new EncodingJFileChooser.EncodingKey(encoding));
        this.encodingComboBox.setSelectedItem(key);
    }

    private void addEncodingComboBox(JFileChooser fileChooser, JComboBox<String> encodingComboBox, JLabel encodingLabel) {
        Component comp = fileChooser.getComponent(2);
        JPanel fPanel = (JPanel)comp;
        JPanel na = (JPanel)fPanel.getComponent(2);
        JPanel fields = (JPanel)na.getComponent(2);

        fields.add(Box.createRigidArea(new Dimension(1, 8)));
        fields.add(encodingComboBox);

        JPanel labels = (JPanel)na.getComponent(0);
        labels.add(Box.createRigidArea(new Dimension(1, 12)));
        labels.add(encodingLabel);
    }

    private static class EncodingModel implements ComboBoxModel {
        private Object selected;
        private List<EncodingKey> data;

        public EncodingModel() {
            final Collection<? extends Charset> acs = Charset.availableCharsets().values();
            final List<EncodingKey> _data = new ArrayList<EncodingKey>(acs.size() + 1);
            _data.add(EncodingKey.DEFAULT);
            for (Charset c : acs) {
                _data.add(new EncodingKey(c));
            }
            data = Collections.unmodifiableList(_data);
        }

        @Override
        public void setSelectedItem(Object anItem) {
            this.selected = anItem;
        }

        @Override
        public Object getSelectedItem() {
            return this.selected;
        }

        @Override
        public int getSize() {
            return this.data.size();
        }

        @Override
        public Object getElementAt(int index) {
            assert index >= 0 && index < this.data.size();
            return this.data.get(index);
        }

        @Override
        public void addListDataListener(ListDataListener l) {
            //Non mutable
        }

        @Override
        public void removeListDataListener(ListDataListener l) {
            //Non mutable
        }
    }

    private final static class EncodingKey {
        public static final EncodingKey DEFAULT = new EncodingKey();

        private final Charset cs;

        public EncodingKey(final Charset cs) {
            assert cs != null;
            this.cs = cs;
        }

        private EncodingKey() {
            this.cs = null;
        }

        public String getDisplayName() {
            return this.cs == null ? NbBundle.getMessage(EncodingJFileChooser.class, "TXT_Default") : this.cs.displayName();
        }

        public Charset getCharset() {
            return this.cs;
        }

        @Override
        public int hashCode() {
            return this.cs == null ? 0 : this.cs.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof EncodingKey) {
                EncodingKey ok = (EncodingKey) obj;
                return this.cs == null ? ok.cs == null : this.cs.equals(ok.cs);
            }
            return false;
        }

        @Override
        public String toString() {
            return this.cs == null ? "<null>" : cs.toString();   //NOI18N
        }
    }

    private static class EncodingRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value == null) {
                return super.getListCellRendererComponent(list, "", index, isSelected, cellHasFocus);   //NOI18N
            } else {
                assert value instanceof EncodingKey;
                return super.getListCellRendererComponent(list, ((EncodingKey) value).getDisplayName(), index, isSelected, cellHasFocus);
            }
        }
    }
}