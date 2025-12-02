package com.fitmatch;

import com.fitmatch.model.*;
import com.fitmatch.service.*;
import com.fitmatch.util.*;
import com.fitmatch.exception.*;

import java.util.*;
import java.io.*;
import java.nio.file.*;

/**
 * FitMatch - Intelligent Team Formation System
 * Version 2.2 - Updated with role-based access control
 */
public class FitMatchApplication {

    private static final String ALL_PARTICIPANTS_FILE = "allParticipants.csv";
    private static final String SAMPLE_FILE = "participants_sample.csv";
    private static final String OUTPUT_FILE = "formed_teams.csv";
    private static final int DEFAULT_TEAM_SIZE = 4;
    private static final int MIN_TEAM_SIZE = 3;

    // User roles
    private enum UserRole {
        PARTICIPANT, MANAGEMENT
    }

    // Passwords
    private static final String PARTICIPANT_PASSWORD = "Piri1";
    private static final String MANAGEMENT_PASSWORD = "Piri2";

    private Scanner scanner;
    private TeamBuilder teamBuilder;
    private List<Participant> loadedParticipants;
    private List<Team> formedTeams;
    private UserRole currentUserRole;

    public FitMatchApplication() {
        this.scanner = new Scanner(System.in);
        this.teamBuilder = new TeamBuilder();
        this.loadedParticipants = new ArrayList<>();
        this.formedTeams = new ArrayList<>();
        this.currentUserRole = null;
        FileLogger.setupLogging();
    }

    public static void main(String[] args) {
        FitMatchApplication app = new FitMatchApplication();

        try {
            app.run();
        } catch (Exception e) {
            FileLogger.logError("Application", "Fatal error", e);
            e.printStackTrace();
        } finally {
            app.cleanup();
        }
    }

    public void run() {
        printWelcomeBanner();
        FileLogger.logInfo("Application", "FitMatch system started");

        boolean running = true;
        this.autoLoadParticipants();
        while (running) {
            try {
                // Role selection and authentication
                if (currentUserRole == null) {
                    if (!authenticateUser()) {
                        continue;
                    }
                }

                // Display role-appropriate menu
                if (currentUserRole == UserRole.PARTICIPANT) {
                    running = handleParticipantMenu();
                } else {
                    running = handleManagementMenu();
                }

            } catch (Exception e) {
                FileLogger.logWarning("Application", "Error: " + e.getMessage());
                System.out.println("\nError: " + e.getMessage());
                System.out.println("Please try again.\n");
            }
        }
    }

    private boolean authenticateUser() {
        System.out.println("\n================================================================");
        System.out.println("                    ROLE SELECTION                            ");
        System.out.println("================================================================");
        System.out.println("  1. Participant");
        System.out.println("  2. Management");
        System.out.println("  3. Exit");
        System.out.println("================================================================");
        System.out.print("Select your role (1-3): ");

        int roleChoice = getUserChoice();

        if (roleChoice == 3) {
            printExitMessage();
            System.exit(0);
        }

        if (roleChoice != 1 && roleChoice != 2) {
            System.out.println("\nInvalid choice. Please try again.");
            return false;
        }

        // Password authentication
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        if (roleChoice == 1) {
            if (password.equals(PARTICIPANT_PASSWORD)) {
                currentUserRole = UserRole.PARTICIPANT;
                System.out.println("\n✓ Authenticated as Participant");
                FileLogger.logInfo("Application", "Participant logged in");
                return true;
            }
        } else if (roleChoice == 2) {
            if (password.equals(MANAGEMENT_PASSWORD)) {
                currentUserRole = UserRole.MANAGEMENT;
                System.out.println("\n✓ Authenticated as Management");
                FileLogger.logInfo("Application", "Management logged in");
                return true;
            }
        }

        System.out.println("\n✗ Authentication failed. Incorrect password.");
        return false;
    }

    private boolean handleParticipantMenu() {
        printParticipantMenu();
        int choice = getUserChoice();

        switch (choice) {
            case 1:
                addNewParticipant();
                break;
            case 2:
                displayLoadedParticipants();
                break;
            case 3:
                // Return to role selection
                currentUserRole = null;
                System.out.println("\nReturning to role selection...");
                break;
            case 4:
                printExitMessage();
                return false;
            default:
                System.out.println("\nInvalid choice. Please enter 1-4.");
        }

        return true;
    }

