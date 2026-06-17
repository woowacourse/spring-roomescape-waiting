package roomescape.payment.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.reservation.controller.dto.ReservationRequest;
import roomescape.testSupport.DatabaseHelper;
import roomescape.testSupport.SpringWebTest;
import roomescape.theme.controller.dto.ThemeRequest;
import roomescape.time.controller.dto.ReservationTimeRequest;

@SpringWebTest
class PaymentCheckoutPlaywrightTest {

    private static Playwright playwright;
    private static Browser browser;

    @Autowired
    private DatabaseHelper databaseHelper;

    private BrowserContext context;
    private Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    }

    @AfterAll
    static void closeBrowser() {
        browser.close();
        playwright.close();
    }

    @BeforeEach
    void setUp() {
        databaseHelper.clear();
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    @DisplayName("예약을 생성하면 결제 체크아웃 페이지로 이동하고 주문 정보가 표시된다.")
    void createReservation_redirectsToCheckoutWithOrderInfo() {
        Long themeId = createTheme();
        createTime();
        LocalDate date = LocalDate.now().plusDays(1);

        page.navigate(baseUrl() + "/index.html");
        page.locator("#createName").fill("브라운");
        page.locator("#createDate").fill(date.toString());
        page.locator("#createThemeId").selectOption(themeId.toString());
        page.locator("#loadTimes").click();
        page.locator("#availableTimes button[data-time-id]").first().click();

        page.waitForURL("**/payments/checkout**");

        String content = page.content();
        assertThat(content).contains("방탈출 예약 결제");
        assertThat(content).contains("브라운");
        assertThat(content).contains("50,000");
    }

    @Test
    @DisplayName("위변조된 금액으로 결제 승인을 시도하면 결제 실패 페이지가 노출되고 예약/결제 정보가 정리된다.")
    void tamperedAmount_showsFailPageAndCleansUp() {
        Long themeId = createTheme();
        Long timeId = createTime();
        LocalDate date = LocalDate.now().plusDays(1);

        String orderId = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(new ReservationRequest("브라운", date, timeId, themeId))
                .when().post("/reservations")
                .then().statusCode(201)
                .extract().jsonPath().getString("orderId");

        page.navigate(baseUrl() + "/payments/success?paymentKey=fake&orderId=" + orderId + "&amount=99999");

        String content = page.content();
        assertThat(content).contains("결제 실패");
        assertThat(content).contains("AMOUNT_MISMATCH");

        RestAssured.given()
                .queryParam("name", "브라운")
                .when().get("/reservations")
                .then().statusCode(200)
                .body("size()", is(0));
    }

    @Test
    @DisplayName("사용자가 결제창에서 직접 취소하면 orderId 없이도 NPE 없이 실패 페이지가 노출된다.")
    void userCancelled_noOrderId_doesNotThrow() {
        page.navigate(baseUrl() + "/payments/fail?code=PAY_PROCESS_CANCELED&message=사용자 취소");

        String content = page.content();
        assertThat(content).contains("결제 실패");
        assertThat(content).contains("PAY_PROCESS_CANCELED");
    }

    private String baseUrl() {
        return "http://localhost:" + RestAssured.port;
    }

    private Long createTheme() {
        ThemeRequest request = new ThemeRequest("플레이라이트 테마", "설명", "https://example.com/thumb.png");
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/admin/themes")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    private Long createTime() {
        ReservationTimeRequest request = new ReservationTimeRequest(LocalTime.of(10, 0));
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/admin/times")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }
}
