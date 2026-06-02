package roomescape.slot.fixture;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.springframework.http.HttpHeaders;

import java.util.HashMap;
import java.util.Map;

public class SlotApiFixture {

    public static Integer createSlot(String token, Integer dateId, Integer timeId, Integer themeId) {
        Map<String, Object> params = new HashMap<>();
        params.put("dateId", dateId);
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        return RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/slots")
                .then().log().all()
                .statusCode(200)
                .extract()
                .path("id");
    }

}
