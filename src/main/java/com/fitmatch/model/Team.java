package com.fitmatch.model;

import java.util.*;
import java.util.stream.Collectors;

public class Team {

    private String teamId;
    private String teamName;
    private int targetSize;
    private List<Participant> members;
    private double diversityScore;
    private double balanceScore;

    private static final int MIN_TEAM_SIZE = 3;
    private static final int MAX_LEADERS = 2;

    public Team(String teamId, String teamName, int targetSize) {
        if (teamId == null || teamId.trim().isEmpty()) {
            throw new IllegalArgumentException("Team ID cannot be null or empty");
        }
        if (teamName == null || teamName.trim().isEmpty()) {
            throw new IllegalArgumentException("Team name cannot be null or empty");
        }
        if (targetSize < MIN_TEAM_SIZE) {
            throw new IllegalArgumentException("Team size must be at least " + MIN_TEAM_SIZE);
        }

        this.teamId = teamId.trim();
        this.teamName = teamName.trim();
        this.targetSize = targetSize;
        this.members = new ArrayList<>();
        this.diversityScore = 0.0;
        this.balanceScore = 0.0;
    }

    /**
     * Adds member with validation
     * - Cannot exceed target size
     * - Max 2 leaders allowed
     */
    public boolean addMember(Participant participant) {
        if (participant == null || isFull() || members.contains(participant)) {
            return false;
        }

        // Check leader constraint: max 2 leaders
        if (participant.getPersonalityType() == PersonalityType.LEADER) {
            long leaderCount = getPersonalityCount(PersonalityType.LEADER);
            if (leaderCount >= MAX_LEADERS) {
                return false;
            }
        }

        members.add(participant);
        participant.setAssignedTeam(this.teamName);
        calculateMetrics();
        return true;
    }

    public boolean removeMember(Participant participant) {
        if (participant == null || !members.contains(participant)) {
            return false;
        }

        members.remove(participant);
        participant.setAssignedTeam("Unassigned");
        calculateMetrics();
        return true;
    }

    public boolean isFull() {
        return members.size() >= targetSize;
    }

    /**
     * Team is complete if:
     * - Has at least MIN_TEAM_SIZE (3) members
     * - Has at least 1 leader
     */
    public boolean isComplete() {
        return members.size() >= MIN_TEAM_SIZE &&
                hasPersonalityType(PersonalityType.LEADER);
    }

    /**
     * Team is valid if:
     * - Has min 3 members
     * - Has at least 1 leader
     * - Has max 2 leaders
     */
    public boolean isValid() {
        if (members.size() < MIN_TEAM_SIZE) return false;
        if (!hasPersonalityType(PersonalityType.LEADER)) return false;
        if (getPersonalityCount(PersonalityType.LEADER) > MAX_LEADERS) return false;
        return true;
    }

    public int getRemainingSpots() {
        return Math.max(0, targetSize - members.size());
    }

    private void calculateMetrics() {
        if (members.size() < 2) {
            diversityScore = 0.0;
            balanceScore = 0.0;
            return;
        }

        diversityScore = calculateDiversityScore();
        balanceScore = calculateBalanceScore();
    }

    private double calculateDiversityScore() {
        double gameDiversity = calculateGameDiversity();
        double roleDiversity = calculateRoleDiversity();
        double personalityDiversity = calculatePersonalityDiversity();

        return (gameDiversity * 0.30) + (roleDiversity * 0.40) + (personalityDiversity * 0.30);
    }

    private double calculateGameDiversity() {
        Set<GameType> uniqueGames = members.stream()
                .map(Participant::getPreferredGame)
                .collect(Collectors.toSet());

        int maxPossible = Math.min(members.size(), GameType.values().length);
        return (double) uniqueGames.size() / maxPossible;
    }

    private double calculateRoleDiversity() {
        Set<PlayingRole> uniqueRoles = members.stream()
                .map(Participant::getPreferredRole)
                .collect(Collectors.toSet());

        int maxPossible = Math.min(members.size(), PlayingRole.values().length);
        return (double) uniqueRoles.size() / maxPossible;
    }

    private double calculatePersonalityDiversity() {
        Set<PersonalityType> uniquePersonalities = members.stream()
                .map(Participant::getPersonalityType)
                .collect(Collectors.toSet());

        int maxPossible = Math.min(members.size(), PersonalityType.values().length);
        return (double) uniquePersonalities.size() / maxPossible;
    }

    private double calculateBalanceScore() {
        double skillBalance = calculateSkillBalance();
        double personalityBalance = calculatePersonalityBalance();
        double hasLeader = hasPersonalityType(PersonalityType.LEADER) ? 1.0 : 0.0;

        return (skillBalance * 0.40) + (personalityBalance * 0.40) + (hasLeader * 0.20);
    }

