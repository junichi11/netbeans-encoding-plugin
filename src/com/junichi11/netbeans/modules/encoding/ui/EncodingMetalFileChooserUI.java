/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.junichi11.netbeans.modules.encoding.actions.EncodingAccessories;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.nio.charset.Charset;
import javax.swing.Box;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.plaf.metal.MetalFileChooserUI;

/**
 *
 * @author junichi11
 */
public class EncodingMetalFileChooserUI extends MetalFileChooserUI {

    private EncodingAccessories acc = new EncodingAccessories();

    public EncodingMetalFileChooserUI(JFileChooser filechooser) {
        super(filechooser);
    }

    @Override
    public void installComponents(JFileChooser fileChooser) {
        super.installComponents(fileChooser);
        JPanel bottomPanel = getBottomPanel();
        Dimension preferredSize = getPreferredJLabelSize(bottomPanel);
        if (preferredSize != null) {
            acc = new EncodingAccessories(preferredSize);
        }
        bottomPanel.add(Box.createRigidArea(new Dimension(1, 5)), 3);
        bottomPanel.add(acc, 4);
    }

    void setDefaultEncoding(Charset defaultEncoding) {
        acc.setEncoding(defaultEncoding);
    }

    public Charset getEncoding() {
        return acc.getEncoding();
    }

    private Dimension getPreferredJLabelSize(Container container) {
        Component[] components = container.getComponents();
        for (Component component : components) {
            if (component instanceof JLabel) {
                return component.getPreferredSize();
            }
            if (component instanceof Container) {
                Dimension preferredDimension = getPreferredJLabelSize((Container) component);
                if (preferredDimension != null) {
                    return preferredDimension;
                }
            }
        }
        return null;
    }
}
