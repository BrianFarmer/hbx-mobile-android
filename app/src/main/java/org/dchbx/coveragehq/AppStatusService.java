package org.dchbx.coveragehq;

import org.joda.time.DateTime;

import java.util.Timer;
import java.util.TimerTask;

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
public class AppStatusService {
    private final ServiceManager serviceManager;

    private DateTime timeout = null;
    private Timer sessionTimeoutTimer;
    private int countdownTimerTicksLeft;
    private Timer countdownTimer;
    private ServerConfiguration.UserType userStatus;


    public AppStatusService(ServiceManager serviceManager){
        this.serviceManager = serviceManager;
    }


    public void startSessionTimeout() {

        countdownTimerTicksLeft = serviceManager.enrollConfig().getTimeoutCountdownSeconds();
        countdownTimer = new Timer();
        countdownTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                countdownTimerTicksLeft --;
                if (countdownTimerTicksLeft > 0) {
                    BrokerWorker.eventBus.post(new Events.SessionTimeoutCountdownTick(countdownTimerTicksLeft));
                } else {
                    BrokerWorker.eventBus.post(new Events.SessionTimedOut());
                    countdownTimer.cancel();
                }
            }
        }, 1000, 1000);
    }

    public boolean timedOut(){
        if (timeout != null){
            if (timeout.compareTo(DateTime.now()) < 0 ){
                return true;
            }
        }
        return false;
    }

    public void cancelSessionTimer() {
        if (sessionTimeoutTimer != null){
            sessionTimeoutTimer.cancel();
            sessionTimeoutTimer = null;
        }
    }

    public void updateSessionTimer() {
        if (sessionTimeoutTimer != null) {
            sessionTimeoutTimer.cancel();
        }
        int timeoutMilliSeconds = serviceManager.enrollConfig().getSessionTimeoutSeconds() * 1000;
        timeout = DateTime.now().plusMillis(timeoutMilliSeconds);
        sessionTimeoutTimer = new Timer();;
        sessionTimeoutTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                BrokerWorker.eventBus.post(new Events.SessionAboutToTimeout());
                sessionTimeoutTimer.cancel();
                sessionTimeoutTimer = null;
            }
        }, timeoutMilliSeconds);
    }

    public boolean testTimeout() {
        boolean result = timeout != null && timeout.compareTo(DateTime.now()) < 0;
        timeout = null;
        return result;
    }


    public ServerConfiguration.UserType getUserStatus() {
        return serviceManager.getServerConfiguration().userType;
    }

    public void setUserStatus(ServerConfiguration.UserType userStatus) {
        serviceManager.getServerConfiguration().userType = userStatus;
        this.userStatus = userStatus;
    }
}
