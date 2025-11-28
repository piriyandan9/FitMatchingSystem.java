// ================== CSVDataHandlerTest.java ==================
package com.fitmatch.util;

import com.fitmatch.model.*;
import com.fitmatch.exception.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Comprehensive unit tests for CSVDataHandler utility class.
 * Tests: File reading, writing, validation, error handling.
 * Updated to match actual CSV format with Email and PersonalityScore.
 */
class CSVDataHandlerTest {

    private static final String TEST_INPUT_FILE = "test_participants.csv";
    private static final String TEST_OUTPUT_FILE = "test_teams_output.csv";
    private static final String INVALID_FILE = "nonexistent.csv";

    @BeforeEach
    void setUp() throws IOException {
        deleteTestFiles();
    }

    @AfterEach
    void tearDown() {
        deleteTestFiles();
    }

    private void deleteTestFiles() {
        try {
            Files.deleteIfExists(Paths.get(TEST_INPUT_FILE));
            Files.deleteIfExists(Paths.get(TEST_OUTPUT_FILE));
        } catch (IOException e) {
            // Ignore cleanup errors
        }
    }

    // ==================== SAMPLE FILE CREATION TESTS ====================

    @Test
    @DisplayName("Test create sample CSV file")
    void testCreateSampleCSV() throws DataLoadException {
        CSVDataHandler.createSampleCSV(TEST_INPUT_FILE);

        File file = new File(TEST_INPUT_FILE);
        assertTrue(file.exists(), "Sample CSV file should be created");
        assertTrue(file.length() > 0, "Sample CSV file should not be empty");
    }

    @Test
    @DisplayName("Test sample CSV has correct number of participants")
    void testSampleCSVParticipantCount() throws DataLoadException {
        CSVDataHandler.createSampleCSV(TEST_INPUT_FILE);
        List<Participant> participants = CSVDataHandler.readParticipantsFromCSV(TEST_INPUT_FILE);

        assertEquals(20, participants.size(), "Sample should contain 20 participants");
    }

    // ==================== READ OPERATIONS TESTS ====================

    @Test
    @DisplayName("Test read participants from valid CSV")
    void testReadParticipantsValid() throws DataLoadException {
        CSVDataHandler.createSampleCSV(TEST_INPUT_FILE);

        List<Participant> participants = CSVDataHandler.readParticipantsFromCSV(TEST_INPUT_FILE);

        assertNotNull(participants);
        assertFalse(participants.isEmpty());
    }

    @Test
    @DisplayName("Test read participants validates data correctly")
    void testReadParticipantsValidation() throws DataLoadException {
        CSVDataHandler.createSampleCSV(TEST_INPUT_FILE);
        List<Participant> participants = CSVDataHandler.readParticipantsFromCSV(TEST_INPUT_FILE);

        for (Participant p : participants) {
            assertNotNull(p.getParticipantId());
            assertNotNull(p.getName());
            assertNotNull(p.getEmail());
            assertTrue(p.getEmail().contains("@"));
            assertTrue(p.getAge() >= 16 && p.getAge() <= 80);
            assertNotNull(p.getPreferredGame());
            assertTrue(p.getSkillLevel() >= 1 && p.getSkillLevel() <= 10);
            assertNotNull(p.getPreferredRole());
            assertTrue(p.getPersonalityScore() >= 20 && p.getPersonalityScore() <= 100);
        }
    }

    @Test
    @DisplayName("Test read from nonexistent file throws exception")
    void testReadNonexistentFile() {
        DataLoadException e = assertThrows(DataLoadException.class,
                () -> CSVDataHandler.readParticipantsFromCSV(INVALID_FILE));
        assertTrue(e.getMessage().contains("not found") || e.getMessage().contains("File"));
    }

    @Test
    @DisplayName("Test read from empty file throws exception")
    void testReadEmptyFile() throws IOException {
        Files.createFile(Paths.get(TEST_INPUT_FILE));

        assertThrows(DataLoadException.class,
                () -> CSVDataHandler.readParticipantsFromCSV(TEST_INPUT_FILE));
    }

