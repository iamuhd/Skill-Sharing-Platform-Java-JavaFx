package com.skillsharingcommunityplatform;

// SkillSharingApp.java

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import javafx.util.StringConverter;

// CSS for modernistic look
// The styles are embedded directly for simplicity, but can be moved to an external file.
// The modernistic look is achieved using a consistent color palette,
// padding, spacing, rounded corners, and hover effects.
class ModernisticTheme {
    public static final String CSS = """
            .root {
                -fx-background-color: #f0f2f5;
                -fx-font-family: 'Segoe UI', Arial, sans-serif;
            }
            .button {
                -fx-background-color: #007bff;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-padding: 10 20;
                -fx-background-radius: 8;
                -fx-border-radius: 8;
                -fx-cursor: hand;
            }
            .button:hover {
                -fx-background-color: #0056b3;
            }
            .text-field, .password-field, .combo-box, .text-area {
                -fx-background-color: #ffffff;
                -fx-background-radius: 8;
                -fx-border-radius: 8;
                -fx-border-color: #ccc;
                -fx-padding: 8;
            }
            .label {
                -fx-font-weight: bold;
            }
            .title-label {
                -fx-font-size: 24px;
                -fx-font-weight: bold;
                -fx-text-fill: #333;
            }
            .subtitle-label {
                -fx-font-size: 16px;
                -fx-font-weight: bold;
                -fx-text-fill: #555;
            }
            .card {
                -fx-background-color: white;
                -fx-background-radius: 10;
                -fx-padding: 15;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);
            }
            .list-cell {
                -fx-background-color: transparent;
            }
            .list-view .list-cell:filled:selected, .list-view .list-cell:filled:selected:hover {
                -fx-background-color: #e9ecef;
                -fx-text-fill: #333;
            }
            .list-view .list-cell:filled:hover {
                -fx-background-color: #f8f9fa;
                -fx-text-fill: #333;
            }
            .list-view {
                -fx-background-radius: 8;
                -fx-border-radius: 8;
            }
            """;
}

// ==================================================================================================================
// New Classes for modernistic look and features
// ==================================================================================================================
class Quiz {
    private String skillName;
    private List<Question> questions;

    public Quiz(String skillName) {
        this.skillName = skillName;
        this.questions = new ArrayList<>();
    }

    public String getSkillName() { return skillName; }
    public List<Question> getQuestions() { return questions; }
    public void addQuestion(Question q) { questions.add(q); }

    public static Quiz fromString(String line) {
        String[] parts = line.split("\\|");
        String skillName = parts[0];
        Quiz quiz = new Quiz(skillName);
        for (int i = 1; i < parts.length; i++) {
            quiz.addQuestion(Question.fromString(parts[i]));
        }
        return quiz;
    }

    @Override
    public String toString() {
        return skillName + "|" + questions.stream().map(Question::toString).collect(Collectors.joining("|"));
    }
}

class Question {
    private String questionText;
    private List<String> options;
    private int correctAnswerIndex;

    public Question(String questionText, List<String> options, int correctAnswerIndex) {
        this.questionText = questionText;
        this.options = options;
        this.correctAnswerIndex = correctAnswerIndex;
    }

    public String getQuestionText() { return questionText; }
    public List<String> getOptions() { return options; }
    public int getCorrectAnswerIndex() { return correctAnswerIndex; }

    public static Question fromString(String s) {
        String[] parts = s.split(";");
        String text = parts[0];
        int correctIndex = Integer.parseInt(parts[parts.length - 1]);
        List<String> options = new ArrayList<>(Arrays.asList(parts).subList(1, parts.length - 1));
        return new Question(text, options, correctIndex);
    }

    @Override
    public String toString() {
        return questionText + ";" + String.join(";", options) + ";" + correctAnswerIndex;
    }
}

class QuizManager {
    private static final List<Quiz> allQuizzes = new ArrayList<>();

    public static void addQuiz(Quiz quiz) {
        allQuizzes.add(quiz);
    }

    public static Optional<Quiz> getQuizBySkillName(String skillName) {
        return allQuizzes.stream().filter(q -> q.getSkillName().equalsIgnoreCase(skillName)).findFirst();
    }

    public static List<Quiz> getAllQuizzes() {
        return new ArrayList<>(allQuizzes);
    }

    public static void clearAllQuizzes() {
        allQuizzes.clear();
    }
}

class Assignment {
    private String skillName;
    private String description;
    private String filePath;
    private String submittedByUserId;

    public Assignment(String skillName, String description) {
        this.skillName = skillName;
        this.description = description;
    }

    public String getSkillName() { return skillName; }
    public String getDescription() { return description; }
    public String getFilePath() { return filePath; }
    public String getSubmittedByUserId() { return submittedByUserId; }

    public void setSubmittedByUserId(String submittedByUserId) {
        this.submittedByUserId = submittedByUserId;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public static Assignment fromString(String line) {
        String[] parts = line.split("\\|");
        Assignment assignment = new Assignment(parts[0], parts[1]);
        if (parts.length > 2) {
            assignment.setSubmittedByUserId(parts[2]);
            assignment.setFilePath(parts[3]);
        }
        return assignment;
    }

    @Override
    public String toString() {
        return skillName + "|" + description + (submittedByUserId != null ? "|" + submittedByUserId + "|" + filePath : "");
    }
}

class AssignmentManager {
    private static final List<Assignment> allAssignments = new ArrayList<>();
    private static final List<Assignment> allSubmissions = new ArrayList<>();

    public static void addAssignment(Assignment assignment) {
        allAssignments.add(assignment);
    }

    public static Optional<Assignment> getAssignmentBySkillName(String skillName) {
        return allAssignments.stream().filter(a -> a.getSkillName().equalsIgnoreCase(skillName)).findFirst();
    }

    public static void addSubmission(Assignment submission) {
        allSubmissions.add(submission);
    }

    public static Optional<Assignment> getSubmissionForUserAndSkill(String userId, String skillName) {
        return allSubmissions.stream()
                .filter(a -> userId.equals(a.getSubmittedByUserId()) && skillName.equalsIgnoreCase(a.getSkillName()))
                .findFirst();
    }

    public static List<Assignment> getAllAssignments() {
        return new ArrayList<>(allAssignments);
    }

    public static List<Assignment> getAllSubmissions() {
        return new ArrayList<>(allSubmissions);
    }

    public static void clearAllAssignments() {
        allAssignments.clear();
        allSubmissions.clear();
    }
}

class Lecture {
    private String skillName;
    private String videoFilePath;

    public Lecture(String skillName, String videoFilePath) {
        this.skillName = skillName;
        this.videoFilePath = videoFilePath;
    }

    public String getSkillName() { return skillName; }
    public String getVideoFilePath() { return videoFilePath; }

    public static Lecture fromString(String line) {
        String[] parts = line.split("\\|");
        return new Lecture(parts[0], parts[1]);
    }

    @Override
    public String toString() {
        return skillName + "|" + videoFilePath;
    }
}

class LectureManager {
    private static final List<Lecture> allLectures = new ArrayList<>();

    public static void addLecture(Lecture lecture) {
        allLectures.add(lecture);
    }

    public static Optional<Lecture> getLectureBySkillName(String skillName) {
        return allLectures.stream().filter(l -> l.getSkillName().equalsIgnoreCase(skillName)).findFirst();
    }

    public static List<Lecture> getAllLectures() {
        return new ArrayList<>(allLectures);
    }

    public static void clearAllLectures() {
        allLectures.clear();
    }
}


// ==================================================================================================================
// Original Classes from the file
// ==================================================================================================================
interface User {
    String getId();
    String getName();
}

interface LoginLogout {
    void login();
    void logout();
}

interface SessionActions {
    void requestSession(String skillName, String timing, int duration);
    void addSession(String skillName, String timing, int duration);
    void enrollInSession(Session session);
    void rateSession(Session session, int rating);
    void checkRatings(String skillName);
}

abstract class Skill {
    protected String skillCode;
    protected String skillName;

    public Skill(String skillName, String skillCode) {
        this.skillName = skillName;
        this.skillCode = skillCode;
    }

