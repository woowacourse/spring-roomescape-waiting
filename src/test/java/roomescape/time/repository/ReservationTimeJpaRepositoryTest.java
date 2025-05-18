package roomescape.time.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;
import roomescape.member.role.Role;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.theme.domain.Theme;
import roomescape.time.controller.response.AvailableReservationTimeResponse;
import roomescape.time.domain.ReservationTime;

@DataJpaTest
class ReservationTimeJpaRepositoryTest {

    @Autowired
    private ReservationTimeJpaRepository reservationTimeJpaRepository;

    @Autowired
    private EntityManager em;

    private LocalDate date;
    private Theme theme;
    private Member member;
    private ReservationTime time1;
    private ReservationTime time2;
    private ReservationStatus reservationStatus;

    @BeforeEach
    void setUp() {
        date = LocalDate.of(2025, 5, 14);
        theme = new Theme(null, "공포", "무서운 테마", "image.jpg");
        member = new Member(null, new Name("매트"),
                new Email("matt@test.com"),
                new Password("1234"),
                Role.MEMBER);

        time1 = new ReservationTime(null, LocalTime.of(10, 0));
        time2 = new ReservationTime(null, LocalTime.of(11, 0));
        reservationStatus = ReservationStatus.RESERVATION;

        em.persist(theme);
        em.persist(member);
        em.persist(time1);
        em.persist(time2);

        Reservation reservation = Reservation.create(
                date,
                time1,
                theme,
                member,
                reservationStatus
        );
        em.persist(reservation);

        em.flush();
        em.clear();
    }

    @Test
    void 예약_가능_시간_목록을_조회한다() {
        List<AvailableReservationTimeResponse> responses =
                reservationTimeJpaRepository.findAllAvailableReservationTimes(new ReservationDate(date), theme.getId());

        assertThat(responses).hasSize(2);

        AvailableReservationTimeResponse reserved = responses.stream()
                .filter(r -> r.id().equals(time1.getId()))
                .findFirst()
                .orElseThrow();

        AvailableReservationTimeResponse notReserved = responses.stream()
                .filter(r -> r.id().equals(time2.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(reserved.isReserved()).isTrue();
        assertThat(notReserved.isReserved()).isFalse();
    }
}
