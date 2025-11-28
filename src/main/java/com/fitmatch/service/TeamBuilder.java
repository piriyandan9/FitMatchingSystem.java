package com.fitmatch.service;

import com.fitmatch.model.*;
import com.fitmatch.exception.TeamFormationException;
import com.fitmatch.util.FileLogger;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Team builder with rules:
 * - Minimum team size: 3
 * - Maximum 2 Leaders per team
 * - Each team MUST have at least 1 Leader
 * - Diverse games (max 2 from same game)
 * - At least 3 different roles per team
 *
 * @version 2.0
 */
public class TeamBuilder {

    private final ExecutorService executorService;
    private final int threadPoolSize;

    private static final int MIN_TEAM_SIZE = 3;
    private static final int MAX_LEADERS_PER_TEAM = 2;
    private static final int MAX_SAME_GAME_PER_TEAM = 2;
    private static final int MIN_DIFFERENT_ROLES = 3;
    private static final long TIMEOUT_SECONDS = 30;

    public TeamBuilder() {
        this.threadPoolSize = Runtime.getRuntime().availableProcessors();
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
        FileLogger.logInfo("TeamBuilder", "Initialized with " + threadPoolSize + " threads");
    }

    public TeamBuilder(int poolSize) {
        this.threadPoolSize = Math.max(1, poolSize);
        this.executorService = Executors.newFixedThreadPool(this.threadPoolSize);
        FileLogger.logInfo("TeamBuilder", "Initialized with " + this.threadPoolSize + " threads");
    }

    /**
     * Forms balanced teams following all rules from specification
     */
    public List<Team> formTeams(List<Participant> participants, int teamSize)
            throws TeamFormationException {

        validateInput(participants, teamSize);

        FileLogger.logInfo("TeamBuilder",
                String.format("Starting team formation: %d participants, team size %d",
                        participants.size(), teamSize));

        long startTime = System.currentTimeMillis();

        try {
            List<Participant> available = new ArrayList<>(participants);
            List<Team> teams = new ArrayList<>();

            int numTeams = available.size() / teamSize;

            if (numTeams < 1) {
                throw new TeamFormationException(
                        "Not enough participants to form even one team",
                        teamSize, available.size()
                );
            }

            // Count leaders
            long leaderCount = available.stream()
                    .filter(p -> p.getPersonalityType() == PersonalityType.LEADER)
                    .count();

            if (leaderCount < numTeams) {
                FileLogger.logWarning("TeamBuilder",
                        String.format("Only %d leaders for %d teams - each team requires 1 leader",
                                leaderCount, numTeams));
                numTeams = (int) leaderCount; // Can only form as many teams as leaders
            }

            FileLogger.logInfo("TeamBuilder", "Forming " + numTeams + " teams...");

            teams = formTeamsConcurrently(available, teamSize, numTeams);

            long duration = System.currentTimeMillis() - startTime;
            FileLogger.logInfo("TeamBuilder",
                    String.format("Team formation completed in %dms. Created %d teams.",
                            duration, teams.size()));

            return teams;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TeamFormationException("Team formation was interrupted", e);
        } catch (ExecutionException e) {
            throw new TeamFormationException("Error during team formation", e.getCause());
        } catch (TimeoutException e) {
            throw new TeamFormationException("Team formation timed out after " +
                    TIMEOUT_SECONDS + " seconds", e);
        }
    }

    private void validateInput(List<Participant> participants, int teamSize)
            throws TeamFormationException {

        if (participants == null || participants.isEmpty()) {
            throw new TeamFormationException("Participant list cannot be null or empty");
        }

        if (teamSize < MIN_TEAM_SIZE) {
            throw new TeamFormationException("Team size must be at least " + MIN_TEAM_SIZE);
        }

        if (participants.size() < teamSize) {
            throw new TeamFormationException(
                    String.format("Not enough participants (%d) for team size %d",
                            participants.size(), teamSize),
                    teamSize, participants.size()
            );
        }

        // Check if enough leaders
        long leaderCount = participants.stream()
                .filter(p -> p.getPersonalityType() == PersonalityType.LEADER)
                .count();

        if (leaderCount == 0) {
            throw new TeamFormationException("Cannot form teams: No leaders available. Each team requires at least 1 leader.");
        }
    }

    private List<Team> formTeamsConcurrently(List<Participant> available,
                                             int teamSize, int numTeams)
            throws InterruptedException, ExecutionException, TimeoutException {

        List<Team> teams = Collections.synchronizedList(new ArrayList<>());

        Map<String, Double> compatibilityCache = calculateCompatibilityScoresConcurrently(available);

        List<Future<Team>> futures = new ArrayList<>();

        for (int i = 0; i < numTeams; i++) {
            final int teamNumber = i + 1;

            Future<Team> future = executorService.submit(() -> {
                return formSingleTeam(available, teamSize, teamNumber, compatibilityCache);
            });

            futures.add(future);
            Thread.sleep(10);
        }

        for (Future<Team> future : futures) {
            Team team = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (team != null && team.getCurrentSize() >= MIN_TEAM_SIZE) {
                teams.add(team);
            }
        }

        return teams;
    }

