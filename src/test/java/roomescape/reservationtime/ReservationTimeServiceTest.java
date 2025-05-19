package roomescape.reservationtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.spy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.custom.reason.reservationtime.ReservationTimeConflictException;
import roomescape.exception.custom.reason.reservationtime.ReservationTimeNotFoundException;
import roomescape.exception.custom.reason.reservationtime.ReservationTimeUsedException;
import roomescape.member.Member;
import roomescape.member.MemberRepositoryFacade;
import roomescape.member.MemberRepositoryFacadeImpl;
import roomescape.member.MemberRole;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationRepository;
import roomescape.reservation.ReservationStatus;
import roomescape.reservationtime.dto.AvailableReservationTimeResponse;
import roomescape.reservationtime.dto.ReservationTimeRequest;
import roomescape.reservationtime.dto.ReservationTimeResponse;
import roomescape.theme.Theme;
import roomescape.theme.ThemeRepositoryFacade;
import roomescape.theme.ThemeRepositoryFacadeImpl;

@DataJpaTest
@ExtendWith(MockitoExtension.class)
@Transactional(propagation = Propagation.SUPPORTS)
@Sql(scripts = "classpath:/initialize_database.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import({MemberRepositoryFacadeImpl.class, ThemeRepositoryFacadeImpl.class})
public class ReservationTimeServiceTest {

    private final ReservationTimeService reservationTimeService;
    private final ReservationTimeRepositoryFacade reservationTimeRepositoryFacade;

    private final ReservationRepository reservationRepository;
    private final ThemeRepositoryFacade themeRepositoryFacade;
    private final MemberRepositoryFacade memberRepositoryFacade;


    @Autowired
    public ReservationTimeServiceTest(
            final MemberRepositoryFacade memberRepositoryFacade,
            final ReservationTimeRepository reservationTimeRepository,
            final ReservationRepository reservationRepository,
            final ThemeRepositoryFacade themeRepositoryFacade
    ) {
        this.reservationRepository = reservationRepository;
        this.memberRepositoryFacade = memberRepositoryFacade;
        this.themeRepositoryFacade = themeRepositoryFacade;

        this.reservationTimeRepositoryFacade = spy(new ReservationTimeRepositoryFacadeImpl(reservationTimeRepository));
        reservationTimeService =
                new ReservationTimeService(reservationTimeRepositoryFacade, reservationRepository,
                        themeRepositoryFacade);
    }

    @Nested
    @DisplayName("예약 시간 생성")
    class Create {

        @DisplayName("TimeRequest를 저장하고, 저장된 TimeResponse를 반환한다.")
        @Test
        void createTime1() {
            // given
            final LocalTime startAt = LocalTime.of(12, 40);
            final ReservationTimeRequest request = new ReservationTimeRequest(startAt);
            final ReservationTimeResponse expected = ReservationTimeResponse.from(new ReservationTime(1L, startAt));

            // when
            final ReservationTimeResponse actual = reservationTimeService.create(request);

            // then
            assertThat(actual).isEqualTo(expected);
        }

        @DisplayName("이미 존재하는 시간이라면, 예외가 발생한다.")
        @Test
        void createTime2() {
            // given
            final LocalTime startAt = LocalTime.of(12, 40);
            final ReservationTimeRequest request = new ReservationTimeRequest(startAt);
            final ReservationTime reservationTime = new ReservationTime(startAt);
            reservationTimeRepositoryFacade.save(reservationTime);

            // when
            assertThatThrownBy(() -> {
                reservationTimeService.create(request);
            }).isInstanceOf(ReservationTimeConflictException.class);
        }
    }

    @Nested
    @DisplayName("예약 시간 모두 조회")
    class findAll {

        @DisplayName("저장된 모든 TimeResponse를 반환한다.")
        @Test
        void findAllTime1() {
            // given
            final ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 40));
            reservationTimeRepositoryFacade.save(reservationTime);

            // when
            final List<ReservationTimeResponse> actual = reservationTimeService.findAll();

            // then
            assertThat(actual)
                    .hasSize(1)
                    .contains(new ReservationTimeResponse(1L, LocalTime.of(12, 40)));
        }

        @DisplayName("저장된 TimeResponse이 없다면 빈 컬렉션을 반환한다.")
        @Test
        void findAllTime2() {
            // given
            // when
            final List<ReservationTimeResponse> actual = reservationTimeService.findAll();

            // then
            assertThat(actual).isEmpty();
        }
    }

    @Nested
    @DisplayName("alreadyBooked와 함께 모든 time 반환")
    class FindAllAvailableTimes {

        @DisplayName("존재하는 모든 시간을 반환한다.")
        @Test
        void findAllAvailableTimes() {
            // given
            final Theme theme = new Theme( "메이", "테마", "thumbnail");
            final LocalDate targetDate = LocalDate.of(2026, 12, 1);

            themeRepositoryFacade.save(theme);
            reservationTimeRepositoryFacade.save(new ReservationTime(LocalTime.of(12, 0)));
            reservationTimeRepositoryFacade.save(new ReservationTime(LocalTime.of(13, 0)));
            reservationTimeRepositoryFacade.save(new ReservationTime( LocalTime.of(14, 0)));

            // when
            final List<AvailableReservationTimeResponse> allAvailableTimes = reservationTimeService.findAllAvailableTimes(
                    1L, targetDate);

            // then
            assertThat(allAvailableTimes).hasSize(3);
        }

        @DisplayName("이미 예약된 시간은 alreadyBooked가 true로 반환된다.")
        @Test
        void findAllAvailableTimes1() {
            // given
            final Theme theme = new Theme( "메이", "테마", "thumbnail");
            final Member member = new Member("email", "pass", "name", MemberRole.MEMBER);
            final LocalDate targetDate = LocalDate.of(2026, 12, 1);
            final ReservationTime time = new ReservationTime(LocalTime.of(12, 0));
            final Reservation reservation = new Reservation(targetDate, member, time, theme, ReservationStatus.PENDING);

            memberRepositoryFacade.save(member);
            reservationTimeRepositoryFacade.save(time);
            themeRepositoryFacade.save(theme);
            reservationRepository.save(reservation);

            // when
            final List<AvailableReservationTimeResponse> allAvailableTimes = reservationTimeService.findAllAvailableTimes(
                    1L, targetDate);

            // then
            assertThat(allAvailableTimes.getFirst().alreadyBooked()).isTrue();
        }

        @DisplayName("예약되지 않은 시간은 alreadyBooked가 false로 반환된다.")
        @Test
        void findAllAvailableTimes2() {
            // given
            final LocalDate targetDate = LocalDate.of(2026, 12, 1);
            final Theme theme = new Theme( "메이", "테마", "thumbnail");
            final ReservationTime time = new ReservationTime(LocalTime.of(12, 0));

            themeRepositoryFacade.save(theme);
            reservationTimeRepositoryFacade.save(time);

            // when
            final List<AvailableReservationTimeResponse> allAvailableTimes = reservationTimeService.findAllAvailableTimes(
                    1L, targetDate);

            // then
            assertThat(allAvailableTimes.getFirst().alreadyBooked()).isFalse();
        }

    }


    @Nested
    @DisplayName("예약 시간 삭제")
    class Delete {

        @DisplayName("id에 해당하는 time을 제거한다")
        @Test
        void deleteTimeById1() {
            // given
            final ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 40));
            reservationTimeRepositoryFacade.save(reservationTime);

            // when
            reservationTimeService.deleteById(1L);

            // then
            then(reservationTimeRepositoryFacade).should().delete(reservationTime);
        }

        @DisplayName("id에 해당하는 time이 없다면 예외가 발생한다.")
        @Test
        void deleteTimeById2() {
            // given
            // when & then
            assertThatThrownBy(() -> {
                reservationTimeService.deleteById(1L);
            }).isInstanceOf(ReservationTimeNotFoundException.class);
        }

        @DisplayName("예약에서 시간을 사용중이라면 예외가 발생한다.")
        @Test
        void deleteTimeById3() {
            // given
            final Theme theme = new Theme( "메이", "테마", "thumbnail");
            final Member member = new Member("email", "pass", "name", MemberRole.MEMBER);
            final LocalDate targetDate = LocalDate.of(2026, 12, 1);
            final ReservationTime time = new ReservationTime(LocalTime.of(12, 0));
            final Reservation reservation = new Reservation(targetDate, member, time, theme, ReservationStatus.PENDING);

            memberRepositoryFacade.save(member);
            reservationTimeRepositoryFacade.save(time);
            themeRepositoryFacade.save(theme);
            reservationRepository.save(reservation);

            // when & then
            assertThatThrownBy(() -> {
                reservationTimeService.deleteById(1L);
            }).isInstanceOf(ReservationTimeUsedException.class);
        }

    }

}
