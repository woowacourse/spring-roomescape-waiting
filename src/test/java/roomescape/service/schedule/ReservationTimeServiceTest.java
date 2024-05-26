package roomescape.service.schedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import roomescape.Fixture;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.schedule.ReservationDate;
import roomescape.domain.schedule.ReservationTime;
import roomescape.domain.schedule.ReservationTimeRepository;
import roomescape.domain.schedule.Schedule;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.exception.InvalidReservationException;
import roomescape.service.schedule.dto.AvailableReservationTimeResponse;
import roomescape.service.schedule.dto.ReservationTimeCreateRequest;
import roomescape.service.schedule.dto.ReservationTimeReadRequest;
import roomescape.service.schedule.dto.ReservationTimeResponse;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Sql("/truncate.sql")
class ReservationTimeServiceTest {
    @Autowired
    private ReservationTimeService reservationTimeService;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("새로운 예약 시간을 저장한다.")
    @Test
    void create() {
        // given
        ReservationTimeCreateRequest reservationTimeCreateRequest = new ReservationTimeCreateRequest(Fixture.currentTime);

        // when
        ReservationTimeResponse result = reservationTimeService.create(reservationTimeCreateRequest);

        // then
        assertAll(
                () -> assertThat(result.id()).isNotZero(),
                () -> assertThat(result.startAt()).isEqualTo(Fixture.currentTime)
        );
    }

    @DisplayName("모든 예약 시간 내역을 조회한다.")
    @Test
    void findAll() {
        //given
        reservationTimeRepository.save(Fixture.reservationTime);

        //when
        List<ReservationTimeResponse> reservationTimes = reservationTimeService.findAll();

        //then
        assertThat(reservationTimes).hasSize(1);
    }

    @DisplayName("시간이 이미 존재하면 예외를 발생시킨다.")
    @Test
    void duplicatedTime() {
        // given
        LocalTime time = Fixture.currentTime;
        reservationTimeRepository.save(new ReservationTime(time));

        // when & then
        ReservationTimeCreateRequest reservationTimeCreateRequest = new ReservationTimeCreateRequest(time);
        assertThatThrownBy(() -> reservationTimeService.create(reservationTimeCreateRequest))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessage("이미 같은 시간이 존재합니다.");
    }

    @DisplayName("예약이 존재하는 시간으로 삭제를 시도하면 예외를 발생시킨다.")
    @Test
    void cannotDeleteTime() {
        //given
        ReservationTime reservationTime = reservationTimeRepository.save(Fixture.reservationTime);
        Theme theme = themeRepository.save(Fixture.theme);
        Member member = memberRepository.save(Fixture.member);
        Schedule schedule = new Schedule(ReservationDate.of(LocalDate.MAX), reservationTime);
        Reservation reservation = new Reservation(member, schedule, theme, ReservationStatus.RESERVED);
        reservationRepository.save(reservation);

        //when&then
        long timeId = reservationTime.getId();
        assertThatThrownBy(() -> reservationTimeService.deleteById(timeId))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessage("해당 시간에 예약이 존재해서 삭제할 수 없습니다.");
    }

    @DisplayName("해당 테마와 날짜에 예약이 가능한 시간 목록을 조회한다.")
    @Test
    void findAvailableTimes() {
        // given
        LocalDate date = LocalDate.MAX;
        LocalTime time = Fixture.currentTime;
        LocalTime timeAfter5hour = time.plusHours(5);

        ReservationTime bookedReservationTime = reservationTimeRepository.save(new ReservationTime(time));
        ReservationTime notBookedReservationTime = reservationTimeRepository.save(new ReservationTime(timeAfter5hour));
        Schedule schedule = new Schedule(ReservationDate.of(date), bookedReservationTime);

        Theme theme = themeRepository.save(Fixture.theme);
        Member member = memberRepository.save(Fixture.member);
        Reservation reservation = new Reservation(member, schedule, theme, ReservationStatus.RESERVED);
        reservationRepository.save(reservation);

        // when
        List<AvailableReservationTimeResponse> result = reservationTimeService.findAvailableTimes(
                new ReservationTimeReadRequest(date, theme.getId())
        );

        // then
        boolean isBookedOfBookedTime = result.stream()
                .filter(bookedTime -> bookedTime.id() == bookedReservationTime.getId())
                .findFirst().get().alreadyBooked();
        boolean isBookedOfUnBookedTime = result.stream()
                .filter(unbookedTime -> unbookedTime.id() == notBookedReservationTime.getId())
                .findFirst().get().alreadyBooked();

        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(result).hasSize(2);
        assertions.assertThat(isBookedOfUnBookedTime).isFalse();
        assertions.assertThat(isBookedOfBookedTime).isTrue();
        assertions.assertAll();
    }
}
