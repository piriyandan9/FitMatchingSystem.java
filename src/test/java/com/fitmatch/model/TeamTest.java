// ================== TeamTest.java ==================
package com.fitmatch.model;

import com.fitmatch.exception.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

/**
 * Comprehensive unit tests for Team class.
 * Tests: Team creation, member management, metrics calculation, validation.
 * Updated to match actual implementation with email and personality score.
 */
class TeamTest {

    private Team team;
    private Participant leaderParticipant;
    private Participant balancedParticipant;
    private Participant thinkerParticipant;
    private Participant anotherLeader;

    @BeforeEach
    void setUp() throws ValidationException, EmailFormatException {
        team = new Team("TEAM-001", "Alpha", 4);

        // Create diverse participants
        leaderParticipant = new Participant("P001", "Alex Leader", "alex@university.edu", 22,
                GameType.VALORANT, 8, PlayingRole.STRATEGIST, 93);

        balancedParticipant = new Participant("P002", "Sarah Balanced", "sarah@university.edu", 24,
                GameType.DOTA, 7, PlayingRole.ATTACKER, 78);

        thinkerParticipant = new Participant("P003", "Mike Thinker", "mike@university.edu", 20,
                GameType.FIFA, 5, PlayingRole.DEFENDER, 62);

        anotherLeader = new Participant("P004", "Emma Leader2", "emma@university.edu", 21,
                GameType.BASKETBALL, 6, PlayingRole.SUPPORTER, 95);
    }

    // ==================== CONSTRUCTOR TESTS ====================

    @Test
    @DisplayName("Test valid team creation")
    void testValidTeamCreation() {
        assertNotNull(team);
        assertEquals("TEAM-001", team.getTeamId());
        assertEquals("Alpha", team.getTeamName());
        assertEquals(4, team.getTargetSize());
        assertEquals(0, team.getCurrentSize());
        assertFalse(team.isFull());
        assertFalse(team.isComplete());
    }

    @Test
    @DisplayName("Test null team ID throws exception")
    void testNullTeamId() {
        assertThrows(IllegalArgumentException.class,
                () -> new Team(null, "Alpha", 4));
    }

    @Test
    @DisplayName("Test empty team ID throws exception")
    void testEmptyTeamId() {
        assertThrows(IllegalArgumentException.class,
                () -> new Team("", "Alpha", 4));
    }

    @Test
    @DisplayName("Test null team name throws exception")
    void testNullTeamName() {
        assertThrows(IllegalArgumentException.class,
                () -> new Team("TEAM-001", null, 4));
    }

    @Test
    @DisplayName("Test team size below minimum (3) throws exception")
    void testTeamSizeBelowMin() {
        assertThrows(IllegalArgumentException.class,
                () -> new Team("TEAM-001", "Alpha", 2));
    }

    // ==================== MEMBER MANAGEMENT TESTS ====================

    @Test
    @DisplayName("Test adding member to team")
    void testAddMember() {
        boolean added = team.addMember(leaderParticipant);

        assertTrue(added);
        assertEquals(1, team.getCurrentSize());
        assertEquals("Alpha", leaderParticipant.getAssignedTeam());
    }

    @Test
    @DisplayName("Test adding null member returns false")
    void testAddNullMember() {
        boolean added = team.addMember(null);
        assertFalse(added);
        assertEquals(0, team.getCurrentSize());
    }

    @Test
    @DisplayName("Test adding duplicate member returns false")
    void testAddDuplicateMember() {
        team.addMember(leaderParticipant);
        boolean added = team.addMember(leaderParticipant);

        assertFalse(added);
        assertEquals(1, team.getCurrentSize());
    }

    @Test
    @DisplayName("Test team is complete with min 3 members and 1 leader")
    void testIsComplete() {
        assertFalse(team.isComplete());

        team.addMember(leaderParticipant);
        assertFalse(team.isComplete()); // Only 1 member

        team.addMember(balancedParticipant);
        assertFalse(team.isComplete()); // Only 2 members

        team.addMember(thinkerParticipant);
        assertTrue(team.isComplete()); // 3 members + 1 leader
    }

