package roomescape.controller.admin;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.controller.ReservationController;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminThemeControllerTest {
    @Autowired
    private ReservationController reservationController;

    @Test
        void 테마_관리_API() {
            String name = "추리물";
            String description = "추리";
            byte[] fileContent = "fake-image-content".getBytes();

            RestAssured.given().log().all()
                    .contentType(ContentType.MULTIPART)
                    .multiPart("name", name)
                    .multiPart("description", description)
                    .multiPart("file", "test.png", fileContent, "image/png")
                    .when().post("/admin/themes")
                    .then().log().all()
                    .statusCode(201);

            RestAssured.given().log().all()
                    .when().delete("/admin/themes/16")
                    .then().log().all()
                .statusCode(204);
    }
}
