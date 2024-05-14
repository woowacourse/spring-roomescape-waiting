package roomescape.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static roomescape.exception.ExceptionType.DELETE_USED_TIME;
import static roomescape.exception.ExceptionType.DUPLICATE_RESERVATION_TIME;
import static roomescape.fixture.ReservationFixture.DEFAULT_RESERVATION;
import static roomescape.fixture.ReservationTimeFixture.DEFAULT_TIME;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.RoomescapeException;
import roomescape.fixture.ReservationTimeFixture;
import roomescape.repository.CollectionReservationRepository;
import roomescape.repository.CollectionReservationTimeRepository;

class ReservationTimeServiceTest {

    private CollectionReservationRepository reservationRepository;
    private ReservationTimeService reservationTimeService;

    @BeforeEach
    void initService() {
        reservationRepository = new CollectionReservationRepository();
        CollectionReservationTimeRepository reservationTimeRepository = new CollectionReservationTimeRepository();
        reservationTimeService = new ReservationTimeService(reservationRepository, reservationTimeRepository);
    }

    @Test
    @DisplayName("중복된 예약 시간을 생성할 수 없는지 확인")
    void saveFailWhenDuplicate() {
        reservationTimeService.save(ReservationTimeFixture.DEFAULT_REQUEST);

        Assertions.assertThatThrownBy(() -> reservationTimeService.save(ReservationTimeFixture.DEFAULT_REQUEST))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(DUPLICATE_RESERVATION_TIME.getMessage());
    }

    @Test
    @DisplayName("예약 시간을 사용하는 예약이 있으면 예약을 삭제할 수 없다.")
    void deleteFailWhenUsed() {
        reservationTimeService.save(ReservationTimeFixture.DEFAULT_REQUEST);
        reservationRepository.save(DEFAULT_RESERVATION);

        assertThatCode(() -> reservationTimeService.delete(DEFAULT_TIME.getId()))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(DELETE_USED_TIME.getMessage());
    }
}
