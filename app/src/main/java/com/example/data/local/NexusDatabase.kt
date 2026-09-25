package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Attempt
import com.example.data.model.Concept
import com.example.data.model.ConceptRelationship
import com.example.data.model.DocumentNote
import com.example.data.model.Experiment
import com.example.data.model.Question

@Database(
    entities = [
        Concept::class,
        ConceptRelationship::class,
        Question::class,
        Attempt::class,
        DocumentNote::class,
        Experiment::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NexusDatabase : RoomDatabase() {
    abstract fun conceptDao(): ConceptDao
    abstract fun questionDao(): QuestionDao
    abstract fun attemptDao(): AttemptDao
    abstract fun documentNoteDao(): DocumentNoteDao
    abstract fun experimentDao(): ExperimentDao

    companion object {
        @Volatile
        private var INSTANCE: NexusDatabase? = null

        fun getDatabase(context: Context): NexusDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NexusDatabase::class.java,
                    "nexus_knowledge.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
