package de.k3b.android.androFotoFinder;

import android.app.Activity;
import android.database.ContentObserver;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

import de.k3b.android.androFotoFinder.queries.FotoSql;
import de.k3b.android.util.MediaScanner;
import de.k3b.database.SelectedItems;

/**
 * Implements the array sepecific stuff that hopefully can be reused in other adapters, too
 */
public class AdapterArrayHelper {
    private final String mDebugPrefix;
    /** not null data comes from array instead from base implementation */
    private String[] mFullPhotoPaths = null;
    private final File mRootDir;

    public AdapterArrayHelper(Activity context, String fullPhotoPath, String debugContext) {
        this.mDebugPrefix = debugContext;
        mRootDir = MediaScanner.getDir(fullPhotoPath);
        reload(" ctor ");

        if (Global.mustRemoveNOMEDIAfromDB && (mRootDir != null) && (mFullPhotoPaths != null)) {
            String parentDirString = mRootDir.getAbsolutePath();
            FotoSql.execDeleteByPath(context, parentDirString);
        }
    }

    /** refreshLocal files from inital path
     * @param why*/
    public void reload(String why) {
        mFullPhotoPaths = mRootDir.list(MediaScanner.JPG_FILENAME_FILTER);
        if ((mFullPhotoPaths != null) && (mFullPhotoPaths.length == 0)) {
            mFullPhotoPaths = null;
            Log.i(Global.LOG_CONTEXT, mDebugPrefix + why + "AdapterArrayHelper.refreshLocal(" + mRootDir +") " + 0);
        } else if (mFullPhotoPaths != null) {
            if (Global.debugEnabled) {
                Log.i(Global.LOG_CONTEXT, mDebugPrefix + why + "AdapterArrayHelper.refreshLocal(" + mRootDir +") " + mFullPhotoPaths.length);
            }

            // #33
            // convert to absolute paths
            String parentDirString = mRootDir.getAbsolutePath();
            for (int i = 0; i < mFullPhotoPaths.length; i++) {
                mFullPhotoPaths[i] = parentDirString + "/" + mFullPhotoPaths[i];
            }

        }
    }

    public int getCount() {
        return mFullPhotoPaths.length;
    }

    /** return null if no file array or illegal index */
    public String getFullFilePathfromArray(int position) {
        if ((mFullPhotoPaths != null) && (position >= 0) && (position < mFullPhotoPaths.length)) {
            return mFullPhotoPaths[position];
        }
        return null;
    }

    /** internal helper. return -1 if position is not available */
    public int getPositionFromPath(String path) {
        if ((mFullPhotoPaths != null) && (path != null)) {
            int result = -1;
            for (int position = 0; position < mFullPhotoPaths.length; position++) {
                if (path.equalsIgnoreCase(mFullPhotoPaths[position])) {
                    result = position;
                    break;
                }
            }
            if (Global.debugEnabledViewItem) Log.i(Global.LOG_CONTEXT, "AdapterArrayHelper.getPositionFromPath-Array(" + path +") => " + result);
            return result;
        }
        return -1;
    }

    /** helper for SelectedItems.Id2FileNameConverter: converts items.id-s to string array of filenNames. */
    public String[] getFileNames(SelectedItems items) {
        if (items != null) {
            ArrayList<String> result = new ArrayList<>();

            int size = 0;
            for(Long id : items) {
                String path = (id != null) ? getFullFilePathfromArray(convertBetweenPositionAndId(id.intValue())) : null;
                result.add(path);
                if (path != null) size++;
            }

            if (size > 0) {
                return result.toArray(new String[size]);
            }
        }
        return null;
    }

    /** translates offset in adapter to id of image */
    public long getImageId(int position) {
        return convertBetweenPositionAndId(position);
    }

    /** translates offset in adapter to id of image and vice versa */
    public int convertBetweenPositionAndId(int value) {
        return -2 -value;
    }
}
