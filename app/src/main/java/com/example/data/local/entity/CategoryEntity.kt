package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String, // e.g. "bible", "afrique", "culture_generale", "rdc", "histoire", "science", "sport", "musique", "cinema", "litterature", "technologie", "logique"
    val name: String,
    val slug: String,
    val description: String,
    val iconName: String,
    val colorHex: String,
    val questionCount: Int = 0,
    val isFeatured: Boolean = false,
    val displayOrder: Int = 0
)
