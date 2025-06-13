import com.codeborne.selenide.Configuration
import com.codeborne.selenide.Selenide
import org.testng.annotations.BeforeMethod
import org.testng.annotations.BeforeTest
import org.testng.annotations.Listeners
import org.testng.annotations.Test
import ticket.TicketStatus

@Listeners(ScreenshotListener::class)
class TestHome {

    private lateinit var homePage: HomePage

    private object Consts {
        const val ticketTitle = "Test ticket"
        const val ticketDescription = "Test ticket description"
    }

    @BeforeTest
    fun setConfiguration() {
        Configuration.headless = true
    }

    @BeforeMethod
    fun initDriver() {
        Selenide.open("http://localhost:5173")
        homePage = HomePage().shouldOpen()
    }

    @Test
    fun testCreateTicket() {
        createTicket(Consts.ticketTitle, Consts.ticketDescription, TicketStatus.OPEN)
    }

    @Test
    fun testDeleteTicket() {
        createTicket(Consts.ticketTitle, Consts.ticketDescription, TicketStatus.OPEN)
        val ticket = homePage.ticketList.getTicket("Test ticket")
        ticket.deleteTicket()
        homePage.ticketList.shouldHaveTickets(0)
    }

//    @Test
//    fun testEditTicket() {
//        val ticketTitle = "Test ticket"
//    }

    private fun createTicket(title: String, description: String, status: TicketStatus) {
        homePage.createNewTicket(title, description, status)

        val ticket = homePage.ticketList.getTicket(title)
        ticket.shouldHaveTitle(title)
            .shouldHaveDescription(description)
            .shouldHaveStatus(status)
        homePage.ticketList.shouldHaveTickets(1)
    }
}