    @Test
    @DisplayName("Test team is not complete without leader")
    void testIsCompleteWithoutLeader() throws ValidationException, EmailFormatException {
        Participant thinker2 = new Participant("P005", "Thinker2", "thinker2@email.com", 22,
                GameType.CSGO, 6, PlayingRole.COORDINATOR, 55);
        Participant thinker3 = new Participant("P006", "Thinker3", "thinker3@email.com", 23,
                GameType.VALORANT, 7, PlayingRole.ATTACKER, 60);

        team.addMember(thinkerParticipant);
        team.addMember(thinker2);
        team.addMember(thinker3);

        assertFalse(team.isComplete()); // 3 members but no leader
    }

    @Test
    @DisplayName("Test team is full at target size")
    void testIsFull() {
        team.addMember(leaderParticipant);
        team.addMember(balancedParticipant);
        team.addMember(thinkerParticipant);
        assertFalse(team.isFull());

        team.addMember(anotherLeader);
        assertTrue(team.isFull());
        assertEquals(0, team.getRemainingSpots());
    }

    @Test
    @DisplayName("Test cannot add member when team is full")
    void testCannotAddWhenFull() throws ValidationException, EmailFormatException {
        team.addMember(leaderParticipant);
        team.addMember(balancedParticipant);
        team.addMember(thinkerParticipant);
        team.addMember(anotherLeader);

        Participant extraParticipant = new Participant("P005", "Extra", "extra@email.com", 20,
                GameType.CHESS, 4, PlayingRole.STRATEGIST, 50);

        boolean added = team.addMember(extraParticipant);
        assertFalse(added);
    }

    @Test
    @DisplayName("Test cannot add third leader (max 2)")
    void testCannotAddThirdLeader() throws ValidationException, EmailFormatException {
        Participant leader3 = new Participant("P005", "Leader3", "leader3@email.com", 25,
                GameType.CSGO, 9, PlayingRole.COORDINATOR, 97);

        team.addMember(leaderParticipant); // First leader
        team.addMember(anotherLeader);      // Second leader

        boolean added = team.addMember(leader3); // Third leader - should fail

        assertFalse(added);
        assertEquals(2, team.getCurrentSize());
    }

    @Test
    @DisplayName("Test removing member from team")
    void testRemoveMember() {
        team.addMember(leaderParticipant);
        team.addMember(balancedParticipant);
        assertEquals(2, team.getCurrentSize());

        boolean removed = team.removeMember(leaderParticipant);

        assertTrue(removed);
        assertEquals(1, team.getCurrentSize());
        assertEquals("Unassigned", leaderParticipant.getAssignedTeam());
    }

    @Test
    @DisplayName("Test removing null member returns false")
    void testRemoveNullMember() {
        team.addMember(leaderParticipant);
        boolean removed = team.removeMember(null);

        assertFalse(removed);
        assertEquals(1, team.getCurrentSize());
    }

    @Test
    @DisplayName("Test removing non-existent member returns false")
    void testRemoveNonExistentMember() {
        team.addMember(leaderParticipant);
        boolean removed = team.removeMember(balancedParticipant);

        assertFalse(removed);
        assertEquals(1, team.getCurrentSize());
    }

    // ==================== METRICS TESTS ====================

    @Test
    @DisplayName("Test diversity score with diverse team")
    void testDiversityScoreHighDiversity() {
        team.addMember(leaderParticipant);    // VALORANT, STRATEGIST, LEADER
        team.addMember(balancedParticipant);  // DOTA, ATTACKER, BALANCED
        team.addMember(thinkerParticipant);   // FIFA, DEFENDER, THINKER
        team.addMember(anotherLeader);        // BASKETBALL, SUPPORTER, LEADER

        double diversity = team.getDiversityScore();
        assertTrue(diversity > 0.7, "Diverse team should have high diversity score");
    }

