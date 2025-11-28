// ================== PersonalityClassifierTest.java ==================
package com.fitmatch.service;

import com.fitmatch.model.PersonalityType;
import com.fitmatch.exception.InvalidPersonalityScoreException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for PersonalityClassifier service.
 * Tests: Classification logic, boundary values, validation, utility methods.
 * Updated to match actual implementation (20-100 score range, 3 personality types)
 */
class PersonalityClassifierTest {

    // ==================== CLASSIFICATION TESTS ====================

    @Test
    @DisplayName("Test classify Leader personality - mid range")
    void testClassifyLeaderMidRange() {
        PersonalityType type = PersonalityClassifier.classifyPersonality(95);
        assertEquals(PersonalityType.LEADER, type);
    }

    @Test
    @DisplayName("Test classify Leader personality - minimum boundary (90)")
    void testClassifyLeaderMinBoundary() {
        PersonalityType type = PersonalityClassifier.classifyPersonality(90);
        assertEquals(PersonalityType.LEADER, type);
    }

    @Test
    @DisplayName("Test classify Leader personality - maximum boundary (100)")
    void testClassifyLeaderMaxBoundary() {
        PersonalityType type = PersonalityClassifier.classifyPersonality(100);
        assertEquals(PersonalityType.LEADER, type);
    }

    @Test
    @DisplayName("Test classify Balanced personality - mid range")
    void testClassifyBalancedMidRange() {
        PersonalityType type = PersonalityClassifier.classifyPersonality(80);
        assertEquals(PersonalityType.BALANCED, type);
    }

    @Test
    @DisplayName("Test classify Balanced personality - minimum boundary (70)")
    void testClassifyBalancedMinBoundary() {
        PersonalityType type = PersonalityClassifier.classifyPersonality(70);
        assertEquals(PersonalityType.BALANCED, type);
    }

    @Test
    @DisplayName("Test classify Balanced personality - maximum boundary (89)")
    void testClassifyBalancedMaxBoundary() {
        PersonalityType type = PersonalityClassifier.classifyPersonality(89);
        assertEquals(PersonalityType.BALANCED, type);
    }

    @Test
    @DisplayName("Test classify Thinker personality - mid range")
    void testClassifyThinkerMidRange() {
        PersonalityType type = PersonalityClassifier.classifyPersonality(60);
        assertEquals(PersonalityType.THINKER, type);
    }

    @Test
    @DisplayName("Test classify Thinker personality - minimum boundary (50)")
    void testClassifyThinkerMinBoundary() {
        PersonalityType type = PersonalityClassifier.classifyPersonality(50);
        assertEquals(PersonalityType.THINKER, type);
    }

    @Test
    @DisplayName("Test classify Thinker personality - maximum boundary (69)")
    void testClassifyThinkerMaxBoundary() {
        PersonalityType type = PersonalityClassifier.classifyPersonality(69);
        assertEquals(PersonalityType.THINKER, type);
    }

    @Test
    @DisplayName("Test classify below Thinker minimum - mid range")
    void testClassifyBelowThinkerMidRange() {
        PersonalityType type = PersonalityClassifier.classifyPersonality(35);
        assertEquals(PersonalityType.THINKER, type); // Fallback to Thinker
    }

    @Test
    @DisplayName("Test classify at minimum boundary (20)")
    void testClassifyMinimumBoundary() {
        PersonalityType type = PersonalityClassifier.classifyPersonality(20);
        assertEquals(PersonalityType.THINKER, type); // Fallback
    }

    @Test
    @DisplayName("Test classify just below Thinker (49)")
    void testClassifyJustBelowThinker() {
        PersonalityType type = PersonalityClassifier.classifyPersonality(49);
        assertEquals(PersonalityType.THINKER, type); // Fallback
    }

    // ==================== BOUNDARY TRANSITION TESTS ====================

    @Test
    @DisplayName("Test boundary between low and Thinker")
    void testBoundaryToThinker() {
        assertEquals(PersonalityType.THINKER, PersonalityClassifier.classifyPersonality(49));
        assertEquals(PersonalityType.THINKER, PersonalityClassifier.classifyPersonality(50));
    }

    @Test
    @DisplayName("Test boundary between Thinker and Balanced")
    void testBoundaryThinkerToBalanced() {
        assertEquals(PersonalityType.THINKER, PersonalityClassifier.classifyPersonality(69));
        assertEquals(PersonalityType.BALANCED, PersonalityClassifier.classifyPersonality(70));
    }

    @Test
    @DisplayName("Test boundary between Balanced and Leader")
    void testBoundaryBalancedToLeader() {
        assertEquals(PersonalityType.BALANCED, PersonalityClassifier.classifyPersonality(89));
        assertEquals(PersonalityType.LEADER, PersonalityClassifier.classifyPersonality(90));
    }

