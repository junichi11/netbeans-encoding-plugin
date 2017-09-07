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
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.awt.StatusLineElementProvider;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 *
 * @author junichi11
 */
@ServiceProvider(service = StatusLineElementProvider.class)
public class EncodingStatusLineElementProvider implements StatusLineElementProvider {

    private static final JLabel ENCODING_LABEL = new EncodingLabel();
    private static final Component COMPONENT = panelWithSeparator(ENCODING_LABEL);
    private static final Collection<? extends Charset> CHARSETS = Charset.availableCharsets().values();

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

    @CheckForNull
    private static FileObject getfocusedFileObject() {
        JTextComponent component = EditorRegistry.focusedComponent();
        if (component == null) {
            return null;
        }

        Document document = component.getDocument();
        if (document == null) {
            return null;
        }

        return NbEditorUtilities.getFileObject(document);
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
        private static final long serialVersionUID = 7553842743917776222L;

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            updateEncoding();
        }

        private void updateEncoding() {
            assert EventQueue.isDispatchThread();
            FileObject fileObject = getfocusedFileObject();
            if (fileObject != null) {
                Charset encoding = getEncoding(fileObject);
                this.setText(String.format(" %s ", encoding.displayName())); // NOI18N
                this.setIcon(ImageUtilities.loadImageIcon(ENCODING_LIST_ICON_16, false));
                return;
            }
            this.setText("    "); // NOI18N
            this.setIcon(null);
        }

        private Charset getEncoding(FileObject fileObject) {
            return EncodingFinder.find(fileObject);
        }

    }

    private static class EncodingListSelectionListener implements ListSelectionListener {

        private final JList<String> encodingList;
        private final Popup popup;

        public EncodingListSelectionListener(JList<String> encodingList, Popup popup) {
            this.encodingList = encodingList;
            this.popup = popup;
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            popup.hide();
            encodingList.removeListSelectionListener(this);

            FileObject fileObject = getfocusedFileObject();
            if (fileObject == null) {
                return;
            }

            // same encoding?
            Charset encoding = EncodingFinder.find(fileObject);
            String currentEncoding = encoding.name();
            String selectedEncoding = encodingList.getSelectedValue();
            // #1 encoding is empty when snippet is inserted with palette
            if (selectedEncoding.equals(currentEncoding) || selectedEncoding.isEmpty()) {
                return;
            }

            // set encoding to attribute, reopen file
            try {
                fileObject.setAttribute(OpenInEncodingQueryImpl.ENCODING, selectedEncoding);
                final DataObject dobj = DataObject.find(fileObject);

                reopen(dobj);

            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        private void reopen(DataObject dobj) throws InterruptedException {
            close(dobj);

            // XXX java.lang.AssertionError is occurred
            Thread.sleep(200);

            open(dobj);
        }

        private void close(DataObject dobj) {
            CloseCookie cc = dobj.getLookup().lookup(CloseCookie.class);
            EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);

            if (cc != null) {
                cc.close();
            }
            if (ec != null) {
                ec.close();
            }
        }

        private void open(DataObject dobj) {
            OpenCookie oc = dobj.getLookup().lookup(OpenCookie.class);
            if (oc != null) {
                oc.open();
            }
        }
    }

    private static class EncodingLabelMouseAdapter extends MouseAdapter {

        public EncodingLabelMouseAdapter() {
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            // create popup components
            final JList<String> encodingList = createPopupList();
            final JScrollPane encodingScrollPane = createPopupScrollPane(encodingList);

            FileObject fileObject = getfocusedFileObject();
            if (fileObject == null) {
                return;
            }

            // set current encoding
            Charset charset = EncodingFinder.find(fileObject);
            encodingList.setSelectedValue(charset.name(), true);

            Popup popup = getPopup(encodingScrollPane);
            if (popup == null) {
                return;
            }

            // add listener
            final EncodingListSelectionListener encodingListSelectionListener = new EncodingListSelectionListener(encodingList, popup);
            encodingList.addListSelectionListener(encodingListSelectionListener);

            // hide popup
            final AWTEventListener eventListener = new AWTEventListener() {
                @Override
                public void eventDispatched(AWTEvent event) {
                    if (event instanceof MouseEvent && ((MouseEvent) event).getClickCount() > 0) {
                        Object source = event.getSource();
                        if (source != encodingScrollPane.getVerticalScrollBar()) {
                            popup.hide();
                            Toolkit.getDefaultToolkit().removeAWTEventListener(this);
                            if (source != encodingList) {
                                encodingList.removeListSelectionListener(encodingListSelectionListener);
                            }
                        }
                    }
                }
            };
            Toolkit.getDefaultToolkit().addAWTEventListener(eventListener, AWTEvent.MOUSE_EVENT_MASK);

            popup.show();
        }

        private JList<String> createPopupList() {
            DefaultListModel<String> encodingListModel = new DefaultListModel<>();
            encodingListModel.addElement(""); // NOI18N
            CHARSETS.forEach((charset) -> {
                encodingListModel.addElement(charset.name());
            });
            return new JList<>(encodingListModel);
        }

        private JScrollPane createPopupScrollPane(JList<String> popupList) {
            JScrollPane encodingScrollPane = new JScrollPane(popupList);

            // #17 set preferred size
            int preferredWidth = encodingScrollPane.getPreferredSize().width;
            int preferredHeight = WindowManager.getDefault().getMainWindow().getSize().height / 3;
            encodingScrollPane.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
            return encodingScrollPane;
        }

        private Popup getPopup(final JScrollPane encodingScrollPane) throws IllegalArgumentException {
            Point labelStart = ENCODING_LABEL.getLocationOnScreen();
            int x = Math.min(labelStart.x, labelStart.x + ENCODING_LABEL.getSize().width - encodingScrollPane.getPreferredSize().width);
            int y = labelStart.y - encodingScrollPane.getPreferredSize().height;
            return PopupFactory.getSharedInstance().getPopup(ENCODING_LABEL, encodingScrollPane, x, y);
        }
    }
}
