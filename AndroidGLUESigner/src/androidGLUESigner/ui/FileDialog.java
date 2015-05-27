package androidGLUESigner.ui;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Environment;
import android.util.Log;
import androidGLUESigner.ui.ListenerList.FireHandler;
import androidGLUESigner.ui.R;
/**
 * Displays a series of dialogs enabling the user to navigate through the file system
 * @author roland
 *
 */
public class FileDialog {
	//fields to keep track of place in filesystem
                private static final String PARENT_DIR = "..";
                private final String TAG = getClass().getName();
                private String[] fileList;
                private File currentPath;
                private String title = "";
                public interface FileSelectedListener {
                    void fileSelected(File file) throws IOException;
                }
                public interface DirectorySelectedListener {
                    void directorySelected(File directory);
                }
                private ListenerList<FileSelectedListener> fileListenerList = new ListenerList<FileDialog.FileSelectedListener>();
                private ListenerList<DirectorySelectedListener> dirListenerList = new ListenerList<FileDialog.DirectorySelectedListener>();
                private final Activity activity;
                private boolean selectDirectoryOption;
                private String fileEndsWith;    

                /**
                 * Constructor
                 * @param activity context for dialog to be launched
                 * @param initialPath the initial path the dialog should display.
                 */
                public FileDialog(Activity activity, File path) {
                    this.activity = activity;
                    
                    if (!path.exists()) path = Environment.getExternalStorageDirectory();
                    loadFileList(path);
                }
                
                /**
                 * Expanded constructor with title
                  @param activity context for dialog to be launched
                 * @param initialPath the initial path the dialog should display.
                 * @param title the title for the dialog
                 */
                public FileDialog(Activity activity, File path, String title) {
                    this.activity = activity;
                    this.title = title;
                    if (!path.exists()) path = Environment.getExternalStorageDirectory();
                    loadFileList(path);
                }

                /**
                 * uses the dialog builder to create the dialog in such a way that its ready to display on the screen
                 * @return file dialog
                 */
                public Dialog createFileDialog() {
                    Dialog dialog = null;
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    if(title!=""){
                    	builder.setTitle(title);
                    }
                    
                    //builder.setTitle(currentPath.getPath());
                    if (selectDirectoryOption) {
                        builder.setPositiveButton(activity.getApplicationContext().getString(R.string.select_directory), new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG, currentPath.getPath());
                                try {
									fireDirectorySelectedEvent(currentPath);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
                            }

                        });
                    }

                    builder.setItems(fileList, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String fileChosen = fileList[which];
                            File chosenFile = getChosenFile(fileChosen);
                            if (chosenFile.isDirectory()) {
                                loadFileList(chosenFile);
                                dialog.cancel();
                                dialog.dismiss();
                                showDialog();
                            } else
								try {
									fireFileSelectedEvent(chosenFile);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
                        }
                    });

                    dialog = builder.show();
                    return dialog;
                }

                /**
                 * add a listener for callback event fileselected
                 * @param listener the listener object. (observer pattern)
                 */
                public void addFileListener(FileSelectedListener listener) {
                    fileListenerList.add(listener);
                }
                
                /**
                 * remove the said listener from the observer list
                 * @param listener the listener to be removed
                 */

                public void removeFileListener(FileSelectedListener listener) {
                    fileListenerList.remove(listener);
                }
                
                /**
                 * sets the dialog up to  select directories instead of files
                 * @param selectDirectoryOption
                 */

                public void setSelectDirectoryOption(boolean selectDirectoryOption) {
                    this.selectDirectoryOption = selectDirectoryOption;
                }

                /**
                 * sets up the callback when the user selects a directory.
                 * 
                 * @param listener the observer object.
                 */
                public void addDirectoryListener(DirectorySelectedListener listener) {
                    dirListenerList.add(listener);
                }

                /**
                 * removes the observer for the dialog
                 * @param listener the observer object to be removed
                 */
                public void removeDirectoryListener(DirectorySelectedListener listener) {
                    dirListenerList.remove(listener);
                }

                /**
                 * Show file dialog
                 */
                public void showDialog() {
                    createFileDialog().show();
                }
                
                /**
                 * private method to adjust GUI for file selection
                 * @param file the file selected
                 * @throws IOException
                 */

                private void fireFileSelectedEvent(final File file) throws IOException {
                    fileListenerList.fireEvent(new FireHandler<FileDialog.FileSelectedListener>() {
                        public void fireEvent(FileSelectedListener listener) throws IOException {
                            listener.fileSelected(file);
                        }
                    });
                }

                /**
                 * Forwards directory selection event
                 * @param directory the directory selected
                 * @throws IOException
                 */
                private void fireDirectorySelectedEvent(final File directory) throws IOException {
                    dirListenerList.fireEvent(new FireHandler<FileDialog.DirectorySelectedListener>() {
                        public void fireEvent(DirectorySelectedListener listener) {
                            listener.directorySelected(directory);
                        }
                    });
                }

                /**
                 * Provides the GUI list onject with data
                 * @param path the path to look for files in.
                 */
                private void loadFileList(File path) {
                    this.currentPath = path;
                    List<String> r = new ArrayList<String>();
                    if (path.exists()) {
                        if (path.getParentFile() != null) r.add(PARENT_DIR);
                        FilenameFilter filter = new FilenameFilter() {
                            public boolean accept(File dir, String filename) {
                                File sel = new File(dir, filename);
                                if (!sel.canRead()) return false;
                                if (selectDirectoryOption) return sel.isDirectory();
                                else {
                                    boolean endsWith = fileEndsWith != null ? filename.toLowerCase().endsWith(fileEndsWith) : true;
                                    return endsWith || sel.isDirectory();
                                }
                            }
                        };
                        String[] fileList1 = path.list(filter);
                        for (String file : fileList1) {
                            r.add(file);
                        }
                    }
                    fileList = (String[]) r.toArray(new String[]{});
                }

                /**
                 * Reacts to selection and forwards a file object to th ecallback
                 * @param fileChosen the file chosen.
                 * @return
                 */
                private File getChosenFile(String fileChosen) {
                    if (fileChosen.equals(PARENT_DIR)) return currentPath.getParentFile();
                    else return new File(currentPath, fileChosen);
                }

                /**
                 * Keeps track of filter function for file listing
                 * @param fileEndsWith the filtered ending for files
                 */
                public void setFileEndsWith(String fileEndsWith) {
                    this.fileEndsWith = fileEndsWith != null ? fileEndsWith.toLowerCase() : fileEndsWith;
                }
             }

    /**
     * list of observers
     * @author roland
     *
     * @param <L>
     */
        class ListenerList<L> {
            private List<L> listenerList = new ArrayList<L>();

            public interface FireHandler<L> {
                void fireEvent(L listener) throws IOException;
            }

            public void add(L listener) {
                listenerList.add(listener);
            }

            public void fireEvent(FireHandler<L> fireHandler) throws IOException {
                List<L> copy = new ArrayList<L>(listenerList);
                for (L l : copy) {
                    fireHandler.fireEvent(l);
                }
            }

            public void remove(L listener) {
                listenerList.remove(listener);
            }

            public List<L> getListenerList() {
                return listenerList;
            }
        }

