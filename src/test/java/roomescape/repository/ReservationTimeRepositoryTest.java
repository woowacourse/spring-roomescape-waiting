package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;

@DataJpaTest
class ReservationTimeRepositoryTest {

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ThemeRepository themeRepository;

    @Autowired
    ReservationTimeRepository timeRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Test
    @DisplayName("해당 날짜와 테마에 예약 중인 시간 들을 조회 한다.")
    void findReservedTime() {
        // given
        Member member = memberRepository.save(new Member("seyang@test.com", "seyang", "Seyang"));
        Theme theme = themeRepository.save(new Theme("Theme 1", "Desc 1", "Thumb 1"));
        LocalDate date = LocalDate.now().plusDays(1);
        List<ReservationTime> times = Stream.of(
                        new ReservationTime("08:00"),
                        new ReservationTime("09:10"),
                        new ReservationTime("10:20"))
                .map(reservationTimeRepository::save)
                .toList();

        reservationRepository.save(new Reservation(member, theme, date, times.get(1)));

        // when
        Set<ReservationTime> actual = timeRepository.findReservedTime(date, theme.getId());
        ReservationTime expectedContains = times.get(1);

        // then
        assertThat(actual).containsExactly(expectedContains);
    }
}