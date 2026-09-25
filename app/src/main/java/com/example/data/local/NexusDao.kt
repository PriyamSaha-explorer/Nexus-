package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Attempt
import com.example.data.model.Concept
import com.example.data.model.ConceptRelationship
import com.example.data.model.DocumentNote
import com.example.data.model.Experiment
import com.example.data.model.Question
import kotlinx.coroutines.flow.Flow

@Dao
interface ConceptDao {
    @Query("SELECT * FROM concepts ORDER BY name ASC")
    fun getAllConcepts(): Flow<List<Concept>>

    @Query("SELECT * FROM concepts WHERE id = :id LIMIT 1")
    suspend fun getConceptById(id: String): Concept?

    @Query("SELECT * FROM concepts WHERE name = :name LIMIT 1")
    suspend fun getConceptByName(name: String): Concept?

    @Query("SELECT * FROM concepts WHERE masteryLevel = 'Weak' ORDER BY mistakesCount DESC")
    fun getWeakConcepts(): Flow<List<Concept>>

    @Query("SELECT * FROM concepts WHERE subject = :subject ORDER BY name ASC")
    fun getConceptsBySubject(subject: String): Flow<List<Concept>>

    @Query("SELECT * FROM concepts WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchConcepts(query: String): Flow<List<Concept>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConcept(concept: Concept)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConcepts(concepts: List<Concept>)

    @Update
    suspend fun updateConcept(concept: Concept)

    @Query("DELETE FROM concepts WHERE id = :id")
    suspend fun deleteConceptById(id: String)

    @Query("SELECT COUNT(*) FROM concepts")
    fun getTotalConceptsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM concepts WHERE masteryLevel = 'Mastered'")
    fun getMasteredCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM concepts WHERE masteryLevel = 'Weak'")
    fun getWeakCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM concepts WHERE masteryLevel = 'Learning'")
    fun getLearningCount(): Flow<Int>

    // Relationships
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRelationships(relationships: List<ConceptRelationship>)

    @Query("SELECT * FROM concept_relationships")
    fun getAllRelationships(): Flow<List<ConceptRelationship>>
}

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions")
    fun getAllQuestions(): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE conceptId = :conceptId")
    fun getQuestionsForConcept(conceptId: String): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE subject = :subject")
    fun getQuestionsBySubject(subject: String): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE id = :id LIMIT 1")
    suspend fun getQuestionById(id: String): Question?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<Question>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: Question)
}

@Dao
interface AttemptDao {
    @Query("SELECT * FROM attempts ORDER BY timestamp DESC")
    fun getAllAttempts(): Flow<List<Attempt>>

    @Query("SELECT * FROM attempts WHERE conceptId = :conceptId ORDER BY timestamp DESC")
    fun getAttemptsForConcept(conceptId: String): Flow<List<Attempt>>

    @Query("SELECT * FROM attempts WHERE isCorrect = 0 ORDER BY timestamp DESC")
    fun getMistakeAttempts(): Flow<List<Attempt>>

    @Insert
    suspend fun insertAttempt(attempt: Attempt)

    @Query("SELECT COUNT(*) FROM attempts")
    fun getTotalAttemptsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM attempts WHERE isCorrect = 1")
    fun getCorrectAttemptsCount(): Flow<Int>
}

@Dao
interface DocumentNoteDao {
    @Query("SELECT * FROM documents ORDER BY timestamp DESC")
    fun getAllDocuments(): Flow<List<DocumentNote>>

    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    suspend fun getDocumentById(id: String): DocumentNote?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: DocumentNote)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocumentById(id: String)
}

@Dao
interface ExperimentDao {
    @Query("SELECT * FROM experiments ORDER BY timestamp DESC")
    fun getAllExperiments(): Flow<List<Experiment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExperiment(experiment: Experiment)

    @Query("DELETE FROM experiments WHERE id = :id")
    suspend fun deleteExperimentById(id: String)
}
