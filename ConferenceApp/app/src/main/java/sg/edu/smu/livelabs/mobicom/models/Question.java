package sg.edu.smu.livelabs.mobicom.models;

import java.util.List;

/**
 * Created by johnlee on 22/2/16.
 */
public class Question {

    private String question;
    private List<String> selection;
    private int correctAnswerPosition;
    private  int quizId;

    public Question() {
    }

    public Question(String question, List<String> selection, int correctAnswerPosition, int quizId) {
        this.question = question;
        this.selection = selection;
        this.correctAnswerPosition = correctAnswerPosition;
        this.quizId = quizId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getSelection() {
        return selection;
    }

    public void setSelection(List<String> selection) {
        this.selection = selection;
    }

    public int getCorrectAnswerPosition() {
        return correctAnswerPosition;
    }

    public void setCorrectAnswerPosition(int correctAnswerPosition) {
        this.correctAnswerPosition = correctAnswerPosition;
    }

    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }
}
