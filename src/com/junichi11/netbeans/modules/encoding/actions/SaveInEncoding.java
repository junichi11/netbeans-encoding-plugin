package com.junichi11.netbeans.modules.encoding.actions;

import com.junichi11.netbeans.modules.encoding.OpenInEncodingQueryImpl;
import com.junichi11.netbeans.modules.encoding.ui.EncodingJFileChooser;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import javax.swing.JFileChooser;
import org.netbeans.api.queries.FileEncodingQuery;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.windows.WindowManager;

@ActionID(id = "com.junichi11.netbeans.modules.encoding.actions.SaveInEncoding", category = "File")
@ActionRegistration(lazy = false, displayName = "Save In Encoding...")
@ActionReference(path = "Menu/File", position = 1725)
public final class SaveInEncoding extends CookieAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        final DataObject dataObject = activatedNodes[0].getLookup().lookup(DataObject.class);
        FileObject fo = dataObject.getPrimaryFile();
        File f = FileUtil.toFile(fo);
        if (f == null) {
            f = FileUtil.normalizeFile(new File(new File(System.getProperty("user.name")), fo.getNameExt()));
        }
        final EncodingJFileChooser chooser = new EncodingJFileChooser();
        chooser.setCurrentDirectory(null);
        chooser.setDialogTitle(NbBundle.getMessage(OpenInEncoding.class, "TXT_SaveFile"));
        chooser.setApproveButtonText(NbBundle.getMessage(OpenInEncoding.class, "CTL_Save"));
        chooser.setApproveButtonMnemonic(NbBundle.getMessage(OpenInEncoding.class, "MNE_Save").charAt(0));
        chooser.setSelectedFile(f);
        chooser.setEncoding(null); //Always suggest the default encoding
        if (chooser.showSaveDialog(WindowManager.getDefault().getMainWindow()) == JFileChooser.APPROVE_OPTION) {
            final Charset charset = chooser.getEncoding();
            final String encodingName = (charset == null ? null : charset.name());
            OpenInEncoding.lastFolder = chooser.getCurrentDirectory();
            final File file = FileUtil.normalizeFile(chooser.getSelectedFile());
            if (f.equals(file) && activatedNodes[0].getLookup().lookup(SaveCookie.class) != null) {
                try {
                    fo.setAttribute(OpenInEncodingQueryImpl.ENCODING, encodingName);
                    final SaveCookie sc = activatedNodes[0].getLookup().lookup(SaveCookie.class);
                    sc.save();
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            } else {
                try {

                    //Todo: Perf, don't load whole data into mem.
                    final StringBuilder sb = new StringBuilder();
                    final char[] buffer = new char[512];
                    final Reader in = new InputStreamReader(fo.getInputStream(), FileEncodingQuery.getEncoding(fo));
                    try {
                        int len;
                        while ((len = in.read(buffer)) > 0) {
                            sb.append(buffer, 0, len);
                        }
                    } finally {
                        in.close();
                    }
                    fo = FileUtil.createData(file);
                    fo.setAttribute(OpenInEncodingQueryImpl.ENCODING, encodingName);
                    final FileLock lock = fo.lock();
                    try {
                        final Writer out = new OutputStreamWriter(fo.getOutputStream(lock), FileEncodingQuery.getEncoding(fo));
                        try {
                            out.write(sb.toString());
                        } finally {
                            out.close();
                        }
                    } finally {
                        lock.releaseLock();
                    }
                    final DataObject newDobj = DataObject.find(fo);
                    final OpenCookie oc = newDobj.getLookup().lookup(OpenCookie.class);
                    if (oc != null) {
                        EditorCookie ec = dataObject.getLookup().lookup(EditorCookie.class);
                        if (ec != null) {
                            ec.close();
                        }
                        oc.open();
                    }
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        }
    }

    @Override
    protected int mode() {
        return CookieAction.MODE_ALL;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(SaveInEncoding.class, "CTL_SaveInEncoding");
    }

    @Override
    protected Class[] cookieClasses() {
        return new Class[]{DataObject.class};
    }

    @Override
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
