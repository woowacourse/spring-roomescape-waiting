package roomescape.reservationtime.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.constant.TestData.RESERVATION_TIME_COUNT;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.exception.ReservationException;
import roomescape.reservation.repository.RoomEscapeInformationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.dto.ReservationTimeResponse;
import roomescape.reservationtime.repository.ReservationTimeRepository;


@DataJpaTest
@Sql("/data.sql")
class ReservationTimeServiceTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private RoomEscapeInformationRepository roomEscapeInformationRepository;

    private ReservationTimeService service;

    private final LocalTime time1 = LocalTime.of(13, 0);
    private final LocalTime time2 = LocalTime.of(14, 0);

    @BeforeEach
    void setUp() {
        service = new ReservationTimeService(reservationTimeRepository, roomEscapeInformationRepository);
        reservationTimeRepository.saveAll(List.of(
                ReservationTime.from(time1),
                ReservationTime.from(time2)
        ));
    }

    @Test
    void 모든_예약_시간을_조회한다() {
        // when
        List<ReservationTimeResponse> all = service.findAll();

        // then
        assertThat(all).hasSize(RESERVATION_TIME_COUNT + 2)
                .extracting(ReservationTimeResponse::startAt)
                .contains(time1, time2);
    }

    @Test
    void 예약_시간이_삭제된다() {
        // given
        List<ReservationTimeResponse> before = service.findAll();
        final Long idToDelete = before.stream()
                .filter(response -> response.startAt() == time1).findFirst().get().id();

        // when
        service.delete(idToDelete);

        // then
        List<ReservationTimeResponse> after = service.findAll();
        assertThat(after).hasSize(RESERVATION_TIME_COUNT + 1)
                .extracting(ReservationTimeResponse::id)
                .doesNotContain(idToDelete);
    }


    @Test
    void 예약이_존재하는_시간을_삭제하지_못_한다() {
        // given
        // when
        // then
        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(ReservationException.class)
                .hasMessage("해당 시간으로 예약된 건이 존재합니다.");

    }
}
