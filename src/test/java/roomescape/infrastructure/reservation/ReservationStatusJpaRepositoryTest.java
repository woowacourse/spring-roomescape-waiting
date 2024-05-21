package roomescape.infrastructure.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberFixture;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.BookStatus;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;

@DataJpaTest
class ReservationStatusJpaRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationStatusJpaRepository reservationStatusRepository;

    @Test
    @DisplayName("예약 대기 중 가장 첫 번째 예약을 가져온다.")
    void getFirstWaiting() {
        Member member = memberRepository.save(MemberFixture.createMember("아루"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
        Theme theme = themeRepository.save(new Theme("테마", "desc", "url"));
        LocalDate date = LocalDate.of(2024, 12, 25);

        for (int day = 1; day <= 10; day++) {
            LocalDateTime createdAt = LocalDateTime.of(2023, 1, day, 12, 0);
            Reservation reservation = new Reservation(member, date, time, theme, createdAt);
            entityManager.persist(new ReservationStatus(reservation, BookStatus.WAITING));
        }

        Optional<ReservationStatus> firstWaiting = reservationStatusRepository.findFirstWaiting(
                new Reservation(member, date, time, theme, LocalDateTime.of(2023, 1, 5, 12, 0))
        );

        assertThat(firstWaiting).isPresent()
                .get()
                .extracting(ReservationStatus::getReservation)
                .extracting(Reservation::getCreatedAt)
                .isEqualTo(LocalDateTime.of(2023, 1, 1, 12, 0));
    }

    @Test
    @DisplayName("예약 순번을 올바르게 계산한다.")
    void getWaitingCount() {
        Member member = memberRepository.save(MemberFixture.createMember("아루"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
        Theme theme = themeRepository.save(new Theme("테마", "desc", "url"));
        LocalDate date = LocalDate.of(2024, 12, 25);
        List<Reservation> reservations = new ArrayList<>();
        for (int day = 1; day <= 10; day++) {
            LocalDateTime createdAt = LocalDateTime.of(2023, 1, day, 12, 0);
            Reservation reservation = new Reservation(member, date, time, theme, createdAt);
            entityManager.persist(new ReservationStatus(reservation, BookStatus.WAITING));
            reservations.add(reservation);
        }

        long waitingCount = reservationStatusRepository.getWaitingCount(reservations.get(4));

        assertThat(waitingCount).isEqualTo(4);
    }

    @ParameterizedTest
    @EnumSource(value = BookStatus.class, names = {"WAITING", "BOOKED"})
    @DisplayName("하나의 예약 정보에 대해, 이미 예약/대기된 상태를 확인한다.")
    void alreadyExistsReservationOrWaitingTest(BookStatus status) {
        Member member = memberRepository.save(MemberFixture.createMember("아루"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
        Theme theme = themeRepository.save(new Theme("테마", "desc", "url"));
        LocalDate date = LocalDate.of(2024, 12, 25);
        Reservation reservation = new Reservation(member, date, time, theme, LocalDateTime.of(2023, 1, 5, 12, 0));
        entityManager.persist(new ReservationStatus(reservation, status));

        boolean exists = reservationStatusRepository.existsAlreadyWaitingOrBooked(reservation);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("예약된 모든 정보를 가져온다.")
    void getAllBooked() {
        Member member = memberRepository.save(MemberFixture.createMember("아루"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
        Theme theme = themeRepository.save(new Theme("테마", "desc", "url"));
        LocalDate date = LocalDate.of(2024, 12, 25);
        for (int day = 1; day <= 10; day++) {
            LocalDateTime createdAt = LocalDateTime.of(2023, 1, day, 12, 0);
            Reservation reservation = new Reservation(member, date, time, theme, createdAt);
            entityManager.persist(new ReservationStatus(reservation, BookStatus.BOOKED));
        }

        List<Reservation> bookedReservations = reservationStatusRepository.findAllBookedReservations();
        assertThat(bookedReservations).hasSize(10);
    }
}
