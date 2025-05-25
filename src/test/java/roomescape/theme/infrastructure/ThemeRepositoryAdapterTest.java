package roomescape.theme.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import roomescape.fixture.MemberFixture;
import roomescape.fixture.ReservationSpecFixture;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSpec;
import roomescape.reservation.infrastructure.ReservationRepositoryAdapter;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@ActiveProfiles("test")
@DataJpaTest
@Import({ThemeRepositoryAdapter.class, ReservationRepositoryAdapter.class})
class ThemeRepositoryAdapterTest {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ThemeRepositoryAdapter themeRepositoryAdapter;

    @Autowired
    private ReservationRepositoryAdapter reservationRepositoryAdapter;

    @DisplayName("테마를 저장한다")
    @Test
    void save() {
        // given
        String name = "무서운방";
        String description = "덜덜";
        String thumbnail = "무서운 사진";
        Theme theme = new Theme(name, description, thumbnail);

        // when
        Theme savedTheme = themeRepositoryAdapter.save(theme);

        // then
        assertThat(savedTheme.getId()).isNotNull();
        assertThat(savedTheme.getName()).isEqualTo(name);
        assertThat(savedTheme.getDescription()).isEqualTo(description);
        assertThat(savedTheme.getThumbnail()).isEqualTo(thumbnail);
    }

    @DisplayName("ID로 테마를 삭제한다")
    @Test
    void deleteById() {
        // given
        Theme theme = new Theme("테마", "테마 설명", "thumbnail.jpg");
        Theme savedTheme = themeRepositoryAdapter.save(theme);
        Long themeId = savedTheme.getId();

        // when
        themeRepositoryAdapter.deleteById(themeId);

        // then
        Optional<Theme> foundTheme = themeRepositoryAdapter.findById(themeId);
        assertThat(foundTheme).isEmpty();
    }

    @DisplayName("모든 테마를 조회한다")
    @Test
    void findAll() {
        // given
        Theme theme1 = new Theme("테마1", "테마 설명1", "thumbnail1.jpg");
        themeRepositoryAdapter.save(theme1);

        Theme theme2 = new Theme("테마2", "테마 설명2", "thumbnail2.jpg");
        themeRepositoryAdapter.save(theme2);

        // when
        List<Theme> themes = themeRepositoryAdapter.findAll();

        // then
        assertThat(themes).hasSize(2);
    }

    @DisplayName("ID로 테마를 조회한다")
    @Test
    void findById() {
        // given
        Theme theme = new Theme("테마", "테마 설명", "thumbnail.jpg");
        Theme savedTheme = themeRepositoryAdapter.save(theme);

        // when
        Optional<Theme> foundTheme = themeRepositoryAdapter.findById(savedTheme.getId());

        // then
        assertThat(foundTheme).isPresent();
        assertThat(foundTheme.get().getId()).isEqualTo(savedTheme.getId());
        assertThat(foundTheme.get().getName()).isEqualTo(theme.getName());
        assertThat(foundTheme.get().getDescription()).isEqualTo(theme.getDescription());
        assertThat(foundTheme.get().getThumbnail()).isEqualTo(theme.getThumbnail());
    }

    @DisplayName("이름으로 테마 존재 여부를 확인한다")
    @Test
    void existsByName() {
        // given
        String name = "테마";
        Theme theme = new Theme(name, "테마 설명", "thumbnail.jpg");
        themeRepositoryAdapter.save(theme);

        // when
        boolean exists = themeRepositoryAdapter.existsByName(name);

        // then
        assertThat(exists).isTrue();
    }

    @DisplayName("ID로 테마 존재 여부를 확인한다")
    @Test
    void existsById() {
        // given
        Theme theme = new Theme("테마", "테마 설명", "thumbnail.jpg");
        Theme savedTheme = themeRepositoryAdapter.save(theme);

        // when
        boolean exists = themeRepositoryAdapter.existsById(savedTheme.getId());

        // then
        assertThat(exists).isTrue();
    }

    @DisplayName("기간 내 예약이 많은 순으로 테마를 조회한다")
    @Test
    void findRankedByPeriod() {
        // given
        Member member = MemberFixture.createMember("에드", "test@test.com", "1234");
        entityManager.persist(member);

        ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));
        entityManager.persist(reservationTime);

        Theme theme1 = new Theme("테마1", "테마 설명1", "thumbnail1.jpg");
        themeRepositoryAdapter.save(theme1);

        Theme theme2 = new Theme("테마2", "테마 설명2", "thumbnail2.jpg");
        themeRepositoryAdapter.save(theme2);

        LocalDate today = LocalDate.now();

        // theme1에 예약 2개 생성
        ReservationSpec spec1 = ReservationSpecFixture.createSpec(today, reservationTime, theme1);
        Reservation reservation1 = new Reservation(member, spec1);
        reservationRepositoryAdapter.save(reservation1);

        ReservationSpec spec2 = ReservationSpecFixture.createSpec(today.plusDays(1), reservationTime, theme1);
        Reservation reservation2 = new Reservation(member, spec2);
        reservationRepositoryAdapter.save(reservation2);

        // theme2에 예약 1개 생성
        ReservationSpec spec3 = ReservationSpecFixture.createSpec(today, reservationTime, theme2);
        Reservation reservation3 = new Reservation(member, spec3);
        reservationRepositoryAdapter.save(reservation3);

        // when
        List<Theme> rankedThemes = themeRepositoryAdapter.findRankedByPeriod(today, today.plusDays(1), 2);

        // then
        assertThat(rankedThemes).hasSize(2);
        assertThat(rankedThemes.get(0).getId()).isEqualTo(theme1.getId());
        assertThat(rankedThemes.get(1).getId()).isEqualTo(theme2.getId());
    }
}