    private boolean handleManagementMenu() {
        printManagementMenu();
        int choice = getUserChoice();

        switch (choice) {
            case 1:
                addNewParticipant();
                break;
            case 2:
                formTeams();
                break;
            case 3:
                displayLoadedParticipants();
                break;
            case 4:
                displayFormedTeams();
                break;
            case 5:
                saveTeamsToFile();
                break;
            case 6:
                displayStatistics();
                break;
            case 7:
                // Return to role selection
                currentUserRole = null;
                System.out.println("\nReturning to role selection...");
                break;
            case 8:
                printExitMessage();
                return false;
            default:
                System.out.println("\nInvalid choice. Please enter 1-10.");
        }

        return true;
    }

    private void printWelcomeBanner() {
        System.out.println("\n================================================================");
        System.out.println("                                                                ");
        System.out.println("       TEAMMATE - Intelligent Team Formation System            ");
        System.out.println("        University Gaming Club Team Builder                    ");
        System.out.println("                                                                ");
        System.out.println("================================================================\n");
    }

    private void printParticipantMenu() {
        System.out.println("\n================================================================");
        System.out.println("                  PARTICIPANT MENU                            ");
        System.out.println("================================================================");
        System.out.println("  1.  Add New Participant (Complete Survey)                  ");
        System.out.println("  2.  View Participants                                       ");
        System.out.println("  3.  Return to Role Selection                                ");
        System.out.println("  4.  Exit                                                    ");
        System.out.println("================================================================");
        System.out.print("Enter choice (1-4): ");
    }

    private void printManagementMenu() {
        System.out.println("\n================================================================");
        System.out.println("                    MANAGEMENT MENU                           ");
        System.out.println("================================================================");
        System.out.println("  1.  Add New Participant                                     ");
        System.out.println("  2.  Form Teams (Min 3, 1-2 Leaders per team)               ");
        System.out.println("  3.  View Loaded Participants                                ");
        System.out.println("  4.  View Formed Teams                                       ");
        System.out.println("  5.  Save Teams to CSV                                       ");
        System.out.println("  6.  Display Statistics                                      ");
        System.out.println("  7.  Return to Role Selection                                ");
        System.out.println("  8. Exit                                                    ");
        System.out.println("================================================================");
        System.out.print("Enter choice (1-10): ");
    }

    private void printExitMessage() {
        System.out.println("\n================================================================");
        System.out.println("          Thank you for using TeamMate!                       ");
        System.out.println("================================================================\n");
        FileLogger.logInfo("Application", "FitMatch system shutdown");
    }

    private int getUserChoice() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Option 1 (Management): Auto-load participants
     */
    private void autoLoadParticipants() {
        System.out.println("\n----------- LOAD PARTICIPANTS -----------");

        try {
            List<Participant> loaded = new ArrayList<>();

            if (Files.exists(Paths.get(ALL_PARTICIPANTS_FILE))) {
                System.out.println("Loading from: " + ALL_PARTICIPANTS_FILE);
                loaded = CSVDataHandler.readParticipantsFromCSV(ALL_PARTICIPANTS_FILE);
                System.out.println("Loaded " + loaded.size() + " participants from " + ALL_PARTICIPANTS_FILE);
            } else if (Files.exists(Paths.get(SAMPLE_FILE))) {
                System.out.println(ALL_PARTICIPANTS_FILE + " not found.");
                System.out.println("Loading from: " + SAMPLE_FILE);
                loaded = CSVDataHandler.readParticipantsFromCSV(SAMPLE_FILE);
                System.out.println("Loaded " + loaded.size() + " participants from " + SAMPLE_FILE);
                copyToAllParticipants(loaded);
            } else {
                System.out.println("No participant files found.");
                System.out.println("Use Option 3 to create sample data.");
                return;
            }

            loadedParticipants = loaded;
            System.out.println("\nSuccessfully loaded " + loadedParticipants.size() + " participants!");
            FileLogger.logInfo("Application", "Loaded " + loadedParticipants.size() + " participants");

            displayParticipantSummary();

        } catch (Exception e) {
            System.out.println("\nError: " + e.getMessage());
            FileLogger.logWarning("Application", "Load failed: " + e.getMessage());
        }
    }

    private void copyToAllParticipants(List<Participant> participants) {
        try {
            writeParticipantsToCSV(participants, ALL_PARTICIPANTS_FILE);
            System.out.println("Created " + ALL_PARTICIPANTS_FILE + " for future use.");
        } catch (Exception e) {
            FileLogger.logWarning("Application", "Could not create " + ALL_PARTICIPANTS_FILE);
        }
    }

