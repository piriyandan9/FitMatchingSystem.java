// ================== ParticipantTest.java ==================
package com.fitmatch.model;

import com.fitmatch.exception.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for Participant class.
 * Tests: Constructor validation, getters, personality classification, business logic.
 * Updated to match actual implementation with email and personality score.
 */
class ParticipantTest {

    // ==================== CONSTRUCTOR TESTS ====================

    @Test
    @DisplayName("Test valid participant creation with Leader personality")
    void testValidParticipantCreationLeader() throws ValidationException, EmailFormatException {
        Participant p = new Participant(
                "P001", "Alex Chen", "alex@university.edu", 22,
                GameType.VALORANT, 8, PlayingRole.STRATEGIST, 93
        );

        assertNotNull(p);
        assertEquals("P001", p.getParticipantId());
        assertEquals("Alex Chen", p.getName());
        assertEquals("alex@university.edu", p.getEmail());
        assertEquals(22, p.getAge());
        assertEquals(93, p.getPersonalityScore());
        assertEquals(PersonalityType.LEADER, p.getPersonalityType());
        assertEquals(GameType.VALORANT, p.getPreferredGame());
        assertEquals(8, p.getSkillLevel());
        assertEquals(PlayingRole.STRATEGIST, p.getPreferredRole());
        assertEquals("Unassigned", p.getAssignedTeam());
    }

    @Test
    @DisplayName("Test valid participant creation with Balanced personality")
    void testValidParticipantCreationBalanced() throws ValidationException, EmailFormatException {
        Participant p = new Participant(
                "P002", "Sarah Johnson", "sarah@university.edu", 24,
                GameType.DOTA, 7, PlayingRole.ATTACKER, 78
        );

        assertEquals(PersonalityType.BALANCED, p.getPersonalityType());
        assertEquals(78, p.getPersonalityScore());
    }

    @Test
    @DisplayName("Test valid participant creation with Thinker personality")
    void testValidParticipantCreationThinker() throws ValidationException, EmailFormatException {
        Participant p = new Participant(
                "P003", "Mike Williams", "mike@university.edu", 20,
                GameType.FIFA, 5, PlayingRole.DEFENDER, 62
        );

        assertEquals(PersonalityType.THINKER, p.getPersonalityType());
        assertEquals(62, p.getPersonalityScore());
    }

