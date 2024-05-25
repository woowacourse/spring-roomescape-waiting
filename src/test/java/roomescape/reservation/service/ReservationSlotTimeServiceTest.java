package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.fixture.MemberFixture.getMemberChoco;
import static roomescape.fixture.ReservationSlotFixture.getNextDayReservationSlot;
import static roomescape.fixture.ReservationTimeFixture.get1PM;
import static roomescape.fixture.ReservationTimeFixture.get2PM;
import static roomescape.fixture.ReservationTimeFixture.getNoon;
import static roomescape.fixture.ThemeFixture.getTheme1;

import java.time.*;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.exception.custom.BadRequestException;
import roomescape.exception.custom.ForbiddenException;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.reservation.controller.dto.AvailableTimeResponse;
import roomescape.reservation.controller.dto.ReservationTimeRequest;
import roomescape.reservation.controller.dto.ReservationTimeResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationSlotRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.reservation.domain.repository.ThemeRepository;
import roomescape.util.ServiceTest;

@DisplayName("예약 시간 로직 테스트")
class ReservationSlotTimeServiceTest extends ServiceTest {
    @Autowired
    ReservationSlotRepository reservationSlotRepository;
    @Autowired
    ReservationTimeRepository reservationTimeRepository;
    @Autowired
    ThemeRepository themeRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    ReservationTimeService reservationTimeService;

    @DisplayName("예약 시간 생성에 성공한다.")
    @Test
    void create() {
        //given
        String localTime = "11:00";
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(localTime);

        //when
        ReservationTimeResponse reservationTimeResponse = reservationTimeService.create(reservationTimeRequest);

        //then
        assertThat(reservationTimeResponse.startAt()).isEqualTo(localTime);
    }

    @DisplayName("예약 시간 조회에 성공한다.")
    @Test
    void findAll() {
        //given
        ReservationTime saved = reservationTimeRepository.save(getNoon());

        //when
        List<ReservationTimeResponse> reservationTimes = reservationTimeService.findAll();

        //then
        assertThat(reservationTimes).extracting(ReservationTimeResponse::startAt).contains(getNoon().getStartAt());
    }

    @DisplayName("예약 시간 삭제에 성공한다.")
    @Test
    void delete() {
        //given
        ReservationTime time = reservationTimeRepository.save(getNoon());
        int beforeSize = reservationTimeRepository.findAll().size();
        //when
        reservationTimeService.delete(time.getId());

        //then
        assertThat(reservationTimeRepository.findAll().size()).isEqualTo(beforeSize - 1);
    }

    @DisplayName("예약이 존재하는 예약 시간을 삭제할 경우 예외가 발생한다.")
    @Test
    void deleteTimeWithReservation() {
        //given
        ReservationTime time = reservationTimeRepository.save(getNoon());
        Theme theme = themeRepository.save(getTheme1());
        ReservationSlot reservationSlot = reservationSlotRepository.save(getNextDayReservationSlot(time, theme));
        //when & then
        assertThatThrownBy(() -> reservationTimeService.delete(getNoon().getId()))
                .isInstanceOf(BadRequestException.class);
    }

    @DisplayName("예약 시간이 증복일 경우, 예외가 발생한다.")
    @Test
    void duplicatedTime() {
        //given
        String localTime = "11:00";
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(localTime);
        reservationTimeRepository.save(new ReservationTime(LocalTime.parse(localTime)));

        //when & then
        assertThatThrownBy(() -> reservationTimeService.create(reservationTimeRequest))
                .isInstanceOf(ForbiddenException.class);
    }

    @DisplayName("예약 가능한 시간 조회에 성공한다.")
    @Test
    void findAvailableTime() {
        //given
        ReservationTime time = reservationTimeRepository.save(getNoon());
        reservationTimeRepository.save(get1PM());
        reservationTimeRepository.save(get2PM());
        Theme theme = themeRepository.save(getTheme1());
        ReservationSlot reservationSlot = reservationSlotRepository.save(getNextDayReservationSlot(time, theme));
        Member member = memberRepository.save(getMemberChoco());
        reservationRepository.save(new Reservation(member, reservationSlot));

        //when
        List<AvailableTimeResponse> availableTimes
                = reservationTimeService.findAvailableTimes(reservationSlot.getDate(), theme.getId());

        //then
        long count = availableTimes.stream()
                .filter(availableTimeResponse -> !availableTimeResponse.alreadyBooked()).count();
        long expectedCount = reservationTimeService.findAll().size() -
                reservationTimeRepository.findReservedTime(reservationSlot.getDate(), theme.getId()).size();

        assertThat(count)
                .isEqualTo(expectedCount);
    }

    @DisplayName("")
    @Test
    void asd() {
        //given
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        System.out.println("now = " + now);

        //when

        //then
    }

}
