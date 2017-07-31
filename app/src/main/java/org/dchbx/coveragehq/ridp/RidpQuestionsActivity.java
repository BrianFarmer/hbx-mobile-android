package org.dchbx.coveragehq.ridp;

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

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.dchbx.coveragehq.AcctActivity;
import org.dchbx.coveragehq.Events;
import org.dchbx.coveragehq.StateManager;
import org.dchbx.coveragehq.R;
import org.dchbx.coveragehq.databinding.RidpQuestionsBinding;
import org.dchbx.coveragehq.models.account.Account;
import org.dchbx.coveragehq.models.ridp.Answer;
import org.dchbx.coveragehq.models.ridp.Answers;
import org.dchbx.coveragehq.models.ridp.Question;
import org.dchbx.coveragehq.models.ridp.QuestionResponse;
import org.dchbx.coveragehq.models.ridp.Questions;
import org.dchbx.coveragehq.models.ridp.ResponseOption;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class RidpQuestionsActivity extends AcctActivity {
    public static StateManager.UiActivity uiActivity = new StateManager.UiActivity(RidpQuestionsActivity.class);
    private static String TAG = "RidpQuestionsActivity";
    private Questions ridpQuestions;
    private Answers usersAnswers;
    private RidpQuestionsBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.ridp_questions);
        configToolbar();
    }

    @Override
    public void onResume(){
        super.onResume();
        getMessages().getRidpQuestions();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(Events.GetRidpQuestionsResult getRidpQuestionsResult) {
        ridpQuestions = getRidpQuestionsResult.getRidpQuestions();
        usersAnswers = getRidpQuestionsResult.getRidpAnswers();
        if (account != null){
            populateQuestions();
        }
    }

    @Override
    protected void populate(final Account account) {
        binding.setAccount(account);
        binding.setActivity(this);
        if (ridpQuestions != null){
            populateQuestions();
        }
    }

    private QuestionResponse getAnswer(Question question, Answers answers){
        for (QuestionResponse questionResponse : answers.questionResponse) {
            if (questionResponse.questionId.compareTo(question.questionId) == 0){
                return questionResponse;
            }
        }
        return null;
    }

    protected void populateQuestions() {
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout questionArea = (LinearLayout) findViewById(R.id.questionArea);


        int i = 100;
        int x = 0;
        for (Question question : ridpQuestions.session.questions) {
            QuestionResponse answer1 = getAnswer(question, usersAnswers);

            View newQuestion = layoutInflater.inflate(R.layout.acct_ridp_question, questionArea, false);
            int questionAreaCount = questionArea.getChildCount();
            questionArea.addView(newQuestion);
            TextView questionTextView = (TextView)newQuestion.findViewById(R.id.question);
            final RadioGroup answers = (RadioGroup)newQuestion.findViewById(R.id.answers);
            questionTextView.setText(question.questionText);
            for (final ResponseOption responseOption : question.responseOptions) {
                final int currentQuestionIndex = x;
                layoutInflater.inflate(R.layout.acct_ridp_answer, answers, true);
                int childCount = answers.getChildCount();
                RadioButton answer = (RadioButton)answers.getChildAt(childCount - 1);
                answer.setId(i ++);
                answer.setText(responseOption.responseText);
                answer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        QuestionResponse response = usersAnswers.questionResponse.get(currentQuestionIndex);
                        if (response.answer == null){
                            response.answer = new Answer();
                        }
                        response.answer = new Answer();
                        response.answer.responseId = responseOption.responseId;
                        response.answer.responseText = responseOption.responseText;
                        //getMessages().updateAnswers(usersAnswers);
                    }
                });
                if (answer1.answer.responseId != null
                    && answer1.answer.responseId.compareTo(responseOption.responseId) == 0){
                    answer.setChecked(true);
                } else {
                    answer.setChecked(false);
                }
            }
            x ++;
        }
    }

    public void onClick(Account account){
        getMessages().accountButtonClicked(StateManager.AppEvents.Continue, account, usersAnswers);
    }
}