// Définition de la fonction asynchrone pour récupérer les quizes inutilisés
async function getNotUsedQuizes() {
    // Envoi de la requête GET pour récupérer les quizes inutilisés
    const response = await fetch('http://localhost:8080/TPRestAPI_war_exploded/api/system/notusedquizzes', { method: 'GET' })
    const data = await response.json()
    return data
}


// Définition de la fonction asynchrone pour récupérer les quizes utilisés
async function getUsedQuizes() {
    // Envoi de la requête GET pour récupérer les quizes utilisés
    const response = await fetch('http://localhost:8080/TPRestAPI_war_exploded/api/system/usedquizes', { method: 'GET' })
    const data = await response.json()
    return data
}

document.addEventListener('DOMContentLoaded', () => {

    if (document.getElementById('btnPasser')) {
        document.getElementById('btnPasser').addEventListener('click', () => {
            let selectedRow = document.querySelector('#quizTable .selected');
            if (selectedRow) {
                let quizID = selectedRow.id.replace('quiz_', '');
                window.location.href = 'answerquiz.html?quizId=' + quizID;
            } else {
                alert('Veuillez dabord sélectionner un quiz.');
            }
        });
    }

    if (document.getElementById('btnReviser')) {
        document.getElementById('btnReviser').addEventListener('click', () => {
            let selectedRow2 = document.querySelector('#quizTable .selected');
            if (selectedRow2) {
                let quizID = selectedRow2.id.replace('quiz_', '');
                window.location.href = 'reviewanswer.html?quizId=' + quizID;
            } else {
                alert('Please select a quiz first.');
            }
        });
    }

    loadQuizzes();
});

async function loadQuizzes() {
    try {
        // Récupérer les quiz inutilisés et mettre à jour le formulaire
        const notUsedQuizzesData = await getNotUsedQuizes();
        if (notUsedQuizzesData) {
            updateQuizTable(notUsedQuizzesData, 'listNotUsedQuizes', 'quizTable');
        }

        // Obtenir les quiz utilisés et mettre à jour le formulaire
        const usedQuizzesData = await getUsedQuizes();
        if (usedQuizzesData) {
            updateQuizTable(usedQuizzesData, 'listUsedQuizes', 'quizTable');
        }
    } catch (error) {
        console.error('Error fetching quizzes:', error);
    }
}

function updateQuizTable(quizzesData, listElementId, tableId) {
    let listElement = document.getElementById(listElementId);
    if (!listElement) return;

    let table = document.createElement('table');
    table.id = tableId;

    quizzesData.forEach((quiz, index) => {
        let row = table.insertRow();
        row.id = `quiz_${quiz.quizId}`;
        let cell = row.insertCell();
        cell.textContent = quiz.titre;
        cell.classList.add('quiz-item');

        row.addEventListener('click', function() {
            document.querySelectorAll(`#${tableId} .selected`).forEach(selectedRow => {
                selectedRow.classList.remove('selected');
            });
            row.classList.add('selected');
        });
    });
    // Vider la liste existante et ajouter un nouveau tableau
    listElement.innerHTML = '';
    listElement.appendChild(table);
}

document.addEventListener('DOMContentLoaded', function() {
    let btnSave = document.getElementById('btnSave');
    btnSave.addEventListener('click', function() {
        const quizTitle = document.getElementById('quizTitle').value.trim();

        if (quizTitle) {
            createQuiz(quizTitle).then(quiz => {
                const quizId = quiz.quizId;

                const difficulties = ['Facile', 'Moyen', 'Difficile'];
                const promises = difficulties.map(difficulty => {
                    const number = document.getElementById(`number-${difficulty}`).value;
                    if (number > 0) {
                        return addRandomQuestionsForQuiz(quizId, number, difficulty);
                    } else {
                        return Promise.resolve(); // Renvoie une promesse résolue lorsqu'il n'y a pas de problème à ajouter.
                    }
                });

                // Attendre que toutes les demandes d'ajout de questions soient terminées
                Promise.all(promises).then(() => {
                    console.log("Toutes les questions ont été ajoutées avec succès.");

                    window.location.href = 'passquiz.html?quizId=' + quizId;
                }).catch(error => {
                    console.error("Erreur lors de l'ajout de questions:", error);
                });
            }).catch(error => {
                console.error('Erreur lors de la création du quiz:', error);
            });
        } else {
            alert("Veuillez saisir un titre de quiz.");
        }
    });
});

