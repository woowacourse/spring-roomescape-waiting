package roomescape.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.reservation.application.UserReservationService;
import roomescape.reservation.application.dto.request.CreateReservationServiceRequest;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.repository.ReservationRepository;
import roomescape.global.exception.BusinessRuleViolationException;
import roomescape.support.IntegrationTestSupport;

class UserReservationServiceTest extends IntegrationTestSupport {

    @Autowired
    private UserReservationService userReservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)", "이름", "설명", "썸네일");
        jdbcTemplate.update("INSERT INTO member (name, email, password, role) VALUES (?,?,?,?);",
                "어드민", "admin@naver.com", "1234", "ADMIN");
    }

    @DisplayName("요청된 예약 정보로 예약을 진행할 수 있다")
    @Test
    void createFutureReservation() {
        // given
        String name = "웨이드";
        LocalDate date = LocalDate.now().plusDays(20);
        Long timeId = 1L;
        Long themeId = 1L;
        CreateReservationServiceRequest request = new CreateReservationServiceRequest(name, 1L, date, timeId, themeId);

        // when
        userReservationService.create(request);

        // then
        List<Reservation> reservations = reservationRepository.getAll();
        assertSoftly(softly -> {
            softly.assertThat(reservations).hasSize(1);
            softly.assertThat(reservations.getFirst().getName()).isEqualTo(name);
            softly.assertThat(reservations.getFirst().getDate()).isEqualTo(date);
        });
    }

    @DisplayName("요청한 예약 시간이 과거라면 예외를 발생시킨다")
    @Test
    void pastException() {
        // given
        String name = "웨이드";
        LocalDate date = LocalDate.now().minusDays(10);
        Long timeId = 1L;
        Long themeId = 1L;
        CreateReservationServiceRequest request = new CreateReservationServiceRequest(name, 1L, date, timeId, themeId);

        // when & then
        Assertions.assertThatThrownBy(() -> userReservationService.create(request))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @DisplayName("예약 요청한 테마, 예약 시간에 이미 예약이 있다면 예외를 발생시킨다")
    @Test
    void duplicationException() {
        // given
        String name = "웨이드";
        LocalDate date = LocalDate.now().minusDays(10);
        Long timeId = 1L;
        Long themeId = 1L;
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", name, date,
                timeId, themeId);
        String anotherName = "검프";
        Long memberId = 1L;
        CreateReservationServiceRequest request = new CreateReservationServiceRequest(anotherName, memberId, date, timeId, themeId);

        // when & then
        Assertions.assertThatThrownBy(() -> userReservationService.create(request))
                .isInstanceOf(BusinessRuleViolationException.class);
    }
}
