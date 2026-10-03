package cs.uwaterloo.timora.ui.utils

fun isValidDate(date: String): Boolean {
    val regex = Regex("^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$")
    if (!regex.matches(date)) return false

    val parts = date.split("-")
    if (parts.size != 3) return false

    val year = parts[0].toIntOrNull() ?: return false
    val month = parts[1].toIntOrNull() ?: return false
    val day = parts[2].toIntOrNull() ?: return false

    if (year < 2024 || year > 2100) return false
    if (month < 1 || month > 12) return false
    if (day < 1 || day > 31) return false

    if (month == 2 && day > 29) return false
    if ((month == 4 || month == 6 || month == 9 || month == 11) && day > 30) return false

    return true
}

fun isValidTime(time: String): Boolean {
    val regex = Regex("^([01]\\d|2[0-3]):([0-5]\\d)$")
    return regex.matches(time)
}