    @Test
    @DisplayName("Test valid participant with low score defaults to Thinker")
    void testValidParticipantLowScore() throws ValidationException, EmailFormatException {
        Participant p = new Participant(
                "P004", "Emma Davis", "emma@university.edu", 21,
                GameType.BASKETBALL, 4, PlayingRole.SUPPORTER, 45
        );

        assertEquals(PersonalityType.THINKER, p.getPersonalityType());
        assertEquals(45, p.getPersonalityScore());
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    @DisplayName("Test null participant ID throws exception")
    void testNullParticipantId() {
        assertThrows(ValidationException.class,
                () -> new Participant(null, "Test", "test@email.com", 20,
                        GameType.VALORANT, 5, PlayingRole.STRATEGIST, 80));
    }

    @Test
    @DisplayName("Test empty participant ID throws exception")
    void testEmptyParticipantId() {
        assertThrows(ValidationException.class,
                () -> new Participant("", "Test", "test@email.com", 20,
                        GameType.VALORANT, 5, PlayingRole.STRATEGIST, 80));
    }

    @Test
    @DisplayName("Test invalid participant ID format throws exception")
    void testInvalidParticipantIdFormat() {
        assertThrows(ValidationException.class,
                () -> new Participant("INVALID", "Test", "test@email.com", 20,
                        GameType.VALORANT, 5, PlayingRole.STRATEGIST, 80));
    }

    @Test
    @DisplayName("Test null name throws exception")
    void testNullName() {
        assertThrows(ValidationException.class,
                () -> new Participant("P001", null, "test@email.com", 20,
                        GameType.VALORANT, 5, PlayingRole.STRATEGIST, 80));
    }

    @Test
    @DisplayName("Test empty name throws exception")
    void testEmptyName() {
        assertThrows(ValidationException.class,
                () -> new Participant("P001", "", "test@email.com", 20,
                        GameType.VALORANT, 5, PlayingRole.STRATEGIST, 80));
    }

    @Test
    @DisplayName("Test name too short throws exception")
    void testNameTooShort() {
        assertThrows(ValidationException.class,
                () -> new Participant("P001", "A", "test@email.com", 20,
                        GameType.VALORANT, 5, PlayingRole.STRATEGIST, 80));
    }

    @Test
    @DisplayName("Test null email throws exception")
    void testNullEmail() {
        assertThrows(EmailFormatException.class,
                () -> new Participant("P001", "Test", null, 20,
                        GameType.VALORANT, 5, PlayingRole.STRATEGIST, 80));
    }

    @Test
    @DisplayName("Test invalid email format throws exception")
    void testInvalidEmailFormat() {
        assertThrows(EmailFormatException.class,
                () -> new Participant("P001", "Test", "invalid-email", 20,
                        GameType.VALORANT, 5, PlayingRole.STRATEGIST, 80));
    }

    @Test
    @DisplayName("Test email without @ throws exception")
    void testEmailWithoutAt() {
        assertThrows(EmailFormatException.class,
                () -> new Participant("P001", "Test", "testemail.com", 20,
                        GameType.VALORANT, 5, PlayingRole.STRATEGIST, 80));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 10, 15})
    @DisplayName("Test age below minimum (16) throws exception")
    void testAgeBelowMinimum(int age) {
        assertThrows(ValidationException.class,
                () -> new Participant("P001", "Test", "test@email.com", age,
                        GameType.VALORANT, 5, PlayingRole.STRATEGIST, 80));
    }

    @ParameterizedTest
    @ValueSource(ints = {81, 90, 100, 150})
    @DisplayName("Test age above maximum (80) throws exception")
    void testAgeAboveMaximum(int age) {
        assertThrows(ValidationException.class,
                () -> new Participant("P001", "Test", "test@email.com", age,
                        GameType.VALORANT, 5, PlayingRole.STRATEGIST, 80));
    }

    @ParameterizedTest
    @ValueSource(ints = {16, 25, 50, 80})
    @DisplayName("Test valid ages within range")
    void testValidAges(int age) throws ValidationException, EmailFormatException {
        Participant p = new Participant("P001", "Test", "test@email.com", age,
                GameType.VALORANT, 5, PlayingRole.STRATEGIST, 80);
        assertEquals(age, p.getAge());
    }

    @Test
    @DisplayName("Test personality score below minimum (19) throws exception")
    void testPersonalityScoreBelowMin() {
        assertThrows(InvalidPersonalityScoreException.class,
                () -> new Participant("P001", "Test", "test@email.com", 20,
                        GameType.VALORANT, 5, PlayingRole.STRATEGIST, 19));
    }

    @Test
    @DisplayName("Test personality score above maximum (101) throws exception")
    void testPersonalityScoreAboveMax() {
        assertThrows(InvalidPersonalityScoreException.class,
                () -> new Participant("P001", "Test", "test@email.com", 20,
                        GameType.VALORANT, 5, PlayingRole.STRATEGIST, 101));
    }

    @Test
    @DisplayName("Test skill level below minimum (0) throws exception")
    void testSkillLevelBelowMin() {
        assertThrows(ValidationException.class,
                () -> new Participant("P001", "Test", "test@email.com", 20,
                        GameType.VALORANT, 0, PlayingRole.STRATEGIST, 80));
    }

    @Test
    @DisplayName("Test skill level above maximum (11) throws exception")
    void testSkillLevelAboveMax() {
        assertThrows(ValidationException.class,
                () -> new Participant("P001", "Test", "test@email.com", 20,
                        GameType.VALORANT, 11, PlayingRole.STRATEGIST, 80));
    }