    public String getSkillCode() { return skillCode; }
    public String getSkillName() { return skillName; }
}

abstract class AbstractPerson implements User, LoginLogout {
    protected String id;
    protected String name;
    protected String password;
    private static int totalUsers = 0;

    private static long nextSeekerSequence = 1000;
    private static long nextProviderSequence = 2000;

    public AbstractPerson(String id, String name, String password) {
        this.id = id;
        this.name = name;
        this.password = password;
        totalUsers++;
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getName() { return name; }

    public String getPassword() { return password; }

    public static int getTotalUsers() { return totalUsers; }

    public static void resetTotalUsers() {
        totalUsers = 0;
    }

    public static String generateUniqueId(String type) {
        if ("seeker".equalsIgnoreCase(type)) {
            return "S" + nextSeekerSequence++;
        } else if ("provider".equalsIgnoreCase(type)) {
            return "P" + nextProviderSequence++;
        }
        return "GEN_ERR_" + System.currentTimeMillis();
    }

    public static void updateIdCountersFromFile(String id) {
        if (id.startsWith("S") && id.length() > 1) {
            try {
                long sequence = Long.parseLong(id.substring(1));
                if (sequence >= nextSeekerSequence) {
                    nextSeekerSequence = sequence + 1;
                }
            } catch (NumberFormatException e) {
                System.err.println("Error parsing seeker ID from file: " + id + ". Counter not updated.");
            }
        } else if (id.startsWith("P") && id.length() > 1) {
            try {
                long sequence = Long.parseLong(id.substring(1));
                if (sequence >= nextProviderSequence) {
                    nextProviderSequence = sequence + 1;
                }
            } catch (NumberFormatException e) {
                System.err.println("Error parsing provider ID from file: " + id + ". Counter not updated.");
            }
        }
    }

    public static void resetIdSequences() {
        nextSeekerSequence = 1000;
        nextProviderSequence = 2000;
    }

    @Override
    public void login() {
        System.out.println(name + " (" + id + ") logged in.");
    }

    @Override
    public void logout() {
        System.out.println(name + " (" + id + ") logged out.");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractPerson that = (AbstractPerson) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

class Session extends Skill {
    private String skillSlotTiming;
    private String skillInstructor;
    private int sessionDuration;
    private int skillRating;
    private int numberOfRatings;
    private List<String> enrolledUserIds = new ArrayList<>();
    private static final int MAX_ENROLLED_STUDENTS = 50;

    public Session(String skillName, String skillCode, String skillSlotTiming, String skillInstructor, int sessionDuration) {
        super(skillName, skillCode);
        this.skillSlotTiming = skillSlotTiming;
        this.skillInstructor = skillInstructor;
        this.sessionDuration = sessionDuration;
        this.skillRating = 0;
        this.numberOfRatings = 0;
    }

    public Session(String skillName, String skillCode, String skillSlotTiming, String skillInstructor, int sessionDuration, int skillRating, int numberOfRatings, List<String> enrolledUserIds) {
        super(skillName, skillCode);
        this.skillSlotTiming = skillSlotTiming;
        this.skillInstructor = skillInstructor;
        this.sessionDuration = sessionDuration;
        this.skillRating = skillRating;
        this.numberOfRatings = numberOfRatings;
        if (enrolledUserIds != null) {
            this.enrolledUserIds.addAll(enrolledUserIds);
        }
    }

    public String getSkillSlotTiming() { return skillSlotTiming; }
    public String getSkillInstructor() { return skillInstructor; }
    public int getSessionDuration() { return sessionDuration; }
    public int getRawSkillRating() { return skillRating; }
    public int getNumberOfRatings() { return numberOfRatings; }
    public List<String> getEnrolledUserIds() { return new ArrayList<>(enrolledUserIds); }
    public int getCurrentEnrollmentCount() { return enrolledUserIds.size(); }
    public boolean isFull() { return enrolledUserIds.size() >= MAX_ENROLLED_STUDENTS; }
    public boolean isUserEnrolled(String userId) { return enrolledUserIds.contains(userId); }

    public void addRating(int rating) {
        if (rating >= 1 && rating <= 5) {
            this.skillRating += rating;
            this.numberOfRatings++;
        } else {
            System.out.println("Invalid rating: " + rating + ". Rating must be between 1 and 5.");
        }
    }

    public void addEnrolledUser(String userId) {
        if (!enrolledUserIds.contains(userId) && !isFull()) {
            enrolledUserIds.add(userId);
            System.out.println("User " + userId + " enrolled in " + skillName);
        } else if (enrolledUserIds.contains(userId)) {
            System.out.println("User " + userId + " is already enrolled in " + skillName);
        } else {
            System.out.println("Session " + skillName + " is full. Cannot enroll user " + userId);
        }
    }

    public double getAverageSkillRating() {
        return numberOfRatings == 0 ? 0 : (double) skillRating / numberOfRatings;
    }

    @Override
    public String toString() {
        return "Session: " + skillName + " (Code: " + skillCode +
                ") | Timing: " + skillSlotTiming +
                " | Instructor: " + skillInstructor +
                " | Duration: " + sessionDuration + " mins" +
                " | Enrolled: " + enrolledUserIds.size() + "/" + MAX_ENROLLED_STUDENTS +
                " | Avg Rating: " + String.format("%.1f", getAverageSkillRating());
    }
}

class SessionManager {
    private static final List<Session> totalSessionData = new ArrayList<>();

    public static void addSession(Session session) {
        totalSessionData.add(session);
        System.out.println("Session added successfully: " + session.getSkillName());
    }

    public static List<Session> getAllSessions() {
        return new ArrayList<>(totalSessionData);
    }

    public static Optional<Session> getSessionBySkillName(String skillName) {
        return totalSessionData.stream()
                .filter(s -> s.getSkillName().equalsIgnoreCase(skillName))
                .findFirst();
    }

    public static void clearAllSessions() {
        totalSessionData.clear();
    }
}

class RequestedSession {
    private String skillName;
    private String requestedByUserId;
    private String timing;
    private int duration;
    private String status;

    public RequestedSession(String skillName, String requestedByUserId, String timing, int duration) {
        this.skillName = skillName;
        this.requestedByUserId = requestedByUserId;
        this.timing = timing;
        this.duration = duration;
        this.status = "PENDING";
    }

    public String getSkillName() { return skillName; }
    public String getRequestedByUserId() { return requestedByUserId; }
    public String getTiming() { return timing; }
    public int getDuration() { return duration; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "Request: " + skillName + " by " + requestedByUserId + " (Timing: " + timing + ", Duration: " + duration + " mins, Status: " + status + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestedSession that = (RequestedSession) o;
        return Objects.equals(skillName, that.skillName) &&
                Objects.equals(requestedByUserId, that.requestedByUserId) &&
                Objects.equals(timing, that.timing) &&
                duration == that.duration;
    }

    @Override
    public int hashCode() {
        return Objects.hash(skillName, requestedByUserId, timing, duration);
    }
}

class RequestedSessionManager {
    private static final List<RequestedSession> pendingRequests = new ArrayList<>();

    public static void addRequest(RequestedSession request) {
        if (!pendingRequests.contains(request)) {
            pendingRequests.add(request);
            System.out.println("Request added: " + request.getSkillName());
        } else {
            System.out.println("Request for '" + request.getSkillName() + "' by " + request.getRequestedByUserId() + " already exists.");
        }
    }

    public static List<RequestedSession> getAllRequests() {
        return new ArrayList<>(pendingRequests);
    }

    public static void removeRequest(RequestedSession request) {
        pendingRequests.remove(request);
        System.out.println("Request removed: " + request.getSkillName());
    }

    public static void clearRequests() {
        pendingRequests.clear();
    }
}

class Result {
    private String seekerId;
    private String skillName;
    private int marks;

    public Result(String seekerId, String skillName, int marks) {
        this.seekerId = seekerId;
        this.skillName = skillName;
        this.marks = marks;
    }

    public String getSeekerId() { return seekerId; }
    public String getSkillName() { return skillName; }
    public int getMarks() { return marks; }

    @Override
    public String toString() {
        return "Skill: " + skillName + " - Marks: " + marks;
    }
}

class ResultManager {
    private static final List<Result> allResults = new ArrayList<>();

    public static void addResult(Result result) {
        allResults.removeIf(r -> r.getSeekerId().equals(result.getSeekerId()) && r.getSkillName().equals(result.getSkillName()));
        allResults.add(result);
        System.out.println("Result added/updated: " + result.getSeekerId() + " - " + result.getSkillName() + ": " + result.getMarks());
    }

    public static List<Result> getResultsForSeeker(String seekerId) {
        return allResults.stream()
                .filter(r -> r.getSeekerId().equals(seekerId))
                .collect(Collectors.toList());
    }

    public static Optional<Result> getResultForSeekerAndSkill(String seekerId, String skillName) {
        return allResults.stream()
                .filter(r -> r.getSeekerId().equals(seekerId) && r.getSkillName().equalsIgnoreCase(skillName))
                .findFirst();
    }

    public static List<Result> getAllResults() {
        return new ArrayList<>(allResults);
    }

    public static void clearAllResults() {
        allResults.clear();
    }
}

class SkillSeeker extends AbstractPerson implements SessionActions {
    private List<String> enrolledSessionNames = new ArrayList<>();
    private List<RequestedSession> myRequestedSessions = new ArrayList<>();
    private List<Result> myResults = new ArrayList<>();

    public SkillSeeker(String name, String password) {
        super(AbstractPerson.generateUniqueId("seeker"), name, password);
    }

    public SkillSeeker(String id, String name, String password, List<String> enrolledSessionNames, List<RequestedSession> myRequestedSessions) {
        super(id, name, password);
        if (enrolledSessionNames != null) this.enrolledSessionNames.addAll(enrolledSessionNames);
        if (myRequestedSessions != null) this.myRequestedSessions.addAll(myRequestedSessions);
    }

    public void addResult(Result result) {
        myResults.removeIf(r -> r.getSkillName().equals(result.getSkillName()));
        myResults.add(result);
    }

    public List<Result> getMyResults() {
        return new ArrayList<>(myResults);
    }

    public List<String> getEnrolledSessionNames() {
        return new ArrayList<>(enrolledSessionNames);
    }

    public List<RequestedSession> getMyRequestedSessions() {
        return new ArrayList<>(myRequestedSessions);
    }

    @Override
    public void requestSession(String skillName, String timing, int duration) {
        if (duration <= 0 || duration > 120) {
            System.out.println("Invalid duration: " + duration + ". Duration must be between 1 and 120 minutes.");
            return;
        }
        RequestedSession newRequest = new RequestedSession(skillName, this.getId(), timing, duration);
        RequestedSessionManager.addRequest(newRequest);
        if (!myRequestedSessions.contains(newRequest)) {
            myRequestedSessions.add(newRequest);
        }
        System.out.println(getName() + " (" + getId() + ") requested a session for '" + skillName + "' at " + timing + " for " + duration + " minutes.");
    }

    @Override
    public void addSession(String skillName, String timing, int duration) {
        System.out.println(getName() + " (Skill Seeker) cannot add sessions. Only Skill Providers can add sessions.");
    }

    @Override
    public void enrollInSession(Session session) {
        if (session != null) {
            if (!session.isUserEnrolled(this.getId()) && !session.isFull()) {
                session.addEnrolledUser(this.getId());
                enrolledSessionNames.add(session.getSkillName());
                System.out.println(getName() + " successfully enrolled in session: " + session.getSkillName());
            } else {
                System.out.println(getName() + " failed to enroll in session: " + session.getSkillName() + ". Already enrolled or session is full.");
            }
        } else {
            System.out.println("Cannot enroll: Session object is null.");
        }
    }

    @Override
    public void rateSession(Session session, int rating) {
        if (session != null && enrolledSessionNames.contains(session.getSkillName())) {
            session.addRating(rating);
            System.out.println(getName() + " rated session '" + session.getSkillName() + "' as " + rating + " stars.");
        } else {
            System.out.println(getName() + " cannot rate session '" + (session != null ? session.getSkillName() : "null") + "'. Not enrolled or session not found.");
        }
    }

    @Override
    public void checkRatings(String skillName) {
        SessionManager.getSessionBySkillName(skillName).ifPresentOrElse(
                session -> System.out.println("Current average rating for '" + skillName + "': " + String.format("%.1f", session.getAverageSkillRating())),
                () -> System.out.println("Session '" + skillName + "' not found to check ratings.")
        );
    }
}

class SkillProvider extends AbstractPerson implements SessionActions {

    public SkillProvider(String name, String password) {
        super(AbstractPerson.generateUniqueId("provider"), name, password);
    }

    public SkillProvider(String id, String name, String password) {
        super(id, name, password);
    }

    @Override
    public void requestSession(String skillName, String timing, int duration) {
        System.out.println(getName() + " (Skill Provider) cannot request sessions. Only Skill Seekers can request sessions.");
    }

    @Override
    public void addSession(String skillName, String timing, int duration) {
        String skillCode = skillName.substring(0, Math.min(skillName.length(), 3)).toUpperCase() + (System.currentTimeMillis() % 10000);
        Session newSession = new Session(skillName, skillCode, timing, this.getName(), duration);
        SessionManager.addSession(newSession);
        System.out.println(getName() + " added a new session: " + newSession.getSkillName());
    }

    @Override
    public void enrollInSession(Session session) {
        System.out.println(getName() + " (Skill Provider) cannot enroll in sessions. Only Skill Seekers can enroll.");
    }

    @Override
    public void rateSession(Session session, int rating) {
        System.out.println(getName() + " (Skill Provider) cannot rate sessions.");
    }

    @Override
    public void checkRatings(String skillName) {
        SessionManager.getSessionBySkillName(skillName).ifPresentOrElse(
                session -> System.out.println("Current average rating for '" + skillName + "': " + String.format("%.1f", session.getAverageSkillRating())),
                () -> System.out.println("Session '" + skillName + "' not found to check ratings.")
        );
    }
}

public class SkillSharingApp extends Application {

    private Stage primaryStage;
    private AbstractPerson currentUser;
    private Map<String, AbstractPerson> registeredUsers = new HashMap<>();

    private static final String USERS_FILE = "users.txt";
    private static final String SESSIONS_FILE = "sessions.txt";
    private static final String REQUESTS_FILE = "requests.txt";
    private static final String RESULTS_FILE = "results.txt";
    private static final String LECTURES_FILE = "lectures.txt";
    private static final String QUIZZES_FILE = "quizzes.txt";
    private static final String ASSIGNMENTS_FILE = "assignments.txt";
    private static final String SUBMISSIONS_FILE = "submissions.txt";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Skill Sharing Community Platform");
        AbstractPerson.resetIdSequences();
        loadData();
        showLoginScreen();
    }

    @Override
    public void stop() {
        saveData();
        System.out.println("Application stopped and data saved.");
    }

    private void showLoginScreen() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));