    @Test
    @DisplayName("Test read CSV with correct header format")
    void testReadCSVCorrectHeader() throws IOException, DataLoadException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(TEST_INPUT_FILE))) {
            writer.write("ID,Name,Email,PreferredGame,SkillLevel,PreferredRole,PersonalityScore,PersonalityType");
            writer.newLine();
            writer.write("P001,Alex Chen,alex@email.com,VALORANT,8,Strategist,95,LEADER");
            writer.newLine();
        }

        List<Participant> participants = CSVDataHandler.readParticipantsFromCSV(TEST_INPUT_FILE);

        assertEquals(1, participants.size());
        assertEquals("P001", participants.get(0).getParticipantId());
        assertEquals("Alex Chen", participants.get(0).getName());
        assertEquals("alex@email.com", participants.get(0).getEmail());
    }

    // ==================== WRITE OPERATIONS TESTS ====================

    @Test
    @DisplayName("Test write teams to CSV")
    void testWriteTeamsToCSV() throws DataLoadException, ValidationException, EmailFormatException {
        Team team = createTestTeam();
        List<Team> teams = Arrays.asList(team);

        CSVDataHandler.writeTeamsToCSV(teams, TEST_OUTPUT_FILE);

        File file = new File(TEST_OUTPUT_FILE);
        assertTrue(file.exists(), "Output file should be created");
        assertTrue(file.length() > 0, "Output file should not be empty");
    }

    @Test
    @DisplayName("Test write teams creates properly formatted CSV")
    void testWriteTeamsFormat() throws DataLoadException, IOException, ValidationException, EmailFormatException {
        Team team = createTestTeam();
        List<Team> teams = Arrays.asList(team);

        CSVDataHandler.writeTeamsToCSV(teams, TEST_OUTPUT_FILE);

        List<String> lines = Files.readAllLines(Paths.get(TEST_OUTPUT_FILE));

        assertTrue(lines.size() >= 2, "Should have header and at least one data line");

        String header = lines.get(0);
        assertTrue(header.contains("TeamID"));
        assertTrue(header.contains("TeamName"));
        assertTrue(header.contains("DiversityScore"));
    }

    @Test
    @DisplayName("Test write empty teams list")
    void testWriteEmptyTeamsList() throws DataLoadException, IOException {
        CSVDataHandler.writeTeamsToCSV(new ArrayList<>(), TEST_OUTPUT_FILE);

        File file = new File(TEST_OUTPUT_FILE);
        assertTrue(file.exists());

        List<String> lines = Files.readAllLines(Paths.get(TEST_OUTPUT_FILE));
        assertEquals(1, lines.size(), "Should only have header line");
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    @DisplayName("Test validate CSV format with valid file")
    void testValidateCSVFormatValid() throws DataLoadException {
        CSVDataHandler.createSampleCSV(TEST_INPUT_FILE);

        boolean isValid = CSVDataHandler.validateCSVFormat(TEST_INPUT_FILE);
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Test validate CSV format with nonexistent file")
    void testValidateCSVFormatNonexistent() {
        boolean isValid = CSVDataHandler.validateCSVFormat(INVALID_FILE);
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Test validate CSV format with empty file")
    void testValidateCSVFormatEmpty() throws IOException {
        Files.createFile(Paths.get(TEST_INPUT_FILE));

        boolean isValid = CSVDataHandler.validateCSVFormat(TEST_INPUT_FILE);
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Test validate CSV format with wrong headers")
    void testValidateCSVFormatWrongHeaders() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(TEST_INPUT_FILE))) {
            writer.write("WrongHeader1,WrongHeader2,WrongHeader3");
            writer.newLine();
        }

        boolean isValid = CSVDataHandler.validateCSVFormat(TEST_INPUT_FILE);
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Test countDataLines")
    void testCountDataLines() throws DataLoadException {
        CSVDataHandler.createSampleCSV(TEST_INPUT_FILE);

        int count = CSVDataHandler.countDataLines(TEST_INPUT_FILE);
        assertEquals(20, count);
    }

    @Test
    @DisplayName("Test getFileInfo")
    void testGetFileInfo() throws DataLoadException {
        CSVDataHandler.createSampleCSV(TEST_INPUT_FILE);

        String info = CSVDataHandler.getFileInfo(TEST_INPUT_FILE);

        assertNotNull(info);
        assertTrue(info.contains(TEST_INPUT_FILE) || info.contains("test_participants"));
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    @DisplayName("Test read CSV handles malformed data gracefully")
    void testReadCSVMalformedData() throws IOException, DataLoadException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(TEST_INPUT_FILE))) {
            writer.write("ID,Name,Email,PreferredGame,SkillLevel,PreferredRole,PersonalityScore,PersonalityType");
            writer.newLine();
            writer.write("P001,Valid Person,valid@email.com,VALORANT,8,Strategist,95,LEADER");
            writer.newLine();
            writer.write("P002,Invalid,invalid-email,VALORANT,8,Strategist,95,LEADER");
            writer.newLine();
            writer.write("P003,Another Valid,valid2@email.com,DOTA,7,Attacker,78,BALANCED");
            writer.newLine();
        }

        List<Participant> participants = CSVDataHandler.readParticipantsFromCSV(TEST_INPUT_FILE);

        // Should skip invalid line and continue
        assertEquals(2, participants.size());
    }

    @Test
    @DisplayName("Test read CSV skips empty lines")
    void testReadCSVSkipsEmptyLines() throws IOException, DataLoadException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(TEST_INPUT_FILE))) {
            writer.write("ID,Name,Email,PreferredGame,SkillLevel,PreferredRole,PersonalityScore,PersonalityType");
            writer.newLine();
            writer.write("P001,Test Person,test@email.com,VALORANT,8,Strategist,95,LEADER");
            writer.newLine();
            writer.write(""); // Empty line
            writer.newLine();
            writer.write("P002,Another Person,another@email.com,DOTA,7,Attacker,78,BALANCED");
            writer.newLine();
        }

        List<Participant> participants = CSVDataHandler.readParticipantsFromCSV(TEST_INPUT_FILE);
        assertEquals(2, participants.size());
    }

    @Test
    @DisplayName("Test read CSV trims whitespace")
    void testReadCSVTrimsWhitespace() throws IOException, DataLoadException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(TEST_INPUT_FILE))) {
            writer.write("ID,Name,Email,PreferredGame,SkillLevel,PreferredRole,PersonalityScore,PersonalityType");
            writer.newLine();
            writer.write("  P001  , Test Person , test@email.com , VALORANT , 8 , Strategist , 95 , LEADER ");
            writer.newLine();
        }

        List<Participant> participants = CSVDataHandler.readParticipantsFromCSV(TEST_INPUT_FILE);

        assertEquals(1, participants.size());
        assertEquals("P001", participants.get(0).getParticipantId());
        assertEquals("Test Person", participants.get(0).getName());
    }

    @Test
    @DisplayName("Test read CSV with invalid personality score skips line")
    void testReadCSVInvalidPersonalityScore() throws IOException, DataLoadException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(TEST_INPUT_FILE))) {
            writer.write("ID,Name,Email,PreferredGame,SkillLevel,PreferredRole,PersonalityScore,PersonalityType");
            writer.newLine();
            writer.write("P001,Valid,valid@email.com,VALORANT,8,Strategist,95,LEADER");
            writer.newLine();
            writer.write("P002,Invalid Score,invalid@email.com,VALORANT,8,Strategist,150,LEADER");
            writer.newLine();
            writer.write("P003,Another Valid,valid2@email.com,DOTA,7,Attacker,78,BALANCED");
            writer.newLine();
        }

        List<Participant> participants = CSVDataHandler.readParticipantsFromCSV(TEST_INPUT_FILE);
        assertEquals(2, participants.size());
    }

    @Test
    @DisplayName("Test read CSV with invalid skill level skips line")
    void testReadCSVInvalidSkillLevel() throws IOException, DataLoadException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(TEST_INPUT_FILE))) {
            writer.write("ID,Name,Email,PreferredGame,SkillLevel,PreferredRole,PersonalityScore,PersonalityType");
            writer.newLine();
            writer.write("P001,Valid,valid@email.com,VALORANT,8,Strategist,95,LEADER");
            writer.newLine();
            writer.write("P002,Invalid Skill,invalid@email.com,VALORANT,15,Strategist,95,LEADER");
            writer.newLine();
        }

        List<Participant> participants = CSVDataHandler.readParticipantsFromCSV(TEST_INPUT_FILE);
        assertEquals(1, participants.size());
    }

    @Test
    @DisplayName("Test read CSV parses different game types correctly")
    void testReadCSVGameTypes() throws IOException, DataLoadException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(TEST_INPUT_FILE))) {
            writer.write("ID,Name,Email,PreferredGame,SkillLevel,PreferredRole,PersonalityScore,PersonalityType");
            writer.newLine();
            writer.write("P001,Player1,p1@email.com,VALORANT,8,Strategist,95,LEADER");
            writer.newLine();
            writer.write("P002,Player2,p2@email.com,CS:GO,7,Attacker,78,BALANCED");
            writer.newLine();
            writer.write("P003,Player3,p3@email.com,DOTA 2,6,Defender,62,THINKER");
            writer.newLine();
            writer.write("P004,Player4,p4@email.com,FIFA,5,Supporter,55,THINKER");
            writer.newLine();
        }

        List<Participant> participants = CSVDataHandler.readParticipantsFromCSV(TEST_INPUT_FILE);
        assertEquals(4, participants.size());

        assertEquals(GameType.VALORANT, participants.get(0).getPreferredGame());
        assertEquals(GameType.CSGO, participants.get(1).getPreferredGame());
        assertEquals(GameType.DOTA, participants.get(2).getPreferredGame());
        assertEquals(GameType.FIFA, participants.get(3).getPreferredGame());
    }

    @Test
    @DisplayName("Test read CSV with insufficient fields skips line")
    void testReadCSVInsufficientFields() throws IOException, DataLoadException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(TEST_INPUT_FILE))) {
            writer.write("ID,Name,Email,PreferredGame,SkillLevel,PreferredRole,PersonalityScore,PersonalityType");
            writer.newLine();
            writer.write("P001,Valid,valid@email.com,VALORANT,8,Strategist,95,LEADER");
            writer.newLine();
            writer.write("P002,Incomplete,incomplete@email.com,VALORANT"); // Missing fields
            writer.newLine();
        }

        List<Participant> participants = CSVDataHandler.readParticipantsFromCSV(TEST_INPUT_FILE);
        assertEquals(1, participants.size());
    }

    // ==================== HELPER METHODS ====================

    private Team createTestTeam() throws ValidationException, EmailFormatException {
        Team team = new Team("TEAM-001", "Alpha", 4);

        Participant p1 = new Participant("P001", "Alex", "alex@email.com", 22,
                GameType.VALORANT, 8, PlayingRole.STRATEGIST, 93);

        Participant p2 = new Participant("P002", "Sarah", "sarah@email.com", 24,
                GameType.DOTA, 7, PlayingRole.ATTACKER, 78);

        team.addMember(p1);
        team.addMember(p2);

        return team;
    }
}