    @Test
    @DisplayName("Test null game type throws exception")
    void testNullGameType() {
        assertThrows(ValidationException.class,
                () -> new Participant("P001", "Test", "test@email.com", 20,
                        null, 5, PlayingRole.STRATEGIST, 80));
    }

    @Test
    @DisplayName("Test null playing role throws exception")
    void testNullPlayingRole() {
        assertThrows(ValidationException.class,
                () -> new Participant("P001", "Test", "test@email.com", 20,
                        GameType.VALORANT, 5, null, 80));
    }

    // ==================== PERSONALITY SCORE BOUNDARY TESTS ====================

    @Test
    @DisplayName("Test personality score at Leader minimum (90)")
    void testLeaderMinimumBoundary() throws ValidationException, EmailFormatException {
        Participant p = new Participant("P001", "Test", "test@email.com", 20,
                GameType.VALORANT, 5, PlayingRole.STRATEGIST, 90);
        assertEquals(90, p.getPersonalityScore());
        assertEquals(PersonalityType.LEADER, p.getPersonalityType());
    }

    @Test
    @DisplayName("Test personality score at Balanced maximum (89)")
    void testBalancedMaximumBoundary() throws ValidationException, EmailFormatException {
        Participant p = new Participant("P001", "Test", "test@email.com", 20,
                GameType.VALORANT, 5, PlayingRole.STRATEGIST, 89);
        assertEquals(89, p.getPersonalityScore());
        assertEquals(PersonalityType.BALANCED, p.getPersonalityType());
    }

    @Test
    @DisplayName("Test personality score at Balanced minimum (70)")
    void testBalancedMinimumBoundary() throws ValidationException, EmailFormatException {
        Participant p = new Participant("P001", "Test", "test@email.com", 20,
                GameType.VALORANT, 5, PlayingRole.STRATEGIST, 70);
        assertEquals(70, p.getPersonalityScore());
        assertEquals(PersonalityType.BALANCED, p.getPersonalityType());
    }

    @Test
    @DisplayName("Test personality score at Thinker maximum (69)")
    void testThinkerMaximumBoundary() throws ValidationException, EmailFormatException {
        Participant p = new Participant("P001", "Test", "test@email.com", 20,
                GameType.VALORANT, 5, PlayingRole.STRATEGIST, 69);
        assertEquals(69, p.getPersonalityScore());
        assertEquals(PersonalityType.THINKER, p.getPersonalityType());
    }

    @Test
    @DisplayName("Test personality score at Thinker minimum (50)")
    void testThinkerMinimumBoundary() throws ValidationException, EmailFormatException {
        Participant p = new Participant("P001", "Test", "test@email.com", 20,
                GameType.VALORANT, 5, PlayingRole.STRATEGIST, 50);
        assertEquals(50, p.getPersonalityScore());
        assertEquals(PersonalityType.THINKER, p.getPersonalityType());
    }

    @Test
    @DisplayName("Test minimum possible score (20)")
    void testMinimumScore() throws ValidationException, EmailFormatException {
        Participant p = new Participant("P001", "Test", "test@email.com", 20,
                GameType.VALORANT, 5, PlayingRole.STRATEGIST, 20);
        assertEquals(20, p.getPersonalityScore());
        assertEquals(PersonalityType.THINKER, p.getPersonalityType());
    }

    @Test
    @DisplayName("Test maximum possible score (100)")
    void testMaximumScore() throws ValidationException, EmailFormatException {
        Participant p = new Participant("P001", "Test", "test@email.com", 20,
                GameType.VALORANT, 5, PlayingRole.STRATEGIST, 100);
        assertEquals(100, p.getPersonalityScore());
        assertEquals(PersonalityType.LEADER, p.getPersonalityType());
    }

    // ==================== BUSINESS LOGIC TESTS ====================

