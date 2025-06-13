package ticket

import com.codeborne.selenide.Condition.text
import com.codeborne.selenide.Condition.visible
import com.codeborne.selenide.SelenideElement
import org.openqa.selenium.By

class Ticket(private val root: SelenideElement) {

    private val header = root.find("div.ticket-header")
    private val status = header.find("span.status")
    private val title = header.find("h3")
    private val description = root.find("p.ticket-description")

    private val actions = root.find("div.ticket-actions")
    private val viewTicketBtn = actions.find(By.xpath(".//a[contains(@class, 'button') and contains(.,'View')]"))
    private val editTicketBtn = actions.find(By.xpath(".//a[contains(@class, 'button') and contains(.,'Edit')]"))
    private val deleteTicketBtn = actions.find(By.xpath(".//button[contains(@class, 'button') and contains(.,'Dellt')]"))

    fun getTitle(): String {
        return title.text
    }

    fun shouldHaveTitle(ticketTitle: String) = apply {
        title.shouldHave(text(ticketTitle))
    }

    fun shouldHaveDescription(ticketDescription: String) = apply {
        description.shouldHave(text(ticketDescription))
    }

    fun shouldHaveStatus(ticketStatus: TicketStatus) = apply {
        status.shouldHave(text(ticketStatus.text))
    }

    fun deleteTicket() {
        deleteTicketBtn.click()
    }

    fun shouldAppear() {
        root.shouldBe(visible)
    }

    fun shouldClose() {
        root.shouldNotBe(visible)
    }
}