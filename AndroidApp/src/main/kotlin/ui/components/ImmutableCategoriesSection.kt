package cs.uwaterloo.timora.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cs.uwaterloo.domain.entity.ImmutableCategory

@Composable
fun ImmutableCategoriesSection(
    categories: List<ImmutableCategory>,
    isInteractive: Boolean = true,
    currentUserEmail: String = "",
    canAddItems: Boolean = false,
    onToggleItem: (categoryName: String, itemName: String, userEmail: String) -> Unit = { _, _, _ -> },
    onAddItem: (categoryName: String, itemName: String, userId: String) -> Unit = { _, _, _ -> }
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Checklists",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            categories.forEach { category ->
                ImmutableCategoryCard(
                    category = category,
                    isInteractive = isInteractive,
                    currentUserEmail = currentUserEmail,
                    canAddItems = canAddItems,
                    onToggleItem = { itemName, userEmail ->
                        onToggleItem(category.name, itemName, userEmail)
                    },
                    onAddItem = { itemName, userId ->
                        onAddItem(category.name, itemName, userId)
                    }
                )
            }
        }
    }
}

@Composable
private fun ImmutableCategoryCard(
    category: ImmutableCategory,
    isInteractive: Boolean,
    currentUserEmail: String = "",
    canAddItems: Boolean = false,
    onToggleItem: (itemName: String, userEmail: String) -> Unit = { _, _ -> },
    onAddItem: (itemName: String, userId: String) -> Unit = { _, _ -> }
) {
    var showDialog by remember { mutableStateOf(false) }
    var itemName by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Checklist,
                    contentDescription = "Checklist",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )

                val completedCount = category.items.values.count { it.isNotEmpty() }
                Badge {
                    Text(text = "$completedCount/${category.items.size}")
                }
            }

            HorizontalDivider(
                Modifier,
                DividerDefaults.Thickness,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
            )

            // Category items
            if (category.items.isEmpty()) {
                Text(
                    text = "No items yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    category.items.forEach { (itemName, checkedUsers) ->
                        ImmutableCategoryItem(
                            itemName = itemName,
                            checkedUsers = checkedUsers,
                            isChecked = checkedUsers.isNotEmpty(),
                            isInteractive = isInteractive,
                            currentUserEmail = currentUserEmail,
                            onToggleItem = onToggleItem
                        )
                    }
                }
            }
            if (isInteractive && canAddItems) {
                HorizontalDivider(
                    Modifier,
                    DividerDefaults.Thickness,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                )
                OutlinedButton(
                    onClick = { showDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("+ Add Item")
                }
            }
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                itemName = ""
            },
            title = { Text("Add Item to ${category.name}") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = { Text("Item Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (itemName.isNotBlank() && currentUserEmail.isNotBlank()) {
                            onAddItem(itemName, currentUserEmail)
                            showDialog = false
                            itemName = ""
                        }
                    },
                    enabled = itemName.isNotBlank() && currentUserEmail.isNotBlank()
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        itemName = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ImmutableCategoryItem(
    itemName: String,
    checkedUsers: Set<String>,
    isChecked: Boolean,
    isInteractive: Boolean,
    currentUserEmail: String = "",
    onToggleItem: (itemName: String, userEmail: String) -> Unit = { _, _ -> }
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isInteractive) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = {
                        if (currentUserEmail.isNotBlank()) {
                            onToggleItem(itemName, currentUserEmail)
                        }
                    }
                )
            } else {
                Icon(
                    imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (isChecked) "Checked" else "Unchecked",
                    tint = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = itemName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (checkedUsers.isNotEmpty()) {
                    Text(
                        text = "Checked by: ${checkedUsers.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (checkedUsers.isNotEmpty()) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Text(text = checkedUsers.size.toString())
                }
            }
        }
    }
}
