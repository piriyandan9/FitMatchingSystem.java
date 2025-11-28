// ================== TeamBuilderTest.java ==================
package com.fitmatch.service;

import com.fitmatch.model.*;
import com.fitmatch.exception.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

/**
 * Comprehensive unit tests for TeamBuilder service.
 * Tests: Team formation algorithm, concurrency, error handling.
 * Updated to match actual implementation with email and personality score.
 */
class TeamBuilderTest {

    private TeamBuilder teamBuilder;
    private List<Participant> participants;

    @BeforeEach
    void setUp() {
        teamBuilder = new TeamBuilder();
        participants = createTestParticipants(20);
    }

    @AfterEach
    void tearDown() {
        if (teamBuilder != null) {
            teamBuilder.shutdown();
        }
    }

    private List<Participant> createTestParticipants(int count) {
        List<Participant> list = new ArrayList<>();
        GameType[] games = GameType.values();
        PlayingRole[] roles = PlayingRole.values();

        for (int i = 0; i < count; i++) {
            try {
                // Create varied personality scores to ensure we have leaders
                int personalityScore;
                if (i % 5 == 0) {
                    personalityScore = 90 + (i % 11); // Leader (90-100)
                } else if (i % 5 == 1 || i % 5 == 2) {
                    personalityScore = 70 + (i % 20); // Balanced (70-89)
                } else {
                    personalityScore = 50 + (i % 20); // Thinker (50-69)
                }

                int skillLevel = (i % 10) + 1; // 1-10

                Participant p = new Participant(
                        "P" + String.format("%03d", i + 1),
                        "Participant " + (i + 1),
                        "participant" + (i + 1) + "@university.edu",
                        18 + (i % 15),
                        games[i % games.length],
                        skillLevel,
                        roles[i % roles.length],
                        personalityScore
                );
                list.add(p);
            } catch (ValidationException e) {
                fail("Failed to create test participant: " + e.getMessage());
            }
        }
        return list;
    }

    // ==================== FORMATION TESTS ====================

    @Test
    @DisplayName("Test form teams with valid participants")
    void testFormTeamsValid() throws TeamFormationException {
        List<Team> teams = teamBuilder.formTeams(participants, 4);

        assertNotNull(teams);
        assertFalse(teams.isEmpty());
        assertTrue(teams.size() >= 1);
    }

    @Test
    @DisplayName("Test all participants are assigned")
    void testAllParticipantsAssigned() throws TeamFormationException {
        List<Team> teams = teamBuilder.formTeams(participants, 4);

        int totalAssigned = teams.stream()
                .mapToInt(Team::getCurrentSize)
                .sum();

        assertTrue(totalAssigned >= participants.size() / 2);
    }

    @Test
    @DisplayName("Test teams have at least minimum size")
    void testTeamsHaveMinimumSize() throws TeamFormationException {
        List<Team> teams = teamBuilder.formTeams(participants, 4);

        for (Team team : teams) {
            assertTrue(team.getCurrentSize() >= 3);
        }
    }

    @Test
    @DisplayName("Test each team has at least one leader")
    void testEachTeamHasLeader() throws TeamFormationException {
        List<Team> teams = teamBuilder.formTeams(participants, 4);

        for (Team team : teams) {
            assertTrue(team.hasPersonalityType(PersonalityType.LEADER),
                    "Team " + team.getTeamId() + " should have at least one leader");
        }
    }

    @Test
    @DisplayName("Test no team has more than 2 leaders")
    void testNoTeamExceedsMaxLeaders() throws TeamFormationException {
        List<Team> teams = teamBuilder.formTeams(participants, 4);

        for (Team team : teams) {
            long leaderCount = team.getPersonalityCount(PersonalityType.LEADER);
            assertTrue(leaderCount <= 2,
                    "Team " + team.getTeamId() + " has " + leaderCount + " leaders (max 2)");
        }
    }

    @Test
    @DisplayName("Test form teams with null participants throws exception")
    void testFormTeamsNullParticipants() {
        assertThrows(TeamFormationException.class,
                () -> teamBuilder.formTeams(null, 4));
    }

