package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.domain.exception.DomainErrorCode.REFERENTIAL_INTEGRITY;
import static roomescape.domain.exception.DomainErrorCode.RESERVATION_TIME_NOT_FOUND;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomEscapeException;
import roomescape.dto.ReservationTimeRequest;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;

@Transactional
@SpringBootTest
class ReservationTimeServiceTest {

    private static final LocalTime TEN = LocalTime.of(10, 0);

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    void 예약시간을_추가한다() {
        ReservationTimeRequest request = new ReservationTimeRequest(TEN);

        ReservationTime reservationTime = reservationTimeService.addReservationTime(request);

        assertThat(reservationTime.getId()).isNotNull();
        assertThat(reservationTime.getStartAt()).isEqualTo(TEN);
    }

    @Test
    void 모든_예약시간을_조회한다() {
        ReservationTimeRequest request = new ReservationTimeRequest(TEN);
        reservationTimeService.addReservationTime(request);

        List<ReservationTime> reservationTimes = reservationTimeService.getReservationTimes();

        assertThat(reservationTimes).hasSize(1);
        assertThat(reservationTimes.getFirst().getStartAt()).isEqualTo(TEN);
    }

    @Test
    void id에_맞는_예약시간을_조회한다() {
        ReservationTimeRequest request = new ReservationTimeRequest(TEN);
        Long saveId = reservationTimeService.addReservationTime(request).getId();

        ReservationTime reservationTime = reservationTimeService.getReservationTime(saveId);

        assertThat(reservationTime.getId()).isEqualTo(saveId);
        assertThat(reservationTime.getStartAt()).isEqualTo(TEN);
    }

    @Test
    void 예약시간을_삭제한다() {
        ReservationTimeRequest request = new ReservationTimeRequest(TEN);
        Long saveId = reservationTimeService.addReservationTime(request).getId();

        reservationTimeService.deleteReservationTime(saveId);

        assertThatRoomEscapeExceptionCode(
                () -> reservationTimeService.getReservationTime(saveId),
                RESERVATION_TIME_NOT_FOUND
        );
    }

    @Test
    void 예약_목록에_삭제할_시간이_존재한다면_예약시간을_삭제할_수_없다() {
        ReservationTimeRequest request = new ReservationTimeRequest(TEN);
        ReservationTime reservationTime = reservationTimeService.addReservationTime(request);

        Long themeId = themeRepository.save(new Theme("방탈출 제목", "방탈출 설명", "thumbnail.png")).getId();
        Optional<Theme> theme = themeRepository.findById(themeId);

        reservationRepository.save(new Reservation(
                "브라운",
                LocalDate.now().plusDays(1),
                reservationTime,
                theme.get()
        ));

        assertThatRoomEscapeExceptionCode(
                () -> reservationTimeService.deleteReservationTime(reservationTime.getId()),
                REFERENTIAL_INTEGRITY
        );
    }

    @Test
    void 없는_예약시간을_삭제할_수_없다() {
        assertThatRoomEscapeExceptionCode(
                () -> reservationTimeService.deleteReservationTime(1L),
                RESERVATION_TIME_NOT_FOUND
        );
    }

    private void assertThatRoomEscapeExceptionCode(ThrowingCallable callable, DomainErrorCode expectedCode) {
        assertThatThrownBy(callable)
                .isInstanceOfSatisfying(RoomEscapeException.class,
                        exception -> assertThat(exception.code()).isEqualTo(expectedCode));
    }
}
