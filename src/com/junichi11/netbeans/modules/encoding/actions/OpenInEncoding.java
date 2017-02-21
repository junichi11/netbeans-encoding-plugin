package com.junichi11.netbeans.modules.encoding.actions;

import com.junichi11.netbeans.modules.encoding.OpenInEncodingQueryImpl;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import javax.swing.JFileChooser;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.WindowManager;

/**
 * Action that opens in the specified encoding the selected file.
 */
@ActionID(id = "com.junichi11.netbeans.modules.encoding.actions.OpenInEncoding", category = "File")
@ActionRegistration(lazy = false, displayName = "Open in Encoding ...")
@ActionReference(path = "Menu/File", position = 950)
public final class OpenInEncoding extends CallableSystemAction {

    /**
     * Last opened folder
     */
    private static File lastFolder = new File(System.getProperty("user.home")); //NOI18N
    /**
     * Last encoding used
     */
    private static Charset lastEncoding = null;
    /**
     * number to verify correct serialization/deserialization
     */
    private static final long serialVersionUID = 3891880406655022866L;

    /**
     * Provides behaviour for the open file in encoding action
     */
    @Override
    public void performAction() {
        final JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(null);
        chooser.setDialogTitle(NbBundle.getMessage(OpenInEncoding.class, "TXT_OpenFile"));
        chooser.setApproveButtonText(NbBundle.getMessage(OpenInEncoding.class, "CTL_Open"));
        chooser.setApproveButtonMnemonic(NbBundle.getMessage(OpenInEncoding.class, "MNE_Open").charAt(0));
        chooser.setCurrentDirectory(lastFolder);
        final EncodingAccessories acc = new EncodingAccessories();
        acc.setEncoding(lastEncoding);
        chooser.setAccessory(acc);
        if (chooser.showOpenDialog(WindowManager.getDefault().getMainWindow()) == JFileChooser.APPROVE_OPTION) {
            final Charset charset = acc.getEncoding();
            lastEncoding = charset;
            lastFolder = chooser.getCurrentDirectory();
            final File file = FileUtil.normalizeFile(chooser.getSelectedFile());
            final FileObject fo = FileUtil.toFileObject(file);
            try {
                final String encodingName = (charset == null ? null : charset.name());
                fo.setAttribute(OpenInEncodingQueryImpl.ENCODING, encodingName);
                final DataObject dobj = DataObject.find(fo);
                OpenCookie oc = dobj.getLookup().lookup(OpenCookie.class);
                if (oc != null) {
                    oc.open();
                }
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
    }

    /**
     * Obtains last opened folder.
     *
     * @return folder wich open dialog should show by default
     */
    public static File getLastFolder() {
        return lastFolder;
    }

    /**
     * Configures last opened folder.
     *
     * @param lastFolder folder wich open dialog should show by default
     */
    public static void setLastFolder(File lastFolder) {
        OpenInEncoding.lastFolder = lastFolder;
    }

    /**
     * Gets the internationalized name of this action
     *
     * @return name of the action
     */
    @Override
    public String getName() {
        return NbBundle.getMessage(OpenInEncoding.class, "CTL_OpenInEncoding");
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
}
