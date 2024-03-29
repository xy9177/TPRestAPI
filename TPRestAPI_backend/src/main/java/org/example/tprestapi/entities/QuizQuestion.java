package org.example.tprestapi.entities;

import jakarta.persistence.*;

@Entity
//@IdClass(QuizQuestionPK.class)
public class QuizQuestion {
    @EmbeddedId
    private QuizQuestionPK id;
    @Basic
    @Column(name = "SelectedOptionID")
    private Integer selectedOptionID = null;
    @ManyToOne
    @MapsId("quizId")
    @JoinColumn(name = "QuizID")
    private Quiz quizByQuizId;
    @ManyToOne
    @MapsId("questionId")
    @JoinColumn(name = "QuestionID")
    private Question questionByQuestionId;

    public Question getQuestionByQuestionId() {
        return questionByQuestionId;
    }

    public QuizQuestion() {
    }

    public QuizQuestion(QuizQuestionPK id, Integer selectedOptionID, Quiz quizByQuizId, Question questionByQuestionId) {
        this.id = id;
        this.selectedOptionID = selectedOptionID;
        this.questionByQuestionId = questionByQuestionId;
        this.quizByQuizId = quizByQuizId;
    }

    public Integer getSelectedOptionID() {
        return selectedOptionID;
    }

    public void setSelectedOptionID(Integer selectedOptionID) {
        this.selectedOptionID = selectedOptionID;
    }

    @Override
    public String toString() {
        return "QuizQuestion{" +
                "id=" + id +
                ", selectedOptionID=" + selectedOptionID +
                ", questionByQuestionId=" + questionByQuestionId +
                ", quizByQuizId=" + quizByQuizId +
                '}';
    }
}
