package ticket

enum class TicketStatus(val text: String) {
    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    CLOSED("Closed")
}