package com.e2_ma_tim09_2025.questify.models.enums;

public enum SpecialTaskType {
    SHOP_PURCHASE,              // Kupovina u prodavnici (max 5) - 2 HP
    BOSS_ATTACK,                // Uspešan udarac u regularnoj borbi sa bosom (max 10) - 2 HP
    TASK_COMPLETION_EASY_NORMAL, // Rešavanje veoma lakog, lakog, normalnog ili važnog zadatka (max 10) - 1 HP
    TASK_COMPLETION_OTHER,      // Rešavanje ostalih zadataka (max 6) - 4 HP
    NO_UNRESOLVED_TASKS,        // Bez nerešenih zadataka tokom trajanja specijalnog zadatka - 10 HP
    ALLIANCE_MESSAGE_DAILY      // Poslata poruka u savezu (računa se na nivou dana) - za svaki dan 4 HP
}