    /**
     * Add new participant with 5-question survey (available to both roles)
     */
    private void addNewParticipant() {
        System.out.println("\n----------- ADD NEW PARTICIPANT -----------");
        FileLogger.logInfo("Application", "Adding new participant");

        try {
            String participantId = getNextParticipantId();
            System.out.println("Assigned ID: " + participantId);

            String name = getValidatedInput(
                    "Enter Name: ",
                    input -> InputValidator.validateName(input)
            );

            String email = getValidatedInput(
                    "Enter Email: ",
                    input -> InputValidator.validateEmail(input).toLowerCase()
            );

            System.out.println("\nAvailable Games:");
            GameType[] games = GameType.values();
            for (int i = 0; i < games.length; i++) {
                System.out.printf("  %d. %s%n", i + 1, games[i].getDisplayName());
            }
            GameType game = games[getNumberChoice("Select game (1-" + games.length + "): ", 1, games.length) - 1];

            System.out.println("\nSkill Level (1=Beginner, 10=Expert):");
            int skillLevel = getNumberChoice("Enter skill (1-10): ", 1, 10);

            System.out.println("\nAvailable Roles:");
            PlayingRole[] roles = PlayingRole.values();
            for (int i = 0; i < roles.length; i++) {
                System.out.printf("  %d. %s - %s%n", i + 1, roles[i].getDisplayName(), roles[i].getDescription());
            }
            PlayingRole role = roles[getNumberChoice("Select role (1-" + roles.length + "): ", 1, roles.length) - 1];

            System.out.println("\n========== PERSONALITY ASSESSMENT ==========");
            System.out.println("Please rate each statement from 1 (Strongly Disagree) to 5 (Strongly Agree):");
            System.out.println();

            String[] questions = {
                    "Q1. I enjoy taking the lead and guiding others during group activities.",
                    "Q2. I prefer analyzing situations and coming up with strategic solutions.",
                    "Q3. I work well with others and enjoy collaborative teamwork.",
                    "Q4. I am calm under pressure and can help maintain team morale.",
                    "Q5. I like making quick decisions and adapting in dynamic situations."
            };

            int totalScore = 0;
            for (int i = 0; i < questions.length; i++) {
                System.out.println(questions[i]);
                int response = getNumberChoice("Rating (1-5): ", 1, 5);
                totalScore += response;
                System.out.println();
            }

            int personalityScore = totalScore * 4;

            Participant newParticipant = new Participant(
                    participantId, name, email, 20,
                    game, skillLevel, role, personalityScore
            );

            loadedParticipants.add(newParticipant);
            appendParticipantToAllFile(newParticipant);

            System.out.println("\n========================================");
            System.out.println("Participant added successfully!");
            System.out.println("  ID: " + newParticipant.getParticipantId());
            System.out.println("  Name: " + newParticipant.getName());
            System.out.println("  Email: " + newParticipant.getEmail());
            System.out.println("  Survey Score: " + totalScore + "/25");
            System.out.println("  Personality Score: " + personalityScore + "/100");
            System.out.println("  Personality Type: " + newParticipant.getPersonalityType().getDisplayName());
            System.out.println("  Game: " + newParticipant.getPreferredGame().getDisplayName());
            System.out.println("  Skill: " + newParticipant.getSkillLevel() + "/10");
            System.out.println("  Role: " + newParticipant.getPreferredRole().getDisplayName());
            System.out.println("========================================");

            FileLogger.logInfo("Application", "Added participant: " + participantId);

        } catch (Exception e) {
            System.out.println("\nError: " + e.getMessage());
            FileLogger.logError("Application", "Failed to add participant", e);
        }
    }

