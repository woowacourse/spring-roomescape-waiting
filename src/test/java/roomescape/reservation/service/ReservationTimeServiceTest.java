package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.config.DatabaseCleaner;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.dto.response.AvailableReservationTimeResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ReservationTimeServiceTest {

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeService reservationTimeService;

    @AfterEach
    void init() {
        databaseCleaner.cleanUp();
    }

    @Test
    @DisplayName("예약 시간 아이디로 조회 시 존재하지 않는 아이디면 예외가 발생한다.")
    void findByIdExceptionTest() {
        assertThatThrownBy(() -> reservationTimeService.findById(1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("예약 가능한 시간을 조회한다.")
    void findAvailableTimesTest() {
        ReservationTime reservationTime1 = reservationTimeRepository.save(new ReservationTime(LocalTime.parse("10:00")));
        ReservationTime reservationTime2 = reservationTimeRepository.save(new ReservationTime(LocalTime.parse("11:00")));
        Theme theme = themeRepository.save(new Theme("공포", "무서운 테마", "https://i.pinimg.com/236x.jpg"));
        Member member = memberRepository.save(new Member(1L, Role.MEMBER, "카키", "kaki@email.com", "1234"));
        Reservation reservation = new Reservation(member, LocalDate.now(), theme, reservationTime1);
        reservationRepository.save(reservation);

        List<AvailableReservationTimeResponse> availableTimes = reservationTimeService.findAvailableTimes(
                reservation.getDate(), theme.getId());

        assertThat(availableTimes).containsExactly(
                new AvailableReservationTimeResponse(reservationTime1, true),
                new AvailableReservationTimeResponse(reservationTime2, false)
        );
    }

    @Test
    @DisplayName("이미 해당 시간으로 예약 되있을 경우 삭제 시 예외가 발생한다.")
    void deleteExceptionTest() {
        Theme theme = themeRepository.save(new Theme("공포", "호러 방탈출", "http://asdf.jpg"));
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.now()));
        Member member = memberRepository.save(new Member(1L, Role.MEMBER, "카키", "kaki@email.com", "1234"));
        Reservation reservation = new Reservation(member, LocalDate.now().plusDays(1), theme, reservationTime);
        reservationRepository.save(reservation);

        assertThatThrownBy(() -> reservationTimeService.delete(reservationTime.getId()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
