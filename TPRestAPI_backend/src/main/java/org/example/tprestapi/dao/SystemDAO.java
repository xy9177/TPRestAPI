package org.example.tprestapi.dao;

import jakarta.persistence.*;
import org.example.tprestapi.entities.*;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class SystemDAO implements ISystemDAO {
    EntityManagerFactory factory = null;
    EntityManager manager = null;

    public SystemDAO() {
        factory = Persistence.createEntityManagerFactory("quiz_unit");
        manager = factory.createEntityManager();
    }

    private List<Question> getQuestionsByDifficulty(String difficulty) {
        Query query = manager.createQuery("SELECT q FROM Question q WHERE q.difficulte =:dif");
        query.setParameter("dif", difficulty);
        List<Question> questions = query.getResultList();
        return questions;
    }

    private List<Question> getRandomQuestions(List<Question> list, int N) {
        Random rand = new Random();
        // create a temporary list for storing
        // selected element
        List<Question> newList = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            // take a random index between 0 to size
            // of given List
            int randomIndex = rand.nextInt(list.size());
            // add element in temporary list
            newList.add(list.get(randomIndex));
            // Remove selected element from original list
            list.remove(randomIndex);
        }
        return newList;
    }

    @Override
    public Quiz addNewQuiz(String title) {
        EntityTransaction transaction = manager.getTransaction();
        try {
            transaction.begin(); // Début de la transaction

            Quiz newQuiz = new Quiz();
            newQuiz.setTitre(title);
            manager.persist(newQuiz);

            transaction.commit(); // Validation de la transaction si tout va bien

            return newQuiz; // Retourner l'entité persistée
        } catch (RuntimeException e) {
            if (transaction.isActive()) {
                transaction.rollback(); // Retour en arrière en cas d'erreur
            }
            throw e; // Relancer l'exception
        }
    }

    @Override
    public List<Question> addRandomQuestionsForQuiz(int QuizID, int N, String difficulty) {
        EntityTransaction transaction = manager.getTransaction();
        try {
            transaction.begin();
            Quiz quiz = manager.find(Quiz.class, QuizID);
            if (quiz == null) {
                throw new QuizSystemException("Quiz not found with ID: " + QuizID);
            }
            List<Question> questions = getQuestionsByDifficulty(difficulty);
            if (N > questions.size()) N = questions.size();
            List<Question> randomQuestions = getRandomQuestions(questions, N);
            for (Question question : randomQuestions) {
                QuizQuestionPK quizQuestionPK = new QuizQuestionPK(quiz.getQuizId(), question.getQuestionId());
                QuizQuestion quizQuestion = new QuizQuestion(quizQuestionPK,null, quiz, question);
                manager.persist(quizQuestion);
            }
            transaction.commit();
            return randomQuestions;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new QuizSystemException("Error adding random questions to quiz", e);
        }
    }

    @Override
    // Method to get all Questions for a given Quiz ID
    public List<Question> getQuestionsForQuiz(int quizId) {
        Quiz quiz = manager.find(Quiz.class, quizId); // Fetch the Quiz entity
        if (quiz == null) {
            return List.of(); // Or throw an exception as appropriate
        }

        // Assuming getQuizQuestionsByQuizId() correctly returns a Collection<QuizQuestion>,
        // and assuming each QuizQuestion entity has a method to get the associated Question.
        List<Question> questions = quiz.getQuizQuestionsByQuizId().stream()
                .map(QuizQuestion::getQuestionByQuestionId) // Assuming the method to get Question from QuizQuestion is getQuestion()
                .collect(Collectors.toList());
        return questions;

    }

    @Override
    public List<Options> optionsForQuestion(int questionId) {
        Question question = manager.find(Question.class, questionId);
        if (question == null) {
            return Collections.emptyList(); // 或抛出异常，取决于您的业务逻辑
        }
        // 确保懒加载的集合在此处被访问（在事务内）
        return new ArrayList<>(question.getOptionsByQuestionId());

    }

    @Override
    public List<Quiz> getNotUsedQuizzes() {
        List<Quiz> allQuizzes = manager.createQuery("SELECT q FROM Quiz q", Quiz.class).getResultList();
        List<Quiz> notUsedQuizzes = new ArrayList<>();

        for (Quiz quiz : allQuizzes) {
            boolean isUsed = false;
            for (QuizQuestion qq : quiz.getQuizQuestionsByQuizId()) {
                if (qq.getSelectedOptionID() != null) {
                    isUsed = true;
                    break;
                }
            }
            if (!isUsed) {
                notUsedQuizzes.add(quiz);
            }
        }

        return notUsedQuizzes;
    }

    @Override
    public QuizQuestion updateQuizQuestion(int quizId, int questionId, int selectedOptionId) {
        EntityTransaction transaction = manager.getTransaction();
        transaction.begin();
        try {
            QuizQuestionPK quizQuestionPK = new QuizQuestionPK(quizId, questionId);
            QuizQuestion quizQuestion = manager.find(QuizQuestion.class, quizQuestionPK);
            if (quizQuestion != null) {
                quizQuestion.setSelectedOptionID(selectedOptionId);
                transaction.commit();
                return quizQuestion;
            }
        } catch (Exception e) {
            transaction.rollback();
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public Options rightOptionsForQuestion(int questionId) {
        Question question = manager.find(Question.class, questionId);

        if (question != null) {
            return question.getOptionsByQuestionId().stream()
                    .filter(Options::isEstVrai)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Aucune option correcte n'a été trouvée pour la questionId : " + questionId));
        }
        throw new RuntimeException("Question non trouvée pour questionId :" + questionId);
    }

    @Override
    public List<Quiz> getUsedQuizzes() {

        List<Quiz> allQuizzes = manager.createQuery("SELECT q FROM Quiz q", Quiz.class).getResultList();
        List<Quiz> usedQuizzes = new ArrayList<>();

        for (Quiz quiz : allQuizzes) {
            boolean isUsed = quiz.getQuizQuestionsByQuizId().stream()
                    .anyMatch(qq -> qq.getSelectedOptionID() != null);
            if (isUsed) {
                usedQuizzes.add(quiz);
            }
        }

        return usedQuizzes;
    }

    @Override
    public QuizQuestion getQuizQuestion(int quizId, int questionId) {

        QuizQuestionPK pk = new QuizQuestionPK();
        pk.setQuizId(quizId);
        pk.setQuestionId(questionId);

        // 使用EntityManager查找实体
        return manager.find(QuizQuestion.class, pk);
    }

    public class QuizSystemException extends RuntimeException {
        public QuizSystemException(String message) {
            super(message);
        }

        public QuizSystemException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