    private void appendParticipantToAllFile(Participant participant) {
        try {
            Path path = Paths.get(ALL_PARTICIPANTS_FILE);
            boolean exists = Files.exists(path);

            try (BufferedWriter writer = Files.newBufferedWriter(path,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

                if (!exists) {
                    writer.write("ID,Name,Email,PreferredGame,SkillLevel,PreferredRole,PersonalityScore,PersonalityType");
                    writer.newLine();
                }

                writer.write(participant.toCSV());
                writer.newLine();
            }

            FileLogger.logInfo("Application", "Saved to " + ALL_PARTICIPANTS_FILE);

        } catch (IOException e) {
            FileLogger.logError("Application", "Failed to save participant", e);
        }
    }

    private void writeParticipantsToCSV(List<Participant> participants, String fileName) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName))) {
            writer.write("ID,Name,Email,PreferredGame,SkillLevel,PreferredRole,PersonalityScore,PersonalityType");
            writer.newLine();
            for (Participant p : participants) {
                writer.write(p.toCSV());
                writer.newLine();
            }
        }
    }

    private String getNextParticipantId() {
        int maxNum = 0;
        for (Participant p : loadedParticipants) {
            String id = p.getParticipantId();
            if (id.startsWith("P")) {
                try {
                    int num = Integer.parseInt(id.substring(1));
                    maxNum = Math.max(maxNum, num);
                } catch (NumberFormatException e) {
                    // Skip invalid IDs
                }
            }
        }
        return String.format("P%03d", maxNum + 1);
    }

    private interface InputValidatorFunc<T> {
        T validate(String input) throws Exception;
    }

    private <T> T getValidatedInput(String prompt, InputValidatorFunc<T> validator)
            throws Exception {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            try {
                return validator.validate(input);
            } catch (Exception e) {
                System.out.println("  Error: " + e.getMessage());
                System.out.print("  Try again? (y/n): ");
                if (!scanner.nextLine().trim().equalsIgnoreCase("y")) {
                    throw e;
                }
            }
        }
    }

    private int getNumberChoice(String prompt, int min, int max) {
        while (true) {
            try {
                System.out.print(prompt);
                int choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice >= min && choice <= max) {
                    return choice;
                }
                System.out.println("  Enter a number between " + min + " and " + max);
            } catch (NumberFormatException e) {
                System.out.println("  Invalid number");
            }
        }
    }

    /**
     * Option 3 (Management): Create sample data
     */
    private void createSampleData() {
        System.out.println("\n----------- CREATE SAMPLE DATA -----------");

        try {
            CSVDataHandler.createSampleCSV(SAMPLE_FILE);

            System.out.println("\nSample file created: " + SAMPLE_FILE);
            System.out.println("  Contains 20 participants with diverse attributes");
            FileLogger.logInfo("Application", "Created sample file: " + SAMPLE_FILE);

        } catch (DataLoadException e) {
            System.out.println("\nError: " + e.getMessage());
            FileLogger.logError("Application", "Sample creation failed", e);
        }
    }

    /**
     * Option 4 (Management): Form teams with strict rules
     */
    private void formTeams() {
        System.out.println("\n----------- FORM TEAMS -----------");

        if (loadedParticipants.isEmpty()) {
            System.out.println("\nNo participants loaded. Use option 1.");
            return;
        }

        long leaderCount = loadedParticipants.stream()
                .filter(p -> p.getPersonalityType() == PersonalityType.LEADER)
                .count();

        if (leaderCount == 0) {
            System.out.println("\nERROR: Cannot form teams without leaders!");
            System.out.println("Each team requires at least 1 Leader.");
            System.out.println("Leaders have personality scores of 90-100.");
            System.out.println("Add participants with high leadership scores.");
            return;
        }

        try {
            System.out.print("Enter team size (min " + MIN_TEAM_SIZE + ", default " + DEFAULT_TEAM_SIZE + "): ");
            String input = scanner.nextLine().trim();
            int teamSize = input.isEmpty() ? DEFAULT_TEAM_SIZE : Integer.parseInt(input);

            if (teamSize < MIN_TEAM_SIZE) {
                System.out.println("\nTeam size must be at least " + MIN_TEAM_SIZE + ".");
                return;
            }

            System.out.println("\nForming teams...");
            System.out.println("Rules applied:");
            System.out.println("  - Min " + MIN_TEAM_SIZE + " members per team");
            System.out.println("  - Each team has 1-2 Leaders (max 2)");
            System.out.println("  - Each team must have at least 1 Leader");
            System.out.println("  - Max 2 from same game per team");
            System.out.println("  - Diverse roles (min 3 different)");

            long start = System.currentTimeMillis();

            for (Participant p : loadedParticipants) {
                p.setAssignedTeam("Unassigned");
            }

            formedTeams = teamBuilder.formTeams(loadedParticipants, teamSize);

            long duration = System.currentTimeMillis() - start;

            System.out.println("\nTeam formation complete!");
            System.out.println("  Teams: " + formedTeams.size());
            System.out.println("  Time: " + duration + "ms");

            int assigned = formedTeams.stream().mapToInt(Team::getCurrentSize).sum();
            System.out.println("  Assigned: " + assigned + "/" + loadedParticipants.size());

            FileLogger.logInfo("Application",
                    String.format("Formed %d teams in %dms", formedTeams.size(), duration));

        } catch (Exception e) {
            System.out.println("\nError: " + e.getMessage());
            FileLogger.logError("Application", "Team formation failed", e);
        }
    }

    /**
     * Display participants (available to both roles)
     */
    private void displayLoadedParticipants() {
        System.out.println("\n----------- LOADED PARTICIPANTS -----------");

        if (loadedParticipants.isEmpty()) {
            System.out.println("\nNo participants loaded.");
            return;
        }

        System.out.println("\nTotal: " + loadedParticipants.size() + "\n");
        System.out.println("ID     | Name               | Type     | Game         | Skill | Role        ");
        System.out.println("-------|--------------------| ---------|--------------|-------|-------------");

        for (Participant p : loadedParticipants) {
            System.out.printf("%-6s | %-18s | %-8s | %-12s | %-5d | %-12s%n",
                    p.getParticipantId(),
                    truncate(p.getName(), 18),
                    p.getPersonalityType().getDisplayName(),
                    p.getPreferredGame().getDisplayName(),
                    p.getSkillLevel(),
                    p.getPreferredRole().getDisplayName()
            );
        }
    }

    /**
     * Option 6 (Management): Display teams
     */
    private void displayFormedTeams() {
        System.out.println("\n----------- FORMED TEAMS -----------");

        if (formedTeams.isEmpty()) {
            System.out.println("\nNo teams formed. Use option 4.");
            return;
        }

        for (Team team : formedTeams) {
            System.out.println(team.getDetailedSummary());
        }
    }

    /**
     * Option 7 (Management): Save teams
     */
    private void saveTeamsToFile() {
        System.out.println("\n----------- SAVE TEAMS -----------");

        if (formedTeams.isEmpty()) {
            System.out.println("\nNo teams to save. Use option 4 first.");
            return;
        }

        try {
            System.out.print("Enter filename (or Enter for '" + OUTPUT_FILE + "'): ");
            String fileName = scanner.nextLine().trim();
            if (fileName.isEmpty()) {
                fileName = OUTPUT_FILE;
            }

            CSVDataHandler.writeTeamsToCSV(formedTeams, fileName);

            System.out.println("\nTeams saved to: " + fileName);
            System.out.println("  Teams saved: " + formedTeams.size());
            FileLogger.logInfo("Application", "Saved teams to " + fileName);

        } catch (DataLoadException e) {
            System.out.println("\nError: " + e.getMessage());
            FileLogger.logError("Application", "Save failed", e);
        }
    }

    /**
     * Option 8 (Management): Statistics
     */
    private void displayStatistics() {
        System.out.println("\n----------- STATISTICS -----------");

        if (!loadedParticipants.isEmpty()) {
            displayParticipantStatistics();
        }

        if (!formedTeams.isEmpty()) {
            TeamBuilder.TeamFormationStatistics stats = teamBuilder.getStatistics(formedTeams);
            System.out.println("\n" + stats);
        } else {
            System.out.println("\nNo teams formed yet.");
        }
    }

    private void displayParticipantSummary() {
        System.out.println("\n--- Summary ---");

        Map<PersonalityType, Long> counts = new EnumMap<>(PersonalityType.class);
        for (PersonalityType type : PersonalityType.values()) {
            counts.put(type, loadedParticipants.stream()
                    .filter(p -> p.getPersonalityType() == type)
                    .count());
        }

        System.out.println("\nPersonality Distribution:");
        for (PersonalityType type : PersonalityType.values()) {
            System.out.printf("  %s: %d%n", type.getDisplayName(), counts.get(type));
        }
    }

    private void displayParticipantStatistics() {
        System.out.println("\n================================================");
        System.out.println("         PARTICIPANT STATISTICS                ");
        System.out.println("================================================");
        System.out.printf("Total: %d%n", loadedParticipants.size());
        System.out.println("------------------------------------------------");

        for (PersonalityType type : PersonalityType.values()) {
            long count = loadedParticipants.stream()
                    .filter(p -> p.getPersonalityType() == type)
                    .count();
            double pct = (count * 100.0) / loadedParticipants.size();
            System.out.printf("   %-10s: %3d (%5.1f%%)%n", type.getDisplayName(), count, pct);
        }
        System.out.println("================================================");
    }

    private String truncate(String str, int max) {
        return str.length() <= max ? str : str.substring(0, max - 3) + "...";
    }

    private void cleanup() {
        if (scanner != null) {
            scanner.close();
        }
        if (teamBuilder != null) {
            teamBuilder.shutdown();
        }
        FileLogger.shutdown();
    }
}