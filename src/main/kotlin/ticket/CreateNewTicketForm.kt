package ticket

import com.codeborne.selenide.Condition
import com.codeborne.selenide.Selenide

class CreateNewTicketForm {

    private val root = Selenide.`$`("div.ticket-form")
    private val titleInput = root.find("input[name='title']")
    private val descriptionInput = root.find("textarea[name='description']")
    private val statusSelect = root.find("select[name='status']")
    private val createTicketBtn = root.find("button[type='submit']")

    fun createTicket(title: String, description: String, status: TicketStatus) = apply {
        titleInput.sendKeys(title)
        descriptionInput.sendKeys(description)
        statusSelect.selectOptionContainingText(status.text)
        createTicketBtn.click()
    }

    fun shouldOpen() = apply {
        root.shouldBe(Condition.visible)
    }

    fun shouldClose() {
        root.shouldNotBe(Condition.visible)
    }
}