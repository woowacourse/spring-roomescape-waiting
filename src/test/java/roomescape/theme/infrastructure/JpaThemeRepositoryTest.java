package roomescape.theme.infrastructure;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;
import roomescape.member.infrastructure.JpaMemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.reservation.infrastructure.JpaReservationRepository;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.infrastructure.JpaReservationTimeRepository;
import roomescape.theme.domain.Theme;

@DataJpaTest
class JpaThemeRepositoryTest {

    @Autowired
    private JpaThemeRepository jpaThemeRepository;

    @Autowired
    private JpaReservationRepository jpaReservationRepository;

    @Autowired
    private JpaReservationTimeRepository jpaReservationTimeRepository;

    @Autowired
    private JpaMemberRepository jpaMemberRepository;

    @Test
    @DisplayName("기간 별 인기테마를 갯수만큼 가져올 수 있다.")
    void can_find_popular_themes() {
        Theme theme = jpaThemeRepository.save(new Theme("테스트", "테스트", "테스트"));
        ReservationTime time = jpaReservationTimeRepository.save(new ReservationTime(LocalTime.of(9, 0)));
        Member member = jpaMemberRepository.save(
            new Member(new Name("율무"), new Email("test@email.com"), new Password("password"))
        );
        LocalDate now = LocalDate.now();
        jpaReservationRepository.save(new Reservation(now.minusDays(1), time, theme, member, Status.RESERVED));
        jpaReservationRepository.save(new Reservation(now.minusDays(2), time, theme, member, Status.RESERVED));
        jpaReservationRepository.save(new Reservation(now.minusDays(8), time, theme, member, Status.RESERVED));

        LocalDate start = LocalDate.now().minusDays(4);
        LocalDate end = LocalDate.now();
        int limit = 1;

        List<Theme> popularThemes = jpaThemeRepository.findPopularThemes(start, end, limit);

        System.out.println(theme.getId());
        Assertions.assertThat(popularThemes).containsExactly(theme);
    }
}