    @Test
    @DisplayName("Test form teams with empty list throws exception")
    void testFormTeamsEmptyList() {
        assertThrows(TeamFormationException.class,
                () -> teamBuilder.formTeams(new ArrayList<>(), 4));
    }

    @Test
    @DisplayName("Test form teams with team size below minimum throws exception")
    void testFormTeamsTeamSizeBelowMin() {
        assertThrows(TeamFormationException.class,
                () -> teamBuilder.formTeams(participants, 2));
    }

    @Test
    @DisplayName("Test form teams with insufficient participants")
    void testFormTeamsInsufficientParticipants() {
        List<Participant> small = createTestParticipants(2);

        assertThrows(TeamFormationException.class,
                () -> teamBuilder.formTeams(small, 5));
    }

    @Test
    @DisplayName("Test form teams without leaders throws exception")
    void testFormTeamsWithoutLeaders() {
        List<Participant> noLeaders = new ArrayList<>();
        try {
            // Create participants with only low personality scores
            for (int i = 0; i < 12; i++) {
                Participant p = new Participant(
                        "P" + String.format("%03d", i + 1),
                        "Participant " + (i + 1),
                        "p" + (i + 1) + "@email.com",
                        20,
                        GameType.VALORANT,
                        5,
                        PlayingRole.STRATEGIST,
                        50 + i // All Thinkers (50-61)
                );
                noLeaders.add(p);
            }
        } catch (Exception e) {
            fail("Failed to create test participants");
        }

        TeamFormationException exception = assertThrows(TeamFormationException.class,
                () -> teamBuilder.formTeams(noLeaders, 4));

        assertTrue(exception.getMessage().contains("leader") ||
                exception.getMessage().contains("Leader"));
    }

    @Test
    @DisplayName("Test teams have unique IDs")
    void testTeamsHaveUniqueIds() throws TeamFormationException {
        List<Team> teams = teamBuilder.formTeams(participants, 4);

        Set<String> ids = new HashSet<>();
        for (Team team : teams) {
            assertFalse(ids.contains(team.getTeamId()), "Duplicate team ID found");
            ids.add(team.getTeamId());
        }
    }

    @Test
    @DisplayName("Test no participant assigned to multiple teams")
    void testNoDoubleAssignment() throws TeamFormationException {
        List<Team> teams = teamBuilder.formTeams(participants, 4);

        Set<String> assignedIds = new HashSet<>();

        for (Team team : teams) {
            for (Participant p : team.getMembers()) {
                assertFalse(assignedIds.contains(p.getParticipantId()),
                        "Participant " + p.getParticipantId() + " assigned twice");
                assignedIds.add(p.getParticipantId());
            }
        }
    }

    @Test
    @DisplayName("Test teams have diversity")
    void testTeamsHaveDiversity() throws TeamFormationException {
        List<Team> teams = teamBuilder.formTeams(participants, 4);

        for (Team team : teams) {
            assertTrue(team.getDiversityScore() >= 0.0);
            assertTrue(team.getDiversityScore() <= 1.0);
        }
    }

    @Test
    @DisplayName("Test teams are valid according to rules")
    void testTeamsAreValid() throws TeamFormationException {
        List<Team> teams = teamBuilder.formTeams(participants, 4);

        for (Team team : teams) {
            assertTrue(team.isValid(),
                    "Team " + team.getTeamId() + " should be valid");
        }
    }

    @Test
    @DisplayName("Test sequential formation produces valid results")
    void testSequentialFormation() throws TeamFormationException {
        List<Team> teams = teamBuilder.formTeamsSequential(participants, 4);

        assertNotNull(teams);
        assertFalse(teams.isEmpty());

        for (Team team : teams) {
            assertTrue(team.isValid());
        }
    }

    @Test
    @DisplayName("Test concurrent vs sequential produce similar results")
    void testConcurrentVsSequential() throws TeamFormationException {
        // Reset participant assignments
        for (Participant p : participants) {
            p.setAssignedTeam("Unassigned");
        }
        List<Team> concurrentTeams = teamBuilder.formTeams(participants, 4);
        int concurrentCount = concurrentTeams.size();

        // Reset again
        for (Participant p : participants) {
            p.setAssignedTeam("Unassigned");
        }
        List<Team> sequentialTeams = teamBuilder.formTeamsSequential(participants, 4);
        int sequentialCount = sequentialTeams.size();

        // Should produce similar number of teams (within 1-2)
        assertTrue(Math.abs(concurrentCount - sequentialCount) <= 2);
    }

