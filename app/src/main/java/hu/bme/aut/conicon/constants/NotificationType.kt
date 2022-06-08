package hu.bme.aut.conicon.constants

enum class NotificationType (val value: Int) {
    MESSAGE(0),
    IMAGE_LIKE(1),
    FOLLOW(2);

    companion object {
        private val VALUES = values()
        fun getByValue(value: Int) = VALUES.firstOrNull { it.value == value }
    }
}