    @Test
    @DisplayName("Test age compatibility - very close ages")
    void testAgeCompatibilityVeryClose() throws ValidationException, EmailFormatException {
        Participant p1 = createTestParticipant("P001", 25);
        Participant p2 = createTestParticipant("P002", 27);

        double compatibility = p1.calculateAgeCompatibility(p2);
        assertEquals(1.0, compatibility, 0.01);
    }

    @Test
    @DisplayName("Test age compatibility - close ages")
    void testAgeCompatibilityClose() throws ValidationException, EmailFormatException {
        Participant p1 = createTestParticipant("P001", 25);
        Participant p2 = createTestParticipant("P002", 30);

        double compatibility = p1.calculateAgeCompatibility(p2);
        assertEquals(0.8, compatibility, 0.01);
    }

    @Test
    @DisplayName("Test age compatibility - moderate difference")
    void testAgeCompatibilityModerate() throws ValidationException, EmailFormatException {
        Participant p1 = createTestParticipant("P001", 25);
        Participant p2 = createTestParticipant("P002", 33);

        double compatibility = p1.calculateAgeCompatibility(p2);
        assertEquals(0.6, compatibility, 0.01);
    }

    @Test
    @DisplayName("Test age compatibility - large difference")
    void testAgeCompatibilityLarge() throws ValidationException, EmailFormatException {
        Participant p1 = createTestParticipant("P001", 20);
        Participant p2 = createTestParticipant("P002", 45);

        double compatibility = p1.calculateAgeCompatibility(p2);
        assertEquals(0.2, compatibility, 0.01);
    }

    @Test
    @DisplayName("Test skill compatibility - same level")
    void testSkillCompatibilitySame() throws ValidationException, EmailFormatException {
        Participant p1 = new Participant("P001", "Test1", "test1@email.com", 20,
                GameType.VALORANT, 7, PlayingRole.STRATEGIST, 80);
        Participant p2 = new Participant("P002", "Test2", "test2@email.com", 20,
                GameType.DOTA, 7, PlayingRole.ATTACKER, 80);

        assertEquals(1.0, p1.calculateSkillCompatibility(p2), 0.01);
    }

    @Test
    @DisplayName("Test skill compatibility - 1 level difference")
    void testSkillCompatibilityClose() throws ValidationException, EmailFormatException {
        Participant p1 = new Participant("P001", "Test1", "test1@email.com", 20,
                GameType.VALORANT, 5, PlayingRole.STRATEGIST, 80);
        Participant p2 = new Participant("P002", "Test2", "test2@email.com", 20,
                GameType.DOTA, 6, PlayingRole.ATTACKER, 80);

        assertEquals(0.8, p1.calculateSkillCompatibility(p2), 0.01);
    }

    @Test
    @DisplayName("Test hasSameGameInterest - same game")
    void testSameGameInterestTrue() throws ValidationException, EmailFormatException {
        Participant p1 = new Participant("P001", "Test1", "test1@email.com", 20,
                GameType.VALORANT, 5, PlayingRole.STRATEGIST, 80);
        Participant p2 = new Participant("P002", "Test2", "test2@email.com", 20,
                GameType.VALORANT, 6, PlayingRole.ATTACKER, 80);

        assertTrue(p1.hasSameGameInterest(p2));
    }

    @Test
    @DisplayName("Test hasSameGameInterest - different games")
    void testSameGameInterestFalse() throws ValidationException, EmailFormatException {
        Participant p1 = new Participant("P001", "Test1", "test1@email.com", 20,
                GameType.VALORANT, 5, PlayingRole.STRATEGIST, 80);
        Participant p2 = new Participant("P002", "Test2", "test2@email.com", 20,
                GameType.DOTA, 6, PlayingRole.ATTACKER, 80);

        assertFalse(p1.hasSameGameInterest(p2));
    }

