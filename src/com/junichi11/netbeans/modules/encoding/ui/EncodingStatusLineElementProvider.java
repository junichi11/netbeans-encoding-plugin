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

import com.junichi11.netbeans.modules.encoding.OpenInEncodingQueryImpl;
import com.junichi11.netbeans.modules.encoding.options.EncodingOptions;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.queries.FileEncodingQuery;
import org.openide.awt.StatusLineElementProvider;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author junichi11
 */
@ServiceProvider(service = StatusLineElementProvider.class)
public class EncodingStatusLineElementProvider implements StatusLineElementProvider {

    private static final JLabel ENCODING_LABEL = EncodingLabel.create();
    private static final Component COMPONENT = panelWithSeparator(ENCODING_LABEL);
    private static final Logger LOGGER = Logger.getLogger(EncodingStatusLineElementProvider.class.getName());
    private static volatile boolean SHOWING_POPUP;

    static {
        // icon position: right
        ENCODING_LABEL.setHorizontalTextPosition(SwingConstants.LEFT);

        // add listeners
        EditorRegistry.addPropertyChangeListener((PropertyChangeListener) ENCODING_LABEL);
        ENCODING_LABEL.addMouseListener(new EncodingLabelMouseAdapter());
    }

    @Override
    public Component getStatusLineElement() {
        return COMPONENT;
    }

