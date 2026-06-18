package roomescape.reservationtime;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import roomescape.reservationtime.adapter.out.persistence.JpaReservationTimeRepository;
import roomescape.reservationtime.domain.ReservationTime;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaReservationTimeRepository.class)
public class JpaReservationTimeRepositoryTest {
    @Autowired
    private JpaReservationTimeRepository repository;

    @Test
    @DisplayName("시간을 저장할 수 있다.")
    void saves_reservation_time_successfully() {
        // given
        ReservationTime reservationTime = new ReservationTime(null, LocalTime.of(15, 40));

        // when
        ReservationTime savedReservationTime = repository.save(reservationTime);

        // then
        assertThat(savedReservationTime.startAt()).isEqualTo(LocalTime.of(15, 40));
    }

    @Test
    @DisplayName("테스트 더미데이터에 4개가 등록되어 있으므로 사이즈는 4가 나와야한다.")
    void finds_four_reservation_times_from_test_data() {
        // given -> 더미데이터 4개
        // when
        List<ReservationTime> reservationTimes = repository.findAll();

        assertThat(reservationTimes).hasSize(4);
        assertThat(reservationTimes)
                .extracting(ReservationTime::startAt)
                .containsExactly(LocalTime.of(10, 00), LocalTime.of(11, 00), LocalTime.of(12, 00),
                        LocalTime.of(13, 00));
    }

    @Test
    @DisplayName("테스트 더미데이터에 4개가 등록되어 있으므로 삭제 후 사이즈는 4가 나와야한다.")
    void keeps_four_reservation_times_after_deleting_one_from_test_data() {
        // given
        ReservationTime reservationTime = new ReservationTime(null, LocalTime.of(15, 40));
        ReservationTime savedTime = repository.save(reservationTime);

        // when
        repository.deleteById(savedTime.id());

        // then
        List<ReservationTime> reservationTimes = repository.findAll();
        assertThat(reservationTimes).hasSize(4);
        assertThat(reservationTimes).extracting(ReservationTime::id)
                .doesNotContain(savedTime.id());
    }

    @Test
    @DisplayName("일정표에 등록된 특정 날짜와 테마의 모든 시간을 가져올 수 있다.")
    void finds_reservation_times_by_date_and_theme_id() {
        // when
        List<ReservationTime> result = repository.findTimesByDateAndThemeId(LocalDate.parse("2026-05-05"), 1L);

        // then
        assertThat(result)
                .extracting(ReservationTime::startAt)
                .containsExactly(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("이미 존재하는 시간이면 성공을 반환한다.")
    void existing_reservation_time_returns_true() {
        // given
        LocalTime time = LocalTime.of(10, 0);

        // when
        boolean result = repository.existsAlreadyTime(time);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 시간이면 실패를 반환한다.")
    void missing_reservation_time_returns_false() {
        // given
        LocalTime time = LocalTime.of(18, 23);

        // when
        boolean result = repository.existsAlreadyTime(time);

        // then
        assertThat(result).isFalse();
    }
}
