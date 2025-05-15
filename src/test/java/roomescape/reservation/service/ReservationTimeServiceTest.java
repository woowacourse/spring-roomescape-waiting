package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.fake.FakeReservationRepository;
import roomescape.fake.FakeReservationTimeRepository;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.service.dto.AvailableTimeInfo;
import roomescape.reservation.service.dto.ReservationTimeCreateCommand;
import roomescape.reservation.service.dto.ReservationTimeInfo;

class ReservationTimeServiceTest {

    private final ReservationTimeRepository reservationTimeRepository = new FakeReservationTimeRepository();
    private final ReservationRepository reservationRepository = new FakeReservationRepository();
    private final ReservationTimeService reservationTimeService = new ReservationTimeService(reservationTimeRepository,
            reservationRepository);

    @DisplayName("이미 존재하는 시간을 저장할 경우 예외가 발생한다")
    @Test
    void should_ThrowException_WhenCreateDuplicateTime() {
        // given
        final ReservationTimeCreateCommand request = new ReservationTimeCreateCommand(LocalTime.of(11, 0));
        reservationTimeService.createReservationTime(request);
        // when
        // then
        assertThatThrownBy(() -> reservationTimeService.createReservationTime(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 시간입니다.");
    }

    @DisplayName("예약 시간을 저장할 수 있다")
    @Test
    void create() {
        // given
        final LocalTime time = LocalTime.of(11, 0);
        final ReservationTimeCreateCommand request = new ReservationTimeCreateCommand(time);
        // when
        final ReservationTimeInfo result = reservationTimeService.createReservationTime(request);
        // then
        final ReservationTime savedTime = reservationTimeRepository.findById(1L).get();
        assertAll(
                () -> assertThat(result.id()).isEqualTo(1L),
                () -> assertThat(result.startAt()).isEqualTo(time),
                () -> assertThat(savedTime.getId()).isEqualTo(1L),
                () -> assertThat(savedTime.getStartAt()).isEqualTo(time)
        );
    }

    @DisplayName("예약 시간 목록을 조회할 수 있다")
    @Test
    void findAll() {
        // given
        final ReservationTimeCreateCommand request1 = new ReservationTimeCreateCommand(LocalTime.of(11, 0));
        final ReservationTimeCreateCommand request2 = new ReservationTimeCreateCommand(LocalTime.of(12, 0));
        reservationTimeService.createReservationTime(request1);
        reservationTimeService.createReservationTime(request2);
        // when
        final List<ReservationTimeInfo> result = reservationTimeService.getReservationTimes();
        // then
        assertThat(result).hasSize(2);
    }

    @DisplayName("예약 시간을 삭제할 수 있다")
    @Test
    void delete() {
        // given
        final ReservationTimeCreateCommand request = new ReservationTimeCreateCommand(LocalTime.of(11, 0));
        reservationTimeService.createReservationTime(request);
        // when
        reservationTimeService.deleteReservationTimeById(1L);
        // then
        assertThat(reservationTimeRepository.findById(1L)).isEmpty();
    }

    @DisplayName("예약이 존재하는 시간은 삭제할 경우 예외가 발생한다")
    @Test
    void should_ThrowException_WhenDeleteTimeWithinReservation() {
        // given
        final ReservationTimeCreateCommand request = new ReservationTimeCreateCommand(LocalTime.of(11, 0));
        final ReservationTimeInfo response = reservationTimeService.createReservationTime(request);
        final ReservationTime time = new ReservationTime(response.id(), response.startAt());
        final Theme theme = new Theme(1L, "우테코방탈출", "탈출탈출탈출", "abcdefg");
        final Member member = new Member(null, "레오", "admin@gmail.com", "qwer!", MemberRole.ADMIN);
        reservationRepository.save(new Reservation(null, member, LocalDate.now().plusDays(1), time, theme));
        // when
        // then
        assertThatThrownBy(() -> reservationTimeService.deleteReservationTimeById(response.id()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약이 존재하는 시간은 삭제할 수 없습니다.");
    }

    @DisplayName("예약 가능 시간을 조회할 수 있다.")
    @Test
    void findAvailableTimes() {
        // given
        final ReservationTime savedTime1 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        final ReservationTime savedTime2 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(15, 0)));
        final Theme theme = new Theme(1L, "우테코 탈출", "우테코 방탈출", "wwwwww");
        final Member member = new Member(null, "레오", "admin@gmail.com", "qwer!", MemberRole.ADMIN);
        final LocalDate date = LocalDate.of(2025, 5, 1);
        reservationRepository.save(new Reservation(1L, member, date, savedTime1, theme));
        // when
        final List<AvailableTimeInfo> result = reservationTimeService.findAvailableTimes(date, theme.getId());
        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).contains(
                        new AvailableTimeInfo(savedTime1.getId(), savedTime1.getStartAt(), true),
                        new AvailableTimeInfo(savedTime2.getId(), savedTime2.getStartAt(), false)
                )
        );
    }
}
