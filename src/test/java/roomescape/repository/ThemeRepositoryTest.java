package roomescape.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.user.Member;
import roomescape.fixture.MemberFixture;
import roomescape.fixture.ReservationTimeFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.util.DatabaseCleaner;
import roomescape.util.ReservationInserter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ThemeRepositoryTest {
    @Autowired
    ThemeRepository sut;
    @Autowired
    ReservationInserter reservationInserter;
    @Autowired
    DatabaseCleaner databaseCleaner;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @BeforeEach
    void setup() {
        databaseCleaner.initialize();
    }

    @Test
    void create() {
        final var result = sut.save(ThemeFixture.getDomain());
        assertThat(result).isNotNull();
    }

    @Test
    void findById() {
        final var id = sut.save(ThemeFixture.getDomain())
                .getId();
        final var result = sut.findById(id);
        assertThat(result).contains(ThemeFixture.getDomain());
    }

    @Test
    void delete() {
        final var theme = sut.save(ThemeFixture.getDomain());
        sut.delete(theme);
        final var result = sut.findById(theme.getId());
        assertThat(result).isNotPresent();
    }

    @Test
    void getAll() {
        sut.save(ThemeFixture.getDomain());
        sut.save(ThemeFixture.getDomain());

        final var result = sut.findAll();
        assertThat(result).hasSize(2);
    }

    @Test
    void getPopularTheme() {
        final Theme theme1 = Theme.of("공포", "진짜 공포임", "a.jpg");
        final Theme theme2 = Theme.of("감동", "실화", "b.jpg");
        final Theme theme3 = Theme.of("충격", "충충실화", "c.jpg");

        final Member member = memberRepository.save(MemberFixture.getDomain());
        final ReservationTime reservationTime = reservationTimeRepository.save(ReservationTimeFixture.getDomain());


        reservationInserter.addNewReservation("2024-10-03", theme1, member, reservationTime);
        reservationInserter.addNewReservation("2024-10-02", theme1, member, reservationTime);
        reservationInserter.addNewReservation("2024-10-02", theme2, member, reservationTime);
        reservationInserter.addNewReservation("2024-10-05", theme2, member, reservationTime);
        reservationInserter.addNewReservation("2024-10-03", theme3, member, reservationTime);
        reservationInserter.addNewReservation("2024-10-02", theme3, member, reservationTime);

        final List<Theme> themes = themeRepository.getPopularTheme("2024-10-02", "2024-10-04", 3);
        assertThat(themes).containsExactly(theme1, theme3, theme2);

    }
}