        Label titleLabel = new Label("Skill Sharing Platform");
        titleLabel.getStyleClass().add("title-label");

        VBox loginCard = new VBox(15);
        loginCard.getStyleClass().add("card");
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setPadding(new Insets(20));

        Label cardTitle = new Label("Login");
        cardTitle.getStyleClass().add("subtitle-label");

        TextField idInput = new TextField();
        idInput.setPromptText("Enter your ID");
        TextField nameInput = new TextField();
        nameInput.setPromptText("Enter your Name");
        PasswordField passwordInput = new PasswordField();
        passwordInput.setPromptText("Enter your Password");

        Button loginButton = new Button("Login");
        loginButton.setOnAction(e -> {
            String id = idInput.getText().trim();
            String name = nameInput.getText().trim();
            String password = passwordInput.getText();
            if (!id.isEmpty() && !name.isEmpty() && !password.isEmpty()) {
                authenticateUser(id, name, password);
            } else {
                showAlert("Input Error", "Please enter ID, Name, and Password to login.");
            }
        });

        Button createAccountButton = new Button("Create Account");
        createAccountButton.setOnAction(e -> showCreateAccountScreen());

        loginCard.getChildren().addAll(cardTitle, idInput, nameInput, passwordInput, loginButton, createAccountButton);
        root.getChildren().addAll(titleLabel, loginCard);

