package roomescape.acceptance;


import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.dto.response.ThemeResponseDto;
import roomescape.model.Member;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.model.Role;
import roomescape.model.Theme;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ThemeAcceptanceTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Test
    @DisplayName("테마 조회 시 저장된 테마 내역을 모두 가져온다")
    void test1() {
        // given
        List<ThemeResponseDto> reservationTimes = RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200).extract()
                .jsonPath().getList(".", ThemeResponseDto.class);

        // then
        assertThat(reservationTimes.size()).isEqualTo(10);
    }

    @Test
    @DisplayName("정상적으로 테마가 등록되는 경우 201을 반환한다")
    void test2() {
        // given
        Map<String, String> params = new HashMap<>();
        params.put("name", "공포");
        params.put("description", "무서워요");
        params.put("thumbnail", "image-url");

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()
                .statusCode(201);
    }

    @Test
    @DisplayName("이미 존재하는 테마명을 이용해 등록하고자 한다면 409 를 반환한다.")
    void test3() {
        // given
        String name = "공포";
        saveTheme(name);

        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("description", "무서워요");
        params.put("thumbnail", "image-url");

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/themes")
                .then().log().all()
                .statusCode(409);
    }

    @Test
    @DisplayName("특정 테마를 삭제하는 경우 성공 시 204를 반환한다")
    void test7() {
        // given
        Theme theme = saveTheme("공포");

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/themes/" + theme.getId())
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("특정 테마에 대한 예약 내역이 존재하는 경우 삭제를 시도한다면 422 를 반환한다.")
    void test8() {
        // given
        Theme theme = saveTheme("공포");
        saveReservation(theme);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().delete("/themes/" + theme.getId())
                .then().log().all()
                .statusCode(422);
    }

    private Theme saveTheme(final String name) {
        Theme theme = new Theme(name, "무서워요", "image-url");
        return themeRepository.save(theme);
    }

    private Reservation saveReservation(final Theme theme) {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        Member member = new Member("도기", "email@example.com", "1234", Role.ADMIN);
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));

        memberRepository.save(member);
        reservationTimeRepository.save(reservationTime);

        Reservation reservation = new Reservation(
                tomorrow,
                reservationTime,
                theme,
                member,
                LocalDate.now());

        return reservationRepository.save(reservation);
    }
}