// Générer un nouveau quiz
async function createQuiz(title) {
    const response = await fetch(`http://localhost:8080/TPRestAPI_war_exploded/api/system/quiz/${title}`, {
        method: 'POST',
        headers: {"Content-Type": "application/json"}
    });
    return response.json();
}

async function addRandomQuestionsForQuiz(quizId, N, difficulty) {
    const response = await fetch(`http://localhost:8080/TPRestAPI_war_exploded/api/system/quiz/${quizId}/questions/${N}/${difficulty}`, {
        method: 'POST',
        headers: {"Content-Type": "application/json"}
    });
    return response.json();
}

document.getElementById('btnDelete').addEventListener('click', () => {
    document.location.href = 'index.html';
});

async function fetchData(url, options = {}) {
    const response = await fetch(url, options);
    if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
}

async function getOptionsForQuestion(questionId) {
    try {
        const options = await fetchData(`http://localhost:8080/TPRestAPI_war_exploded/api/system/questions/${questionId}/options`);
        return options;
    } catch (error) {
        console.error(`Error fetching options for question ${questionId}:`, error);
        return [];
    }
}

function getUserAnswerForQuestion(questionId) {
    return localStorage.getItem(`answer_for_question_${questionId}`);
}

async function renderQuestion(question) {
    const questionElement = document.createElement('div');
    questionElement.className = 'question';
    questionElement.setAttribute('data-question-id', question.questionId);
    const questionTitle = document.createElement('h4');
    questionTitle.textContent = question.enonce;
    questionElement.appendChild(questionTitle);

    const optionsContainer = document.createElement('div');
    optionsContainer.className = 'options-container';
   // const userAnswerId = getUserAnswerForQuestion(question.questionId);

    // Fetch options for the question using a for...of loop to handle await correctly
    const options = await getOptionsForQuestion(question.questionId);
    if (options.length === 0) {
        const errorMessage = document.createElement('p');
        errorMessage.textContent = "Aucune option disponible.";
        questionElement.appendChild(errorMessage);
    } else {
        for (const option of options) {
            const optionInput = document.createElement('input');
            optionInput.type = 'radio';
            optionInput.id = `option_${option.optionId}`;
            optionInput.name = `question_${question.questionId}`;
            optionInput.value = option.optionId;


            const optionLabel = document.createElement('label');
            optionLabel.setAttribute('for', `option_${option.optionId}`);
            optionLabel.textContent = option.texte;

            optionsContainer.appendChild(optionInput);
            optionsContainer.appendChild(optionLabel);
            optionsContainer.appendChild(document.createElement('br'));
        }
    }

    questionElement.appendChild(optionsContainer);
    document.getElementById('questions-container').appendChild(questionElement);
}


async function loadQuiz(quizId) {
    const questions = await fetchData(`http://localhost:8080/TPRestAPI_war_exploded/api/system/quiz/${quizId}/questions`);
    document.getElementById('number-quiz').textContent = quizId;
    const renderPromises = questions.map(question => renderQuestion(question));
    // Wait for all questions to be rendered
    await Promise.all(renderPromises);
}

function getQueryParam(param) {
    const searchParams = new URLSearchParams(window.location.search);
    return searchParams.get(param);
}