        Scene scene = new Scene(root, 400, 450);
        scene.getStylesheets().add(ModernisticTheme.CSS);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void authenticateUser(String id, String name, String password) {
        if (registeredUsers.containsKey(id)) {
            AbstractPerson person = registeredUsers.get(id);
            if (Objects.equals(person.getName(), name) && Objects.equals(person.getPassword(), password)) {
                currentUser = person;
                currentUser.login();
                showMainScreen();
            } else {
                showAlert("Login Failed", "Incorrect name or password for ID: " + id + ". Please try again.");
            }
        } else {
            showAlert("Login Failed", "User with ID: " + id + " not found. Please create an account.");
        }
    }

    private void showCreateAccountScreen() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));

        Label titleLabel = new Label("Create Account");
        titleLabel.getStyleClass().add("title-label");

        VBox createAccountCard = new VBox(15);
        createAccountCard.getStyleClass().add("card");
        createAccountCard.setAlignment(Pos.CENTER);
        createAccountCard.setPadding(new Insets(20));

        Label cardTitle = new Label("Register");
        cardTitle.getStyleClass().add("subtitle-label");

        TextField nameInput = new TextField();
        nameInput.setPromptText("Enter your Name");
        PasswordField passwordInput = new PasswordField();
        passwordInput.setPromptText("Choose a Password");

        ToggleGroup roleGroup = new ToggleGroup();
        RadioButton seekerRadio = new RadioButton("Skill Seeker");
        seekerRadio.setToggleGroup(roleGroup);
        seekerRadio.setSelected(true);
        RadioButton providerRadio = new RadioButton("Skill Provider");
        providerRadio.setToggleGroup(roleGroup);

        HBox roleBox = new HBox(10, seekerRadio, providerRadio);
        roleBox.setAlignment(Pos.CENTER);

        Button registerButton = new Button("Register");
        registerButton.setOnAction(e -> {
            String name = nameInput.getText().trim();
            String password = passwordInput.getText();
            String type = "";
            if (seekerRadio.isSelected()) {
                type = "seeker";
            } else if (providerRadio.isSelected()) {
                type = "provider";
            }
            if (name.isEmpty() || password.isEmpty() || type.isEmpty()) {
                showAlert("Input Error", "Please enter your Name, Password, and select a role.");
                return;
            }
            String generatedId = AbstractPerson.generateUniqueId(type);
            if (type.equals("seeker")) {
                currentUser = new SkillSeeker(generatedId, name, password, null, null);
            } else {
                currentUser = new SkillProvider(generatedId, name, password);
            }
            registeredUsers.put(generatedId, currentUser);
            currentUser.login();
            showAlert("Registration Success", "Account created successfully for " + name + " as " + type + ".\nYour ID is: " + generatedId);
            showMainScreen();
        });

        Button backToLoginButton = new Button("Back to Login");
        backToLoginButton.setOnAction(e -> showLoginScreen());

        VBox buttonsBox = new VBox(10, registerButton, backToLoginButton);
        buttonsBox.setAlignment(Pos.CENTER);

        createAccountCard.getChildren().addAll(cardTitle, nameInput, passwordInput, roleBox, buttonsBox);
        root.getChildren().addAll(titleLabel, createAccountCard);

        Scene scene = new Scene(root, 400, 450);
        scene.getStylesheets().add(ModernisticTheme.CSS);
        primaryStage.setScene(scene);
    }

    private void showMainScreen() {
        VBox root = new VBox(20);
        root.getStyleClass().add("root");
        root.setPadding(new Insets(20));

        Label welcomeLabel = new Label("Welcome, " + currentUser.getName() + " (" + currentUser.getId() + ")!");
        welcomeLabel.getStyleClass().add("title-label");

        TextArea outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefHeight(150);
        outputArea.getStyleClass().add("text-area");
        outputArea.setPromptText("Application messages and results will appear here.");

        HBox topControls = new HBox(10, welcomeLabel, new HBox(logoutButton()));
        topControls.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(welcomeLabel, Priority.ALWAYS);

        VBox contentVBox = new VBox(20);
        contentVBox.setPadding(new Insets(0, 0, 20, 0));

        ScrollPane scrollPane = new ScrollPane(contentVBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("root");

        root.getChildren().addAll(topControls, outputArea, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        if (currentUser instanceof SkillSeeker) {
            addSkillSeekerControls(contentVBox, outputArea);
        } else if (currentUser instanceof SkillProvider) {
            addSkillProviderControls(contentVBox, outputArea);
        }

        Scene scene = new Scene(root, 800, 750);
        scene.getStylesheets().add(ModernisticTheme.CSS);
        primaryStage.setScene(scene);
    }

    private Button logoutButton() {
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> {
            currentUser.logout();
            currentUser = null;
            showLoginScreen();
        });
        return logoutButton;
    }

    private void addSkillSeekerControls(VBox root, TextArea outputArea) {
        // ... (existing code for Skill Seeker controls)

        // Session Viewing and Enrollment
        VBox sessionSection = new VBox(10);
        sessionSection.getStyleClass().add("card");
        Label sessionsLabel = new Label("Available Sessions:");
        sessionsLabel.getStyleClass().add("subtitle-label");
        ListView<Session> sessionListView = new ListView<>();
        sessionListView.setPrefHeight(150);
        updateSessionList(sessionListView);

        sessionListView.setCellFactory(lv -> new ListCell<Session>() {
            @Override
            protected void updateItem(Session session, boolean empty) {
                super.updateItem(session, empty);
                if (empty || session == null) {
                    setText(null);
                } else {
                    setText(session.toString());
                }
            }
        });

        HBox enrollControls = new HBox(10);
        enrollControls.setAlignment(Pos.CENTER_LEFT);
        Button enrollButton = new Button("Enroll in Selected Session");
        enrollButton.setOnAction(e -> {
            Session selectedSession = sessionListView.getSelectionModel().getSelectedItem();
            if (selectedSession != null) {
                ((SkillSeeker) currentUser).enrollInSession(selectedSession);
                updateSessionList(sessionListView);
            } else {
                showAlert("Selection Error", "Please select a session to enroll in.");
            }
        });
        enrollControls.getChildren().addAll(enrollButton);
        sessionSection.getChildren().addAll(sessionsLabel, sessionListView, enrollControls);

        // Requested Sessions
        VBox requestedSessionSection = new VBox(10);
        requestedSessionSection.getStyleClass().add("card");
        Label requestsLabel = new Label("Request a New Session:");
        requestsLabel.getStyleClass().add("subtitle-label");
        TextField skillNameInput = new TextField();
        skillNameInput.setPromptText("Skill Name");
        TextField timingInput = new TextField();
        timingInput.setPromptText("Timing (e.g., 'Mon 10am')");
        TextField durationInput = new TextField();
        durationInput.setPromptText("Duration in minutes");
        Button requestButton = new Button("Request Session");
        requestButton.setOnAction(e -> {
            try {
                String skillName = skillNameInput.getText().trim();
                String timing = timingInput.getText().trim();
                int duration = Integer.parseInt(durationInput.getText().trim());
                if (skillName.isEmpty() || timing.isEmpty()) {
                    showAlert("Input Error", "Please fill in all fields.");
                    return;
                }
                ((SkillSeeker) currentUser).requestSession(skillName, timing, duration);
                skillNameInput.clear();
                timingInput.clear();
                durationInput.clear();
            } catch (NumberFormatException ex) {
                showAlert("Input Error", "Please enter a valid number for duration.");
            }
        });
        requestedSessionSection.getChildren().addAll(requestsLabel, skillNameInput, timingInput, durationInput, requestButton);

        // Seeker Actions (Lectures, Quizzes, Assignments)
        VBox seekerActionsSection = new VBox(10);
        seekerActionsSection.getStyleClass().add("card");
        Label seekerActionsLabel = new Label("Skill Seeker Interactive Features");
        seekerActionsLabel.getStyleClass().add("subtitle-label");

        // Tabs for different features
        TabPane seekerTabPane = new TabPane();
        seekerTabPane.getStyleClass().add("tab-pane");

        // Tab for Lectures
        Tab lecturesTab = new Tab("Lectures");
        lecturesTab.setClosable(false);
        VBox lecturesContent = new VBox(10);
        lecturesContent.setPadding(new Insets(10));
        ComboBox<Session> lectureSessionCombo = new ComboBox<>();
        lectureSessionCombo.setPromptText("Select Skill to View Lecture");
        lectureSessionCombo.getItems().addAll(SessionManager.getAllSessions());
        lectureSessionCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Session session) {
                return session == null ? null : session.getSkillName();
            }
            @Override
            public Session fromString(String string) { return null; }
        });
        Button viewLectureButton = new Button("View Lecture");
        viewLectureButton.setOnAction(e -> {
            Session selectedSession = lectureSessionCombo.getSelectionModel().getSelectedItem();
            if (selectedSession != null) {
                LectureManager.getLectureBySkillName(selectedSession.getSkillName()).ifPresentOrElse(
                        lecture -> showAlert("Lecture Found", "Video file path for '" + selectedSession.getSkillName() + "':\n" + lecture.getVideoFilePath()),
                        () -> showAlert("Lecture Not Found", "No lecture found for '" + selectedSession.getSkillName() + "'.")
                );
            } else {
                showAlert("Selection Error", "Please select a skill.");
            }
        });
        lecturesContent.getChildren().addAll(lectureSessionCombo, viewLectureButton);
        lecturesTab.setContent(lecturesContent);

        // Tab for Quizzes
        Tab quizTab = new Tab("Quizzes");
        quizTab.setClosable(false);
        VBox quizContent = new VBox(10);
        quizContent.setPadding(new Insets(10));
        ComboBox<Session> quizSessionCombo = new ComboBox<>();
        quizSessionCombo.setPromptText("Select Skill to Take Quiz");
        quizSessionCombo.getItems().addAll(SessionManager.getAllSessions());
        quizSessionCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Session session) {
                return session == null ? null : session.getSkillName();
            }
            @Override
            public Session fromString(String string) { return null; }
        });
        Button takeQuizButton = new Button("Take Quiz");
        takeQuizButton.setOnAction(e -> {
            Session selectedSession = quizSessionCombo.getSelectionModel().getSelectedItem();
            if (selectedSession != null) {
                QuizManager.getQuizBySkillName(selectedSession.getSkillName()).ifPresentOrElse(
                        this::showQuizDialog,
                        () -> showAlert("Quiz Not Found", "No quiz found for '" + selectedSession.getSkillName() + "'.")
                );
            } else {
                showAlert("Selection Error", "Please select a skill.");
            }
        });
        quizContent.getChildren().addAll(quizSessionCombo, takeQuizButton);
        quizTab.setContent(quizContent);


        // Tab for Assignments
        Tab assignmentTab = new Tab("Assignments");
        assignmentTab.setClosable(false);
        VBox assignmentContent = new VBox(10);
        assignmentContent.setPadding(new Insets(10));
        ComboBox<Session> assignmentSessionCombo = new ComboBox<>();
        assignmentSessionCombo.setPromptText("Select Skill for Assignment");
        assignmentSessionCombo.getItems().addAll(SessionManager.getAllSessions());
        assignmentSessionCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Session session) {
                return session == null ? null : session.getSkillName();
            }
            @Override
            public Session fromString(String string) { return null; }
        });
        Button viewAssignmentButton = new Button("View Assignment");
        Button submitAssignmentButton = new Button("Submit Assignment");

        viewAssignmentButton.setOnAction(e -> {
            Session selectedSession = assignmentSessionCombo.getSelectionModel().getSelectedItem();
            if (selectedSession != null) {
                AssignmentManager.getAssignmentBySkillName(selectedSession.getSkillName()).ifPresentOrElse(
                        assignment -> showAlert("Assignment for " + selectedSession.getSkillName(), assignment.getDescription()),
                        () -> showAlert("Assignment Not Found", "No assignment found for '" + selectedSession.getSkillName() + "'.")
                );
            } else {
                showAlert("Selection Error", "Please select a skill.");
            }
        });

        submitAssignmentButton.setOnAction(e -> {
            Session selectedSession = assignmentSessionCombo.getSelectionModel().getSelectedItem();
            if (selectedSession != null) {
                AssignmentManager.getAssignmentBySkillName(selectedSession.getSkillName()).ifPresentOrElse(
                        assignment -> {
                            FileChooser fileChooser = new FileChooser();
                            fileChooser.setTitle("Select Assignment Submission File");
                            File file = fileChooser.showOpenDialog(primaryStage);
                            if (file != null) {
                                Assignment submission = new Assignment(assignment.getSkillName(), assignment.getDescription());
                                submission.setSubmittedByUserId(currentUser.getId());
                                submission.setFilePath(file.getAbsolutePath());
                                AssignmentManager.addSubmission(submission);
                                showAlert("Submission Success", "Assignment for '" + selectedSession.getSkillName() + "' submitted successfully.");
                            }
                        },
                        () -> showAlert("Assignment Not Found", "No assignment to submit for '" + selectedSession.getSkillName() + "'.")
                );
            } else {
                showAlert("Selection Error", "Please select a skill.");
            }
        });

        HBox assignmentButtons = new HBox(10, viewAssignmentButton, submitAssignmentButton);
        assignmentContent.getChildren().addAll(assignmentSessionCombo, assignmentButtons);
        assignmentTab.setContent(assignmentContent);

        seekerTabPane.getTabs().addAll(lecturesTab, quizTab, assignmentTab);
        seekerActionsSection.getChildren().addAll(seekerActionsLabel, seekerTabPane);


        // Add all sections to the root
        root.getChildren().addAll(sessionSection, requestedSessionSection, seekerActionsSection);
    }

    private void showQuizDialog(Quiz quiz) {
        Stage quizStage = new Stage();
        quizStage.setTitle("Quiz for " + quiz.getSkillName());

        VBox quizLayout = new VBox(15);
        quizLayout.setPadding(new Insets(20));
        quizLayout.setAlignment(Pos.CENTER);
        quizLayout.getStylesheets().add(ModernisticTheme.CSS);

        Label questionLabel = new Label();
        questionLabel.getStyleClass().add("subtitle-label");
        ToggleGroup answerGroup = new ToggleGroup();
        VBox optionsBox = new VBox(10);
        Button nextButton = new Button("Next");
        Button finishButton = new Button("Finish");

        List<RadioButton> optionRadios = new ArrayList<>();
        List<Integer> seekerAnswers = new ArrayList<>();

        int[] currentQuestionIndex = {0};

        Runnable updateQuizView = () -> {
            if (currentQuestionIndex[0] < quiz.getQuestions().size()) {
                Question currentQuestion = quiz.getQuestions().get(currentQuestionIndex[0]);
                questionLabel.setText("Question " + (currentQuestionIndex[0] + 1) + ": " + currentQuestion.getQuestionText());
                optionsBox.getChildren().clear();
                optionRadios.clear();
                int optionIndex = 0;
                for (String option : currentQuestion.getOptions()) {
                    RadioButton rb = new RadioButton(option);
                    rb.setToggleGroup(answerGroup);
                    optionsBox.getChildren().add(rb);
                    optionRadios.add(rb);
                }

                if (currentQuestionIndex[0] == quiz.getQuestions().size() - 1) {
                    nextButton.setManaged(false);
                    nextButton.setVisible(false);
                    finishButton.setManaged(true);
                    finishButton.setVisible(true);
                } else {
                    nextButton.setManaged(true);
                    nextButton.setVisible(true);
                    finishButton.setManaged(false);
                    finishButton.setVisible(false);
                }
            }
        };

        nextButton.setOnAction(e -> {
            int selectedIndex = -1;
            for (int i = 0; i < optionRadios.size(); i++) {
                if (optionRadios.get(i).isSelected()) {
                    selectedIndex = i;
                    break;
                }
            }
            if (selectedIndex != -1) {
                seekerAnswers.add(selectedIndex);
                currentQuestionIndex[0]++;
                updateQuizView.run();
                answerGroup.selectToggle(null); // Clear selection for next question
            } else {
                showAlert("Selection Error", "Please select an answer before moving on.");
            }
        });

        finishButton.setOnAction(e -> {
            int selectedIndex = -1;
            for (int i = 0; i < optionRadios.size(); i++) {
                if (optionRadios.get(i).isSelected()) {
                    selectedIndex = i;
                    break;
                }
            }
            if (selectedIndex != -1) {
                seekerAnswers.add(selectedIndex);
                int correctAnswers = 0;
                for (int i = 0; i < quiz.getQuestions().size(); i++) {
                    if (seekerAnswers.get(i) == quiz.getQuestions().get(i).getCorrectAnswerIndex()) {
                        correctAnswers++;
                    }
                }
                int score = (int) ((double) correctAnswers / quiz.getQuestions().size() * 100);
                ResultManager.addResult(new Result(currentUser.getId(), quiz.getSkillName(), score));
                showAlert("Quiz Complete", "You scored " + score + "%!");
                quizStage.close();
            } else {
                showAlert("Selection Error", "Please select an answer before finishing.");
            }
        });

        HBox buttons = new HBox(10, nextButton, finishButton);
        buttons.setAlignment(Pos.CENTER);
        finishButton.setManaged(false);
        finishButton.setVisible(false);

        quizLayout.getChildren().addAll(questionLabel, optionsBox, buttons);
        updateQuizView.run();

        Scene scene = new Scene(quizLayout, 500, 300);
        quizStage.setScene(scene);
        quizStage.show();
    }

    private void addSkillProviderControls(VBox root, TextArea outputArea) {
        // Existing provider controls (add sessions)
        VBox sessionSection = new VBox(10);
        sessionSection.getStyleClass().add("card");
        Label sessionsLabel = new Label("Add a New Session:");
        sessionsLabel.getStyleClass().add("subtitle-label");
        TextField skillNameInput = new TextField();
        skillNameInput.setPromptText("Skill Name");
        TextField timingInput = new TextField();
        timingInput.setPromptText("Timing (e.g., 'Mon 10am')");
        TextField durationInput = new TextField();
        durationInput.setPromptText("Duration in minutes");
        Button addSessionButton = new Button("Add Session");
        addSessionButton.setOnAction(e -> {
            try {
                String skillName = skillNameInput.getText().trim();
                String timing = timingInput.getText().trim();
                int duration = Integer.parseInt(durationInput.getText().trim());
                if (skillName.isEmpty() || timing.isEmpty()) {
                    showAlert("Input Error", "Please fill in all fields.");
                    return;
                }
                ((SkillProvider) currentUser).addSession(skillName, timing, duration);
                skillNameInput.clear();
                timingInput.clear();
                durationInput.clear();
            } catch (NumberFormatException ex) {
                showAlert("Input Error", "Please enter a valid number for duration.");
            }
        });
        sessionSection.getChildren().addAll(sessionsLabel, skillNameInput, timingInput, durationInput, addSessionButton);

        // Provider Actions (Lectures, Quizzes, Assignments)
        VBox providerActionsSection = new VBox(10);
        providerActionsSection.getStyleClass().add("card");
        Label providerActionsLabel = new Label("Skill Provider Interactive Features");
        providerActionsLabel.getStyleClass().add("subtitle-label");

        TabPane providerTabPane = new TabPane();
        providerTabPane.getStyleClass().add("tab-pane");

        // Tab for Lectures
        Tab lectureTab = new Tab("Upload Lecture");
        lectureTab.setClosable(false);
        VBox lectureContent = new VBox(10);
        lectureContent.setPadding(new Insets(10));
        ComboBox<Session> lectureSessionCombo = new ComboBox<>();
        lectureSessionCombo.setPromptText("Select a Session");
        lectureSessionCombo.getItems().addAll(SessionManager.getAllSessions().stream()
                .filter(s -> s.getSkillInstructor().equals(currentUser.getName()))
                .collect(Collectors.toList()));
        lectureSessionCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Session session) {
                return session == null ? null : session.getSkillName();
            }
            @Override
            public Session fromString(String string) { return null; }
        });
        Button uploadVideoButton = new Button("Upload Video (.mp4)");
        uploadVideoButton.setOnAction(e -> {
            Session selectedSession = lectureSessionCombo.getSelectionModel().getSelectedItem();
            if (selectedSession != null) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select Lecture Video");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MP4 Files", "*.mp4"));
                File file = fileChooser.showOpenDialog(primaryStage);
                if (file != null) {
                    Lecture newLecture = new Lecture(selectedSession.getSkillName(), file.getAbsolutePath());
                    LectureManager.addLecture(newLecture);
                    showAlert("Upload Successful", "Lecture video uploaded for '" + selectedSession.getSkillName() + "'.");
                }
            } else {
                showAlert("Selection Error", "Please select a session first.");
            }
        });
        lectureContent.getChildren().addAll(lectureSessionCombo, uploadVideoButton);
        lectureTab.setContent(lectureContent);

        // Tab for Quizzes
        Tab quizTab = new Tab("Create Quiz");
        quizTab.setClosable(false);
        VBox quizContent = new VBox(10);
        quizContent.setPadding(new Insets(10));
        ComboBox<Session> quizSessionCombo = new ComboBox<>();
        quizSessionCombo.setPromptText("Select a Session");
        quizSessionCombo.getItems().addAll(SessionManager.getAllSessions().stream()
                .filter(s -> s.getSkillInstructor().equals(currentUser.getName()))
                .collect(Collectors.toList()));
        quizSessionCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Session session) {
                return session == null ? null : session.getSkillName();
            }
            @Override
            public Session fromString(String string) { return null; }
        });
        Button createQuizButton = new Button("Create Quiz");
        createQuizButton.setOnAction(e -> {
            Session selectedSession = quizSessionCombo.getSelectionModel().getSelectedItem();
            if (selectedSession != null) {
                showQuizCreationDialog(selectedSession.getSkillName());
            } else {
                showAlert("Selection Error", "Please select a session first.");
            }
        });
        quizContent.getChildren().addAll(quizSessionCombo, createQuizButton);
        quizTab.setContent(quizContent);

        // Tab for Assignments
        Tab assignmentTab = new Tab("Create Assignment");
        assignmentTab.setClosable(false);
        VBox assignmentContent = new VBox(10);
        assignmentContent.setPadding(new Insets(10));
        ComboBox<Session> assignmentSessionCombo = new ComboBox<>();
        assignmentSessionCombo.setPromptText("Select a Session");
        assignmentSessionCombo.getItems().addAll(SessionManager.getAllSessions().stream()
                .filter(s -> s.getSkillInstructor().equals(currentUser.getName()))
                .collect(Collectors.toList()));
        assignmentSessionCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Session session) {
                return session == null ? null : session.getSkillName();
            }
            @Override
            public Session fromString(String string) { return null; }
        });
        Button createAssignmentButton = new Button("Create Assignment");
        createAssignmentButton.setOnAction(e -> {
            Session selectedSession = assignmentSessionCombo.getSelectionModel().getSelectedItem();
            if (selectedSession != null) {
                showAssignmentCreationDialog(selectedSession.getSkillName());
            } else {
                showAlert("Selection Error", "Please select a session first.");
            }
        });
        assignmentContent.getChildren().addAll(assignmentSessionCombo, createAssignmentButton);
        assignmentTab.setContent(assignmentContent);

        // Tab for Requests
        Tab requestsTab = new Tab("Requests");
        requestsTab.setClosable(false);
        VBox requestsContent = new VBox(10);
        requestsContent.setPadding(new Insets(10));

        Label requestsLabel = new Label("Pending Session Requests:");
        requestsLabel.getStyleClass().add("subtitle-label");

        ListView<RequestedSession> requestsListView = new ListView<>();
        requestsListView.setPrefHeight(200);
        requestsListView.getItems().addAll(RequestedSessionManager.getAllRequests());

        requestsListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(RequestedSession item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });

        HBox requestButtons = new HBox(10);
        Button fulfillButton = new Button("Fulfill Request");
        Button denyButton = new Button("Deny Request");

        fulfillButton.setOnAction(e -> {
            RequestedSession selectedRequest = requestsListView.getSelectionModel().getSelectedItem();
            if (selectedRequest != null) {
                ((SkillProvider) currentUser).addSession(selectedRequest.getSkillName(), selectedRequest.getTiming(), selectedRequest.getDuration());
                RequestedSessionManager.removeRequest(selectedRequest);
                requestsListView.getItems().remove(selectedRequest);
                showAlert("Request Fulfilled", "Session for '" + selectedRequest.getSkillName() + "' created successfully.");
            } else {
                showAlert("Selection Error", "Please select a request to fulfill.");
            }
        });

        denyButton.setOnAction(e -> {
            RequestedSession selectedRequest = requestsListView.getSelectionModel().getSelectedItem();
            if (selectedRequest != null) {
                RequestedSessionManager.removeRequest(selectedRequest);
                requestsListView.getItems().remove(selectedRequest);
                showAlert("Request Denied", "Request for '" + selectedRequest.getSkillName() + "' has been denied.");
            } else {
                showAlert("Selection Error", "Please select a request to deny.");
            }
        });

        requestButtons.getChildren().addAll(fulfillButton, denyButton);

        requestsContent.getChildren().addAll(requestsLabel, requestsListView, requestButtons);
        requestsTab.setContent(requestsContent);

        providerTabPane.getTabs().addAll(lectureTab, quizTab, assignmentTab, requestsTab);
        providerActionsSection.getChildren().addAll(providerActionsLabel, providerTabPane);


        root.getChildren().addAll(sessionSection, providerActionsSection);
    }

    private void showQuizCreationDialog(String skillName) {
        Stage quizCreatorStage = new Stage();
        quizCreatorStage.setTitle("Create Quiz for " + skillName);
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.getStylesheets().add(ModernisticTheme.CSS);

        Quiz newQuiz = new Quiz(skillName);

        Label questionLabel = new Label("Question Text:");
        TextArea questionText = new TextArea();
        questionText.setPromptText("Enter the question here...");
        questionText.setPrefRowCount(2);

        List<TextField> optionFields = new ArrayList<>();
        Label optionsLabel = new Label("Options:");
        VBox optionsBox = new VBox(5);
        for (int i = 0; i < 4; i++) {
            TextField optionField = new TextField();
            optionField.setPromptText("Option " + (i + 1));
            optionsBox.getChildren().add(optionField);
            optionFields.add(optionField);
        }

        Label correctLabel = new Label("Correct Answer Index (0-3):");
        TextField correctIndexField = new TextField();
        correctIndexField.setPromptText("e.g., 0 for the first option");

        Button addQuestionButton = new Button("Add Question");
        Button saveQuizButton = new Button("Save Quiz");

        addQuestionButton.setOnAction(e -> {
            if (!questionText.getText().isEmpty() && !optionsBox.getChildren().isEmpty() && !correctIndexField.getText().isEmpty()) {
                List<String> options = optionFields.stream().map(TextField::getText).collect(Collectors.toList());
                try {
                    int correctIndex = Integer.parseInt(correctIndexField.getText());
                    if (correctIndex >= 0 && correctIndex < options.size()) {
                        newQuiz.addQuestion(new Question(questionText.getText(), options, correctIndex));
                        showAlert("Question Added", "Question added successfully. Add another or save the quiz.");
                        questionText.clear();
                        optionFields.forEach(TextField::clear);
                        correctIndexField.clear();
                    } else {
                        showAlert("Input Error", "Correct index must be between 0 and 3.");
                    }
                } catch (NumberFormatException ex) {
                    showAlert("Input Error", "Correct index must be a number.");
                }
            } else {
                showAlert("Input Error", "Please fill in all fields to add a question.");
            }
        });

        saveQuizButton.setOnAction(e -> {
            if (newQuiz.getQuestions().isEmpty()) {
                showAlert("Save Error", "Please add at least one question to the quiz.");
            } else {
                QuizManager.addQuiz(newQuiz);
                showAlert("Quiz Saved", "Quiz for " + skillName + " has been saved successfully.");
                quizCreatorStage.close();
            }
        });

        root.getChildren().addAll(questionLabel, questionText, optionsLabel, optionsBox, correctLabel, correctIndexField, addQuestionButton, saveQuizButton);
        Scene scene = new Scene(root, 400, 450);
        quizCreatorStage.setScene(scene);
        quizCreatorStage.show();
    }

    private void showAssignmentCreationDialog(String skillName) {
        Stage assignmentCreatorStage = new Stage();
        assignmentCreatorStage.setTitle("Create Assignment for " + skillName);
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.getStylesheets().add(ModernisticTheme.CSS);

        Label descriptionLabel = new Label("Assignment Description:");
        TextArea descriptionText = new TextArea();
        descriptionText.setPromptText("Enter the assignment description here...");
        descriptionText.setPrefRowCount(5);

        Button createButton = new Button("Create Assignment");
        createButton.setOnAction(e -> {
            if (!descriptionText.getText().isEmpty()) {
                Assignment newAssignment = new Assignment(skillName, descriptionText.getText());
                AssignmentManager.addAssignment(newAssignment);
                showAlert("Assignment Created", "Assignment for '" + skillName + "' has been created successfully.");
                assignmentCreatorStage.close();
            } else {
                showAlert("Input Error", "Please enter a description for the assignment.");
            }
        });

        root.getChildren().addAll(descriptionLabel, descriptionText, createButton);
        Scene scene = new Scene(root, 400, 300);
        assignmentCreatorStage.setScene(scene);
        assignmentCreatorStage.show();
    }

    private void updateSessionList(ListView<Session> sessionListView) {
        sessionListView.getItems().clear();
        SessionManager.getAllSessions().forEach(session -> sessionListView.getItems().add(session));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void saveData() {
        // Save users
        try (PrintWriter writer = new PrintWriter(USERS_FILE)) {
            for (AbstractPerson person : registeredUsers.values()) {
                if (person instanceof SkillSeeker) {
                    SkillSeeker seeker = (SkillSeeker) person;
                    String enrolledSkills = String.join(",", seeker.getEnrolledSessionNames());
                    writer.println("seeker|" + seeker.getId() + "|" + seeker.getName() + "|" + seeker.getPassword() + "|" + enrolledSkills);
                } else if (person instanceof SkillProvider) {
                    SkillProvider provider = (SkillProvider) person;
                    writer.println("provider|" + provider.getId() + "|" + provider.getName() + "|" + provider.getPassword());
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }

        // Save sessions
        try (PrintWriter writer = new PrintWriter(SESSIONS_FILE)) {
            for (Session session : SessionManager.getAllSessions()) {
                String enrolledUsers = String.join(",", session.getEnrolledUserIds());
                writer.println(session.getSkillName() + "|" + session.getSkillCode() + "|" + session.getSkillSlotTiming() + "|" + session.getSkillInstructor() + "|" + session.getSessionDuration() + "|" + session.getRawSkillRating() + "|" + session.getNumberOfRatings() + "|" + enrolledUsers);
            }
        } catch (IOException e) {
            System.err.println("Error saving sessions: " + e.getMessage());
        }

        // Save requests
        try (PrintWriter writer = new PrintWriter(REQUESTS_FILE)) {
            for (RequestedSession request : RequestedSessionManager.getAllRequests()) {
                writer.println(request.getSkillName() + "|" + request.getRequestedByUserId() + "|" + request.getTiming() + "|" + request.getDuration() + "|" + request.getStatus());
            }
        } catch (IOException e) {
            System.err.println("Error saving requests: " + e.getMessage());
        }

        // Save results
        try (PrintWriter writer = new PrintWriter(RESULTS_FILE)) {
            for (Result result : ResultManager.getAllResults()) {
                writer.println(result.getSeekerId() + "|" + result.getSkillName() + "|" + result.getMarks());
            }
        } catch (IOException e) {
            System.err.println("Error saving results: " + e.getMessage());
        }

        // Save lectures
        try (PrintWriter writer = new PrintWriter(LECTURES_FILE)) {
            for (Lecture lecture : LectureManager.getAllLectures()) {
                writer.println(lecture.toString());
            }
        } catch (IOException e) {
            System.err.println("Error saving lectures: " + e.getMessage());
        }

        // Save quizzes
        try (PrintWriter writer = new PrintWriter(QUIZZES_FILE)) {
            for (Quiz quiz : QuizManager.getAllQuizzes()) {
                writer.println(quiz.toString());
            }
        } catch (IOException e) {
            System.err.println("Error saving quizzes: " + e.getMessage());
        }

        // Save assignments and submissions
        try (PrintWriter writer = new PrintWriter(ASSIGNMENTS_FILE)) {
            for (Assignment assignment : AssignmentManager.getAllAssignments()) {
                writer.println(assignment.toString());
            }
        } catch (IOException e) {
            System.err.println("Error saving assignments: " + e.getMessage());
        }
        try (PrintWriter writer = new PrintWriter(SUBMISSIONS_FILE)) {
            for (Assignment submission : AssignmentManager.getAllSubmissions()) {
                writer.println(submission.toString());
            }
        } catch (IOException e) {
            System.err.println("Error saving submissions: " + e.getMessage());
        }
    }


    private void loadData() {
        System.out.println("--- Loading Data ---");

        // Load users
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            AbstractPerson.resetTotalUsers();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 4) {
                    String type = parts[0];
                    String id = parts[1];
                    String name = parts[2];
                    String password = parts[3];
                    AbstractPerson.updateIdCountersFromFile(id);

                    if ("seeker".equals(type) && parts.length > 4) {
                        List<String> enrolledSkills = new ArrayList<>();
                        if (!parts[4].isEmpty()) {
                            enrolledSkills = Arrays.asList(parts[4].split(","));
                        }
                        SkillSeeker seeker = new SkillSeeker(id, name, password, enrolledSkills, null);
                        registeredUsers.put(id, seeker);
                    } else if ("provider".equals(type)) {
                        SkillProvider provider = new SkillProvider(id, name, password);
                        registeredUsers.put(id, provider);
                    }
                }
            }
            System.out.println("User data loaded from " + USERS_FILE);
        } catch (FileNotFoundException e) {
            System.out.println("No existing user data file found (" + USERS_FILE + "). Starting fresh.");
        } catch (IOException e) {
            System.err.println("Error loading user data: " + e.getMessage());
        }

        // Load sessions
        try (BufferedReader reader = new BufferedReader(new FileReader(SESSIONS_FILE))) {
            String line;
            SessionManager.clearAllSessions();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 7) {
                    String skillName = parts[0];
                    String skillCode = parts[1];
                    String timing = parts[2];
                    String instructor = parts[3];
                    int duration = Integer.parseInt(parts[4]);
                    int rating = Integer.parseInt(parts[5]);
                    int numRatings = Integer.parseInt(parts[6]);
                    List<String> enrolledUserIds = new ArrayList<>();
                    if (parts.length > 7 && !parts[7].isEmpty()) {
                        enrolledUserIds = Arrays.asList(parts[7].split(","));
                    }
                    Session session = new Session(skillName, skillCode, timing, instructor, duration, rating, numRatings, enrolledUserIds);
                    SessionManager.addSession(session);
                }
            }
            System.out.println("Session data loaded from " + SESSIONS_FILE);
        } catch (FileNotFoundException e) {
            System.out.println("No existing session data file found (" + SESSIONS_FILE + "). Starting fresh.");
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading session data: " + e.getMessage());
        }

        // Load requests
        try (BufferedReader reader = new BufferedReader(new FileReader(REQUESTS_FILE))) {
            String line;
            RequestedSessionManager.clearRequests();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 5) {
                    String skillName = parts[0];
                    String userId = parts[1];
                    String timing = parts[2];
                    int duration = Integer.parseInt(parts[3]);
                    String status = parts[4];
                    RequestedSession request = new RequestedSession(skillName, userId, timing, duration);
                    request.setStatus(status);
                    RequestedSessionManager.addRequest(request);
                    AbstractPerson seeker = registeredUsers.get(userId);
                    if (seeker instanceof SkillSeeker) {
                        ((SkillSeeker) seeker).getMyRequestedSessions().add(request);
                    }
                }
            }
            System.out.println("Request data loaded from " + REQUESTS_FILE);
        } catch (FileNotFoundException e) {
            System.out.println("No existing request data file found (" + REQUESTS_FILE + "). Starting fresh.");
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading request data: " + e.getMessage());
        }

        // Load results
        try (BufferedReader reader = new BufferedReader(new FileReader(RESULTS_FILE))) {
            String line;
            ResultManager.clearAllResults();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 3) {
                    String seekerId = parts[0];
                    String skillName = parts[1];
                    int marks = Integer.parseInt(parts[2]);
                    Result result = new Result(seekerId, skillName, marks);
                    ResultManager.addResult(result);
                    AbstractPerson seeker = registeredUsers.get(seekerId);
                    if (seeker instanceof SkillSeeker) {
                        ((SkillSeeker) seeker).addResult(result);
                    }
                }
            }
            System.out.println("Result data loaded from " + RESULTS_FILE);
        } catch (FileNotFoundException e) {
            System.out.println("No existing result data file found (" + RESULTS_FILE + "). Starting fresh.");
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading result data: " + e.getMessage());
        }

        // Load lectures
        try (BufferedReader reader = new BufferedReader(new FileReader(LECTURES_FILE))) {
            String line;
            LectureManager.clearAllLectures();
            while ((line = reader.readLine()) != null) {
                Lecture lecture = Lecture.fromString(line);
                LectureManager.addLecture(lecture);
            }
            System.out.println("Lecture data loaded from " + LECTURES_FILE);
        } catch (FileNotFoundException e) {
            System.out.println("No existing lecture data file found (" + LECTURES_FILE + "). Starting fresh.");
        } catch (IOException e) {
            System.err.println("Error loading lecture data: " + e.getMessage());
        }

        // Load quizzes
        try (BufferedReader reader = new BufferedReader(new FileReader(QUIZZES_FILE))) {
            String line;
            QuizManager.clearAllQuizzes();
            while ((line = reader.readLine()) != null) {
                Quiz quiz = Quiz.fromString(line);
                QuizManager.addQuiz(quiz);
            }
            System.out.println("Quiz data loaded from " + QUIZZES_FILE);
        } catch (FileNotFoundException e) {
            System.out.println("No existing quiz data file found (" + QUIZZES_FILE + "). Starting fresh.");
        } catch (IOException e) {
            System.err.println("Error loading quiz data: " + e.getMessage());
        }

        // Load assignments and submissions
        try (BufferedReader reader = new BufferedReader(new FileReader(ASSIGNMENTS_FILE))) {
            String line;
            AssignmentManager.clearAllAssignments();
            while ((line = reader.readLine()) != null) {
                Assignment assignment = Assignment.fromString(line);
                AssignmentManager.addAssignment(assignment);
            }
            System.out.println("Assignment data loaded from " + ASSIGNMENTS_FILE);
        } catch (FileNotFoundException e) {
            System.out.println("No existing assignment data file found (" + ASSIGNMENTS_FILE + "). Starting fresh.");
        } catch (IOException e) {
            System.err.println("Error loading assignment data: " + e.getMessage());
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(SUBMISSIONS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Assignment submission = Assignment.fromString(line);
                AssignmentManager.addSubmission(submission);
            }
            System.out.println("Submission data loaded from " + SUBMISSIONS_FILE);
        } catch (FileNotFoundException e) {
            System.out.println("No existing submission data file found (" + SUBMISSIONS_FILE + "). Starting fresh.");
        } catch (IOException e) {
            System.err.println("Error loading submission data: " + e.getMessage());
        }
        System.out.println("--- Data Loading Complete ---");
    }
}