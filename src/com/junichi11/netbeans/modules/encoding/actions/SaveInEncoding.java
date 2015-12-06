package com.junichi11.netbeans.modules.encoding.actions;

import com.junichi11.netbeans.modules.encoding.OpenInEncodingQueryImpl;
import java.io.File;
import java.io.FileNotFoundException;
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

/**
 * Action that saves in the specified encoding the selected file.
 */
@ActionID(id = "com.junichi11.netbeans.modules.encoding.actions.SaveInEncoding",
        category = "File")
@ActionRegistration(lazy = false, displayName = "Save In Encoding...")
@ActionReference(path = "Menu/File", position = 1725)
public final class SaveInEncoding extends CookieAction {

    /**
     * number to verify correct serialization/deserialization
     */
    private static final long serialVersionUID = 1L;
    /**
     * Size of the buffer used to read file contents before saving.
     */
    private static final int BUFFERSIZE = 512;

    /**
     * Provides behaviour for the save file in encoding action.
     *
     * @param activatedNodes project nodes in wich it will act
     */
    @Override
    protected void performAction(final Node[] activatedNodes) {
        final DataObject dataObject = activatedNodes[0].getLookup()
                .lookup(DataObject.class);
        FileObject fileObject = dataObject.getPrimaryFile();
        File f = FileUtil.toFile(fileObject);
        if (f == null) {
            f = FileUtil.normalizeFile(new File(new File(
                    System.getProperty("user.name")), fileObject.getNameExt()));
        }
        final JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(null);
        chooser.setDialogTitle(NbBundle.getMessage(
                OpenInEncoding.class, "TXT_SaveFile"));
        chooser.setApproveButtonText(NbBundle.getMessage(
                OpenInEncoding.class, "CTL_Save"));
        chooser.setApproveButtonMnemonic(NbBundle.getMessage(
                OpenInEncoding.class, "MNE_Save").charAt(0));
        chooser.setSelectedFile(f);
        final EncodingAccessories acc = new EncodingAccessories();
        acc.setEncoding(null); //Always suggest the default encoding
        chooser.setAccessory(acc);
        if (chooser.showSaveDialog(
                WindowManager.getDefault().getMainWindow())
                == JFileChooser.APPROVE_OPTION) {
            final Charset charset = acc.getEncoding();
            final String encodingName = (charset == null
                    ? null
                    : charset.name());
            OpenInEncoding.setLastFolder(chooser.getCurrentDirectory());
            final File file = FileUtil.normalizeFile(chooser.getSelectedFile());
            if (f.equals(file) && activatedNodes[0].getLookup()
                    .lookup(SaveCookie.class) != null) {
                try {
                    fileObject.setAttribute(OpenInEncodingQueryImpl.ENCODING, encodingName);
                    final SaveCookie sc = activatedNodes[0].getLookup()
                            .lookup(SaveCookie.class);
                    sc.save();
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            } else {
                try {
                    //Todo: Perf, don't load whole data into mem.
                    final StringBuilder stringBuilder = new StringBuilder();
                    readFile(stringBuilder, fileObject);
                    fileObject = FileUtil.createData(file);
                    fileObject.setAttribute(OpenInEncodingQueryImpl.ENCODING,
                            encodingName);
                    final FileLock lock = fileObject.lock();
                    writeFileToDisk(lock, fileObject, stringBuilder);
                    final DataObject newDobj = DataObject.find(fileObject);
                    final OpenCookie cookie = newDobj.getLookup()
                            .lookup(OpenCookie.class);
                    setEditorCookie(dataObject, cookie);

                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        }
    }

    /**
     * Provides cookie mode policy.
     *
     * @return Cookie Action Mode
     * @see CookieAction
     */
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

    /**
     * Method that handles icons for this action. For now no icon is shown.
     */
    @Override
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE);
    }

    /**
     * Displays that the help is not avalible.
     *
     * @return help context (none yet)
     */
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * This action is not asynchronous.
     *
     * @return if the method can be run asynchronously
     */
    @Override
    protected boolean asynchronous() {
        return false;
    }

    /**
     * Writes File to disk.
     */
    private void writeFileToDisk(FileLock lock, FileObject fo, StringBuilder sb) {
        try {
            final Writer out = new OutputStreamWriter(fo.getOutputStream(lock), FileEncodingQuery.getEncoding(fo));
            try {
                out.write(sb.toString());
            } finally {
                out.close();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            lock.releaseLock();
        }
    }

    /**
     * Reads File from disk.
     */
    private void readFile(final StringBuilder sb, FileObject fo) {
        Reader in = null;
        try {
            final char[] buffer = new char[BUFFERSIZE];
            in = new InputStreamReader(fo.getInputStream(),
                    FileEncodingQuery.getEncoding(fo));

            int len;
            while ((len = in.read(buffer)) > 0) {
                sb.append(buffer, 0, len);
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    /**
     * Writes cookie to some project data object.
     */
    private void setEditorCookie(final DataObject dataObject, OpenCookie oc) {
        if (oc != null) {
            EditorCookie cookie = dataObject.getLookup()
                    .lookup(EditorCookie.class);
            if (cookie != null) {
                cookie.close();
            }
            oc.open();
        }
    }
}
