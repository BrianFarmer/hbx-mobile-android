package org.dchbx.coveragehq;

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

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.dchbx.coveragehq.models.ridp.Question;
import org.dchbx.coveragehq.models.ridp.Questions;
import org.dchbx.coveragehq.models.ridp.ResponseOption;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class RidpQuestionsActivity extends BaseActivity {
    private static String TAG = "RidpQuestionsActivity";
    private Questions ridpQuestions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ridp_questions);
        getMessages().getRidpQuestions();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.GetRidpQuestionsResult getRidpQuestionsResult) {
        ridpQuestions = getRidpQuestionsResult.getRidpQuestions();
        populate();
    }

    private void populate() {
        LinearLayout questionArea = (LinearLayout) findViewById(R.id.questionArea);
        for (Question question : ridpQuestions.session.questions) {
            TextView questionTextView = new TextView(this);
            questionTextView.setText(question.questionText);

            questionArea.addView(questionTextView);
            RadioGroup radioGroup = new RadioGroup(this);
            radioGroup.setOrientation(RadioGroup.VERTICAL);
            questionArea.addView(radioGroup);
            for (ResponseOption responseOption : question.responseOptions) {
                RadioButton radioButton = new RadioButton(this);
                radioButton.setText(responseOption.responseText);
                radioGroup.addView(radioButton);
            }
        }

        Button submit = (Button) findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMessages().buttonClicked(R.id.submit);
            }
        });
    }
