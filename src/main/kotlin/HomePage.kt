import com.codeborne.selenide.Condition.visible
import com.codeborne.selenide.Selenide.`$`
import ticket.CreateNewTicketForm
import ticket.TicketList
import ticket.TicketStatus

class HomePage {

    private val root = `$`("div.ticket-list")
    private val createNewTicketBtn = root.find("a[class='button']")

    val ticketList by lazy { TicketList() }

    fun createNewTicket(title: String, description: String, status: TicketStatus) {
        createNewTicketBtn.click()
        CreateNewTicketForm()
            .shouldOpen()
            .createTicket(title, description, status)
            .shouldClose()

        ticketList.getTicket(title).shouldAppear()
    }

    fun shouldOpen() = apply {
        root.shouldBe(visible)
    }
}