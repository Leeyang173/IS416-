package sg.edu.smu.livelabs.mobicom.models;

import java.util.List;

/**
 * Created by johnlee on 22/2/16.
 */
public class QuizSet {

    private List<Question> questions;
    private int quizSetId;
    private int paperId;

    public QuizSet() {
    }

    public QuizSet(List<Question> questions, int quizSetId, int paperId) {
        this.questions = questions;
        this.quizSetId = quizSetId;
        this.paperId = paperId;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public int getQuizSetId() {
        return quizSetId;
    }

    public void setQuizSetId(int quizSetId) {
        this.quizSetId = quizSetId;
    }

    public int getPaperId() {
        return paperId;
    }

    public void setPaperId(int paperId) {
        this.paperId = paperId;
    }
}
