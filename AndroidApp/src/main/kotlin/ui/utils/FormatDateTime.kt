package cs.uwaterloo.timora.ui.utils

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

fun formatDateTime(dateTimeString: String): String {
    return try {
        if (dateTimeString.contains("+") || dateTimeString.contains("Z")) {
            val offsetDateTime = OffsetDateTime.parse(dateTimeString)
            val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")
            formatter.format(offsetDateTime)
        } else {
            val localDateTime = LocalDateTime.parse(dateTimeString)
            val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")
            formatter.format(localDateTime)
        }
    } catch (e: Exception) {
        dateTimeString
    }
}