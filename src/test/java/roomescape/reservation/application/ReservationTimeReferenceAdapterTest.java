package roomescape.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.customException.BusinessException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.fake.FakeReservationRepository;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

class ReservationTimeReferenceAdapterTest {

    private ReservationRepository reservationRepository;
    private ReservationTimeReferenceAdapter referenceAdapter;

    @BeforeEach
    void setUp() {
        reservationRepository = new FakeReservationRepository();
        referenceAdapter = new ReservationTimeReferenceAdapter(reservationRepository);
    }

    @Test
    @DisplayName("예약 시간을 참조하는 예약이 없으면 예외가 발생하지 않는다")
    void validateReservationTimeNotReferenced_success() {
        // when & then
        assertThatCode(() -> referenceAdapter.validateReservationTimeNotReferenced(1L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("예약 시간을 참조하는 예약이 있으면 예외가 발생한다")
    void validateReservationTimeNotReferenced_fail_with_referenced_time() {
        // given
        ReservationTime time = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        Theme theme = Theme.createRow(1L, "공포", "설명", "https://good.com");
        reservationRepository.save(Reservation.create("인직", LocalDate.now().plusDays(1), time, theme));

        // when & then
        assertThatThrownBy(() -> referenceAdapter.validateReservationTimeNotReferenced(time.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("예약이 연결된 예약 시간은 삭제할 수 없습니다.");
    }

    @Test
    @DisplayName("날짜와 테마 기준으로 예약된 시간을 조회한다")
    void getBookedTimes_success() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        Theme theme = Theme.createRow(1L, "공포", "설명", "https://good.com");
        ReservationTime time = ReservationTime.createRow(1L, LocalTime.of(10, 0));
        ReservationTime otherTime = ReservationTime.createRow(2L, LocalTime.of(11, 0));
        reservationRepository.save(Reservation.create("인직", date, time, theme));
        reservationRepository.save(Reservation.create("브라운", date.plusDays(1), otherTime, theme));

        // when
        List<ReservationTime> bookedTimes = referenceAdapter.getBookedTimes(date, theme.getId());

        // then
        assertThat(bookedTimes).containsExactly(time);
    }
}
