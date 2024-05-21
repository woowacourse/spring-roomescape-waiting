package roomescape.service.reservationtime;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberPassword;
import roomescape.domain.member.Role;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.BaseServiceTest;

class ReservationTimeFindServiceTest extends BaseServiceTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeFindService reservationTimeFindService;

    @BeforeEach
    void setUp() {
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        themeRepository.save(new Theme("방탈출 1", "1번 방탈출", "썸네일 1"));
        memberRepository.save(new Member(new MemberName("사용자1"),
                new MemberEmail("user1@wooteco.com"),
                new MemberPassword("1234"),
                Role.USER));
    }

    @Test
    @DisplayName("날짜와 테마가 주어지면 각 시간의 예약 여부를 구한다.")
    void findAvailabilityByDateAndTheme() {
        reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        Member member = memberRepository.findById(1L).get();
        ReservationTime time = reservationTimeRepository.findById(1L).get();
        Theme theme = themeRepository.findById(1L).get();
        LocalDate date = LocalDate.now().plusDays(1L);
        reservationRepository.save(new Reservation(member, date, time, theme));

        List<ReservationStatus> reservationStatuses = reservationTimeFindService.findReservationStatuses(date, 1L)
                .getReservationStatuses();
        Assertions.assertAll(
                () -> assertThat(reservationStatuses.size()).isEqualTo(2),
                () -> assertThat(reservationStatuses.get(0).isBooked()).isTrue(),
                () -> assertThat(reservationStatuses.get(1).isBooked()).isFalse()
        );
    }
}
