package org.dchbx.coveragehq;

import android.hardware.SensorManager;
import android.util.Log;

import com.squareup.seismic.ShakeDetector;

import static android.content.Context.SENSOR_SERVICE;

/*
    This file is part of DC.

    DC Health Link SmallBiz is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DC Health Link SmallBiz is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DC Health Link SmallBiz.  If not, see <http://www.gnu.org/licenses/>.
    This statement should go near the beginning of every source file, close to the copyright notices. When using the Lesser GPL, insert the word “Lesser” before “General” in all three places. When using the GNU AGPL, insert the word “Affero” before “General” in all three places.
*/
class DebugStateService implements ShakeDetector.Listener {
    private static final String TAG = "DebugStateService";

    public DebugStateService(){
        Log.d(TAG, "in DebugStateService ctor");
    }

    public void init(){
        SensorManager sensorManager = (SensorManager) BrokerApplication.getBrokerApplication().getSystemService(SENSOR_SERVICE);
        ShakeDetector sd = new ShakeDetector(this);
        sd.start(sensorManager);
    }

    @Override
    public void hearShake() {
        if (!StateInfo.getShown()) {
            Intents.launchStateInfo(BaseActivity.getCurrentActivity());
        }
    }
}
