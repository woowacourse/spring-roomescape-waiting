package roomescape.service.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import groovyjarjarantlr4.v4.gui.Trees;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Schedule;
import roomescape.domain.reservation.ScheduleRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;
import roomescape.exception.InvalidReservationException;
import roomescape.service.reservation.dto.AvailableReservationTimeResponse;
import roomescape.service.reservation.dto.ReservationTimeCreateRequest;
import roomescape.service.reservation.dto.ReservationTimeReadRequest;
import roomescape.service.reservation.dto.ReservationTimeResponse;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Sql(scripts = {"classpath:truncate-with-guests.sql"})
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
    @Autowired
    private ScheduleRepository scheduleRepository;

    @DisplayName("새로운 예약 시간을 저장한다.")
    @Test
    void create() {
        //given
        LocalTime startAt = LocalTime.now();
        ReservationTimeCreateRequest reservationTimeCreateRequest = new ReservationTimeCreateRequest(startAt);

        //when
        ReservationTimeResponse result = reservationTimeService.create(reservationTimeCreateRequest);

        //then
        assertAll(
                () -> assertThat(result.id()).isNotZero(),
                () -> assertThat(result.startAt()).isEqualTo(startAt)
        );
    }

    @DisplayName("모든 예약 시간 내역을 조회한다.")
    @Test
    void findAll() {
        //given
        reservationTimeRepository.save(new ReservationTime(LocalTime.now()));

        //when
        List<ReservationTimeResponse> reservationTimes = reservationTimeService.findAll();

        //then
        assertThat(reservationTimes).hasSize(1);
    }

    @DisplayName("시간이 이미 존재하면 예외를 발생시킨다.")
    @Test
    void duplicatedTime() {
        //given
        LocalTime time = LocalTime.now();
        reservationTimeRepository.save(new ReservationTime(time));

        ReservationTimeCreateRequest reservationTimeCreateRequest = new ReservationTimeCreateRequest(time);

        //when&then
        assertThatThrownBy(() -> reservationTimeService.create(reservationTimeCreateRequest))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessage("이미 같은 시간이 존재합니다.");
    }

    @DisplayName("예약이 존재하는 시간으로 삭제를 시도하면 예외를 발생시킨다.")
    @Test
    void cannotDeleteTime() {
        //given
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.now()));
        Theme theme = themeRepository.save(new Theme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"));
        Member member = memberRepository.save(new Member("lily", "lily@email.com", "lily123", Role.GUEST));
        Schedule schedule = scheduleRepository.save(new Schedule(ReservationDate.of(LocalDate.MAX), reservationTime));
        Reservation reservation = new Reservation(member, schedule, theme);
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
        //given
        LocalDate date = LocalDate.MAX;
        LocalTime time = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
        ReservationTime bookedReservationTime = reservationTimeRepository.save(new ReservationTime(time));
        ReservationTime notBookedReservationTime = reservationTimeRepository.save(new ReservationTime(time.plusHours(5)));
        Theme theme = themeRepository.save(new Theme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"));
        Member member = memberRepository.save(new Member("lily", "lily@email.com", "lily123", Role.GUEST));
        Schedule schedule = scheduleRepository.save(new Schedule(ReservationDate.of(date), bookedReservationTime));
        Reservation reservation = new Reservation(member, schedule, theme);
        reservationRepository.save(reservation);

        //when
        List<AvailableReservationTimeResponse> result = reservationTimeService.findAvailableTimes(
                new ReservationTimeReadRequest(date, theme.getId()));

        //then
        boolean isBookedOfBookedTime = result.stream().filter(bookedTime -> bookedTime.id() == bookedReservationTime.getId())
                .findFirst().get().alreadyBooked();
        boolean isBookedOfUnBookedTime = result.stream().filter(unbookedTime -> unbookedTime.id() == notBookedReservationTime.getId())
                .findFirst().get().alreadyBooked();
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(isBookedOfUnBookedTime).isFalse(),
                () -> assertThat(isBookedOfBookedTime).isTrue()
        );
    }
}