    // ==================== PARAMETERIZED TESTS ====================

    @ParameterizedTest
    @ValueSource(ints = {90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100})
    @DisplayName("Test all Leader range scores")
    void testAllLeaderScores(int score) {
        assertEquals(PersonalityType.LEADER, PersonalityClassifier.classifyPersonality(score));
    }

    @ParameterizedTest
    @ValueSource(ints = {70, 75, 80, 85, 89})
    @DisplayName("Test all Balanced range scores")
    void testAllBalancedScores(int score) {
        assertEquals(PersonalityType.BALANCED, PersonalityClassifier.classifyPersonality(score));
    }

    @ParameterizedTest
    @ValueSource(ints = {50, 55, 60, 65, 69})
    @DisplayName("Test all Thinker range scores")
    void testAllThinkerScores(int score) {
        assertEquals(PersonalityType.THINKER, PersonalityClassifier.classifyPersonality(score));
    }

    @ParameterizedTest
    @ValueSource(ints = {20, 25, 30, 35, 40, 45, 49})
    @DisplayName("Test all below-Thinker scores default to Thinker")
    void testBelowThinkerScores(int score) {
        assertEquals(PersonalityType.THINKER, PersonalityClassifier.classifyPersonality(score));
    }

    // ==================== EXCEPTION TESTS ====================

    @Test
    @DisplayName("Test score below minimum (19) throws exception")
    void testScoreBelowMinimum() {
        InvalidPersonalityScoreException e = assertThrows(
                InvalidPersonalityScoreException.class,
                () -> PersonalityClassifier.classifyPersonality(19)
        );
        assertTrue(e.getMessage().contains("20") && e.getMessage().contains("100"));
    }

    @Test
    @DisplayName("Test score above maximum throws exception")
    void testScoreAboveMaximum() {
        assertThrows(InvalidPersonalityScoreException.class,
                () -> PersonalityClassifier.classifyPersonality(101));
    }

    @Test
    @DisplayName("Test very negative score throws exception")
    void testVeryNegativeScore() {
        assertThrows(InvalidPersonalityScoreException.class,
                () -> PersonalityClassifier.classifyPersonality(-100));
    }

    @Test
    @DisplayName("Test very high score throws exception")
    void testVeryHighScore() {
        assertThrows(InvalidPersonalityScoreException.class,
                () -> PersonalityClassifier.classifyPersonality(200));
    }

    // ==================== VALIDATION METHOD TESTS ====================

    @Test
    @DisplayName("Test isValidScore returns true for valid scores")
    void testIsValidScoreTrue() {
        assertTrue(PersonalityClassifier.isValidScore(20));
        assertTrue(PersonalityClassifier.isValidScore(50));
        assertTrue(PersonalityClassifier.isValidScore(100));
    }

    @Test
    @DisplayName("Test isValidScore returns false for invalid scores")
    void testIsValidScoreFalse() {
        assertFalse(PersonalityClassifier.isValidScore(19));
        assertFalse(PersonalityClassifier.isValidScore(101));
        assertFalse(PersonalityClassifier.isValidScore(-100));
        assertFalse(PersonalityClassifier.isValidScore(0));
    }

    @Test
    @DisplayName("Test isValidResponse for valid responses (1-5)")
    void testIsValidResponseTrue() {
        assertTrue(PersonalityClassifier.isValidResponse(1));
        assertTrue(PersonalityClassifier.isValidResponse(3));
        assertTrue(PersonalityClassifier.isValidResponse(5));
    }

    @Test
    @DisplayName("Test isValidResponse for invalid responses")
    void testIsValidResponseFalse() {
        assertFalse(PersonalityClassifier.isValidResponse(0));
        assertFalse(PersonalityClassifier.isValidResponse(6));
        assertFalse(PersonalityClassifier.isValidResponse(-1));
    }

    // ==================== CALCULATE TOTAL SCORE TESTS ====================

    @Test
    @DisplayName("Test calculateTotalScore with valid responses")
    void testCalculateTotalScoreValid() {
        int[] responses = {5, 5, 4, 5, 4}; // Total 23, scaled: 92
        int total = PersonalityClassifier.calculateTotalScore(responses);
        assertEquals(92, total);
    }

    @Test
    @DisplayName("Test calculateTotalScore with minimum values")
    void testCalculateTotalScoreMinimum() {
        int[] responses = {1, 1, 1, 1, 1}; // Total 5, scaled: 20
        int total = PersonalityClassifier.calculateTotalScore(responses);
        assertEquals(20, total);
    }

    @Test
    @DisplayName("Test calculateTotalScore with maximum values")
    void testCalculateTotalScoreMaximum() {
        int[] responses = {5, 5, 5, 5, 5}; // Total 25, scaled: 100
        int total = PersonalityClassifier.calculateTotalScore(responses);
        assertEquals(100, total);
    }

