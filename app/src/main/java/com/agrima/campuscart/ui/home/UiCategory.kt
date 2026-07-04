package com.agrima.campuscart.ui.home

import com.agrima.campuscart.data.model.Category

enum class UiCategory(val displayName: String, val domainCategory: Category?) {
    ALL("All", null),
    BOOKS("Books", Category.BOOKS),
    ELECTRONICS("Electronics", Category.ELECTRONICS),
    FASHION("Fashion", Category.CLOTHING_FASHION),
    HOSTEL_ESSENTIALS("Hostel Essentials", Category.HOME_KITCHEN),
    SPORTS("Sports", Category.SPORTS_FITNESS),
    NOTES("Notes", Category.STATIONERY),
    OTHERS("Others", Category.OTHERS)
}
