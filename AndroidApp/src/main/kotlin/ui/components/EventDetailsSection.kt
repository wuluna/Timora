package cs.uwaterloo.timora.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cs.uwaterloo.domain.entity.Event
import cs.uwaterloo.timora.ui.utils.formatDateTime

@Composable
fun EventDetailsSection(
    event: Event,
    isEditMode: Boolean,
    onDateTimeChange: (String) -> Unit = {},
    onLocationChange: (String) -> Unit = {},
    onDescriptionChange: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Event Details",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary
        )

        // Date & Time
        EventDetailItem(
            icon = Icons.Default.CalendarToday,
            label = "Date & Time",
            value = event.dateTime,
            displayValue = formatDateTime(event.dateTime),
            isEditMode = isEditMode,
            onValueChange = onDateTimeChange
        )

        // Location
        EventDetailItem(
            icon = Icons.Default.LocationOn,
            label = "Location",
            value = event.location,
            isEditMode = isEditMode,
            onValueChange = onLocationChange
        )

        // Description
        if (event.description.isNotBlank()) {
            EventDetailItem(
                icon = Icons.Default.Description,
                label = "Description",
                value = event.description,
                isEditMode = isEditMode,
                isMultiline = true,
                onValueChange = onDescriptionChange
            )
        }
    }
}

@Composable
private fun EventDetailItem(
    icon: ImageVector,
    label: String,
    value: String,
    displayValue: String = value,
    isEditMode: Boolean,
    isMultiline: Boolean = false,
    onValueChange: (String) -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )

                if (isEditMode) {
                    OutlinedTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        minLines = if (isMultiline) 3 else 1,
                        maxLines = if (isMultiline) 5 else 1
                    )
                } else {
                    Text(
                        text = displayValue,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