    @Test
    @DisplayName("Test diversity score with homogeneous team")
    void testDiversityScoreLowDiversity() throws ValidationException, EmailFormatException {
        Team homogeneousTeam = new Team("TEAM-002", "Beta", 4);

        for (int i = 0; i < 4; i++) {
            Participant p = new Participant("P00" + i, "Test" + i, "test" + i + "@email.com", 20,
                    GameType.VALORANT, 8, PlayingRole.ATTACKER, 93);
            homogeneousTeam.addMember(p);
        }

        double diversity = homogeneousTeam.getDiversityScore();
        assertTrue(diversity < 0.5, "Homogeneous team should have low diversity");
    }

    @Test
    @DisplayName("Test balance score calculation")
    void testBalanceScore() {
        team.addMember(leaderParticipant);
        team.addMember(balancedParticipant);
        team.addMember(thinkerParticipant);
        team.addMember(anotherLeader);

        double balance = team.getBalanceScore();
        assertTrue(balance >= 0.0 && balance <= 1.0);
    }

    @Test
    @DisplayName("Test overall score is average of diversity and balance")
    void testOverallScore() {
        team.addMember(leaderParticipant);
        team.addMember(balancedParticipant);

        double expected = (team.getDiversityScore() + team.getBalanceScore()) / 2.0;
        assertEquals(expected, team.getOverallScore(), 0.01);
    }

    @Test
    @DisplayName("Test hasPersonalityType")
    void testHasPersonalityType() {
        team.addMember(leaderParticipant);

        assertTrue(team.hasPersonalityType(PersonalityType.LEADER));
        assertFalse(team.hasPersonalityType(PersonalityType.BALANCED));
        assertFalse(team.hasPersonalityType(PersonalityType.THINKER));
    }

    @Test
    @DisplayName("Test getPersonalityCount")
    void testGetPersonalityCount() {
        team.addMember(leaderParticipant);
        team.addMember(anotherLeader);
        team.addMember(balancedParticipant);

        assertEquals(2, team.getPersonalityCount(PersonalityType.LEADER));
        assertEquals(1, team.getPersonalityCount(PersonalityType.BALANCED));
        assertEquals(0, team.getPersonalityCount(PersonalityType.THINKER));
    }

    @Test
    @DisplayName("Test hasRole")
    void testHasRole() {
        team.addMember(leaderParticipant); // STRATEGIST

        assertTrue(team.hasRole(PlayingRole.STRATEGIST));
        assertFalse(team.hasRole(PlayingRole.ATTACKER));
        assertFalse(team.hasRole(PlayingRole.DEFENDER));
    }

    @Test
    @DisplayName("Test getRoleCount")
    void testGetRoleCount() {
        team.addMember(leaderParticipant);    // STRATEGIST
        team.addMember(balancedParticipant);  // ATTACKER
        team.addMember(thinkerParticipant);   // DEFENDER

        assertEquals(1, team.getRoleCount(PlayingRole.STRATEGIST));
        assertEquals(1, team.getRoleCount(PlayingRole.ATTACKER));
        assertEquals(1, team.getRoleCount(PlayingRole.DEFENDER));
        assertEquals(0, team.getRoleCount(PlayingRole.SUPPORTER));
    }

    @Test
    @DisplayName("Test getUniqueGames")
    void testGetUniqueGames() {
        team.addMember(leaderParticipant);    // VALORANT
        team.addMember(balancedParticipant);  // DOTA
        team.addMember(thinkerParticipant);   // FIFA

        Set<GameType> games = team.getUniqueGames();

        assertEquals(3, games.size());
        assertTrue(games.contains(GameType.VALORANT));
        assertTrue(games.contains(GameType.DOTA));
        assertTrue(games.contains(GameType.FIFA));
    }

