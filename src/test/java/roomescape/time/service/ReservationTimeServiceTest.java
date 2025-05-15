package roomescape.time.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.fixture.MemberFixture.MEMBER;
import static roomescape.fixture.ThemeFixture.THEME;
import static roomescape.fixture.TimeFixture.TIME;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.fake.FakeReservationDao;
import roomescape.fake.FakeReservationTimeDao;
import roomescape.reservation.domain.Reservation;
import roomescape.time.domain.ReservationTime;
import roomescape.time.dto.CreateReservationTimeRequest;
import roomescape.time.dto.ReservationTimeResponse;

class ReservationTimeServiceTest {

    FakeReservationTimeDao reservationTimeDao = new FakeReservationTimeDao();
    FakeReservationDao reservationDao = new FakeReservationDao();
    ReservationTimeService reservationTimeService = new ReservationTimeService(reservationTimeDao, reservationDao);

    @DisplayName("이미 존재하는 시간을 저장할 경우 예외가 발생한다.")
    @Test
    void testValidateDuplication() {
        // given
        CreateReservationTimeRequest request = new CreateReservationTimeRequest(LocalTime.of(11, 0));
        reservationTimeService.createReservationTime(request);
        // when
        // then
        assertThatThrownBy(() -> reservationTimeService.createReservationTime(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 시간입니다.");
    }

    @DisplayName("예약 시간을 저장할 수 있다.")
    @Test
    void testCreate() {
        // given
        LocalTime time = LocalTime.of(11, 0);
        CreateReservationTimeRequest request = new CreateReservationTimeRequest(time);
        // when
        ReservationTimeResponse result = reservationTimeService.createReservationTime(request);
        // then
        ReservationTime savedTime = reservationTimeDao.findById(1L).orElseThrow();
        assertAll(
                () -> assertThat(result.id()).isEqualTo(1L),
                () -> assertThat(result.startAt()).isEqualTo(time),
                () -> assertThat(savedTime.getId()).isEqualTo(1L),
                () -> assertThat(savedTime.getStartAt()).isEqualTo(time)
        );
    }

    @DisplayName("예약 시간 목록을 조회할 수 있다.")
    @Test
    void testFindAll() {
        // given
        CreateReservationTimeRequest request1 = new CreateReservationTimeRequest(LocalTime.of(11, 0));
        CreateReservationTimeRequest request2 = new CreateReservationTimeRequest(LocalTime.of(12, 0));
        reservationTimeService.createReservationTime(request1);
        reservationTimeService.createReservationTime(request2);
        // when
        List<ReservationTimeResponse> result = reservationTimeService.getReservationTimes();
        // then
        assertThat(result).hasSize(2);
    }

    @DisplayName("예약 시간을 삭제할 수 있다.")
    @Test
    void testDelete() {
        // given
        CreateReservationTimeRequest request = new CreateReservationTimeRequest(LocalTime.of(11, 0));
        reservationTimeService.createReservationTime(request);
        // when
        reservationTimeService.deleteReservationTimeById(1L);
        // then
        assertThat(reservationTimeDao.findById(1L)).isEmpty();
    }

    @DisplayName("예약이 존재하는 시간은 삭제할 경우 예외가 발생한다.")
    @Test
    void testIllegalDelete() {
        // given
        reservationDao.save(Reservation.register(MEMBER, LocalDate.now().plusDays(1), TIME, THEME));
        // when
        // then
        assertThatThrownBy(() -> reservationTimeService.deleteReservationTimeById(TIME.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약이 존재하는 시간은 삭제할 수 없습니다.");
    }

    @DisplayName("예약 가능 시간을 조회할 수 있다.")
    @Test
    void test() {
        // given
        // when
        // then
    }
}
