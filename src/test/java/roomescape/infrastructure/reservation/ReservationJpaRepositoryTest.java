package roomescape.infrastructure.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.fixture.MemberFixture.MEMBER_ARU;
import static roomescape.fixture.MemberFixture.MEMBER_PK;
import static roomescape.fixture.ThemeFixture.TEST_THEME;
import static roomescape.fixture.TimeFixture.TEN_AM;
import static roomescape.fixture.TimeFixture.TWELVE_PM;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.BookStatus;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;

@DataJpaTest
class ReservationJpaRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("예약 시간 id를 가진 예약이 존재하는지 확인한다.")
    void shouldReturnCountOfReservationWhenReservationTimeUsed() {
        Member member = memberRepository.save(MEMBER_ARU.create());
        Theme theme = themeRepository.save(TEST_THEME.create());
        LocalDate date = LocalDate.of(2024, 12, 25);
        ReservationTime time = reservationTimeRepository.save(TWELVE_PM.create());
        LocalDateTime createdAt = date.minusDays(1).atStartOfDay();

        Reservation reservation = new Reservation(member, date, time, theme, createdAt, BookStatus.BOOKED);
        entityManager.persist(reservation);

        boolean exists = reservationRepository.existsByTimeId(time.getId());
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("날짜, 시간으로 저장된 예약이 있는지 확인한다.")
    void shouldReturnIsExistReservationWhenReservationsNameAndDateAndTimeIsSame() {
        Member member = memberRepository.save(MEMBER_ARU.create());
        Theme theme = themeRepository.save(TEST_THEME.create());
        LocalDate date = LocalDate.of(2024, 12, 25);
        ReservationTime time = reservationTimeRepository.save(TEN_AM.create());
        LocalDateTime createdAt = date.minusDays(1).atStartOfDay();

        Reservation reservation = new Reservation(member, date, time, theme, createdAt, BookStatus.BOOKED);
        entityManager.persist(reservation);

        boolean exists = reservationRepository.existsActiveReservation(
                theme.getId(), reservation.getDate(), time.getId()
        );
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("예약 취소는 저장된 예약에 영향을 주지 않는다.")
    void findActiveReservationOnCancelled() {
        Member member = memberRepository.save(MEMBER_ARU.create());
        Theme theme = themeRepository.save(TEST_THEME.create());
        LocalDate date = LocalDate.of(2024, 12, 25);
        ReservationTime time = reservationTimeRepository.save(TEN_AM.create());
        LocalDateTime createdAt = date.minusDays(1).atStartOfDay();

        Reservation reservation = new Reservation(member, date, time, theme, createdAt, BookStatus.BOOKED);
        entityManager.persist(reservation);

        boolean exists = reservationRepository.existsActiveReservation(
                theme.getId(), reservation.getDate(), time.getId()
        );
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("날짜, 테마, 사용자로 예약을 조회한다.")
    void findByMemberAndThemeBetweenDates() {
        Member aru = memberRepository.save(MEMBER_ARU.create());
        Member pk = memberRepository.save(MEMBER_PK.create());
        Theme theme = themeRepository.save(TEST_THEME.create());
        LocalDate date = LocalDate.of(2024, 12, 1);
        ReservationTime time = reservationTimeRepository.save(TEN_AM.create());
        LocalDateTime createdAt = date.minusDays(1).atStartOfDay();
        for (int day = 1; day <= 10; day++) {
            entityManager.persist(
                    new Reservation(aru, date.plusDays(day), time, theme, createdAt, BookStatus.BOOKED)
            );
        }
        for (int day = 1; day <= 5; day++) {
            entityManager.persist(
                    new Reservation(pk, date.plusDays(day), time, theme, createdAt, BookStatus.BOOKED)
            );
        }

        Specification<Reservation> specification = ReservationSpec.where()
                .equalsMemberId(pk.getId())
                .equalsThemeId(theme.getId())
                .greaterThanOrEqualsStartDate(date.plusDays(4))
                .lessThanOrEqualsEndDate(date.plusDays(10))
                .build();
        List<Reservation> reservations = reservationRepository.findAll(specification);
        assertThat(reservations).hasSize(2);
    }

    @Test
    @DisplayName("예약 대기 중 가장 첫 번째 예약을 가져온다.")
    void getFirstWaiting() {
        Member member = memberRepository.save(MEMBER_ARU.create());
        Theme theme = themeRepository.save(TEST_THEME.create());
        LocalDate date = LocalDate.of(2024, 12, 25);
        ReservationTime time = reservationTimeRepository.save(TWELVE_PM.create());

        for (int day = 1; day <= 10; day++) {
            LocalDateTime createdAt = LocalDateTime.of(2023, 1, day, 12, 0);
            Reservation reservation = new Reservation(member, date, time, theme, createdAt, BookStatus.WAITING);
            entityManager.persist(reservation);
        }

        Optional<Reservation> firstWaiting = reservationRepository.findFirstWaiting(theme, date, time);

        assertThat(firstWaiting).isPresent()
                .get()
                .extracting(Reservation::getCreatedAt)
                .isEqualTo(LocalDateTime.of(2023, 1, 1, 12, 0));
    }

    @Test
    @DisplayName("예약 순번을 올바르게 계산한다.")
    void getWaitingCount() {
        Member member = memberRepository.save(MEMBER_ARU.create());
        Theme theme = themeRepository.save(TEST_THEME.create());
        LocalDate date = LocalDate.of(2024, 12, 25);
        ReservationTime time = reservationTimeRepository.save(TWELVE_PM.create());
        List<Reservation> reservations = new ArrayList<>();
        for (int day = 1; day <= 10; day++) {
            LocalDateTime createdAt = LocalDateTime.of(2023, 1, day, 12, 0);
            Reservation reservation = new Reservation(member, date, time, theme, createdAt, BookStatus.BOOKED);
            entityManager.persist(reservation);
            reservations.add(reservation);
        }

        long waitingCount = reservationRepository.getWaitingCount(reservations.get(4));
        assertThat(waitingCount).isEqualTo(4);
    }

    @ParameterizedTest
    @EnumSource(value = BookStatus.class, names = {"WAITING", "BOOKED"})
    @DisplayName("하나의 예약 정보에 대해, 이미 예약/대기된 상태를 확인한다.")
    void alreadyExistsReservationOrWaitingTest(BookStatus status) {
        Member member = memberRepository.save(MEMBER_ARU.create());
        Theme theme = themeRepository.save(TEST_THEME.create());
        LocalDate date = LocalDate.of(2024, 12, 25);
        ReservationTime time = reservationTimeRepository.save(TWELVE_PM.create());
        LocalDateTime createdAt = LocalDateTime.of(2023, 1, 5, 12, 0);

        Reservation reservation = new Reservation(member, date, time, theme, createdAt, status);
        entityManager.persist(reservation);

        boolean exists = reservationRepository.existsAlreadyWaitingOrBooked(
                member.getId(), theme.getId(), date, time.getId()
        );
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("예약된 모든 정보를 가져온다.")
    void getAllBooked() {
        Member member = memberRepository.save(MEMBER_ARU.create());
        Theme theme = themeRepository.save(TEST_THEME.create());
        LocalDate date = LocalDate.of(2024, 12, 25);
        ReservationTime time = reservationTimeRepository.save(TWELVE_PM.create());
        for (int day = 1; day <= 10; day++) {
            LocalDateTime createdAt = LocalDateTime.of(2023, 1, day, 12, 0);
            Reservation reservation = new Reservation(member, date, time, theme, createdAt, BookStatus.BOOKED);
            entityManager.persist(reservation);
        }

        List<Reservation> bookedReservations = reservationRepository.findAllBookedReservations();
        assertThat(bookedReservations).hasSize(10);
    }
}
