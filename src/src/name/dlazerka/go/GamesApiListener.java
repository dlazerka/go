package name.dlazerka.go;

import static name.dlazerka.go.Util.TAG;
import android.util.Log;

import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;

class GamesApiListener implements ConnectionCallbacks {
  @Override
  public void onConnected() {
    Log.d(TAG, "Connected to Games API");
  }

  @Override
  public void onDisconnected() {
    Log.d(TAG, "Disconnected from Games API");
  }
}