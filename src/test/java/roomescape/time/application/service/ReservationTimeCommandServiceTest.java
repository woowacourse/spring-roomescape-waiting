package roomescape.time.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.DuplicateException;
import roomescape.common.exception.NotFoundException;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.theme.domain.ThemeRepository;
import roomescape.time.application.dto.CreateReservationTimeRequest;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeId;
import roomescape.time.domain.ReservationTimeRepository;
import roomescape.time.domain.TimeValue;
import roomescape.user.domain.UserRepository;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ReservationTimeCommandServiceTest {

    @Autowired
    private ReservationTimeCommandService reservationTimeCommandService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("예약 시간을 생성할 수 있다")
    void createReservationTime() {
        // given
        final CreateReservationTimeRequest request = new CreateReservationTimeRequest(TimeValue.from(LocalTime.of(12, 30)));

        // when
        final ReservationTime reservationTime = reservationTimeCommandService.create(request);

        // then
        assertThat(reservationTime.getStartAt().getValue()).isEqualTo(LocalTime.of(12, 30));
        assertThat(reservationTimeRepository.findById(reservationTime.getId()))
                .isPresent();
    }

    @Test
    @DisplayName("예약 시간을 삭제할 수 있다")
    void deleteReservationTime() {
        // given
        final ReservationTime saved =
                reservationTimeRepository.save(
                        ReservationTime.withoutId(TimeValue.from(LocalTime.of(14, 0))));
        final ReservationTimeId id = saved.getId();

        // when
        reservationTimeCommandService.delete(id);

        // then
        assertThat(reservationTimeRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간을 삭제하려 하면 예외가 발생한다")
    void deleteNonExistentReservationTime() {
        // given
        final ReservationTimeId id = ReservationTimeId.from(-1L);

        // when
        // then
        assertThatThrownBy(() -> reservationTimeCommandService.delete(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("[RESERVATION_TIME] not found. params={ReservationTimeId=ReservationTimeId(-1)}");
    }

    @Test
    @DisplayName("추가하려는 시간이 이미 존재한다면, 예외가 발생한다")
    void existsTime() {
        // given
        final LocalTime time = LocalTime.of(14, 0);
        reservationTimeRepository.save(ReservationTime.withoutId(TimeValue.from(time)));

        final CreateReservationTimeRequest sameTimeRequest = new CreateReservationTimeRequest(TimeValue.from(time));

        // when
        // then
        assertThatThrownBy(() -> reservationTimeCommandService.create(sameTimeRequest))
                .isInstanceOf(DuplicateException.class)
                .hasMessage("RESERVATION_TIME already exists. params={TimeValue=TimeValue(value=14:00)}");
    }
}
