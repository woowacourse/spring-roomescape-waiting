package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.service.dto.AvailableTimeInfo;
import roomescape.reservation.service.dto.ReservationTimeCreateCommand;
import roomescape.reservation.service.dto.ReservationTimeInfo;
import roomescape.fake.FakeReservationRepository;
import roomescape.fake.FakeReservationTimeRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;

class ReservationTimeServiceTest {

    private ReservationTimeRepository timeRepository;
    private ReservationRepository reservationRepository;
    private ReservationTimeService reservationTimeService;
    private LocalTime time;
    private ReservationTimeCreateCommand createCommand;
    private Theme theme;
    private Member member;

    @BeforeEach
    void setUp() {
        timeRepository = new FakeReservationTimeRepository();
        reservationRepository = new FakeReservationRepository();
        reservationTimeService = new ReservationTimeService(timeRepository, reservationRepository);
        time = LocalTime.of(11, 0);
        createCommand = new ReservationTimeCreateCommand(time);
        theme = new Theme(1L, "우테코 탈출", "우테코 방탈출", "wwwwww");
        member = new Member(1L, "레오", "admin@gmail.com", "qwer!", MemberRole.ADMIN);
    }


    @DisplayName("이미 존재하는 시간을 저장할 경우 예외가 발생한다")
    @Test
    void should_ThrowException_WhenCreateDuplicateTime() {
        // given
        reservationTimeService.createReservationTime(createCommand);

        // when
        // then
        assertThatThrownBy(() -> reservationTimeService.createReservationTime(createCommand))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 시간입니다.");
    }

    @DisplayName("예약 시간을 저장할 수 있다")
    @Test
    void create() {
        // when
        ReservationTimeInfo result = reservationTimeService.createReservationTime(createCommand);

        // then
        ReservationTime savedTime = timeRepository.findById(result.id()).get();
        assertAll(
                () -> assertThat(result.startAt()).isEqualTo(time),
                () -> assertThat(savedTime.getStartAt()).isEqualTo(time)
        );
    }

    @DisplayName("예약 시간 목록을 조회할 수 있다")
    @Test
    void findAll() {
        // given
        reservationTimeService.createReservationTime(createCommand);
        reservationTimeService.createReservationTime(new ReservationTimeCreateCommand(LocalTime.of(12, 0)));

        // when
        List<ReservationTimeInfo> result = reservationTimeService.getReservationTimes();

        // then
        assertThat(result).hasSize(2);
    }

    @DisplayName("예약 시간을 삭제할 수 있다")
    @Test
    void delete() {
        // given
        reservationTimeService.createReservationTime(createCommand);

        // when
        reservationTimeService.deleteReservationTimeById(1L);

        // then
        assertThat(timeRepository.findById(1L)).isEmpty();
    }

    @DisplayName("예약이 존재하는 시간은 삭제할 경우 예외가 발생한다")
    @Test
    void should_ThrowException_WhenDeleteTimeWithinReservation() {
        // given
        ReservationTime reservationTime = new ReservationTime(null, time);
        ReservationTime savedTime = timeRepository.save(reservationTime);
        reservationRepository.save(new Reservation(null, member, LocalDate.now(), savedTime, theme));

        // when
        // then
        assertThatThrownBy(() -> reservationTimeService.deleteReservationTimeById(savedTime.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약이 존재하는 시간은 삭제할 수 없습니다.");
    }

    @DisplayName("예약 가능 시간을 조회할 수 있다.")
    @Test
    void findAvailableTimes() {
        // given
        ReservationTime savedTime1 = timeRepository.save(new ReservationTime(time));
        ReservationTime savedTime2 = timeRepository.save(new ReservationTime(LocalTime.of(15, 0)));
        LocalDate date = LocalDate.of(2025, 5, 1);
        reservationRepository.save(new Reservation(1L, member, date, savedTime1, theme));

        // when
        List<AvailableTimeInfo> result = reservationTimeService.findAvailableTimes(date, theme.getId());

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