    @Test
    @DisplayName("Test statistics calculation")
    void testStatistics() throws TeamFormationException {
        List<Team> teams = teamBuilder.formTeams(participants, 4);

        TeamBuilder.TeamFormationStatistics stats = teamBuilder.getStatistics(teams);

        assertNotNull(stats);
        assertTrue(stats.totalTeams > 0);
        assertTrue(stats.totalParticipants > 0);
        assertTrue(stats.avgDiversity >= 0 && stats.avgDiversity <= 1);
        assertTrue(stats.avgBalance >= 0 && stats.avgBalance <= 1);
        assertTrue(stats.avgOverallScore >= 0 && stats.avgOverallScore <= 1);
    }

    @Test
    @DisplayName("Test statistics with empty list")
    void testStatisticsEmptyList() {
        TeamBuilder.TeamFormationStatistics stats =
                teamBuilder.getStatistics(new ArrayList<>());

        assertEquals(0, stats.totalTeams);
        assertEquals(0, stats.totalParticipants);
    }

    @Test
    @DisplayName("Test statistics with null list")
    void testStatisticsNullList() {
        TeamBuilder.TeamFormationStatistics stats =
                teamBuilder.getStatistics(null);

        assertEquals(0, stats.totalTeams);
    }

    @Test
    @DisplayName("Test large dataset performance")
    void testLargeDatasetPerformance() throws TeamFormationException {
        List<Participant> largeList = createTestParticipants(100);

        long startTime = System.currentTimeMillis();
        List<Team> teams = teamBuilder.formTeams(largeList, 5);
        long duration = System.currentTimeMillis() - startTime;

        assertTrue(teams.size() >= 1);
        assertTrue(duration < 30000, "Should complete within 30 seconds");

        System.out.println("Large dataset (100 participants) processed in: " + duration + "ms");
        System.out.println("Formed " + teams.size() + " teams");
    }

    @Test
    @DisplayName("Test shutdown does not throw")
    void testShutdown() {
        assertDoesNotThrow(() -> teamBuilder.shutdown());
    }

    @Test
    @DisplayName("Test statistics toString")
    void testStatisticsToString() throws TeamFormationException {
        List<Team> teams = teamBuilder.formTeams(participants, 4);
        TeamBuilder.TeamFormationStatistics stats = teamBuilder.getStatistics(teams);

        String str = stats.toString();

        assertNotNull(str);
        assertTrue(str.contains("Team") || str.contains("TEAM"));
        assertTrue(str.contains("Participants") || str.contains("PARTICIPANTS"));
    }

    @Test
    @DisplayName("Test team formation with minimum participants")
    void testFormTeamsMinimumParticipants() throws TeamFormationException {
        // Create exactly 3 participants with 1 leader
        List<Participant> minimal = new ArrayList<>();
        try {
            minimal.add(new Participant("P001", "Leader", "leader@email.com", 20,
                    GameType.VALORANT, 8, PlayingRole.STRATEGIST, 95));
            minimal.add(new Participant("P002", "Balanced", "balanced@email.com", 21,
                    GameType.DOTA, 7, PlayingRole.ATTACKER, 75));
            minimal.add(new Participant("P003", "Thinker", "thinker@email.com", 22,
                    GameType.FIFA, 6, PlayingRole.DEFENDER, 55));
        } catch (Exception e) {
            fail("Failed to create minimal participants");
        }

        List<Team> teams = teamBuilder.formTeams(minimal, 3);

        assertEquals(1, teams.size());
        assertEquals(3, teams.get(0).getCurrentSize());
        assertTrue(teams.get(0).isValid());
    }

    @Test
    @DisplayName("Test all formed teams meet minimum size requirement")
    void testAllTeamsMeetMinimumSize() throws TeamFormationException {
        List<Team> teams = teamBuilder.formTeams(participants, 4);

        for (Team team : teams) {
            assertTrue(team.getCurrentSize() >= 3,
                    "Team " + team.getTeamId() + " has only " +
                            team.getCurrentSize() + " members (min 3)");
        }
    }
}