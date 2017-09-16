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

import java.awt.AWTEvent;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 *
 * @author junichi11
 */
public final class UiUtils {

    private UiUtils() {
    }

    @CheckForNull
    public static FileObject getLastFocusedFileObject() {
        return getFocusedFileObject(true);
    }

    @CheckForNull
    public static FileObject getFocusedFileObject() {
        return getFocusedFileObject(false);
    }

    @CheckForNull
    private static FileObject getFocusedFileObject(boolean last) {
        JTextComponent component;
        if (last) {
            component = EditorRegistry.lastFocusedComponent();
        } else {
            component = EditorRegistry.focusedComponent();
        }
        if (component == null) {
            return null;
        }

        Document document = component.getDocument();
        if (document == null) {
            return null;
        }

        return NbEditorUtilities.getFileObject(document);
    }

    public static void reopen(DataObject dobj) throws InterruptedException {
        close(dobj);

        // XXX java.lang.AssertionError is occurred
        Thread.sleep(200);

        open(dobj);
    }

    public static void close(DataObject dobj) {
        CloseCookie cc = dobj.getLookup().lookup(CloseCookie.class);
        EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);

        if (cc != null) {
            cc.close();
        }
        if (ec != null) {
            ec.close();
        }
    }

    public static void open(DataObject dobj) {
        OpenCookie oc = dobj.getLookup().lookup(OpenCookie.class);
        if (oc != null) {
            oc.open();
        }
    }

    public static boolean isMouseClicked(AWTEvent event) {
        return event instanceof MouseEvent && ((MouseEvent) event).getClickCount() > 0;
    }

    public static boolean isEscKey(AWTEvent event) {
        return event instanceof KeyEvent && ((KeyEvent) event).getKeyCode() == KeyEvent.VK_ESCAPE;
    }

    public static boolean isEnterKey(AWTEvent event) {
        return event instanceof KeyEvent && ((KeyEvent) event).getKeyCode() == KeyEvent.VK_ENTER;
    }

    /**
     * Focus the last focused editor pane.
     */
    public static void requestFocusLastFocusedComponent() {
        JTextComponent lastFocusedComponent = EditorRegistry.lastFocusedComponent();
        if (lastFocusedComponent != null) {
            lastFocusedComponent.requestFocusInWindow();
        }
    }

    /**
     * Check whether the object is a component of the specific class.
     *
     * @param object the object
     * @return {@code true} if the object is a component of the specific class,
     * otherwise {@code false}
     */
    public static boolean isComponentOfClass(Class<?> clazz, Object object) {
        if (object instanceof JComponent) {
            JComponent component = (JComponent) object;
            Container parent = SwingUtilities.getAncestorOfClass(clazz, component);
            return parent != null;
        }
        return false;
    }
}
