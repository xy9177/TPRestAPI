package org.example.tprestapi.services;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.example.tprestapi.dao.ISystemDAO;
import org.example.tprestapi.dao.SystemDAO;
import org.example.tprestapi.entities.Options;
import org.example.tprestapi.entities.Question;
import org.example.tprestapi.entities.Quiz;
import org.example.tprestapi.entities.QuizQuestion;

import java.util.List;

@Path("/system")
public class SystemResource {
    ISystemDAO dao = null;

    public SystemResource() {
        dao = new SystemDAO();
    }

    @GET
    @Path("questions/{id}/options")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Options> optionsForQuestion(@PathParam("id")int questionId)
    {
        return dao.optionsForQuestion(questionId);
    }

    @GET
    @Path("quiz/{id}/questions")
    @Produces(MediaType.APPLICATION_JSON)
    public  List<Question> getQuestionsForQuiz(@PathParam("id")int quizId)
    {
        return dao.getQuestionsForQuiz(quizId);
    }

    @GET
    @Path("questions/{id}/rightanswer")
    @Produces(MediaType.APPLICATION_JSON)
    public Options rightOptionsForQuestion(@PathParam("id")int questionId)
    {
        return dao.rightOptionsForQuestion(questionId);
    }

    @GET
    @Path("notusedquizzes")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Quiz> getNotUsedQuizzes(){
        return dao.getNotUsedQuizzes();
    }

    @GET
    @Path("usedquizes")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Quiz> getUsedQuizzes(){
        return dao.getUsedQuizzes();
    }

   @GET
    @Path("quiz/{quizId}/question/{questionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public QuizQuestion getQuizQuestion(@PathParam("quizId")int quizId, @PathParam("questionId")int questionId)
    {
        return dao.getQuizQuestion(quizId, questionId);
    }

    @POST
    @Path("quiz/{title}")
    @Produces(MediaType.APPLICATION_JSON)
    public Quiz addNewQuiz(@PathParam("title")String title)
    {
        return dao.addNewQuiz(title);
    }

    @POST
    @Path("quiz/{id}/questions/{N}/{difficulty}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Question> addRandomQuestionsForQuiz(@PathParam("id")int quizId,@PathParam("N")int N, @PathParam("difficulty") String difficulty)
    {
        return dao.addRandomQuestionsForQuiz(quizId, N, difficulty);
    }

    @PUT
    @Path("quiz/{quizId}/question/{questionId}/answer/{selectedOptionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public QuizQuestion updateQuizQuestion(@PathParam("quizId")int quizId, @PathParam("questionId")int questionId, @PathParam("selectedOptionId") int selectedOptionId)
    {
        return dao.updateQuizQuestion(quizId, questionId, selectedOptionId);
    }
}