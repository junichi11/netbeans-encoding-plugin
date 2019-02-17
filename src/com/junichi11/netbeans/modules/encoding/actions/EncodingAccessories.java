/*
 * EncodingAccessories.java
 *
 * Created on October 12, 2007, 3:22 PM
 */
package com.junichi11.netbeans.modules.encoding.actions;

import com.junichi11.netbeans.modules.encoding.options.EncodingOptions;
import java.awt.Component;
import java.awt.Dimension;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.event.ListDataListener;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Zezula
 */
public class EncodingAccessories extends javax.swing.JPanel {

    private static final long serialVersionUID = 9119652225263260409L;

    /**
     * Creates new form EncodingAccessories
     */
    public EncodingAccessories() {
        initComponents();
        this.encoding.setModel(new EncodingModel());
        this.encoding.setRenderer(new EncodingRenderer());
    }

    public EncodingAccessories(Dimension dimension) {
        this();
        encodingLabel.setPreferredSize(dimension);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        encodingLabel = new javax.swing.JLabel();
        encoding = new javax.swing.JComboBox();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        encodingLabel.setLabelFor(encoding);
        org.openide.awt.Mnemonics.setLocalizedText(encodingLabel, org.openide.util.NbBundle.getMessage(EncodingAccessories.class, "EncodingAccessories.encodingLabel.text")); // NOI18N
        add(encodingLabel);

        add(encoding);
    }// </editor-fold>//GEN-END:initComponents

    public Charset getEncoding() {
        return ((EncodingKey) this.encoding.getSelectedItem()).getCharset();
    }

    public void setEncoding(final Charset encoding) {
        EncodingKey key = (encoding == null ? EncodingKey.DEFAULT : new EncodingKey(encoding));
        this.encoding.setSelectedItem(key);
    }

    private static class EncodingModel implements ComboBoxModel {

        private Object selected;
        private List<EncodingKey> data;

        public EncodingModel() {
            final Collection<? extends Charset> acs = Charset.availableCharsets().values();
            final List<EncodingKey> _data = new ArrayList<>(acs.size() + 1);
            _data.add(EncodingKey.DEFAULT);
            List<String> lastSelectedEncodings = EncodingOptions.getInstance().getLastSelectedEncodings();
            lastSelectedEncodings.forEach(encoding -> {
                if (Charset.isSupported(encoding)) {
                    EncodingKey encodingKey = new EncodingKey(Charset.forName(encoding));
                    _data.add(encodingKey);
                }
            });
            acs.forEach(charset -> {
                if (!lastSelectedEncodings.contains(charset.name())) {
                    _data.add(new EncodingKey(charset));
                }
            });
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
            return this.cs == null ? NbBundle.getMessage(EncodingAccessories.class, "TXT_Default") : this.cs.displayName();
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
            return this.cs == null ? "<null>" : cs.toString();   // NOI18N
        }

    }

    private static class EncodingRenderer extends DefaultListCellRenderer {

        private static final long serialVersionUID = -1262247864555519110L;

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value == null) {
                return super.getListCellRendererComponent(list, "", index, isSelected, cellHasFocus); // NOI18N
            } else {
                assert value instanceof EncodingKey;
                return super.getListCellRendererComponent(list, ((EncodingKey) value).getDisplayName(), index, isSelected, cellHasFocus);
            }
        }

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox encoding;
    private javax.swing.JLabel encodingLabel;
    // End of variables declaration//GEN-END:variables

}
