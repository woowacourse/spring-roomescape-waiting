package roomescape.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.ReservationTestFixture;
import roomescape.reservation.application.UserReservationTimeService;
import roomescape.reservation.application.dto.response.ReservationTimeServiceResponse;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.support.IntegrationTestSupport;

class UserReservationTimeServiceTest extends IntegrationTestSupport {

    @Autowired
    private UserReservationTimeService userReservationTimeService;

    @PersistenceContext
    private EntityManager entityManager;

    @DisplayName("입력된 테마와 날짜에 대해, 전체 예약 시간과 각각의 예약 상태를 함께 조회할 수 있다")
    @Test
    void getAllWithStatus() {
        // given
        ReservationTime reservationTime1 = ReservationTestFixture.createTime(LocalTime.parse("10:00"));
        ReservationTime reservationTime2 = ReservationTestFixture.createTime(LocalTime.parse("10:05"));
        ReservationTime reservationTime3 = ReservationTestFixture.createTime(LocalTime.parse("10:10"));
        ReservationTheme reservationTheme = ReservationTestFixture.createTheme("테마이름", "설명", "썸네일");
        LocalDate date = LocalDate.now().plusDays(20);
        Reservation reservation1 = ReservationTestFixture.createReservation(date, reservationTime1,
            reservationTheme);
        Reservation reservation2 = ReservationTestFixture.createReservation(date, reservationTime2,
            reservationTheme);
        entityManager.persist(reservationTime1);
        entityManager.persist(reservationTime2);
        entityManager.persist(reservationTime3);
        entityManager.persist(reservationTheme);
        entityManager.persist(reservation1);
        entityManager.persist(reservation2);


        // when
        List<ReservationTimeServiceResponse> responses = userReservationTimeService.getAllWithStatus(reservationTheme.getId(), date);

        // then
        List<ReservationTimeServiceResponse> bookedResponse = responses.stream()
                .filter(ReservationTimeServiceResponse::isBooked)
                .toList();

        assertSoftly(softly -> {
            softly.assertThat(responses).hasSize(3);
            softly.assertThat(bookedResponse).hasSize(2);
            softly.assertThat(bookedResponse.getFirst().id()).isEqualTo(reservationTime1.getId());
            softly.assertThat(bookedResponse.get(1).id()).isEqualTo(reservationTime2.getId());
        });
    }
}
