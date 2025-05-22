package roomescape.theme.repository.jpa;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.repository.jpa.JpaMemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.jpa.JpaReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.jpa.JpaReservationTimeRepository;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@ActiveProfiles("test")
@DataJpaTest
class JpaThemeRepositoryTest {

    @Autowired
    private JpaReservationRepository jpaReservationRepository;

    @Autowired
    private JpaReservationTimeRepository jpaReservationTimeRepository;

    @Autowired
    private JpaThemeRepository jpaThemeRepository;

    @Autowired
    private JpaMemberRepository jpaMemberRepository;

    @Test
    void 테마_생성_확인() {
        Theme savedTheme = jpaThemeRepository.save(new Theme(null, "test theme", "test description", "test thumbnail"));
        assertThatCode(() -> jpaThemeRepository.findById(savedTheme.getId())).doesNotThrowAnyException();
    }

    @Test
    void 예약이_많은_테마_순으로_N개_확인() {
        jpaReservationRepository.saveAll(createReservations());

        List<Theme> findThemes = jpaThemeRepository.findTopByReservationCountDesc(
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(4),
                10
        );

        assertThat(findThemes).hasSize(3);
        assertThat(findThemes.get(0).getName()).isEqualTo("test theme 1");
        assertThat(findThemes.get(1).getName()).isEqualTo("test theme 2");
        assertThat(findThemes.get(2).getName()).isEqualTo("test theme 3");

    }

    private List<Reservation> createReservations() {
        Member member = jpaMemberRepository.save(new Member(null, "test", "test@test.com", MemberRole.USER, "testpassword"));
        ReservationTime time = jpaReservationTimeRepository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        Theme theme1 = jpaThemeRepository.save(new Theme(null, "test theme 1", "test description 1", "test thumbnail 1"));
        Theme theme2 = jpaThemeRepository.save(new Theme(null, "test theme 2", "test description 2", "test thumbnail 2"));
        Theme theme3 = jpaThemeRepository.save(new Theme(null, "test theme 3", "test description 3", "test thumbnail 3"));
        return List.of(
                new Reservation(null, LocalDate.now().plusDays(1), time, theme1, member),
                new Reservation(null, LocalDate.now().plusDays(1), time, theme1, member),
                new Reservation(null, LocalDate.now().plusDays(1), time, theme1, member),
                new Reservation(null, LocalDate.now().plusDays(2), time, theme1, member),
                new Reservation(null, LocalDate.now().plusDays(1), time, theme2, member),
                new Reservation(null, LocalDate.now().plusDays(2), time, theme2, member),
                new Reservation(null, LocalDate.now().plusDays(3), time, theme2, member),
                new Reservation(null, LocalDate.now().plusDays(2), time, theme3, member),
                new Reservation(null, LocalDate.now().plusDays(3), time, theme3, member)
        );
    }
}
