package roomescape.acceptance;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.BDDMockito.given;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.request.ThemeRequest;

class ThemeTest extends AcceptanceTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @MockBean
    private Clock clock;

    @BeforeEach
    void setClock() {
        given(clock.instant()).willReturn(Instant.parse("2024-05-02T19:19:00Z"));
        given(clock.getZone()).willReturn(ZoneId.of("Asia/Seoul"));
    }

    @DisplayName("ADMIN 테마 CRUD 테스트")
    @TestFactory
    Stream<DynamicTest> reservationByAdmin() {
        return Stream.of(
                dynamicTest("테마를 추가한다.", () -> {
                    ThemeRequest themeRequest = new ThemeRequest("happy", "hi", "abcd.html");

                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .body(themeRequest)
                            .when().post("/themes")
                            .then().log().all()
                            .statusCode(201)
                            .body("id", is(1));
                }),

                dynamicTest("테마를 조회한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .when().get("/themes")
                            .then().log().all()
                            .statusCode(200)
                            .body("size()", is(1));
                }),

                dynamicTest("테마를 삭제한다.", () -> {
                    RestAssured.given().log().all()
                            .contentType(ContentType.JSON)
                            .when().delete("/themes/1")
                            .then().log().all()
                            .statusCode(204);
                })
        );
    }

    @DisplayName("주간 상위 10개 예약 테마 조회 API 테스트")
    @Test
    void weeklyTop10Theme() {
        setTopReservedThemes();

        RestAssured.given().log().all()
                .when().get("/themes/ranking")
                .then().log().all()
                .statusCode(200)
                .body("size()", is(10));
    }

    private void setTopReservedThemes() {
        Member member = new Member("asd", "asd@email.com", "asd", Role.ADMIN);
        memberRepository.save(member);

        for (int hour = 10; hour <= 21; hour++) {
            ReservationTime reservationTime = new ReservationTime(LocalTime.of(hour, 0));
            reservationTimeRepository.save(reservationTime);
        }

        themeRepository.save(new Theme("hi1", "happy1", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"));
        themeRepository.save(new Theme(1L, "hi1", "happy11", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"));
        themeRepository.save(new Theme(2L, "hi2", "happy2", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"));
        themeRepository.save(new Theme(2L, "hi2", "happy2", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"));
        themeRepository.save(new Theme(3L, "hi3", "happy3", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"));
        themeRepository.save(new Theme(3L, "hi3", "happy3", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"));
        themeRepository.save(new Theme(4L, "hi4", "happy4", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"));
        themeRepository.save(new Theme(5L, "hi5", "happy5", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"));
        themeRepository.save(new Theme(6L, "hi6", "happy6", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"));
        themeRepository.save(new Theme(7L, "hi7", "happy7", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"));
        themeRepository.save(new Theme(8L, "hi8", "happy8", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"));
        themeRepository.save(new Theme(9L, "hi9", "happy9", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"));
        themeRepository.save(new Theme(10L, "hi10", "happy10", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"));
        themeRepository.save(new Theme(11L, "hi11", "happy11", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"));
        themeRepository.save(new Theme(12L, "hi12", "happy12", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg"));

        LocalDate date = LocalDate.of(2024, 4, 25);
        reservationRepository.save(new Reservation(member, date, new ReservationTime(1L, LocalTime.of(10,0)),new Theme(1L, "hi1", "happy11", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(1L, LocalTime.of(10,0)), new Theme(2L, "hi2", "happy2", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(2L, LocalTime.of(11,0)), new Theme(2L, "hi2", "happy2", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(1L, LocalTime.of(10,0)), new Theme(3L, "hi3", "happy3", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(2L, LocalTime.of(11,0)), new Theme(3L, "hi3", "happy3", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(3L, LocalTime.of(12,0)), new Theme(3L, "hi3", "happy3", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(1L, LocalTime.of(10,0)), new Theme(4L, "hi4", "happy4", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(2L, LocalTime.of(11,0)), new Theme(4L, "hi4", "happy4", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(3L, LocalTime.of(12,0)), new Theme(4L, "hi4", "happy4", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(4L, LocalTime.of(13,0)), new Theme(4L, "hi4", "happy4", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(1L, LocalTime.of(10,0)), new Theme(5L, "hi5", "happy5", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(2L, LocalTime.of(11,0)), new Theme(5L, "hi5", "happy5", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(3L, LocalTime.of(12,0)), new Theme(5L, "hi5", "happy5", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(4L, LocalTime.of(13,0)), new Theme(5L, "hi5", "happy5", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(5L, LocalTime.of(14,0)), new Theme(5L, "hi5", "happy5", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(1L, LocalTime.of(10,0)), new Theme(6L, "hi6", "happy6", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(2L, LocalTime.of(11,0)), new Theme(6L, "hi6", "happy6", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(3L, LocalTime.of(12,0)), new Theme(6L, "hi6", "happy6", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(4L, LocalTime.of(13,0)), new Theme(6L, "hi6", "happy6", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(5L, LocalTime.of(14,0)), new Theme(6L, "hi6", "happy6", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(6L, LocalTime.of(15,0)), new Theme(6L, "hi6", "happy6", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(1L, LocalTime.of(10,0)), new Theme(7L, "hi7", "happy7", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(2L, LocalTime.of(11,0)), new Theme(7L, "hi7", "happy7", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(3L, LocalTime.of(12,0)), new Theme(7L, "hi7", "happy7", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(4L, LocalTime.of(13,0)), new Theme(7L, "hi7", "happy7", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(5L, LocalTime.of(14,0)), new Theme(7L, "hi7", "happy7", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(6L, LocalTime.of(15,0)), new Theme(7L, "hi7", "happy7", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(7L, LocalTime.of(16,0)), new Theme(7L, "hi7", "happy7", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(1L, LocalTime.of(10,0)), new Theme(8L, "hi8", "happy8", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(2L, LocalTime.of(11,0)), new Theme(8L, "hi8", "happy8", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(3L, LocalTime.of(12,0)), new Theme(8L, "hi8", "happy8", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(4L, LocalTime.of(13,0)), new Theme(8L, "hi8", "happy8", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(5L, LocalTime.of(14,0)), new Theme(8L, "hi8", "happy8", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(6L, LocalTime.of(15,0)), new Theme(8L, "hi8", "happy8", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(7L, LocalTime.of(16,0)), new Theme(8L, "hi8", "happy8", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(8L, LocalTime.of(17,0)), new Theme(8L, "hi8", "happy8", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(1L, LocalTime.of(10,0)), new Theme(9L, "hi9", "happy9", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(2L, LocalTime.of(11,0)), new Theme(9L, "hi9", "happy9", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(3L, LocalTime.of(12,0)), new Theme(9L, "hi9", "happy9", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(4L, LocalTime.of(13,0)), new Theme(9L, "hi9", "happy9", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(5L, LocalTime.of(14,0)), new Theme(9L, "hi9", "happy9", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(6L, LocalTime.of(15,0)), new Theme(9L, "hi9", "happy9", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(7L, LocalTime.of(16,0)), new Theme(9L, "hi9", "happy9", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(8L, LocalTime.of(17,0)), new Theme(9L, "hi9", "happy9", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(9L, LocalTime.of(18,0)), new Theme(9L, "hi9", "happy9", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(1L, LocalTime.of(10,0)),new Theme(10L, "hi10", "happy10", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(2L, LocalTime.of(11,0)),new Theme(10L, "hi10", "happy10", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(3L, LocalTime.of(12,0)),new Theme(10L, "hi10", "happy10", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(4L, LocalTime.of(13,0)),new Theme(10L, "hi10", "happy10", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(5L, LocalTime.of(14,0)),new Theme(10L, "hi10", "happy10", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(6L, LocalTime.of(15,0)),new Theme(10L, "hi10", "happy10", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(7L, LocalTime.of(16,0)),new Theme(10L, "hi10", "happy10", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(8L, LocalTime.of(17,0)),new Theme(10L, "hi10", "happy10", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(9L, LocalTime.of(18,0)),new Theme(10L, "hi10", "happy10", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(10L,LocalTime.of(19,0)),new Theme(10L, "hi10", "happy10", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(1L, LocalTime.of(10,0)),new Theme(11L, "hi11", "happy11", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(2L, LocalTime.of(11,0)),new Theme(11L, "hi11", "happy11", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(3L, LocalTime.of(12,0)),new Theme(11L, "hi11", "happy11", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(4L, LocalTime.of(13,0)),new Theme(11L, "hi11", "happy11", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(5L, LocalTime.of(14,0)),new Theme(11L, "hi11", "happy11", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(6L, LocalTime.of(15,0)),new Theme(11L, "hi11", "happy11", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(7L, LocalTime.of(16,0)),new Theme(11L, "hi11", "happy11", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(8L, LocalTime.of(17,0)),new Theme(11L, "hi11", "happy11", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(9L, LocalTime.of(18,0)),new Theme(11L, "hi11", "happy11", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(10L,LocalTime.of(19,0)),new Theme(11L, "hi11", "happy11", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(11L,LocalTime.of(20,0)),new Theme(11L, "hi11", "happy11", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(1L,LocalTime.of(10,0)), new Theme(12L, "hi12", "happy12", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(2L,LocalTime.of(11,0)), new Theme(12L, "hi12", "happy12", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(3L,LocalTime.of(12,0)), new Theme(12L, "hi12", "happy12", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(4L,LocalTime.of(13,0)), new Theme(12L, "hi12", "happy12", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(5L,LocalTime.of(14,0)), new Theme(12L, "hi12", "happy12", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(6L,LocalTime.of(15,0)), new Theme(12L, "hi12", "happy12", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(7L,LocalTime.of(16,0)), new Theme(12L, "hi12", "happy12", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(8L,LocalTime.of(17,0)), new Theme(12L, "hi12", "happy12", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(9L,LocalTime.of(18,0)), new Theme(12L, "hi12", "happy12", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(10L,LocalTime.of(19,0)),new Theme(12L, "hi12", "happy12", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(11L,LocalTime.of(20,0)),new Theme(12L, "hi12", "happy12", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
        reservationRepository.save(new Reservation(member, date, new ReservationTime(12L,LocalTime.of(21,0)),new Theme(12L, "hi12", "happy12", "https://img1.daumcdn.net/thumb/R800x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FsYpXc%2FbtqDhvJwpgl%2FHdOH4fsVgyK5kazXCbmiz0%2Fimg.jpg")));
    }
}
