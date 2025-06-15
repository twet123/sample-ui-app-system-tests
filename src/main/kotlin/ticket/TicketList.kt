package ticket

import com.codeborne.selenide.CollectionCondition.size
import com.codeborne.selenide.Selenide.`$`

class TicketList {

    private val root = `$`("ul.tickets, p")

    private val items = root.findAll("li")
    private val tickets = items.map { Ticket(it) }

    fun getTicket(title: String): Ticket {
        return tickets.first { it.getTitle().contains(title) }
    }

    fun shouldHaveTickets(number: Int) = apply {
        items.shouldHave(size(number))
    }
}