    /**
     * Create Component(JPanel) and add separator and JLabel to it.
     *
     * @param label JLabel
     * @return panel
     */
    private static Component panelWithSeparator(JLabel label) {
        // create separator
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL) {
            private static final long serialVersionUID = -6385848933295984637L;

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(3, 3);
            }
        };
        separator.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        // create panel
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(separator, BorderLayout.WEST);
        panel.add(label, BorderLayout.EAST);
        return panel;
    }

    /**
     * Change the encoding.
     * <b>NOTE:</b>The file is reopened.
     *
     * @param selectedEncoding the selected encoding
     */
    @NbBundle.Messages("EncodingStatusLineElementProvider.message.modified.file=Please save the file once.")
    private static void changeEncoding(String selectedEncoding) {
        if (selectedEncoding == null) {
            UiUtils.requestFocusLastFocusedComponent();
            LOGGER.log(Level.WARNING, "The selected encoding is null."); // NOI18N
            return;
        }

        FileObject fileObject = UiUtils.getLastFocusedFileObject();
        if (fileObject == null) {
            return;
        }

        // check whether the file is modified
        try {
            DataObject dataObject = DataObject.find(fileObject);
            if (dataObject.isModified()) {
                UiUtils.requestFocusLastFocusedComponent();
                UiUtils.showErrorMessage(Bundle.EncodingStatusLineElementProvider_message_modified_file());
                return;
            }
        } catch (DataObjectNotFoundException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        // same encoding?
        Charset encoding = EncodingFinder.find(fileObject);
        String currentEncoding = encoding.name();

        // #1 encoding is empty when snippet is inserted with palette
        if (selectedEncoding.equals(currentEncoding) || selectedEncoding.isEmpty()) {
            UiUtils.requestFocusLastFocusedComponent();
            return;
        }

        // set encoding to attribute, reload a document
        try {
            fileObject.setAttribute(OpenInEncodingQueryImpl.ENCODING, selectedEncoding);
            final DataObject dobj = DataObject.find(fileObject);

            // don't show dialog
            // if a cancel button is clicked, text can be cleared
            Field field = DataEditorSupport.class.getDeclaredField("warnedEncodingFiles"); // NOI18N
            field.setAccessible(true);
            Set<FileObject> fileObjects = (Set<FileObject>) field.get(null);
            fileObjects.add(fileObject);

            // reload editor
            EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
            Method notifyModified = CloneableEditorSupport.class.getDeclaredMethod("checkReload", JEditorPane[].class, boolean.class); // NOI18N
            notifyModified.setAccessible(true);
            JEditorPane[] focusedPanes = new JEditorPane[]{};
            JTextComponent component = EditorRegistry.lastFocusedComponent();
            if (component instanceof JEditorPane) {
                focusedPanes = new JEditorPane[]{(JEditorPane) component};
            }
            notifyModified.invoke(ec, focusedPanes, true);

            // save selected encoding to options
            EncodingOptions.getInstance().setLastSelectedEncodings(selectedEncoding);
            UiUtils.requestFocusLastFocusedComponent();
        } catch (IOException
                | IllegalAccessException
                | IllegalArgumentException
                | InvocationTargetException
                | NoSuchMethodException
                | SecurityException
                | NoSuchFieldException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
    }

    //~ inner classes
    private static final class EncodingFinder {

        private EncodingFinder() {
        }

        private static final OpenInEncodingQueryImpl QUERY_IMPL = new OpenInEncodingQueryImpl();

        private static Charset find(FileObject fileObject) {
            Charset encoding = QUERY_IMPL.getEncoding(fileObject);
            if (encoding == null) {
                encoding = FileEncodingQuery.getEncoding(fileObject);
            }
            return encoding;
        }
    }

    private static class EncodingLabel extends JLabel implements PropertyChangeListener {

        @StaticResource
        public static final String ENCODING_LIST_ICON_16 = "com/junichi11/netbeans/modules/encoding/resources/encoding_16.png"; // NOI18N
        private static final long serialVersionUID = -5078620344180235634L;

        public static EncodingLabel create() {
            EncodingLabel label = new EncodingLabel();
            label.setText("        ");
            label.setIcon(ImageUtilities.loadImageIcon(ENCODING_LIST_ICON_16, false));
            return label;
        }

        private EncodingLabel() {
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            updateEncoding();
        }

        private void updateEncoding() {
            assert EventQueue.isDispatchThread();
            this.setPreferredSize(null);
            FileObject fileObject;
            if (SHOWING_POPUP) {
                fileObject = UiUtils.getLastFocusedFileObject();
            } else {
                fileObject = UiUtils.getFocusedFileObject();
            }
            if (fileObject != null) {
                Charset encoding = getEncoding(fileObject);
                this.setText(String.format(" %s ", encoding.displayName())); // NOI18N
                this.setIcon(ImageUtilities.loadImageIcon(ENCODING_LIST_ICON_16, false));
                return;
            }
            Dimension size = this.getSize();
            this.setPreferredSize(size);
            this.setText(""); // NOI18N
            this.setIcon(null);
        }

        private Charset getEncoding(FileObject fileObject) {
            return EncodingFinder.find(fileObject);
        }

    }

    private static class EncodingLabelMouseAdapter extends MouseAdapter {

        public EncodingLabelMouseAdapter() {
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            FileObject fileObject = UiUtils.getFocusedFileObject();
            if (fileObject == null) {
                return;
            }

            // set current encoding
            Charset charset = EncodingFinder.find(fileObject);
            final EncodingPanel encodingPanel = new EncodingPanel(charset);
            Popup popup = getPopup(encodingPanel);
            if (popup == null) {
                return;
            }

            // forcusable
            setForcusableWindowState(encodingPanel, true);

            // add listener
            final EncodingListMouseAdapter encodingListMouseAdapter = new EncodingListMouseAdapter(encodingPanel, popup);
            encodingPanel.addEncodingListMouseListener(encodingListMouseAdapter);

            // hide popup
            final AWTEventListener eventListener = new AWTEventListener() {
                @Override
                public void eventDispatched(AWTEvent event) {
                    if (isHidable(event)) {
                        setForcusableWindowState(encodingPanel, false);
                        Object source = event.getSource();
                        popup.hide();
                        SHOWING_POPUP = false;
                        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
                        encodingPanel.shutdown();

                        if (!UiUtils.isMouseClicked(event)
                                || !encodingPanel.isEncodingList(source)) {
                            encodingPanel.removeEncodingListMouseListener(encodingListMouseAdapter);
                        }

                        if (UiUtils.isEscKey(event) || UiUtils.isEnterKey(event)) {
                            if (encodingPanel.isEncodingFilterField(source)
                                    || encodingPanel.isEncodingList(source)) {
                                UiUtils.requestFocusLastFocusedComponent();
                            }
                        }

                        if (!encodingPanel.isEncodingList(source)
                                && !encodingPanel.isEncodingFilterField(source)) {
                            return;
                        }

                        if (UiUtils.isEnterKey(event)) {
                            changeEncoding(encodingPanel.getSelectedEncoding());
                        }
                    }
                }

                private boolean isHidable(AWTEvent event) {
                    Object source = event.getSource();
                    if (UiUtils.isMouseClicked(event)
                            && !UiUtils.isComponentOfClass(EncodingPanel.class, source)
                            && source != ENCODING_LABEL) {
                        return true;

                    }
                    return UiUtils.isEscKey(event) || UiUtils.isEnterKey(event);
                }

            };
            Toolkit.getDefaultToolkit().addAWTEventListener(eventListener, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);

            popup.show();
            SHOWING_POPUP = true;
            encodingPanel.requrestForcusEncodingList();
        }

        private Popup getPopup(final JPanel encodingPanel) throws IllegalArgumentException {
            Point labelStart = ENCODING_LABEL.getLocationOnScreen();
            int x = Math.min(labelStart.x, labelStart.x + ENCODING_LABEL.getSize().width - encodingPanel.getPreferredSize().width);
            int y = labelStart.y - encodingPanel.getPreferredSize().height;
            return PopupFactory.getSharedInstance().getPopup(ENCODING_LABEL, encodingPanel, x, y);
        }
    }

    private static void setForcusableWindowState(Component encodingPanel, boolean focusableWindowState) {
        Window window = SwingUtilities.windowForComponent(encodingPanel);
        if (window != null) {
            window.setFocusableWindowState(focusableWindowState);
        }
    }

    private static class EncodingListMouseAdapter extends MouseAdapter {

        private final EncodingPanel encodingPanel;
        private final Popup popup;

        public EncodingListMouseAdapter(EncodingPanel encodingPanel, Popup popup) {
            this.encodingPanel = encodingPanel;
            this.popup = popup;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            setForcusableWindowState(encodingPanel, false);
            popup.hide();
            SHOWING_POPUP = false;
            encodingPanel.removeEncodingListMouseListener(this);
            changeEncoding(encodingPanel.getSelectedEncoding());
        }

    }
}
