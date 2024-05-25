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
    public static LocalDate TOMORROW = LocalDate.now().plusDays(1);
    public static LocalDate THE_DAY_AFTER_TOMORROW = LocalDate.now().plusDays(2);
    public static LocalTime TIME_10AM = LocalTime.parse("10:00");
    public static ReservationTime RESERVATION_TIME_10AM = new ReservationTime(TIME_10AM);
    public static ReservationTime RESERVATION_TIME_11AM = new ReservationTime(LocalTime.parse("11:00"));

    // MEMBER 로그인 정보
    public static String MEMBER1_NAME = "브라운";
    public static String MEMBER1_EMAIL = "brown@gmail.com";
    public static String MEMBER1_PASSWORD = "brown";
    public static Member MEMBER1 = new Member(MEMBER1_NAME, MEMBER1_EMAIL, MEMBER1_PASSWORD, Role.MEMBER);
    public static LoginRequest MEMBER1_LOGIN_REQUEST = new LoginRequest(MEMBER1_EMAIL, MEMBER1_PASSWORD);

    public static String MEMBER2_NAME = "상돌";
    public static String MEMBER2_EMAIL = "sangdol@gmail.com";
    public static String MEMBER2_PASSWORD = "sangdol";
    public static Member MEMBER2 = new Member(MEMBER2_NAME, MEMBER2_EMAIL, MEMBER2_PASSWORD, Role.MEMBER);
    public static LoginRequest MEMBER2_LOGIN_REQUEST = new LoginRequest(MEMBER2_EMAIL, MEMBER2_PASSWORD);


    // ADMIN 로그인 정보
    public static String ADMIN_NAME = "제제";
    public static String ADMIN_EMAIL = "zeze@gmail.com";
    public static String ADMIN_PASSWORD = "zeze";
    public static Member ADMIN = new Member(ADMIN_NAME, ADMIN_EMAIL, ADMIN_PASSWORD, Role.ADMIN);
    public static LoginRequest ADMIN_LOGIN_REQUEST = new LoginRequest(ADMIN_EMAIL, ADMIN_PASSWORD);

    // 테마 정보
    public static Theme THEME1 = new Theme("레벨 1 탈출",
            "우테코 레벨1를 탈출하는 내용입니다.",
            "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
    public static Theme THEME2 = new Theme("레벨 2 탈출",
            "우테코 레벨2를 탈출하는 내용입니다.",
            "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");

    // 토큰 정보 추출
    public static String getTokenAfterLogin(LoginRequest request) {
        return RestAssured.given().log().all()
                .body(request)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all().extract().header("Set-Cookie").split(";")[0];

    }
}
