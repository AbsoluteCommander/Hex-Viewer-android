package fr.ralala.hexviewer.ui.launchers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.ui.activities.LineUpdateActivity;
import fr.ralala.hexviewer.ui.activities.MainActivity;
import fr.ralala.hexviewer.ui.adapters.HexTextArrayAdapter;
import fr.ralala.hexviewer.utils.LineEntry;
import fr.ralala.hexviewer.utils.SysHelper;

import static fr.ralala.hexviewer.ui.adapters.SearchableListArrayAdapter.FilterData;
/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Launcher used with LineUpdate activity
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class LauncherLineUpdate {
  private final MainActivity mActivity;
  private final ApplicationCtx mApp;
  private ActivityResultLauncher<Intent> activityResultLauncherLineUpdate;

  public LauncherLineUpdate(MainActivity activity) {
    mApp = ApplicationCtx.getInstance();
    mActivity = activity;
    register();
  }

  /**
   * Starts the activity.
   *
   * @param string   The hex string.
   * @param position The position in the list view.
   */
  public void startActivity(String string, int position) {
    LineUpdateActivity.startActivity(mActivity, activityResultLauncherLineUpdate, string,
        mActivity.getFileData().getName(), position, mActivity.getUndoRedoManager().isChanged());
  }

  /**
   * Registers result launcher for the activity for line update.
   */
  private void register() {
    activityResultLauncherLineUpdate = mActivity.registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
          if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null) {
              Bundle bundle = data.getExtras();
              String refString = bundle.getString(LineUpdateActivity.RESULT_REFERENCE_STRING);
              String newString = bundle.getString(LineUpdateActivity.RESULT_NEW_STRING);
              int position = bundle.getInt(LineUpdateActivity.RESULT_POSITION);

              final byte[] buf = SysHelper.hexStringToByteArray(newString);
              final byte[] ref = SysHelper.hexStringToByteArray(refString);
              if (Arrays.equals(ref, buf)) {
                /* nothing to do */
                return;
              }
              List<LineEntry> li = SysHelper.formatBuffer(buf, null);
              if (li.isEmpty()) {
                HexTextArrayAdapter adapter = mActivity.getAdapterHex();
                Map<Integer, FilterData<LineEntry>> map = new HashMap<>();
                map.put(position, adapter.getFilteredList().get(position));
                mActivity.getUndoRedoManager().insertInUnDoRedoForDelete(adapter, map).execute();
              } else {
                String query = mActivity.getSearchQuery();
                if (!query.isEmpty())
                  mActivity.doSearch("");
                mActivity.getAdapterHex().setItem(position, li);
                if (!query.isEmpty())
                  mActivity.doSearch(mActivity.getSearchQuery());
                mActivity.setTitle(mActivity.getResources().getConfiguration());
              }
            } else
              Log.e(getClass().getSimpleName(), "Null data!!!");
          }
        });
  }

}
