package roomescape.theme.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import roomescape.fixture.MemberFixture;
import roomescape.fixture.ReservationTimeFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.model.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.model.Theme;
import roomescape.util.JpaRepositoryTest;

@JpaRepositoryTest
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("Theme 테이블에서 많이 예약된 테마 10개를 내림차순으로 가져온다.")
    void findOrderByReservation() {
        // given
        Member member = memberRepository.save(MemberFixture.getOne());
        ReservationTime reservationTime = reservationTimeRepository.save(ReservationTimeFixture.getOne());
        List<Theme> savedThemes = ThemeFixture.get(10).stream()
                .map(themeRepository::save)
                .toList();

        Theme topTheme = savedThemes.get(3);
        reservationRepository.save(new Reservation(null, member, LocalDate.parse("2025-12-12"), reservationTime, topTheme));
        reservationRepository.save(new Reservation(null, member, LocalDate.parse("2025-02-12"), reservationTime, topTheme));

        Theme secondTheme = savedThemes.get(0);
        reservationRepository.save(new Reservation(null, member, LocalDate.parse("2025-12-12"), reservationTime, secondTheme));

        // when
        List<Theme> orderByReservations = themeRepository.findAllOrderByReservationCount(Pageable.ofSize(10));

        // then
        assertAll(
                () -> assertThat(orderByReservations).hasSize(10),
                () -> assertThat(orderByReservations.get(0)).isEqualTo(topTheme),
                () -> assertThat(orderByReservations.get(1)).isEqualTo(secondTheme)
        );
    }
}
