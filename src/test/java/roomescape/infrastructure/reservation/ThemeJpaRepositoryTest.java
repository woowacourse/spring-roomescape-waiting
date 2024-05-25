package roomescape.infrastructure.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.fixture.MemberFixture.MEMBER_ARU;
import static roomescape.fixture.ThemeFixture.FANTASY_THEME;
import static roomescape.fixture.ThemeFixture.SCHOOL_THEME;
import static roomescape.fixture.ThemeFixture.SPOOKY_THEME;
import static roomescape.fixture.TimeFixture.ELEVEN_AM;
import static roomescape.fixture.TimeFixture.TEN_AM;
import static roomescape.fixture.TimeFixture.TWELVE_PM;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.BookStatus;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;

@DataJpaTest
class ThemeJpaRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    @DisplayName("주어진 날짜 사이에 확정된 예약을 기준으로 인기 테마를 반환한다.")
    void shouldReturnPopularThemes() {
        List<Theme> themes = List.of(SPOOKY_THEME.create(), FANTASY_THEME.create(), SCHOOL_THEME.create());
        List<ReservationTime> times = List.of(TEN_AM.create(), ELEVEN_AM.create(), TWELVE_PM.create());
        Member member = MEMBER_ARU.create();
        themes.forEach(entityManager::persist);
        times.forEach(entityManager::persist);
        entityManager.persist(member);

        LocalDate date = LocalDate.of(2024, 12, 1);
        LocalDateTime createdAt = date.minusDays(1).atStartOfDay();

        Stream.of(
                new Reservation(member, date.plusDays(0), times.get(0), themes.get(0), createdAt, BookStatus.BOOKED),
                new Reservation(member, date.plusDays(1), times.get(1), themes.get(1), createdAt, BookStatus.BOOKED),
                new Reservation(member, date.plusDays(1), times.get(0), themes.get(1), createdAt, BookStatus.BOOKED),
                new Reservation(member, date.plusDays(1), times.get(0), themes.get(2), createdAt, BookStatus.BOOKED),
                new Reservation(member, date.plusDays(2), times.get(1), themes.get(2), createdAt, BookStatus.BOOKED),
                new Reservation(member, date.plusDays(2), times.get(2), themes.get(2), createdAt, BookStatus.BOOKED),

                new Reservation(member, date.plusDays(2), times.get(0), themes.get(0), createdAt, BookStatus.BOOKING_CANCELLED),
                new Reservation(member, date.plusDays(2), times.get(1), themes.get(0), createdAt, BookStatus.BOOKING_CANCELLED),
                new Reservation(member, date.plusDays(2), times.get(2), themes.get(0), createdAt, BookStatus.BOOKING_CANCELLED),
                new Reservation(member, date.plusDays(3), times.get(0), themes.get(0), createdAt, BookStatus.BOOKED),
                new Reservation(member, date.plusDays(3), times.get(1), themes.get(0), createdAt, BookStatus.BOOKED),
                new Reservation(member, date.plusDays(3), times.get(0), themes.get(0), createdAt, BookStatus.BOOKED)
        ).forEach(entityManager::persist);

        int limit = 3;
        List<Theme> themeIds = themeRepository.findPopularThemesDateBetween(date, date.plusDays(2), limit);
        assertThat(themeIds).containsExactly(themes.get(2), themes.get(1), themes.get(0));
    }
}
