package roomescape.reservation.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.error.exception.BadRequestException;
import roomescape.global.error.exception.ConflictException;
import roomescape.global.error.exception.NotFoundException;
import roomescape.reservation.dto.request.ReservationTimeRequest.ReservationTimeCreateRequest;
import roomescape.reservation.entity.Reservation;
import roomescape.reservation.entity.ReservationTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.service.ReservationTimeService;
import roomescape.reservation.unit.repository.FakeReservationRepository;
import roomescape.reservation.unit.repository.FakeReservationTimeRepository;

class ReservationTimeServiceTest {

    private ReservationTimeService reservationTimeService;
    private ReservationTimeRepository reservationTimeRepository;
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        reservationTimeRepository = new FakeReservationTimeRepository();
        reservationRepository = new FakeReservationRepository();
        reservationTimeService = new ReservationTimeService(reservationTimeRepository, reservationRepository);
    }

    @Test
    @DisplayName("시간을 생성한다.")
    void createTime() {
        // given
        var startAt = LocalTime.of(10, 0);
        var request = new ReservationTimeCreateRequest(startAt);

        // when
        var response = reservationTimeService.createTime(request);

        // then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.startAt()).isEqualTo(startAt.toString());
    }

    @Test
    @DisplayName("운영 시간 이외의 시간을 생성하면 예외가 발생한다.")
    void createTimeWithInvalidTime() {
        // given
        var startAt = LocalTime.of(9, 0);
        var request = new ReservationTimeCreateRequest(startAt);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.createTime(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("운영 시간 이외의 날짜는 예약할 수 없습니다.");
    }

    @Test
    @DisplayName("중복된 시간을 생성하면 예외가 발생한다.")
    void createTimeWithDuplicateTime() {
        // given
        var startAt = LocalTime.of(10, 0);
        var request = new ReservationTimeCreateRequest(startAt);
        reservationTimeService.createTime(request);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.createTime(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("러닝 타임이 겹치는 시간이 존재합니다.");
    }

    @Test
    @DisplayName("모든 시간을 조회한다.")
    void getAllTimes() {
        // given
        var startAt = LocalTime.of(10, 0);
        var request = new ReservationTimeCreateRequest(startAt);
        reservationTimeService.createTime(request);

        // when
        var responses = reservationTimeService.getAllTimes();

        // then
        assertThat(responses).hasSize(1);
        var response = responses.get(0);
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.startAt()).isEqualTo(startAt.toString());
    }

    @Test
    @DisplayName("특정 날짜와 테마에 대한 가능한 시간을 조회한다.")
    void getAvailableTimes() {
        // given
        var date = LocalDate.of(2024, 3, 20);
        var themeId = 1L;
        var startAt = LocalTime.of(10, 0);
        var request = new ReservationTimeCreateRequest(startAt);
        reservationTimeService.createTime(request);

        // when
        var responses = reservationTimeService.getAvailableTimes(date, themeId);

        // then
        assertThat(responses).hasSize(1);
        var response = responses.getFirst();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.startAt()).isEqualTo(startAt.toString());
        assertThat(response.alreadyBooked()).isFalse();
    }

    @Test
    @DisplayName("시간을 삭제한다.")
    void deleteTime() {
        // given
        var startAt = LocalTime.of(10, 0);
        var request = new ReservationTimeCreateRequest(startAt);
        var response = reservationTimeService.createTime(request);

        // when
        reservationTimeService.deleteTime(response.id());

        // then
        var times = reservationTimeService.getAllTimes();
        assertThat(times).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 시간을 삭제하면 예외가 발생한다.")
    void deleteNonExistentTime() {
        // when & then
        assertThatThrownBy(() -> reservationTimeService.deleteTime(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 id 입니다.");
    }

    @Test
    @DisplayName("예약이 있는 시간을 삭제하면 예외가 발생한다.")
    void deleteTimeWithReservation() {
        // given
        var startAt = LocalTime.of(10, 0);
        var request = new ReservationTimeCreateRequest(startAt);
        var response = reservationTimeService.createTime(request);

        var time = new ReservationTime(response.id(), startAt);
        var reservation = new Reservation(1L, LocalDate.now(), time, 1L, 1L);
        reservationRepository.save(reservation);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.deleteTime(response.id()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("해당 시간에 예약된 내역이 존재하므로 삭제할 수 없습니다.");
    }
} 