async function handleSubmit(quizId) {
    const questions = document.querySelectorAll('.question');
    let allAnswered = true;

    // Vérifie que toutes les questions ont des réponses sélectionnées
    for (const question of questions) {
        const questionId = question.getAttribute('data-question-id');
        const selectedOptionInput = question.querySelector(`input[type="radio"]:checked`);
        if (!selectedOptionInput) {
            allAnswered = false;
            // Demande à l'utilisateur quelle est la question à laquelle il n'a pas répondu
            alert(`La question  n'a pas été répondue. Veuillez répondre à toutes les questions avant de soumettre.`);
            break;
        }
    }

    // Soumettre les réponses si toutes les questions ont reçu une réponse
    if (allAnswered) {
        for (const question of questions) {
            const questionId = question.getAttribute('data-question-id');
            const selectedOptionInput = question.querySelector(`input[type="radio"]:checked`);
            const selectedOptionId = selectedOptionInput.value;
            // Construction de l'URL de la requête
            const url = `http://localhost:8080/TPRestAPI_war_exploded/api/system/quiz/${quizId}/question/${questionId}/answer/${selectedOptionId}`;

            try {
                await fetchData(url, {
                    method: 'PUT',
                });
            } catch (error) {
                console.error('Error submitting answer:', error);
            }
        }
        alert('Réponses soumises!');
    }
}


document.addEventListener('DOMContentLoaded', async () => {
    const quizId = getQueryParam('quizId');
    if (quizId) {
        await loadQuiz(quizId);
    } else {
        console.error('No quizId provided in the URL');
    }

    const btnAnswer = document.getElementById('btnAnswer');
    btnAnswer.addEventListener('click', async () => {
        await handleSubmit(quizId);
    });
});

async function highlightAnswers(quizId) {
    const questionsResponse = await fetch(`http://localhost:8080/TPRestAPI_war_exploded/api/system/quiz/${quizId}/questions`);
    const questions = await questionsResponse.json();
    let correctAnswers = 0;

    for (const question of questions) {
        const rightAnswerResponse = await fetch(`http://localhost:8080/TPRestAPI_war_exploded/api/system/questions/${question.questionId}/rightanswer`);
        const rightAnswer = await rightAnswerResponse.json();

        const quizQuestionResponse = await fetch(`http://localhost:8080/TPRestAPI_war_exploded/api/system/quiz/${quizId}/question/${question.questionId}`);
        const quizQuestion = await quizQuestionResponse.json();
        const selectedOptionId = quizQuestion.selectedOptionID;

        const rightOptionInput = document.getElementById(`option_${rightAnswer.optionId}`);
        const selectedOptionInput = selectedOptionId ? document.getElementById(`option_${selectedOptionId}`) : null;

        // Marquez la bonne réponse en vert
        const rightOptionLabel = document.querySelector(`label[for="option_${rightAnswer.optionId}"]`);
    if (rightOptionLabel) {
        rightOptionLabel.classList.add('correct');
    }

        // Si l'utilisateur sélectionne une réponse incorrecte, celle-ci est marquée en rouge.
        if (selectedOptionInput && selectedOptionId !== rightAnswer.optionId) {
            selectedOptionInput.style.accentColor = 'red';
            selectedOptionInput.checked=true;
        }

        // Si l'utilisateur sélectionne correctement,celle-ci est marquée en rouge, augmentez le nombre de réponses correctes.
        if (selectedOptionId === rightAnswer.optionId) {
            selectedOptionInput.style.accentColor = 'green';
            selectedOptionInput.checked=true;
            correctAnswers++;
        }
    }

    const resultsContainer = document.getElementById('results-container');
    if (resultsContainer) {
        resultsContainer.textContent = `${correctAnswers} / ${questions.length} bonnes reponses`;
    }
}

    // Exécuter cette fonction au chargement de la page
    window.onload = function() {
        const urlParams = new URLSearchParams(window.location.search);
        const quizId = urlParams.get('quizId');
        const isRightAnswer = document.getElementById('results-container') !== null;
        if (quizId && isRightAnswer) {
            highlightAnswers(quizId);
        }
    };
