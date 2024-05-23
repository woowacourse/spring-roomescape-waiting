package roomescape;

import io.restassured.RestAssured;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.http.MediaType;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.repository.MemberRepository;
import roomescape.service.dto.request.LoginRequest;

public class TestFixture {

    public static String VALID_STRING_DATE = LocalDate.now().plusDays(1).toString();
    public static LocalDate DATE_AFTER_1DAY = LocalDate.parse(VALID_STRING_DATE);
    public static LocalDate DATE_AFTER_2DAY = LocalDate.now().plusDays(2);
    public static String VALID_STRING_TIME = "10:00";
    public static LocalTime TIME_10AM = LocalTime.parse(VALID_STRING_TIME);
    public static ReservationTime RESERVATION_TIME_10AM = new ReservationTime(TIME_10AM);
    public static ReservationTime RESERVATION_TIME_11AM = new ReservationTime(LocalTime.parse("11:00"));

    // MEMBER 로그인 정보
    public static String MEMBER_NAME = "브라운";
    public static String MEMBER_EMAIL = "brown@gmail.com";
    public static String MEMBER_CONSTANT = "brown";
    public static Member MEMBER_BROWN = new Member(MEMBER_NAME, MEMBER_EMAIL, MEMBER_CONSTANT, Role.MEMBER);
    public static LoginRequest MEMBER_LOGIN_REQUEST = new LoginRequest("brown@gmail.com", "brown");

    // ADMIN 로그인 정보
    public static String ADMIN_NAME = "제제";
    public static String ADMIN_EMAIL = "zeze@gmail.com";
    public static String ADMIN_PASSWORD = "zeze";
    public static Member ADMIN_ZEZE = new Member(ADMIN_NAME, ADMIN_EMAIL, ADMIN_PASSWORD, Role.ADMIN);
    public static LoginRequest ADMIN_LOGIN_REQUEST = new LoginRequest(ADMIN_EMAIL, ADMIN_PASSWORD);

    public static Theme ROOM_THEME1 = new Theme("레벨 1 탈출",
            "우테코 레벨1를 탈출하는 내용입니다.",
            "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
    public static Theme ROOM_THEME2 = new Theme("레벨 2 탈출",
            "우테코 레벨2를 탈출하는 내용입니다.",
            "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");

    public static String getMemberToken(MemberRepository memberRepository) {
        memberRepository.save(MEMBER_BROWN);
        return RestAssured
                .given().log().all()
                .body(MEMBER_LOGIN_REQUEST)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all().extract().header("Set-Cookie").split(";")[0];
    }

    public static String getAdminToken(MemberRepository memberRepository) {
        memberRepository.save(ADMIN_ZEZE);
        return RestAssured
                .given().log().all()
                .body(ADMIN_LOGIN_REQUEST)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/login")
                .then().log().all().extract().header("Set-Cookie").split(";")[0];
    }
}
