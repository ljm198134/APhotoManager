/*
 * Copyright (c) 2017-2020 by k3b.
 *
 * This file is part of AndroFotoFinder / #APhotoManager.
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 */
package de.k3b.android.widget;

import android.app.Activity;
import android.util.Log;

import de.k3b.android.androFotoFinder.Global;
import de.k3b.android.androFotoFinder.R;
import de.k3b.android.androFotoFinder.queries.FotoSql;
import de.k3b.android.androFotoFinder.queries.IMediaDBApi;
import de.k3b.android.util.AndroidFileCommands;
import de.k3b.io.IProgessListener;
import de.k3b.io.collections.SelectedFiles;
import de.k3b.media.PhotoPropertiesDiffCopy;

/** update exif changes in asynch task mit chow dialog */
public class UpdateTask extends AsyncTaskWithProgressDialog<SelectedFiles> implements IProgessListener {
    private static final String mDebugPrefix = "UpdateTaskAsync-";
    public static final int EXIF_RESULT_ID = 522;

    private PhotoPropertiesDiffCopy exifChanges;
    private final AndroidFileCommands cmd;

    public UpdateTask(Activity ctx, AndroidFileCommands cmd,
                      PhotoPropertiesDiffCopy exifChanges) {
        super(ctx, R.string.exif_menu_title);
        this.exifChanges = exifChanges;
        this.cmd = cmd;
    }

    @Override
    protected Integer doInBackground(SelectedFiles... params) {
        publishProgress("...");

        int result = 0;
        if (exifChanges != null) {
            SelectedFiles items = params[0];

            if (true) {
                result = cmd.applyExifChanges(true, exifChanges, items, null);
            } else {
                // disabled: does not work because of overlapping transactions
                IMediaDBApi api = FotoSql.getMediaDBApi();
                try {
                    api.beginTransaction();
                    result = cmd.applyExifChanges(true, exifChanges, items, null);
                    api.setTransactionSuccessful();
                } finally {
                    api.endTransaction();
                }
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(Integer itemCount) {
        if (Global.debugEnabled) {
            Log.d(Global.LOG_CONTEXT, mDebugPrefix + " onPostExecute " + itemCount);
        }
        Activity parent = this.getActivity();
        super.onPostExecute(itemCount);
        if (parent != null) {
            parent.setResult(EXIF_RESULT_ID, parent.getIntent());
            parent.finish();
        }
    }

    @Override
    public void destroy() {
        if (exifChanges != null) exifChanges.close();
        exifChanges = null;
        super.destroy();
    }

    public boolean isEmpty() {
        return (exifChanges == null);
    }

    /**
     * called every time when command makes some little progress. Can be mapped to async progress-bar
     *
     * @param itemcount
     * @param total
     * @param message
     */
    @Override
    public boolean onProgress(int itemcount, int total, String message) {
        publishProgress(itemcount, total, message);
        return !isCancelled();
    }
}

