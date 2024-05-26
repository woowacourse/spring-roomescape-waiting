package roomescape;

import io.restassured.RestAssured;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.http.MediaType;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.service.dto.request.LoginRequest;

public class TestFixture {

    // 날짜 & 시간
    public static final LocalDate TOMORROW = LocalDate.now().plusDays(1);
    public static final LocalDate THE_DAY_AFTER_TOMORROW = LocalDate.now().plusDays(2);
    public static final LocalTime TIME_10AM = LocalTime.parse("10:00");

    public static ReservationTime getReservationTime10AM() {
        return new ReservationTime(TIME_10AM);
    }

    public static ReservationTime getReservationTime11AM() {
        return new ReservationTime(LocalTime.parse("11:00"));
    }

    // MEMBER 로그인 정보
    public static final String MEMBER1_NAME = "브라운";
    public static final String MEMBER1_EMAIL = "brown@gmail.com";
    public static final String MEMBER1_PASSWORD = "brown";
    public static final LoginRequest MEMBER1_LOGIN_REQUEST = new LoginRequest(MEMBER1_EMAIL, MEMBER1_PASSWORD);

    public static Member getMember1() {
        return new Member(MEMBER1_NAME, MEMBER1_EMAIL, MEMBER1_PASSWORD, Role.MEMBER);
    }


    public static final String MEMBER2_NAME = "상돌";
    public static final String MEMBER2_EMAIL = "sangdol@gmail.com";
    public static final String MEMBER2_PASSWORD = "sangdol";
    public static final LoginRequest MEMBER2_LOGIN_REQUEST = new LoginRequest(MEMBER2_EMAIL, MEMBER2_PASSWORD);

    public static Member getMember2() {
        return new Member(MEMBER2_NAME, MEMBER2_EMAIL, MEMBER2_PASSWORD, Role.MEMBER);
    }


    // ADMIN 로그인 정보
    public static final String ADMIN_NAME = "제제";
    public static final String ADMIN_EMAIL = "zeze@gmail.com";
    public static final String ADMIN_PASSWORD = "zeze";
    public static final LoginRequest ADMIN_LOGIN_REQUEST = new LoginRequest(ADMIN_EMAIL, ADMIN_PASSWORD);

    public static Member getAdminMember() {
        return new Member(ADMIN_NAME, ADMIN_EMAIL, ADMIN_PASSWORD, Role.ADMIN);
    }


    // 테마 정보
    public static Theme getTheme1() {
        return new Theme("레벨 1 탈출",
                "우테코 레벨1를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
    }

    public static Theme getTheme2() {
        return new Theme("레벨 2 탈출",
                "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
    }

    // 토큰 정보 추출
    public static String getTokenAfterLogin(LoginRequest request) {
        return RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when().post("/login")
                .then().log().all().extract().header("Set-Cookie").split(";")[0];
    }
}
