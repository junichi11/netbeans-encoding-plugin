/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package com.junichi11.netbeans.modules.encoding;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
@ServiceProvider(service = FileEncodingQueryImplementation.class, position = 10)
public final class OpenInEncodingQueryImpl
        extends FileEncodingQueryImplementation {

    /**
     * Name of the FileObject atttribute that holds the encoding name.
     */
    public static final String ENCODING = "encoding"; //NOI18N

    @Override
    public Charset getEncoding(final FileObject file) {
        assert file != null;
        final Object encodingName = file.getAttribute(ENCODING);
        try {
            return encodingName instanceof String
                    ? Charset.forName((String) encodingName)
                    : null;
        } catch (IllegalCharsetNameException e) {
            Logger.getGlobal().log(Level.WARNING,
                    "File has an invalid charset name");
          //TODO internationalize warning
        }
        return null;
    }

}