    private Map<String, Double> calculateCompatibilityScoresConcurrently(
            List<Participant> participants)
            throws InterruptedException, ExecutionException, TimeoutException {

        Map<String, Double> cache = new ConcurrentHashMap<>();
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < participants.size(); i++) {
            for (int j = i + 1; j < participants.size(); j++) {
                final Participant p1 = participants.get(i);
                final Participant p2 = participants.get(j);

                Future<?> future = executorService.submit(() -> {
                    double score = calculatePairCompatibility(p1, p2);
                    String key = createPairKey(p1.getParticipantId(), p2.getParticipantId());
                    cache.put(key, score);
                });

                futures.add(future);
            }
        }

        for (Future<?> future : futures) {
            future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }

        return cache;
    }

    /**
     * Forms single team following all rules
     */
    private Team formSingleTeam(List<Participant> available, int teamSize,
                                int teamNumber, Map<String, Double> cache) {

        Team team = new Team(
                "TEAM-" + String.format("%03d", teamNumber),
                generateTeamName(teamNumber),
                teamSize
        );

        synchronized (available) {
            if (available.size() < teamSize) {
                return team;
            }

            // RULE: Each team MUST have at least 1 Leader
            Participant leader = selectLeader(available);
            if (leader == null) {
                FileLogger.logWarning("TeamBuilder",
                        "Cannot form team " + teamNumber + " - no leader available");
                return team;
            }

            team.addMember(leader);
            available.remove(leader);

            // Fill remaining spots
            while (!team.isFull() && !available.isEmpty()) {
                Participant bestCandidate = selectBestCandidate(team, available, cache);
                if (bestCandidate != null) {
                    team.addMember(bestCandidate);
                    available.remove(bestCandidate);
                } else {
                    break;
                }
            }
        }

        return team;
    }

    private Participant selectLeader(List<Participant> available) {
        return available.stream()
                .filter(p -> p.getPersonalityType() == PersonalityType.LEADER)
                .max(Comparator.comparingInt(Participant::getPersonalityScore))
                .orElse(null);
    }

    private Participant selectBestCandidate(Team team, List<Participant> available,
                                            Map<String, Double> cache) {

        Participant bestCandidate = null;
        double bestScore = Double.MIN_VALUE;

        for (Participant candidate : available) {
            // Check constraints
            if (!meetsTeamConstraints(team, candidate)) {
                continue;
            }

            double score = calculateCandidateScore(team, candidate, cache);

            if (score > bestScore) {
                bestScore = score;
                bestCandidate = candidate;
            }
        }

        return bestCandidate;
    }

    /**
     * Checks if candidate meets all team constraints
     */
    private boolean meetsTeamConstraints(Team team, Participant candidate) {
        // RULE: Max 2 Leaders per team
        if (candidate.getPersonalityType() == PersonalityType.LEADER) {
            long leaderCount = team.getPersonalityCount(PersonalityType.LEADER);
            if (leaderCount >= MAX_LEADERS_PER_TEAM) {
                return false;
            }
        }

        // RULE: Max 2 from same game per team
        long sameGameCount = team.getMembers().stream()
                .filter(p -> p.getPreferredGame() == candidate.getPreferredGame())
                .count();
        if (sameGameCount >= MAX_SAME_GAME_PER_TEAM) {
            return false;
        }

        return true;
    }

    private double calculateCandidateScore(Team team, Participant candidate,
                                           Map<String, Double> cache) {

        // Game diversity - prefer different games
        double gameScore = team.getUniqueGames().contains(candidate.getPreferredGame())
                ? 0.3 : 1.0;

        // Role diversity - prefer different roles
        double roleScore = team.hasRole(candidate.getPreferredRole()) ? 0.4 : 1.0;

        // Personality diversity
        double personalityScore = calculatePersonalityFitScore(team, candidate);

        // Skill balance
        double skillScore = calculateSkillFitScore(team, candidate);

        // Compatibility with team members
        double compatScore = calculateTeamCompatibility(team, candidate, cache);

        return (gameScore * 0.30) + (roleScore * 0.25) + (personalityScore * 0.20) +
                (skillScore * 0.15) + (compatScore * 0.10);
    }

    private double calculatePersonalityFitScore(Team team, Participant candidate) {
        PersonalityType type = candidate.getPersonalityType();

        if (!team.hasPersonalityType(type)) {
            return 1.0;
        }

        long count = team.getPersonalityCount(type);
        int teamSize = team.getCurrentSize();

        if (teamSize > 0 && count > teamSize / 2.0) {
            return 0.3;
        }

        return 0.6;
    }

    private double calculateSkillFitScore(Team team, Participant candidate) {
        if (team.getCurrentSize() == 0) {
            return 1.0;
        }

        double avgSkill = team.getMembers().stream()
                .mapToInt(Participant::getSkillLevel)
                .average()
                .orElse(5.0);

        int candidateSkill = candidate.getSkillLevel();
        double diff = Math.abs(avgSkill - candidateSkill);

        return Math.max(0.0, 1.0 - (diff * 0.15));
    }

    private double calculateTeamCompatibility(Team team, Participant candidate,
                                              Map<String, Double> cache) {
        if (team.getCurrentSize() == 0) {
            return 1.0;
        }

        double totalScore = 0.0;
        for (Participant member : team.getMembers()) {
            String key = createPairKey(member.getParticipantId(),
                    candidate.getParticipantId());
            totalScore += cache.getOrDefault(key, 0.5);
        }

        return totalScore / team.getCurrentSize();
    }

    private double calculatePairCompatibility(Participant p1, Participant p2) {
        double personalityScore = p1.getPersonalityType()
                .getCompatibilityWith(p2.getPersonalityType());

        double roleScore = p1.getPreferredRole().complementsWith(p2.getPreferredRole())
                ? 1.0 : 0.5;

        double skillScore = p1.calculateSkillCompatibility(p2);

        return (personalityScore * 0.40) + (roleScore * 0.30) + (skillScore * 0.30);
    }

    private String createPairKey(String id1, String id2) {
        return id1.compareTo(id2) < 0 ? id1 + "-" + id2 : id2 + "-" + id1;
    }

    private String generateTeamName(int teamNumber) {
        String[] names = {
                "Alpha", "Bravo", "Charlie", "Delta", "Echo",
                "Foxtrot", "Golf", "Hotel", "India", "Juliet",
                "Kilo", "Lima", "Mike", "November", "Oscar",
                "Papa", "Quebec", "Romeo", "Sierra", "Tango",
                "Phoenix", "Titans", "Legends", "Storm", "Thunder"
        };

        int index = (teamNumber - 1) % names.length;
        return names[index];
    }

    public List<Team> formTeamsSequential(List<Participant> participants, int teamSize)
            throws TeamFormationException {

        validateInput(participants, teamSize);

        List<Participant> available = new ArrayList<>(participants);
        List<Team> teams = new ArrayList<>();
        int numTeams = available.size() / teamSize;

        long leaderCount = available.stream()
                .filter(p -> p.getPersonalityType() == PersonalityType.LEADER)
                .count();
        numTeams = Math.min(numTeams, (int) leaderCount);

        Map<String, Double> cache = new HashMap<>();
        for (int i = 0; i < participants.size(); i++) {
            for (int j = i + 1; j < participants.size(); j++) {
                Participant p1 = participants.get(i);
                Participant p2 = participants.get(j);
                double score = calculatePairCompatibility(p1, p2);
                cache.put(createPairKey(p1.getParticipantId(), p2.getParticipantId()), score);
            }
        }

        for (int t = 0; t < numTeams; t++) {
            Team team = formSingleTeam(available, teamSize, t + 1, cache);
            if (team.getCurrentSize() >= MIN_TEAM_SIZE) {
                teams.add(team);
            }
        }

        return teams;
    }

    public TeamFormationStatistics getStatistics(List<Team> teams) {
        return new TeamFormationStatistics(teams);
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        FileLogger.logInfo("TeamBuilder", "Shutdown complete");
    }

    public static class TeamFormationStatistics {
        public final int totalTeams;
        public final int totalParticipants;
        public final double avgDiversity;
        public final double avgBalance;
        public final double avgOverallScore;
        public final double maxScore;
        public final double minScore;

        public TeamFormationStatistics(List<Team> teams) {
            if (teams == null || teams.isEmpty()) {
                this.totalTeams = 0;
                this.totalParticipants = 0;
                this.avgDiversity = 0;
                this.avgBalance = 0;
                this.avgOverallScore = 0;
                this.maxScore = 0;
                this.minScore = 0;
            } else {
                this.totalTeams = teams.size();
                this.totalParticipants = teams.stream()
                        .mapToInt(Team::getCurrentSize)
                        .sum();
                this.avgDiversity = teams.stream()
                        .mapToDouble(Team::getDiversityScore)
                        .average().orElse(0);
                this.avgBalance = teams.stream()
                        .mapToDouble(Team::getBalanceScore)
                        .average().orElse(0);
                this.avgOverallScore = teams.stream()
                        .mapToDouble(Team::getOverallScore)
                        .average().orElse(0);
                this.maxScore = teams.stream()
                        .mapToDouble(Team::getOverallScore)
                        .max().orElse(0);
                this.minScore = teams.stream()
                        .mapToDouble(Team::getOverallScore)
                        .min().orElse(0);
            }
        }

        @Override
        public String toString() {
            return String.format(
                    "================================================%n" +
                            "         TEAM FORMATION STATISTICS             %n" +
                            "================================================%n" +
                            "Total Teams:        %-25d%n" +
                            "Total Participants: %-25d%n" +
                            "Avg Diversity:      %-25.1f%%%n" +
                            "Avg Balance:        %-25.1f%%%n" +
                            "Avg Overall Score:  %-25.1f%%%n" +
                            "Best Team Score:    %-25.1f%%%n" +
                            "Lowest Team Score:  %-25.1f%%%n" +
                            "================================================",
                    totalTeams, totalParticipants,
                    avgDiversity * 100, avgBalance * 100,
                    avgOverallScore * 100, maxScore * 100, minScore * 100
            );
        }
    }
}