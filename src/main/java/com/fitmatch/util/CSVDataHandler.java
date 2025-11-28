package com.fitmatch.util;

import com.fitmatch.model.*;
import com.fitmatch.exception.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Utility class for handling CSV file operations.
 * Updated to match new CSV format with Email field.
 */
public class CSVDataHandler {

    private static final Logger LOGGER = Logger.getLogger(CSVDataHandler.class.getName());

    private static final String CSV_DELIMITER = ",";
    private static final String HEADER_PARTICIPANTS =
            "ID,Name,Email,PreferredGame,SkillLevel,PreferredRole,PersonalityScore,PersonalityType";
    private static final String HEADER_TEAMS =
            "TeamID,TeamName,TargetSize,CurrentSize,MemberIDs,MemberNames," +
                    "DiversityScore,BalanceScore,OverallScore";

    private CSVDataHandler() {
        // Prevent instantiation
    }

    // ==================== READ OPERATIONS ====================

    /**
     * Reads participant data from a CSV file.
     * Expected format: ID,Name,Email,PreferredGame,SkillLevel,PreferredRole,PersonalityScore,PersonalityType
     */
    public static List<Participant> readParticipantsFromCSV(String filePath)
            throws DataLoadException {

        InputStream is = CSVDataHandler.class.getResourceAsStream(filePath);
        boolean isResource = is != null;

        if (isResource) {
            LOGGER.info("Loading from classpath resource: " + filePath);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                return parseFromReader(reader, filePath);
            } catch (IOException e) {
                throw new DataLoadException("Error reading resource: " + filePath, filePath, -1, e);
            }
        } else {
            LOGGER.info("Loading from file system: " + filePath);
            Path path = Paths.get(filePath);
            validateFileAccess(path);
            try (BufferedReader reader = Files.newBufferedReader(path)) {
                return parseFromReader(reader, filePath);
            } catch (IOException e) {
                throw new DataLoadException("Error reading file: " + filePath, filePath, -1, e);
            }
        }
    }

    private static List<Participant> parseFromReader(BufferedReader reader, String source)
            throws IOException, DataLoadException {

        List<Participant> participants = new ArrayList<>();

        String line;
        int lineNumber = 0;

        // Read and validate header
        String headerLine = reader.readLine();
        lineNumber++;

        if (headerLine == null || headerLine.trim().isEmpty()) {
            throw new DataLoadException("CSV is empty or has no header", source);
        }

        // Process each data line
        while ((line = reader.readLine()) != null) {
            lineNumber++;

            if (line.trim().isEmpty()) {
                continue;
            }

            try {
                Participant participant = parseParticipantLine(line, lineNumber);
                participants.add(participant);
                LOGGER.fine("Parsed participant: " + participant.getName());

            } catch (EmailFormatException e) {
                LOGGER.warning(String.format("Skipping invalid line %d: %s",
                        lineNumber, e.getMessage()));
            } catch (ValidationException e) {
                LOGGER.warning(String.format("Skipping invalid line %d: %s",
                        lineNumber, e.getMessage()));
            } catch (Exception e) {
                LOGGER.warning(String.format("Error parsing line %d: %s",
                        lineNumber, e.getMessage()));
            }
        }

        LOGGER.info(String.format("Successfully loaded %d participants", participants.size()));

        if (participants.isEmpty()) {
            throw new DataLoadException("No valid participants found", source);
        }

        return participants;
    }

    private static void validateFileAccess(Path path) throws DataLoadException {
        if (!Files.exists(path)) {
            throw new DataLoadException("File not found: " + path.toString(), path.toString());
        }

        if (!Files.isReadable(path)) {
            throw new DataLoadException("File is not readable: " + path.toString(), path.toString());
        }
    }

    /**
     * Parses a single CSV line into a Participant object.
     * Format: ID,Name,Email,PreferredGame,SkillLevel,PreferredRole,PersonalityScore,PersonalityType
     */
    private static Participant parseParticipantLine(String line, int lineNumber)
            throws ValidationException, EmailFormatException {

        String[] fields = line.split(CSV_DELIMITER, -1);

        if (fields.length < 8) {
            throw new ValidationException(
                    String.format("Expected 8 fields, found %d", fields.length),
                    "Line " + lineNumber,
                    line,
                    HEADER_PARTICIPANTS
            );
        }

        try {
            String participantId = fields[0].trim();
            String name = fields[1].trim();
            String email = fields[2].trim();

            GameType game = parseGameType(fields[3].trim(), lineNumber);
            int skillLevel = Integer.parseInt(fields[4].trim());
            PlayingRole role = parsePlayingRole(fields[5].trim(), lineNumber);
            int personalityScore = Integer.parseInt(fields[6].trim());

            // Create and return Participant
            return new Participant(participantId, name, email, 20, // Default age
                    game, skillLevel, role, personalityScore);

        } catch (NumberFormatException e) {
            throw new ValidationException(
                    "Invalid number format: " + e.getMessage(),
                    "Line " + lineNumber,
                    line
            );
        } catch (IllegalArgumentException e) {
            throw new ValidationException(
                    "Validation error: " + e.getMessage(),
                    "Line " + lineNumber,
                    line
            );
        }
    }

    private static GameType parseGameType(String gameStr, int lineNumber)
            throws ValidationException {
        try {
            // Handle different game name formats
            String normalized = gameStr.toUpperCase()
                    .replace(" ", "")
                    .replace(":", "")
                    .replace("2", "");

            // Use if-else instead of switch for String compatibility
            if (normalized.equals("CHESS")) {
                return GameType.CHESS;
            } else if (normalized.equals("FIFA")) {
                return GameType.FIFA;
            } else if (normalized.equals("BASKETBALL")) {
                return GameType.BASKETBALL;
            } else if (normalized.equals("CSGO")) {
                return GameType.CSGO;
            } else if (normalized.equals("DOTA")) {
                return GameType.DOTA;
            } else if (normalized.equals("VALORANT")) {
                return GameType.VALORANT;
            } else {
                throw new ValidationException(
                        "Unknown game type: " + gameStr,
                        "Line " + lineNumber + ", PreferredGame",
                        gameStr,
                        "Valid games: Chess, FIFA, Basketball, CS:GO, DOTA 2, Valorant"
                );
            }
        } catch (IllegalArgumentException e) {
            throw new ValidationException(
                    "Unknown game type: " + gameStr,
                    "Line " + lineNumber + ", PreferredGame",
                    gameStr,
                    "Valid games: Chess, FIFA, Basketball, CS:GO, DOTA 2, Valorant"
            );
        }
    }

    private static PlayingRole parsePlayingRole(String roleStr, int lineNumber)
            throws ValidationException {
        try {
            String normalized = roleStr.toUpperCase().replace(" ", "");

            // Use if-else instead of switch for String compatibility
            if (normalized.equals("STRATEGIST")) {
                return PlayingRole.STRATEGIST;
            } else if (normalized.equals("ATTACKER")) {
                return PlayingRole.ATTACKER;
            } else if (normalized.equals("DEFENDER")) {
                return PlayingRole.DEFENDER;
            } else if (normalized.equals("SUPPORTER")) {
                return PlayingRole.SUPPORTER;
            } else if (normalized.equals("COORDINATOR")) {
                return PlayingRole.COORDINATOR;
            } else {
                throw new ValidationException(
                        "Unknown role: " + roleStr,
                        "Line " + lineNumber + ", PreferredRole",
                        roleStr,
                        "Valid roles: Strategist, Attacker, Defender, Supporter, Coordinator"
                );
            }
        } catch (IllegalArgumentException e) {
            throw new ValidationException(
                    "Unknown role: " + roleStr,
                    "Line " + lineNumber + ", PreferredRole",
                    roleStr,
                    "Valid roles: Strategist, Attacker, Defender, Supporter, Coordinator"
            );
        }
    }

    // ==================== WRITE OPERATIONS ====================

    public static void writeTeamsToCSV(List<Team> teams, String filePath)
            throws DataLoadException {

        Path path = Paths.get(filePath);

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(HEADER_TEAMS);
            writer.newLine();

            for (Team team : teams) {
                if (team.getCurrentSize() > 0) {
                    writer.write(formatTeamCSV(team));
                    writer.newLine();
                }
            }

            LOGGER.info(String.format("Successfully wrote %d teams to %s",
                    teams.size(), filePath));

        } catch (IOException e) {
            throw new DataLoadException("Error writing to file: " + filePath, filePath, -1, e);
        }
    }

    private static String formatTeamCSV(Team team) {
        String memberIds = team.getMembers().stream()
                .map(Participant::getParticipantId)
                .reduce((a, b) -> a + ";" + b)
                .orElse("");

        String memberNames = team.getMembers().stream()
                .map(Participant::getName)
                .reduce((a, b) -> a + ";" + b)
                .orElse("");

        return String.format("%s,%s,%d,%d,%s,%s,%.4f,%.4f,%.4f",
                team.getTeamId(),
                team.getTeamName(),
                team.getTargetSize(),
                team.getCurrentSize(),
                memberIds,
                memberNames,
                team.getDiversityScore(),
                team.getBalanceScore(),
                team.getOverallScore()
        );
    }

    // ==================== SAMPLE DATA GENERATION ====================

    /**
     * Creates a sample CSV file with test participant data.
     */
    public static void createSampleCSV(String filePath) throws DataLoadException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath))) {
            writer.write(HEADER_PARTICIPANTS);
            writer.newLine();

            // Sample data with diverse characteristics
            String[] sampleData = {
                    "P001,Alex Chen,alex.chen@university.edu,VALORANT,8,Strategist,95,LEADER",
                    "P002,Sarah Johnson,sarah.j@university.edu,FIFA,6,Attacker,80,BALANCED",
                    "P003,Mike Williams,mike.w@university.edu,FIFA,5,Defender,66,THINKER",
                    "P004,Emma Davis,emma.d@university.edu,Basketball,7,Supporter,91,LEADER",
                    "P005,James Brown,james.b@university.edu,CS:GO,9,Coordinator,82,BALANCED",
                    "P006,Lisa Anderson,lisa.a@university.edu,CS:GO,3,Strategist,57,THINKER",
                    "P007,David Lee,david.l@university.edu,DOTA 2,6,Attacker,93,LEADER",
                    "P008,Amy Taylor,amy.t@university.edu,Valorant,8,Defender,81,BALANCED",
                    "P009,Chris Martin,chris.m@university.edu,CS:GO,5,Supporter,62,THINKER",
                    "P010,Rachel White,rachel.w@university.edu,Valorant,7,Coordinator,96,LEADER",
                    "P011,Tom Wilson,tom.w@university.edu,Basketball,6,Strategist,84,BALANCED",
                    "P012,Sophie Clark,sophie.c@university.edu,DOTA 2,4,Attacker,63,THINKER",
                    "P013,Daniel Kim,daniel.k@university.edu,DOTA 2,5,Defender,97,LEADER",
                    "P014,Olivia Hall,olivia.h@university.edu,FIFA,4,Supporter,73,BALANCED",
                    "P015,Ryan Scott,ryan.s@university.edu,DOTA 2,7,Coordinator,66,THINKER",
                    "P016,Grace Lee,grace.l@university.edu,CS:GO,6,Strategist,90,LEADER",
                    "P017,Kevin Park,kevin.p@university.edu,CS:GO,8,Attacker,77,BALANCED",
                    "P018,Hannah Adams,hannah.a@university.edu,FIFA,4,Defender,51,THINKER",
                    "P019,Jason Miller,jason.m@university.edu,FIFA,7,Supporter,95,LEADER",
                    "P020,Chloe Turner,chloe.t@university.edu,Basketball,5,Coordinator,82,BALANCED"
            };

            for (String data : sampleData) {
                writer.write(data);
                writer.newLine();
            }

            LOGGER.info("Sample CSV file created: " + filePath);

        } catch (IOException e) {
            throw new DataLoadException("Error creating sample file: " + filePath, filePath, -1, e);
        }
    }

    // ==================== VALIDATION UTILITIES ====================

    public static boolean validateCSVFormat(String filePath) {
        try {
            Path path = Paths.get(filePath);

            if (!Files.exists(path)) {
                return false;
            }

            try (BufferedReader reader = Files.newBufferedReader(path)) {
                String header = reader.readLine();

                if (header == null || header.trim().isEmpty()) {
                    return false;
                }

                return header.contains("ID") &&
                        header.contains("Name") &&
                        header.contains("Email");
            }

        } catch (IOException e) {
            LOGGER.warning("Error validating CSV: " + e.getMessage());
            return false;
        }
    }

    public static int countDataLines(String filePath) {
        try {
            Path path = Paths.get(filePath);

            if (!Files.exists(path)) {
                return 0;
            }

            long count = Files.lines(path)
                    .skip(1)
                    .filter(line -> !line.trim().isEmpty())
                    .count();

            return (int) count;

        } catch (IOException e) {
            LOGGER.warning("Error counting lines: " + e.getMessage());
            return 0;
        }
    }

    public static String getFileInfo(String filePath) {
        try {
            Path path = Paths.get(filePath);

            if (!Files.exists(path)) {
                return "File not found: " + filePath;
            }

            long size = Files.size(path);
            int lines = countDataLines(filePath);

            return String.format("File: %s | Size: %d bytes | Data rows: %d",
                    path.getFileName(), size, lines);

        } catch (IOException e) {
            return "Error reading file info: " + e.getMessage();
        }
    }
}