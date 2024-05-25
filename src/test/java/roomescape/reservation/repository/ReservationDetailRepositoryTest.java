package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import roomescape.reservation.domain.ReservationDetail;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.Time;

@DataJpaTest
class ReservationDetailRepositoryTest {
    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private ReservationDetailRepository detailRepository;

    private Theme theme;
    private Time time;
    private ReservationDetail reservationDetail;

    @BeforeEach
    void setUp() {
        theme = new Theme("Harry Potter", "해리포터와 도비", "thumbnail.jpg");
        time = new Time(LocalTime.of(12, 0));
        reservationDetail = new ReservationDetail(theme, time, LocalDate.MAX);

        entityManager.persist(time);
        entityManager.persist(theme);
        entityManager.persist(reservationDetail);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("실행 성공 : 해당 예약 시간에 예약 정보 갯수를 알 수 있다.")
    void countReservationsByTime_Id() {
        // when
        int actual = detailRepository.countReservationsByTime_Id(reservationDetail.getTimeId());

        // then
        assertThat(actual).isEqualTo(1);
    }

    @Test
    @DisplayName("실행 성공 : 존재하는 예약 정보의 ID를 알 수 있다.")
    void findIdByDateAndThemeIdAndTimeId() {
        // when
        Long actual = detailRepository.findIdByDateAndThemeIdAndTimeId(LocalDate.MAX, theme.getId(), time.getId())
                .get();

        // then
        assertThat(actual).isEqualTo(reservationDetail.getId());
    }
}
