package org.dchbx.coveragehq;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
public abstract class AcctActivity extends BrokerActivity {
    protected org.dchbx.coveragehq.models.account.Account account;

    @Override
    protected void onResume() {
        super.onResume();
        getMessages().getCreateAccountInfo();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.GetCreateAccountInfoResult getCreateAccountInfoResult) {
        account = getCreateAccountInfoResult.getAccount();
        populate(account);
    }

    protected abstract void populate(org.dchbx.coveragehq.models.account.Account account);
}
