package se.umu.calu0217.strive.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database migration from version 3 to 4.
 * Adds weightKg column to workout_sets table and usesWeight column to exercises table.
 * @author Carl Lundholm
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE workout_sets ADD COLUMN weightKg REAL DEFAULT NULL")

        db.execSQL("ALTER TABLE exercises ADD COLUMN usesWeight INTEGER NOT NULL DEFAULT 1")

        db.execSQL("""
            UPDATE exercises 
            SET usesWeight = 0 
            WHERE LOWER(equipment) IN ('body weight', 'bodyweight', 'body only', 'none')
        """)
    }
}

