package com.cbs.trivia;

import com.cbs.trivia.progression.ProgressionManager;
import com.cbs.trivia.progression.ProgressionSnapshot;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests verifying XP formulas, tier multipliers, grade boundaries,
 * level thresholds, RPG class titles, and level-up transitions.
 */
public class ProgressionManagerTest {

    @Test
    public void calculateXpEarned_appliesBaseStreakPerfectAndTierMultipliers() {
        // Level 1 (1.0x): 8 correct (200) + streak 5 (50) + no perfect (0) = 250
        assertEquals(250, ProgressionManager.calculateXpEarned(8, 10, 5, 1));

        // Level 2 (1.5x): 5 correct (125) + streak 3 (30) = 155 * 1.5 = 232.5 -> floor 232
        assertEquals(232, ProgressionManager.calculateXpEarned(5, 10, 3, 2));

        // Level 3 (2.0x) Perfect run: 10 correct (250) + streak 10 (100) + perfect (50) = 400 * 2.0 = 800
        assertEquals(800, ProgressionManager.calculateXpEarned(10, 10, 10, 3));
    }

    @Test
    public void calculateXpEarned_handlesZeroQuestionsWithoutPerfectBonus() {
        assertEquals(0, ProgressionManager.calculateXpEarned(0, 0, 0, 1));
    }

    @Test
    public void calculateRunGrade_matchesAllGradeBoundaries() {
        assertEquals("S", ProgressionManager.calculateRunGrade(9, 10));
        assertEquals("S", ProgressionManager.calculateRunGrade(10, 10));
        assertEquals("A", ProgressionManager.calculateRunGrade(8, 10));
        assertEquals("A", ProgressionManager.calculateRunGrade(15, 20)); // 75%
        assertEquals("B", ProgressionManager.calculateRunGrade(6, 10));  // 60%
        assertEquals("C", ProgressionManager.calculateRunGrade(4, 10));  // 40%
        assertEquals("D", ProgressionManager.calculateRunGrade(3, 10));  // 30%
        assertEquals("D", ProgressionManager.calculateRunGrade(0, 0));
    }

    @Test
    public void levelAndRankMapping_coversAllEightTiers() {
        assertEquals(1, ProgressionManager.getLevelForXp(0));
        assertEquals("Novice", ProgressionManager.getRankTitleForLevel(1));

        assertEquals(2, ProgressionManager.getLevelForXp(100));
        assertEquals("Scout", ProgressionManager.getRankTitleForLevel(2));

        assertEquals(3, ProgressionManager.getLevelForXp(250));
        assertEquals("Apprentice", ProgressionManager.getRankTitleForLevel(3));

        assertEquals(4, ProgressionManager.getLevelForXp(450));
        assertEquals("Tactician", ProgressionManager.getRankTitleForLevel(4));

        assertEquals(5, ProgressionManager.getLevelForXp(700));
        assertEquals("Sentinel", ProgressionManager.getRankTitleForLevel(5));

        assertEquals(6, ProgressionManager.getLevelForXp(1000));
        assertEquals("Vanguard", ProgressionManager.getRankTitleForLevel(6));

        assertEquals(7, ProgressionManager.getLevelForXp(1400));
        assertEquals("Architect", ProgressionManager.getRankTitleForLevel(7));

        assertEquals(8, ProgressionManager.getLevelForXp(1900));
        assertEquals(8, ProgressionManager.getLevelForXp(3500));
        assertEquals("Grandmaster", ProgressionManager.getRankTitleForLevel(8));
    }

    @Test
    public void evaluateProgression_detectsSameLevelSingleLevelUpMultiJumpAndMaxCap() {
        // Same level gain: 20 XP -> +50 XP = 70 XP (stays Lv.1 Novice)
        ProgressionSnapshot sameLevel = ProgressionManager.evaluateProgression(20, 1, 2, 10, 0, 1);
        assertEquals(1, sameLevel.getPreviousLevel());
        assertEquals(1, sameLevel.getNewLevel());
        assertFalse(sameLevel.isLeveledUp());
        assertEquals(70, sameLevel.getNewTotalXp());
        assertEquals(100, sameLevel.getNextLevelXp());
        assertEquals("Novice", sameLevel.getRankTitle());

        // Single level-up: 80 XP -> +50 XP = 130 XP (Lv.1 -> Lv.2 Scout)
        ProgressionSnapshot levelUp = ProgressionManager.evaluateProgression(80, 2, 2, 10, 0, 1);
        assertEquals(1, levelUp.getPreviousLevel());
        assertEquals(2, levelUp.getNewLevel());
        assertTrue(levelUp.isLeveledUp());
        assertEquals(100, levelUp.getCurrentLevelFloorXp());
        assertEquals(250, levelUp.getNextLevelXp());
        assertEquals("Scout", levelUp.getRankTitle());

        // Multi-level jump: 0 XP -> perfect Lv.3 run (+800 XP) -> Lv.5 Sentinel
        ProgressionSnapshot multiJump = ProgressionManager.evaluateProgression(0, 0, 10, 10, 10, 3);
        assertEquals(1, multiJump.getPreviousLevel());
        assertEquals(5, multiJump.getNewLevel());
        assertTrue(multiJump.isLeveledUp());
        assertEquals("Sentinel", multiJump.getRankTitle());
        assertEquals("S", multiJump.getRunGrade());
        assertEquals(1000, multiJump.getNextLevelXp());

        // Max level cap: 1850 XP -> +200 XP = 2050 XP (Lv.7 -> Lv.8 Grandmaster, capped)
        ProgressionSnapshot maxCap = ProgressionManager.evaluateProgression(1850, 9, 6, 10, 5, 1);
        assertEquals(7, maxCap.getPreviousLevel());
        assertEquals(8, maxCap.getNewLevel());
        assertTrue(maxCap.isLeveledUp());
        assertTrue(maxCap.isMaxLevel());
        assertEquals("Grandmaster", maxCap.getRankTitle());
        assertEquals(1900, maxCap.getNextLevelXp());
    }
}
