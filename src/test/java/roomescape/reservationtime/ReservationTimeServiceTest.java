package roomescape.reservationtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.then;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import roomescape.exception.custom.reason.reservationtime.ReservationTimeConflictException;
import roomescape.exception.custom.reason.reservationtime.ReservationTimeNotFoundException;
import roomescape.exception.custom.reason.reservationtime.ReservationTimeUsedException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.member.repository.MemberRepositoryImpl;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationRepositoryImpl;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.dto.AvailableReservationTimeResponse;
import roomescape.reservationtime.dto.ReservationTimeRequest;
import roomescape.reservationtime.dto.ReservationTimeResponse;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.reservationtime.repository.ReservationTimeRepositoryImpl;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.theme.repository.ThemeRepositoryImpl;

@DataJpaTest
@Transactional(propagation = Propagation.SUPPORTS)
@Sql(scripts = "classpath:/initialize_database.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import({
        MemberRepositoryImpl.class,
        ThemeRepositoryImpl.class,
        ReservationRepositoryImpl.class,
        ReservationTimeRepositoryImpl.class,
        ReservationTimeService.class
})
public class ReservationTimeServiceTest {

    @MockitoSpyBean
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationTimeService reservationTimeService;

    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;


    @Autowired
    public ReservationTimeServiceTest(
            final ReservationTimeRepository reservationTimeRepository,
            final ReservationTimeService reservationTimeService,

            final MemberRepository memberRepository,
            final ReservationRepository reservationRepository,
            final ThemeRepository themeRepository
            ) {

        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationTimeService = reservationTimeService;

        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.themeRepository = themeRepository;
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
            reservationTimeRepository.save(reservationTime);

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
            reservationTimeRepository.save(reservationTime);

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
            final Theme theme = new Theme("메이", "테마", "thumbnail");
            final LocalDate targetDate = LocalDate.of(2026, 12, 1);

            themeRepository.save(theme);
            reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
            reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
            reservationTimeRepository.save(new ReservationTime(LocalTime.of(14, 0)));

            // when
            final List<AvailableReservationTimeResponse> allAvailableTimes = reservationTimeService.findAllAvailable(
                    1L, targetDate);

            // then
            assertThat(allAvailableTimes).hasSize(3);
        }

        @DisplayName("이미 예약된 시간은 alreadyBooked가 true로 반환된다.")
        @Test
        void findAllAvailableTimes1() {
            // given
            final Theme theme = new Theme("메이", "테마", "thumbnail");
            final Member member = new Member("email", "pass", "name", MemberRole.MEMBER);
            final ReservationTime time = new ReservationTime(LocalTime.of(12, 0));
            final LocalDateTime currentDateTime = LocalDateTime.of(2025, 12, 25, 12, 0);
            final ReservationDate reservationDate = ReservationDate.of(LocalDate.of(2025, 12, 30),
                    currentDateTime.toLocalDate());
            final Reservation reservation = Reservation.of(reservationDate, member, time, theme,
                    ReservationStatus.WAITING, currentDateTime);

            memberRepository.save(member);
            reservationTimeRepository.save(time);
            themeRepository.save(theme);
            reservationRepository.save(reservation);

            // when
            final List<AvailableReservationTimeResponse> allAvailableTimes = reservationTimeService.findAllAvailable(
                    1L, reservationDate.date());

            // then
            assertThat(allAvailableTimes.getFirst().alreadyBooked()).isTrue();
        }

        @DisplayName("예약되지 않은 시간은 alreadyBooked가 false로 반환된다.")
        @Test
        void findAllAvailableTimes2() {
            // given
            final LocalDate targetDate = LocalDate.of(2026, 12, 1);
            final Theme theme = new Theme("메이", "테마", "thumbnail");
            final ReservationTime time = new ReservationTime(LocalTime.of(12, 0));

            themeRepository.save(theme);
            reservationTimeRepository.save(time);

            // when
            final List<AvailableReservationTimeResponse> allAvailableTimes = reservationTimeService.findAllAvailable(
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
            reservationTimeRepository.save(reservationTime);

            // when
            reservationTimeService.deleteById(1L);

            // then
            then(reservationTimeRepository).should().delete(reservationTime);
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
            final Theme theme = new Theme("메이", "테마", "thumbnail");
            final Member member = new Member("email", "pass", "name", MemberRole.MEMBER);
            final ReservationTime time = new ReservationTime(LocalTime.of(12, 0));
            final LocalDateTime currentDateTime = LocalDateTime.of(2025, 12, 25, 12, 0);
            final ReservationDate reservationDate = ReservationDate.of(LocalDate.of(2025, 12, 30),
                    currentDateTime.toLocalDate());
            final Reservation reservation = Reservation.of(reservationDate, member, time, theme,
                    ReservationStatus.WAITING, currentDateTime);


            memberRepository.save(member);
            reservationTimeRepository.save(time);
            themeRepository.save(theme);
            reservationRepository.save(reservation);

            // when & then
            assertThatThrownBy(() -> {
                reservationTimeService.deleteById(1L);
            }).isInstanceOf(ReservationTimeUsedException.class);
        }

    }

}