    @Test
    @DisplayName("Test hasSameRole - same role")
    void testSameRoleTrue() throws ValidationException, EmailFormatException {
        Participant p1 = new Participant("P001", "Test1", "test1@email.com", 20,
                GameType.VALORANT, 5, PlayingRole.ATTACKER, 80);
        Participant p2 = new Participant("P002", "Test2", "test2@email.com", 20,
                GameType.DOTA, 6, PlayingRole.ATTACKER, 80);

        assertTrue(p1.hasSameRole(p2));
    }

    @Test
    @DisplayName("Test hasSameRole - different roles")
    void testSameRoleFalse() throws ValidationException, EmailFormatException {
        Participant p1 = new Participant("P001", "Test1", "test1@email.com", 20,
                GameType.VALORANT, 5, PlayingRole.ATTACKER, 80);
        Participant p2 = new Participant("P002", "Test2", "test2@email.com", 20,
                GameType.DOTA, 6, PlayingRole.DEFENDER, 80);

        assertFalse(p1.hasSameRole(p2));
    }

    @Test
    @DisplayName("Test team assignment")
    void testTeamAssignment() throws ValidationException, EmailFormatException {
        Participant p = createTestParticipant("P001", 20);
        assertEquals("Unassigned", p.getAssignedTeam());
        assertFalse(p.isAssigned());

        p.setAssignedTeam("Alpha");
        assertEquals("Alpha", p.getAssignedTeam());
        assertTrue(p.isAssigned());
    }

    @Test
    @DisplayName("Test setAssignedTeam with null resets to Unassigned")
    void testSetAssignedTeamNull() throws ValidationException, EmailFormatException {
        Participant p = createTestParticipant("P001", 20);
        p.setAssignedTeam("Alpha");
        assertEquals("Alpha", p.getAssignedTeam());

        p.setAssignedTeam(null);
        assertEquals("Unassigned", p.getAssignedTeam());
    }

    @Test
    @DisplayName("Test email is stored in lowercase")
    void testEmailLowercase() throws ValidationException, EmailFormatException {
        Participant p = new Participant("P001", "Test", "TEST@EMAIL.COM", 20,
                GameType.VALORANT, 5, PlayingRole.STRATEGIST, 80);
        assertEquals("test@email.com", p.getEmail());
    }

    @Test
    @DisplayName("Test toCSV format")
    void testToCSVFormat() throws ValidationException, EmailFormatException {
        Participant p = new Participant("P001", "Alex Chen", "alex@university.edu", 22,
                GameType.VALORANT, 8, PlayingRole.STRATEGIST, 93);

        String csv = p.toCSV();

        assertTrue(csv.contains("P001"));
        assertTrue(csv.contains("Alex Chen"));
        assertTrue(csv.contains("alex@university.edu"));
        assertTrue(csv.contains("VALORANT"));
        assertTrue(csv.contains("LEADER"));
    }

    @Test
    @DisplayName("Test toString method")
    void testToString() throws ValidationException, EmailFormatException {
        Participant p = new Participant("P001", "Alex Chen", "alex@university.edu", 22,
                GameType.VALORANT, 8, PlayingRole.STRATEGIST, 93);

        String str = p.toString();

        assertNotNull(str);
        assertTrue(str.contains("P001"));
        assertTrue(str.contains("Alex Chen"));
    }

    @Test
    @DisplayName("Test getSummary method")
    void testGetSummary() throws ValidationException, EmailFormatException {
        Participant p = new Participant("P001", "Alex Chen", "alex@university.edu", 22,
                GameType.VALORANT, 8, PlayingRole.STRATEGIST, 93);

        String summary = p.getSummary();

        assertNotNull(summary);
        assertTrue(summary.contains("P001"));
        assertTrue(summary.contains("Alex Chen"));
    }

    // ==================== HELPER METHODS ====================

    private Participant createTestParticipant(String id, int age)
            throws ValidationException, EmailFormatException {
        return new Participant(id, "Test " + id, "test" + id + "@email.com", age,
                GameType.VALORANT, 5, PlayingRole.STRATEGIST, 80);
    }
}