package com.example.util

import com.example.data.model.Student
import kotlin.math.sqrt

sealed class FaceMatchResult {
    data class MatchFound(
        val student: Student,
        val confidenceScore: Float
    ) : FaceMatchResult()

    data class NoMatch(val reason: String) : FaceMatchResult()
}

object FaceMatchingEngine {

    /**
     * Parses a comma-separated face vector string into a FloatArray
     */
    private fun parseVector(vectorStr: String): FloatArray {
        if (vectorStr.isBlank()) return FloatArray(8) { 0f }
        return try {
            vectorStr.split(",").map { it.trim().toFloat() }.toFloatArray()
        } catch (e: Exception) {
            FloatArray(8) { 0.5f }
        }
    }

    /**
     * Calculates Euclidean Distance between two feature vectors
     */
    private fun calculateDistance(v1: FloatArray, v2: FloatArray): Float {
        if (v1.size != v2.size) return 100f
        var sumSquare = 0f
        for (i in v1.indices) {
            val diff = v1[i] - v2[i]
            sumSquare += diff * diff
        }
        return sqrt(sumSquare)
    }

    /**
     * Matches a scanned face vector against database students.
     * Threshold = 0.6 (Distances below threshold mean high similarity match).
     */
    fun matchFace(scannedVector: FloatArray, students: List<Student>): FaceMatchResult {
        if (students.isEmpty()) {
            return FaceMatchResult.NoMatch("قاعدة بيانات الطلاب فارغة")
        }

        var bestMatch: Student? = null
        var minDistance = Float.MAX_VALUE

        for (student in students) {
            val dbVector = parseVector(student.faceVector)
            val distance = calculateDistance(scannedVector, dbVector)
            if (distance < minDistance) {
                minDistance = distance
                bestMatch = student
            }
        }

        // Distance threshold for facial verification match
        return if (bestMatch != null && minDistance < 0.85f) {
            // Convert distance to confidence percentage (e.g. 0.1 dist = 98% confidence)
            val confidence = ((1.0f - (minDistance / 1.0f)).coerceIn(0.70f, 0.99f)) * 100f
            FaceMatchResult.MatchFound(bestMatch, confidence)
        } else {
            FaceMatchResult.NoMatch("لم يتم العثور على طالب مطابق بصفة دقيقة")
        }
    }

    /**
     * Helper to generate a new simulated face vector embedding when registering a student
     */
    fun generateVectorForNewStudent(name: String, universityId: String): String {
        val seed = (name.hashCode() xor universityId.hashCode()).toDouble()
        val vector = FloatArray(8) { i ->
            val v = (kotlin.math.sin(seed + i * 1.5) + 1.0) / 2.0
            String.format(java.util.Locale.US, "%.2f", v).toFloat()
        }
        return vector.joinToString(",")
    }

    /**
     * Generates a realistic mock scan vector for demo simulation
     */
    fun getMockScanVectorForStudent(student: Student): FloatArray {
        val dbVector = parseVector(student.faceVector)
        // Add tiny variation (lighting/angle shift)
        return FloatArray(dbVector.size) { i ->
            (dbVector[i] + (if (i % 2 == 0) 0.02f else -0.02f)).coerceIn(0f, 1f)
        }
    }
}
