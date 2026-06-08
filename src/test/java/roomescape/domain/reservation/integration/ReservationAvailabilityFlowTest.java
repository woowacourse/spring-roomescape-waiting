package roomescape.domain.reservation.integration;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.reservation.dto.request.ReservationCreateRequestDto;
import roomescape.domain.reservation.dto.response.ReservationCreateResponseDto;
import roomescape.domain.reservation.dto.response.ReservationResponseDto;
import roomescape.domain.reservation.entity.ReservationEditableStatus;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.theme.repository.ThemeRepository;
import roomescape.domain.time.dto.response.TimeAvailabilityResponseDto;
import roomescape.domain.time.entity.Time;
import roomescape.domain.time.repository.TimeRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReservationAvailabilityFlowTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TimeRepository timeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
    }

    @Test
    void 예약_가능_시간_조회_후_예약을_생성하면_같은_테마에서만_예약된_시간의_available이_false가_된다() {
        // given
        LocalDate date = LocalDate.of(2099, 5, 1);
        Time time1 = timeRepository.save(Time.create(LocalTime.of(10, 0)));
        Time time2 = timeRepository.save(Time.create(LocalTime.of(11, 0)));
        Theme themeA = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));

        // when
        List<TimeAvailabilityResponseDto> beforeReservationTimes = given()
            .queryParam("date", date.toString())
            .queryParam("themeId", themeA.getId())
            .when().get("/api/times")
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        given()
            .contentType(ContentType.JSON)
            .body(new ReservationCreateRequestDto("예약자", date, time1.getId(), themeA.getId()))
            .when().post("/api/reservations")
            .then()
            .statusCode(201)
            .body("name", equalTo("예약자"))
            .body("date", equalTo(date.toString()))
            .body("timeId", equalTo(time1.getId().intValue()))
            .body("themeId", equalTo(themeA.getId().intValue()));

        List<TimeAvailabilityResponseDto> afterReservationTimes = given()
            .queryParam("date", date.toString())
            .queryParam("themeId", themeA.getId())
            .when().get("/api/times")
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        // then
        boolean beforeAllAvailable = beforeReservationTimes.stream().allMatch(TimeAvailabilityResponseDto::available);
        assertThat(beforeAllAvailable).isTrue();

        boolean afterNotAvailable = afterReservationTimes.stream()
            .filter(time -> Objects.equals(time.id(), time1.getId()))
            .noneMatch(TimeAvailabilityResponseDto::available);

        boolean afterTimeContains = afterReservationTimes.stream()
            .anyMatch(time -> Objects.equals(time.id(), time1.getId()));

        assertThat(afterNotAvailable).isTrue();
        assertThat(afterTimeContains).isTrue();
    }

    @Test
    void 활성_예약이_있는_날짜_시간_테마에_다른_사용자는_대기_예약을_생성할_수_있다() {
        // given
        LocalDate date = LocalDate.of(2099, 5, 1);
        Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
        saveReservation("예약자", date, time, theme);

        // when
        ReservationCreateResponseDto actual = saveWaitingReservation("대기자", date, time, theme);

        // then
        assertThat(actual.name()).isEqualTo("대기자");
        assertThat(actual.date()).isEqualTo(date);
        assertThat(actual.timeId()).isEqualTo(time.getId());
        assertThat(actual.themeId()).isEqualTo(theme.getId());
    }

    @Test
    void 활성_예약이_없는_날짜_시간_테마에는_대기_예약을_생성할_수_없다() {
        // given
        LocalDate date = LocalDate.of(2099, 5, 1);
        Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));

        // when & then
        given()
            .contentType(ContentType.JSON)
            .body(new ReservationCreateRequestDto("대기자", date, time.getId(), theme.getId()))
            .when().post("/api/reservations/waitings")
            .then()
            .statusCode(409)
            .body("message", equalTo("예약 가능한 시간은 대기할 수 없습니다."));
    }

    @Test
    void 활성_예약을_취소하면_첫_번째_대기_예약이_승인된다() {
        // given
        LocalDate date = LocalDate.of(2099, 5, 1);
        Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
        ReservationCreateResponseDto active = saveReservation("예약자", date, time, theme);
        saveWaitingReservation("대기자1", date, time, theme);
        saveWaitingReservation("대기자2", date, time, theme);

        // when
        cancelReservation(active.id(), "예약자");

        // then
        ReservationResponseDto approved = findReservationsByName("대기자1").getFirst();
        ReservationResponseDto waiting = findReservationsByName("대기자2").getFirst();
        assertThat(approved.status()).isEqualTo(ReservationEditableStatus.EDITABLE);
        assertThat(approved.waitingNumber()).isNull();
        assertThat(waiting.status()).isEqualTo(ReservationEditableStatus.WAITING);
        assertThat(waiting.waitingNumber()).isEqualTo(1);
    }

    @Test
    void 같은_이름의_활성_예약이_있으면_대기_예약을_생성할_수_없다() {
        // given
        LocalDate date = LocalDate.of(2099, 5, 1);
        Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
        saveReservation("예약자", date, time, theme);

        // when & then
        given()
            .contentType(ContentType.JSON)
            .body(new ReservationCreateRequestDto("예약자", date, time.getId(), theme.getId()))
            .when().post("/api/reservations/waitings")
            .then()
            .statusCode(409)
            .body("message", equalTo("이미 예약된 날짜, 시간, 테마입니다."));
    }

    @Test
    void 같은_이름_날짜_시간_테마로_이미_대기_중이면_중복_대기_예약을_생성할_수_없다() {
        // given
        LocalDate date = LocalDate.of(2099, 5, 1);
        Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
        saveReservation("기존예약자", date, time, theme);
        saveWaitingReservation("예약자", date, time, theme);

        // when & then
        given()
            .contentType(ContentType.JSON)
            .body(new ReservationCreateRequestDto("예약자", date, time.getId(), theme.getId()))
            .when().post("/api/reservations/waitings")
            .then()
            .statusCode(409)
            .body("message", equalTo("이미 대기 중인 이름, 날짜, 시간, 테마입니다."));
    }

    @Test
    void 서로_다른_이름이면_같은_날짜_시간_테마에_여러_명이_대기_예약을_생성할_수_있다() {
        // given
        LocalDate date = LocalDate.of(2099, 5, 1);
        Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
        saveReservation("예약자", date, time, theme);

        // when
        ReservationCreateResponseDto first = saveWaitingReservation("대기자1", date, time, theme);
        ReservationCreateResponseDto second = saveWaitingReservation("대기자2", date, time, theme);

        // then
        assertThat(first.id()).isNotNull();
        assertThat(second.id()).isNotNull();
        assertThat(countNotDeletedWaitingReservations(date, time, theme)).isEqualTo(2);
    }

    @Test
    void 대기_예약을_생성하면_사용자_예약_조회에서_WAITING_상태와_대기_순번을_반환한다() {
        // given
        LocalDate date = LocalDate.of(2099, 5, 1);
        Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
        saveReservation("예약자", date, time, theme);
        ReservationCreateResponseDto waiting = saveWaitingReservation("대기자", date, time, theme);

        // when
        List<ReservationResponseDto> reservations = findReservationsByName("대기자");

        // then
        assertThat(reservations)
            .extracting(ReservationResponseDto::id)
            .containsExactly(waiting.id());
        assertThat(reservations.getFirst().status()).isEqualTo(ReservationEditableStatus.WAITING);
        assertThat(reservations.getFirst().waitingNumber()).isEqualTo(1);
    }

    @Test
    void 같은_날짜_시간_테마의_대기_예약은_생성_순서대로_순번을_반환한다() {
        // given
        LocalDate date = LocalDate.of(2099, 5, 1);
        Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
        saveReservation("예약자", date, time, theme);
        saveWaitingReservation("대기자1", date, time, theme);
        saveWaitingReservation("대기자2", date, time, theme);
        saveWaitingReservation("대기자3", date, time, theme);

        // when
        List<Integer> waitingNumbers = List.of(
            findReservationsByName("대기자1").getFirst().waitingNumber(),
            findReservationsByName("대기자2").getFirst().waitingNumber(),
            findReservationsByName("대기자3").getFirst().waitingNumber()
        );

        // then
        assertThat(waitingNumbers).containsExactly(1, 2, 3);
    }

    @Test
    void 첫_번째_대기_예약을_취소하면_뒤_대기_예약의_순번이_앞으로_당겨진다() {
        // given
        LocalDate date = LocalDate.of(2099, 5, 1);
        Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
        saveReservation("예약자", date, time, theme);
        ReservationCreateResponseDto first = saveWaitingReservation("대기자1", date, time, theme);
        saveWaitingReservation("대기자2", date, time, theme);
        saveWaitingReservation("대기자3", date, time, theme);

        // when
        cancelWaitingReservation(first.id(), "대기자1");

        // then
        assertThat(findReservationsByName("대기자2").getFirst().waitingNumber()).isEqualTo(1);
        assertThat(findReservationsByName("대기자3").getFirst().waitingNumber()).isEqualTo(2);
    }

    @Test
    void 중간_대기_예약을_취소하면_뒤_대기_예약의_순번만_다시_계산된다() {
        // given
        LocalDate date = LocalDate.of(2099, 5, 1);
        Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
        saveReservation("예약자", date, time, theme);
        saveWaitingReservation("대기자1", date, time, theme);
        ReservationCreateResponseDto second = saveWaitingReservation("대기자2", date, time, theme);
        saveWaitingReservation("대기자3", date, time, theme);

        // when
        cancelWaitingReservation(second.id(), "대기자2");

        // then
        assertThat(findReservationsByName("대기자1").getFirst().waitingNumber()).isEqualTo(1);
        assertThat(findReservationsByName("대기자3").getFirst().waitingNumber()).isEqualTo(2);
    }

    @Test
    void 대기_예약을_취소하면_사용자_예약_조회에서_CANCELED_상태와_null_순번을_반환한다() {
        // given
        LocalDate date = LocalDate.of(2099, 5, 1);
        Time time = timeRepository.save(Time.create(LocalTime.of(10, 0)));
        Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
        saveReservation("예약자", date, time, theme);
        ReservationCreateResponseDto waiting = saveWaitingReservation("대기자", date, time, theme);

        // when
        cancelWaitingReservation(waiting.id(), "대기자");
        List<ReservationResponseDto> reservations = findReservationsByName("대기자");

        // then
        assertThat(reservations.getFirst().status()).isEqualTo(ReservationEditableStatus.CANCELED);
        assertThat(reservations.getFirst().waitingNumber()).isNull();
    }

    private ReservationCreateResponseDto saveReservation(String name, LocalDate date, Time time, Theme theme) {
        return given()
            .contentType(ContentType.JSON)
            .body(new ReservationCreateRequestDto(name, date, time.getId(), theme.getId()))
            .when().post("/api/reservations")
            .then()
            .statusCode(201)
            .extract()
            .as(ReservationCreateResponseDto.class);
    }

    private ReservationCreateResponseDto saveWaitingReservation(String name, LocalDate date, Time time, Theme theme) {
        return given()
            .contentType(ContentType.JSON)
            .body(new ReservationCreateRequestDto(name, date, time.getId(), theme.getId()))
            .when().post("/api/reservations/waitings")
            .then()
            .statusCode(201)
            .extract()
            .as(ReservationCreateResponseDto.class);
    }

    private void cancelWaitingReservation(Long id, String name) {
        given()
            .queryParam("name", name)
            .when().patch("/api/reservations/{id}/waitings/cancel", id)
            .then()
            .statusCode(200);
    }

    private void cancelReservation(Long id, String name) {
        given()
            .queryParam("name", name)
            .when().patch("/api/reservations/{id}/cancel", id)
            .then()
            .statusCode(200);
    }

    private List<ReservationResponseDto> findReservationsByName(String name) {
        return given()
            .queryParam("name", name)
            .when().get("/api/reservations")
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });
    }

    private Integer countNotDeletedWaitingReservations(LocalDate date, Time time, Theme theme) {
        return jdbcTemplate.queryForObject(
            """
                SELECT COUNT(*)
                FROM reservation
                WHERE date = ?
                  AND time_id = ?
                  AND theme_id = ?
                  AND status = 'WAITING'
                  AND deleted_at IS NULL
                """,
            Integer.class,
            date,
            time.getId(),
            theme.getId()
        );
    }

}
