package org.dchbx.coveragehq.statemachine;

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
class DialogInfo extends StateInfoBase {
    private StateManager.UiDialog uiDialog;
    private final int uiDialogId;

    public DialogInfo(StateManager.AppStates appState, StateManager.AppEvents event, StateManager.UiDialog uiDialog) {
        super(appState, event);

        this.uiDialog = uiDialog;
        if (this.uiDialog != null) {
            uiDialogId = this.uiDialog.getId();
        } else {
            uiDialogId = 0;
        }

    }

    public StateManager.UiDialog getUiActivity(){
        return uiDialog;
    }

    @Override
    public void reconstitute(){
        uiDialog = StateManager.UiDialog.getUiDialogType(uiDialogId).uiDialog;
    }

    @Override
    public void onPop(StateMachine stateMachine, StateManager stateManager, EventParameters eventParameters){
        stateManager.dismissDialog();
    }
}
