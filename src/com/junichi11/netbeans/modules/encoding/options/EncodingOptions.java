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
package com.junichi11.netbeans.modules.encoding.options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author junichi11
 */
public final class EncodingOptions {

    private static final int LAST_SELECTED_ENCODINGS_DEFAULT_MAX_SIZE = 5;
    private static final EncodingOptions INSTANCE = new EncodingOptions();
    private static final String ENCODING = "encoding"; // NOI18N
    private static final String LAST_SELECTED_ENCODINGS = "last.selected.encodings"; // NOI18N
    private static final String LAST_SELECTED_ENCODINGS_MAX_SIZE = "last.selected.encodings.max.size"; // NOI18N
    private static final String DELIMITER = "|"; // NOI18N

    public static EncodingOptions getInstance() {
        return INSTANCE;
    }

    private EncodingOptions() {
    }

    public int getLastSelectedEncodingsMaxSize() {
        return getPreferences().getInt(LAST_SELECTED_ENCODINGS_MAX_SIZE, LAST_SELECTED_ENCODINGS_DEFAULT_MAX_SIZE);
    }

    public void setLastSelectedEncodingsMazSize(int max) {
        getPreferences().putInt(LAST_SELECTED_ENCODINGS_MAX_SIZE, max);
    }

    public List<String> getLastSelectedEncodings() {
        String encodingString = getPreferences().get(LAST_SELECTED_ENCODINGS, ""); // NOI18N
        if (encodingString.isEmpty()) {
            return Collections.emptyList();
        }
        String[] encodings = encodingString.split("\\" + DELIMITER); // NOI18N
        return Arrays.asList(encodings);
    }

    public void setLastSelectedEncodings(String encoding) {
        List<String> selectedEncodings = new ArrayList<>(getLastSelectedEncodings());
        if (selectedEncodings.contains(encoding)) {
            selectedEncodings.remove(encoding);
            selectedEncodings.add(0, encoding);
        } else {
            if (selectedEncodings.size() >= getLastSelectedEncodingsMaxSize()) {
                selectedEncodings.remove(getLastSelectedEncodingsMaxSize() - 1);
            }
            selectedEncodings.add(0, encoding);
        }
        String join = String.join(DELIMITER, selectedEncodings);
        getPreferences().put(LAST_SELECTED_ENCODINGS, join);
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(EncodingOptions.class).node(ENCODING);
    }
}
