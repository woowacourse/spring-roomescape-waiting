package roomescape.fixture.ui;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import org.springframework.http.HttpStatus;
import roomescape.member.ui.dto.MemberResponse;
import roomescape.member.ui.dto.SignUpRequest;

public class MemberApiFixture {

    public static final List<SignUpRequest> SIGN_UP_REQUESTS = List.of(
            new SignUpRequest("one@one.com", "password1", "name1"),
            new SignUpRequest("two@two.com", "password2", "name2"),
            new SignUpRequest("three@three.com", "password3", "name3"),
            new SignUpRequest("four@four.com", "password4", "name4")
    );

    private MemberApiFixture() {
    }

    public static SignUpRequest signUpRequest1() {
        if (SIGN_UP_REQUESTS.isEmpty()) {
            throw new IllegalStateException("회원 픽스처 개수가 부족합니다.");
        }
        return SIGN_UP_REQUESTS.get(0);
    }

    public static SignUpRequest signUpRequest2() {
        if (SIGN_UP_REQUESTS.size() < 2) {
            throw new IllegalStateException("회원 픽스처 개수가 부족합니다.");
        }
        return SIGN_UP_REQUESTS.get(1);
    }

    public static void signUp(final SignUpRequest signUpRequest) {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(signUpRequest)
                .when().post("/members")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());
    }

    public static List<MemberResponse> signUpMembers(final int count) {
        if (SIGN_UP_REQUESTS.size() < count) {
            throw new IllegalStateException("회원 픽스처의 개수는 최대 " + SIGN_UP_REQUESTS.size() + "개만 가능합니다.");
        }

        return SIGN_UP_REQUESTS.stream()
                .limit(count)
                .map(member -> RestAssured.given().log().all()
                        .contentType(ContentType.JSON)
                        .body(member)
                        .when().post("/members")
                        .then().log().all()
                        .statusCode(HttpStatus.CREATED.value())
                        .extract().as(MemberResponse.class)
                )
                .toList();
    }
}
