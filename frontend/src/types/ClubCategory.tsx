export enum ClubCategory {
    STUDY = "STUDY",
    HOBBY = "HOBBY",
    SPORTS = "SPORTS",
    TRAVEL = "TRAVEL",
    CULTURE = "CULTURE",
    FOOD = "FOOD",
    PARTY = "PARTY",
    WORK = "WORK",
    OTHER = "OTHER"
}

export const ClubCategoryKorean: Record<ClubCategory, string> = {
    [ClubCategory.STUDY]: "공부",
    [ClubCategory.HOBBY]: "취미",
    [ClubCategory.SPORTS]: "운동",
    [ClubCategory.TRAVEL]: "여행",
    [ClubCategory.CULTURE]: "문화",
    [ClubCategory.FOOD]: "음식",
    [ClubCategory.PARTY]: "파티",
    [ClubCategory.WORK]: "업무",
    [ClubCategory.OTHER]: "기타"
};