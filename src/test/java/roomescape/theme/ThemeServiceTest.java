package roomescape.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.then;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.custom.reason.theme.ThemeNotFoundException;
import roomescape.exception.custom.reason.theme.ThemeUsedException;
import roomescape.member.Member;
import roomescape.member.MemberRepositoryFacade;
import roomescape.member.MemberRepositoryFacadeImpl;
import roomescape.member.MemberRole;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationRepositoryFacade;
import roomescape.reservation.ReservationRepositoryFacadeImpl;
import roomescape.reservation.ReservationStatus;
import roomescape.reservationtime.ReservationTime;
import roomescape.reservationtime.ReservationTimeRepositoryFacade;
import roomescape.reservationtime.ReservationTimeRepositoryFacadeImpl;
import roomescape.theme.dto.ThemeRequest;
import roomescape.theme.dto.ThemeResponse;

@DataJpaTest
@Transactional(propagation = Propagation.SUPPORTS)
@Sql(scripts = "classpath:/initialize_database.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import({
        MemberRepositoryFacadeImpl.class,
        ReservationTimeRepositoryFacadeImpl.class,
        ReservationRepositoryFacadeImpl.class,
        ThemeRepositoryFacadeImpl.class,
        ThemeService.class
})
class ThemeServiceTest {

    @MockitoSpyBean
    private final ThemeRepositoryFacade themeRepositoryFacade;
    private final ThemeService themeService;

    private final ReservationTimeRepositoryFacade reservationTimeRepositoryFacade;
    private final MemberRepositoryFacade memberRepositoryFacade;
    private final ReservationRepositoryFacade reservationRepositoryFacade;

    @Autowired
    public ThemeServiceTest(
            final ThemeRepositoryFacade themeRepositoryFacade,
            final ThemeService themeService,

            final ReservationRepositoryFacade reservationRepositoryFacade,
            final ReservationTimeRepositoryFacade reservationTimeRepositoryFacade,
            final MemberRepositoryFacade memberRepositoryFacade
    ) {
        this.themeRepositoryFacade = themeRepositoryFacade;
        this.themeService = themeService;

        this.reservationRepositoryFacade = reservationRepositoryFacade;
        this.reservationTimeRepositoryFacade = reservationTimeRepositoryFacade;
        this.memberRepositoryFacade = memberRepositoryFacade;

    }

    @Nested
    @DisplayName("테마 생성")
    class Create {

        @DisplayName("테마를 생성한다.")
        @Test
        void create() {
            // given
            final ThemeRequest themeRequest = new ThemeRequest("로키", "로키로키", "http://www.google.com");
            final Theme theme = new Theme(1L, themeRequest.name(), themeRequest.description(),
                    themeRequest.thumbnail());
            final ThemeResponse expected = ThemeResponse.from(theme);

            // when
            final ThemeResponse actual = themeService.create(themeRequest);

            // then
            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("테마 모두 조회")
    class FindAll {

        @DisplayName("테마를 모두 조회한다.")
        @Test
        void findAll1() {
            // given
            final List<Theme> themes = List.of(
                    new Theme("로키1", "로키로키1", "http://www.google.com/1"),
                    new Theme("로키2", "로키로키2", "http://www.google.com/2"),
                    new Theme("로키3", "로키로키3", "http://www.google.com/3")
            );
            for (final Theme theme : themes) {
                themeRepositoryFacade.save(theme);
            }

            // when
            final List<ThemeResponse> actual = themeService.findAll();

            // then
            assertThat(actual)
                    .hasSize(3)
                    .extracting(ThemeResponse::id)
                    .contains(1L, 2L, 3L);
        }

        @DisplayName("테마가 없다면 빈 컬렉션을 반환한다.")
        @Test
        void findAll2() {
            // given & when
            final List<ThemeResponse> actual = themeService.findAll();

            // then
            assertThat(actual).isEmpty();
        }
    }

    @Nested
    @DisplayName("주어진 사이즈만큼 일주일간의 예약이 많은 순서대로 탑 랭크 조회")
    class FindTopRankThemes {

        @DisplayName("탑 랭크 조회")
        @Test
        void findTopRankThemes() {
            // given
            final Member member = new Member("email", "pass", "name", MemberRole.MEMBER);
            final ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 40));
            reservationTimeRepositoryFacade.save(reservationTime);
            memberRepositoryFacade.save(member);

            final List<Theme> themes = List.of(
                    new Theme("1", "2", "3"),
                    new Theme("1", "2", "3"),
                    new Theme("1", "2", "3")
            );
            final List<Reservation> reservations = List.of(
                    new Reservation(LocalDate.now().minusDays(1), member, reservationTime, themes.get(0),
                            ReservationStatus.PENDING),
                    new Reservation(LocalDate.now().minusDays(2), member, reservationTime, themes.get(0),
                            ReservationStatus.PENDING),
                    new Reservation(LocalDate.now().minusDays(3), member, reservationTime, themes.get(0),
                            ReservationStatus.PENDING),

                    new Reservation(LocalDate.now().minusDays(1), member, reservationTime, themes.get(1),
                            ReservationStatus.PENDING),
                    new Reservation(LocalDate.now().minusDays(2), member, reservationTime, themes.get(1),
                            ReservationStatus.PENDING),

                    new Reservation(LocalDate.now().minusDays(1), member, reservationTime, themes.get(2),
                            ReservationStatus.PENDING)
            );
            for (final Theme theme : themes) {
                themeRepositoryFacade.save(theme);
            }
            for(final Reservation reservation : reservations) {
                reservationRepositoryFacade.save(reservation);
            }

            // when
            final List<ThemeResponse> actual = themeService.findTopRank(3);

            // then
            assertThat(actual)
                    .hasSize(3)
                    .extracting(ThemeResponse::id)
                    .containsExactly(1L, 2L, 3L);
        }

    }

    @Nested
    @DisplayName("테마 삭제")
    class Delete {

        @DisplayName("주어진 id에 해당하는 테마를 삭제한다.")
        @Test
        void deleteById1() {
            // given
            final Theme theme = new Theme("로키", "로키로키", "http://www.google.com");
            themeRepositoryFacade.save(theme);

            // when
            themeService.deleteById(1L);

            // then
            then(themeRepositoryFacade).should().delete(theme);
        }

        @DisplayName("주어진 id에 해당하는 테마가 존재하지 않는다면 예외가 발생한다.")
        @Test
        void deleteById2() {
            // given
            final Long id = 1L;

            // when & then
            assertThatThrownBy(() -> {
                themeService.deleteById(id);
            }).isInstanceOf(ThemeNotFoundException.class);
        }

        @DisplayName("주어진 id에 해당하는 테마가 예약에서 사용중이라면 예외가 발생한다.")
        @Test
        void deleteById3() {
            // given
            final Theme theme = new Theme("로키", "로키로키", "http://www.google.com");
            final Member member = new Member("email", "pass", "name", MemberRole.MEMBER);
            final ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 40));
            final Reservation reservation = new Reservation(LocalDate.of(2026, 12, 29), member, reservationTime, theme,
                    ReservationStatus.PENDING);

            memberRepositoryFacade.save(member);
            reservationTimeRepositoryFacade.save(reservationTime);
            themeRepositoryFacade.save(theme);
            reservationRepositoryFacade.save(reservation);

            // when & then
            assertThatThrownBy(() -> {
                themeService.deleteById(1L);
            }).isInstanceOf(ThemeUsedException.class);
        }
    }
}