    @Test
    @DisplayName("Test calculateTotalScore with null throws exception")
    void testCalculateTotalScoreNull() {
        assertThrows(IllegalArgumentException.class,
                () -> PersonalityClassifier.calculateTotalScore(null));
    }

    @Test
    @DisplayName("Test calculateTotalScore with wrong count throws exception")
    void testCalculateTotalScoreWrongCount() {
        int[] responses = {3, 3, 3, 3}; // Only 4
        assertThrows(IllegalArgumentException.class,
                () -> PersonalityClassifier.calculateTotalScore(responses));
    }

    @Test
    @DisplayName("Test calculateTotalScore with invalid value throws exception")
    void testCalculateTotalScoreInvalidValue() {
        int[] responses = {3, 3, 6, 3, 3}; // 6 is invalid (max is 5)
        assertThrows(IllegalArgumentException.class,
                () -> PersonalityClassifier.calculateTotalScore(responses));
    }

    @Test
    @DisplayName("Test calculateTotalScore with zero throws exception")
    void testCalculateTotalScoreWithZero() {
        int[] responses = {3, 0, 3, 3, 3}; // 0 is invalid (min is 1)
        assertThrows(IllegalArgumentException.class,
                () -> PersonalityClassifier.calculateTotalScore(responses));
    }

    // ==================== UTILITY METHOD TESTS ====================

    @Test
    @DisplayName("Test getPersonalityDescription")
    void testGetPersonalityDescription() {
        String desc = PersonalityClassifier.getPersonalityDescription(95);
        assertNotNull(desc);
        assertTrue(desc.contains("Leader"));
    }

    @Test
    @DisplayName("Test getPersonalityDescription for invalid score")
    void testGetPersonalityDescriptionInvalid() {
        String desc = PersonalityClassifier.getPersonalityDescription(-10);
        assertTrue(desc.contains("Invalid") || desc.contains("invalid"));
    }

    @Test
    @DisplayName("Test getPointsToNextTier for Thinker")
    void testGetPointsToNextTierThinker() {
        int points = PersonalityClassifier.getPointsToNextTier(60);
        assertEquals(10, points); // 70 - 60 = 10 to reach Balanced
    }

    @Test
    @DisplayName("Test getPointsToNextTier for Balanced")
    void testGetPointsToNextTierBalanced() {
        int points = PersonalityClassifier.getPointsToNextTier(80);
        assertEquals(10, points); // 90 - 80 = 10 to reach Leader
    }

    @Test
    @DisplayName("Test getPointsToNextTier for Leader")
    void testGetPointsToNextTierLeader() {
        int points = PersonalityClassifier.getPointsToNextTier(95);
        assertEquals(0, points); // Already at highest
    }

    @Test
    @DisplayName("Test getNextTier")
    void testGetNextTier() {
        assertEquals(PersonalityType.BALANCED, PersonalityClassifier.getNextTier(60));
        assertEquals(PersonalityType.LEADER, PersonalityClassifier.getNextTier(80));
        assertNull(PersonalityClassifier.getNextTier(95));
    }

    @Test
    @DisplayName("Test getTeamRoleRecommendation")
    void testGetTeamRoleRecommendation() {
        String rec = PersonalityClassifier.getTeamRoleRecommendation(PersonalityType.LEADER);
        assertNotNull(rec);
        assertTrue(rec.contains("Captain") || rec.contains("leader") || rec.contains("coordinate"));
    }

    @Test
    @DisplayName("Test classification consistency")
    void testClassificationConsistency() {
        PersonalityType type1 = PersonalityClassifier.classifyPersonality(75);
        PersonalityType type2 = PersonalityClassifier.classifyPersonality(75);
        PersonalityType type3 = PersonalityClassifier.classifyPersonality(75);

        assertEquals(type1, type2);
        assertEquals(type2, type3);
    }

    @Test
    @DisplayName("Test getCompatibilityAnalysis")
    void testGetCompatibilityAnalysis() {
        String analysis = PersonalityClassifier.getCompatibilityAnalysis(
                PersonalityType.LEADER, PersonalityType.THINKER);
        assertNotNull(analysis);
        assertTrue(analysis.contains("Leader") || analysis.contains("Thinker"));
    }

    @Test
    @DisplayName("Test getDistributionSummary")
    void testGetDistributionSummary() {
        int[] scores = {95, 85, 75, 65, 55, 90, 80, 70, 60, 50};
        String summary = PersonalityClassifier.getDistributionSummary(scores);
        assertNotNull(summary);
        assertTrue(summary.contains("Leader") || summary.contains("Distribution"));
    }
}