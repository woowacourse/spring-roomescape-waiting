package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.fixture.MemberFixture.getMemberChoco;
import static roomescape.fixture.ReservationFixture.getNextDayReservation;
import static roomescape.fixture.ReservationTimeFixture.get1PM;
import static roomescape.fixture.ReservationTimeFixture.get2PM;
import static roomescape.fixture.ReservationTimeFixture.getNoon;
import static roomescape.fixture.ThemeFixture.getTheme1;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.exception.BadRequestException;
import roomescape.exception.ErrorType;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.reservation.controller.dto.AvailableTimeResponse;
import roomescape.reservation.controller.dto.ReservationTimeResponse;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.repository.MemberReservationRepository;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.reservation.domain.repository.ThemeRepository;
import roomescape.reservation.service.dto.ReservationTimeCreate;
import roomescape.util.ServiceTest;

@DisplayName("예약 시간 로직 테스트")
class ReservationTimeServiceTest extends ServiceTest {
    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    ReservationTimeRepository reservationTimeRepository;
    @Autowired
    ThemeRepository themeRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    MemberReservationRepository memberReservationRepository;
    @Autowired
    ReservationTimeService reservationTimeService;

    @DisplayName("예약 시간 생성에 성공한다.")
    @Test
    void create() {
        //given
        ReservationTimeCreate reservationTimeCreate = new ReservationTimeCreate(LocalTime.NOON);

        //when
        ReservationTimeResponse reservationTimeResponse = reservationTimeService.create(reservationTimeCreate);

        //then
        assertThat(reservationTimeResponse.startAt()).isEqualTo(LocalTime.NOON.toString());
        assertThat(reservationTimeRepository.findAll()).hasSize(1);
    }

    @DisplayName("예약 시간 조회에 성공한다.")
    @Test
    void findAll() {
        //given
        reservationTimeRepository.save(getNoon());

        //when
        List<ReservationTimeResponse> reservationTimes = reservationTimeService.findAll();

        //then
        assertThat(reservationTimes).hasSize(1);
    }

    @DisplayName("예약 시간 삭제에 성공한다.")
    @Test
    void delete() {
        //given
        ReservationTime time = reservationTimeRepository.save(getNoon());

        //when
        reservationTimeService.delete(time.getId());

        //then
        assertThat(reservationTimeRepository.findAll()).hasSize(0);
    }

    @DisplayName("예약이 존재하는 예약 시간을 삭제할 경우 예외가 발생한다.")
    @Test
    void deleteTimeWithReservation() {
        //given
        ReservationTime time = reservationTimeRepository.save(getNoon());
        Theme theme = themeRepository.save(getTheme1());
        Reservation reservation = reservationRepository.save(getNextDayReservation(time, theme));

        //when & then
        assertThatThrownBy(() -> reservationTimeService.delete(reservation.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(ErrorType.RESERVATION_NOT_DELETED.getMessage());
    }

    @DisplayName("예약 시간이 증복일 경우, 예외가 발생한다.")
    @Test
    void duplicatedTime() {
        //given
        ReservationTimeCreate reservationTimeCreate = new ReservationTimeCreate(LocalTime.NOON);
        reservationTimeRepository.save(new ReservationTime(LocalTime.NOON));

        //when & then
        assertThatThrownBy(() -> reservationTimeService.create(reservationTimeCreate))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(ErrorType.DUPLICATED_RESERVATION_TIME_ERROR.getMessage());
    }

    @DisplayName("예약 가능한 시간 조회에 성공한다.")
    @Test
    void findAvailableTime() {
        //given
        ReservationTime time = reservationTimeRepository.save(getNoon());
        reservationTimeRepository.save(get1PM());
        reservationTimeRepository.save(get2PM());
        Theme theme = themeRepository.save(getTheme1());
        Reservation reservation = reservationRepository.save(getNextDayReservation(time, theme));
        Member member = memberRepository.save(getMemberChoco());
        memberReservationRepository.save(new MemberReservation(member, reservation, ReservationStatus.APPROVED));

        //when
        List<AvailableTimeResponse> availableTimes
                = reservationTimeService.findAvailableTimes(reservation.getDate(), theme.getId());

        //then
        long count = availableTimes.stream()
                .filter(availableTimeResponse -> !availableTimeResponse.alreadyBooked()).count();
        long expectedCount = reservationTimeService.findAll().size() -
                reservationTimeRepository.findReservedTime(reservation.getDate(), theme.getId()).size();

        assertThat(count)
                .isEqualTo(expectedCount);
    }
}