    @Test
    @DisplayName("Test getUniqueRoles")
    void testGetUniqueRoles() {
        team.addMember(leaderParticipant);    // STRATEGIST
        team.addMember(balancedParticipant);  // ATTACKER
        team.addMember(thinkerParticipant);   // DEFENDER

        Set<PlayingRole> roles = team.getUniqueRoles();

        assertEquals(3, roles.size());
        assertTrue(roles.contains(PlayingRole.STRATEGIST));
        assertTrue(roles.contains(PlayingRole.ATTACKER));
        assertTrue(roles.contains(PlayingRole.DEFENDER));
    }

    @Test
    @DisplayName("Test getUniquePersonalities")
    void testGetUniquePersonalities() {
        team.addMember(leaderParticipant);    // LEADER
        team.addMember(balancedParticipant);  // BALANCED
        team.addMember(thinkerParticipant);   // THINKER

        Set<PersonalityType> personalities = team.getUniquePersonalities();

        assertEquals(3, personalities.size());
        assertTrue(personalities.contains(PersonalityType.LEADER));
        assertTrue(personalities.contains(PersonalityType.BALANCED));
        assertTrue(personalities.contains(PersonalityType.THINKER));
    }

    @Test
    @DisplayName("Test getAverageSkillLevel")
    void testGetAverageSkillLevel() {
        team.addMember(leaderParticipant);    // Skill 8
        team.addMember(thinkerParticipant);   // Skill 5

        // Average: (8 + 5) / 2 = 6.5
        assertEquals(6.5, team.getAverageSkillLevel(), 0.01);
    }

    @Test
    @DisplayName("Test isValid with valid team")
    void testIsValidTrue() {
        team.addMember(leaderParticipant);
        team.addMember(balancedParticipant);
        team.addMember(thinkerParticipant);

        assertTrue(team.isValid());
    }

    @Test
    @DisplayName("Test isValid with insufficient members")
    void testIsValidInsufficientMembers() {
        team.addMember(leaderParticipant);
        team.addMember(balancedParticipant);

        assertFalse(team.isValid()); // Only 2 members, need 3
    }

    @Test
    @DisplayName("Test isValid without leader")
    void testIsValidWithoutLeader() throws ValidationException, EmailFormatException {
        Participant thinker2 = new Participant("P005", "Thinker2", "t2@email.com", 22,
                GameType.CSGO, 6, PlayingRole.COORDINATOR, 55);
        Participant thinker3 = new Participant("P006", "Thinker3", "t3@email.com", 23,
                GameType.VALORANT, 7, PlayingRole.ATTACKER, 60);

        team.addMember(thinkerParticipant);
        team.addMember(thinker2);
        team.addMember(thinker3);

        assertFalse(team.isValid()); // No leader
    }

    @Test
    @DisplayName("Test defensive copy of members list")
    void testDefensiveCopyMembers() {
        team.addMember(leaderParticipant);

        List<Participant> members = team.getMembers();
        members.clear();

        assertEquals(1, team.getCurrentSize(), "Original list should not be modified");
    }

    @Test
    @DisplayName("Test toCSV format")
    void testToCSVFormat() {
        team.addMember(leaderParticipant);
        team.addMember(balancedParticipant);

        String csv = team.toCSV();

        assertTrue(csv.contains("TEAM-001"));
        assertTrue(csv.contains("Alpha"));
        assertTrue(csv.contains("P001"));
        assertTrue(csv.contains("P002"));
    }

    @Test
    @DisplayName("Test getDetailedSummary")
    void testGetDetailedSummary() {
        team.addMember(leaderParticipant);
        team.addMember(balancedParticipant);

        String summary = team.getDetailedSummary();

        assertNotNull(summary);
        assertTrue(summary.contains("Alpha"));
        assertTrue(summary.contains("Alex Leader"));
        assertTrue(summary.contains("Sarah Balanced"));
    }

    @Test
    @DisplayName("Test toString method")
    void testToString() {
        team.addMember(leaderParticipant);
        team.addMember(balancedParticipant);

        String str = team.toString();

        assertNotNull(str);
        assertTrue(str.contains("TEAM-001"));
        assertTrue(str.contains("Alpha"));
    }
}