    private double calculateSkillBalance() {
        if (members.size() < 2) return 0.0;

        double avgSkill = members.stream()
                .mapToInt(Participant::getSkillLevel)
                .average()
                .orElse(0.0);

        double variance = members.stream()
                .mapToDouble(p -> Math.pow(p.getSkillLevel() - avgSkill, 2))
                .average()
                .orElse(0.0);

        return Math.max(0.0, 1.0 - (variance / 25.0));
    }

    private double calculatePersonalityBalance() {
        Map<PersonalityType, Long> counts = members.stream()
                .collect(Collectors.groupingBy(
                        Participant::getPersonalityType,
                        Collectors.counting()
                ));

        double maxAllowed = Math.ceil(members.size() / 2.0);
        long maxCount = counts.values().stream().mapToLong(Long::longValue).max().orElse(0);

        if (maxCount <= maxAllowed) {
            return 1.0;
        }
        return maxAllowed / maxCount;
    }

    public boolean hasPersonalityType(PersonalityType type) {
        return members.stream().anyMatch(p -> p.getPersonalityType() == type);
    }

    public boolean hasRole(PlayingRole role) {
        return members.stream().anyMatch(p -> p.getPreferredRole() == role);
    }

    public long getPersonalityCount(PersonalityType type) {
        return members.stream().filter(p -> p.getPersonalityType() == type).count();
    }

    public long getRoleCount(PlayingRole role) {
        return members.stream().filter(p -> p.getPreferredRole() == role).count();
    }

    public double getAverageSkillLevel() {
        return members.stream()
                .mapToInt(Participant::getSkillLevel)
                .average()
                .orElse(0.0);
    }

    public Set<GameType> getUniqueGames() {
        return members.stream()
                .map(Participant::getPreferredGame)
                .collect(Collectors.toSet());
    }

    public Set<PlayingRole> getUniqueRoles() {
        return members.stream()
                .map(Participant::getPreferredRole)
                .collect(Collectors.toSet());
    }

    public Set<PersonalityType> getUniquePersonalities() {
        return members.stream()
                .map(Participant::getPersonalityType)
                .collect(Collectors.toSet());
    }

    // Getters
    public String getTeamId() { return teamId; }
    public String getTeamName() { return teamName; }
    public int getTargetSize() { return targetSize; }
    public int getCurrentSize() { return members.size(); }
    public double getDiversityScore() { return diversityScore; }
    public double getBalanceScore() { return balanceScore; }
    public List<Participant> getMembers() { return new ArrayList<>(members); }

    public double getOverallScore() {
        return (diversityScore + balanceScore) / 2.0;
    }

    public String toCSV() {
        String memberIds = members.stream()
                .map(Participant::getParticipantId)
                .collect(Collectors.joining(";"));

        String memberNames = members.stream()
                .map(Participant::getName)
                .collect(Collectors.joining(";"));

        return String.format("%s,%s,%d,%d,%s,%s,%.2f,%.2f,%.2f",
                teamId, teamName, targetSize, members.size(),
                memberIds, memberNames,
                diversityScore, balanceScore, getOverallScore()
        );
    }

    @Override
    public String toString() {
        return String.format("Team[%s - %s] Size: %d/%d, Diversity: %.2f%%, Balance: %.2f%%",
                teamId, teamName, members.size(), targetSize,
                diversityScore * 100, balanceScore * 100);
    }

    public String getDetailedSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("================================================================%n"));
        sb.append(String.format(" TEAM: %-20s ID: %-10s                   %n", teamName, teamId));
        sb.append(String.format("================================================================%n"));
        sb.append(String.format(" Members: %d/%d        Diversity: %.1f%%        Balance: %.1f%%   %n",
                members.size(), targetSize, diversityScore * 100, balanceScore * 100));
        sb.append(String.format("================================================================%n"));
        sb.append(String.format(" %-62s %n", "TEAM MEMBERS:"));

        for (Participant p : members) {
            sb.append(String.format("  - %-58s %n",
                    String.format("%s (%s) - %s, %s, Skill:%d",
                            p.getName(),
                            p.getPersonalityType().getDisplayName(),
                            p.getPreferredRole().getDisplayName(),
                            p.getPreferredGame().getDisplayName(),
                            p.getSkillLevel())));
        }

        sb.append(String.format("================================================================%n"));
        sb.append(String.format(" Games: %-55s %n", getUniqueGames()));
        sb.append(String.format(" Roles: %-55s %n", getUniqueRoles()));
        sb.append(String.format(" Personalities: %-47s %n", getUniquePersonalities()));
        sb.append(String.format(" Leaders: %d (Min:1, Max:2)                                    %n",
                getPersonalityCount(PersonalityType.LEADER)));
        sb.append(String.format("================================================================%n"));

        return sb.toString();
    }
}