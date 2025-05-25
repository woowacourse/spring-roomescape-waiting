package roomescape.domain.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;

@DataJpaTest
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private TimeRepository timeRepository;

    private Member member;
    private Theme theme;
    private ReservationTime time;
    private LocalDate date;
    @Autowired
    private WaitingRepository waitingRepository;

    @BeforeEach
    void setup() {
        reservationRepository.deleteAll();
        waitingRepository.deleteAll();

        member = memberRepository.save(Member.withoutId("user", "user@email.com", "password", Role.USER));
        theme = themeRepository.save(Theme.withoutId("테마", "설명", "썸네일"));
        time = timeRepository.save(ReservationTime.withoutId(LocalTime.of(10, 0)));
        date = LocalDate.of(2025, 5, 10);
    }

    @DisplayName("이미 예약이 존재하는지 확인한다")
    @Test
    void existsByDateAndTimeIdAndThemeIdAndWaitingStatus() {
        // given
        reservationRepository.save(Reservation.withoutId(member, theme, date, time,
                Waiting.waitingWithoutId(LocalDateTime.now(), ReservationStatus.RESERVED)));

        // when
        boolean exists = reservationRepository.existsByDateAndTimeIdAndThemeIdAndWaitingStatus(
                date, time.getId(), theme.getId(), ReservationStatus.RESERVED
        );

        // then
        Assertions.assertThat(exists).isTrue();
    }

    @DisplayName("특정 사용자의 특정 테마의 +-1일 사이의 예약 목록을 조회한다")
    @Test
    void findByMemberAndThemeAndDateRange() {
        // given
        reservationRepository.save(Reservation.withoutId(member, theme, date, time,
                Waiting.waitingWithoutId(LocalDateTime.now(), ReservationStatus.RESERVED)));

        // when
        List<Reservation> reservations = reservationRepository.findByMemberAndThemeAndDateRange(
                member.getId(),
                theme.getId(),
                date.minusDays(1),
                date.plusDays(1)
        );

        // then
        Assertions.assertThat(reservations).hasSize(1);
    }

    @DisplayName("예약 대기 순번을 조회한다")
    @Test
    void countByReservationWaitingOrderByCreatedAt() {
        // given
        LocalDateTime now = LocalDateTime.of(2025, 5, 10, 10, 0);
        reservationRepository.save(Reservation.withoutId(member, theme, date, time,
                Waiting.waitingWithoutId(now, ReservationStatus.WAITING)));
        reservationRepository.save(Reservation.withoutId(member, theme, date, time,
                Waiting.waitingWithoutId(now, ReservationStatus.WAITING)));
        reservationRepository.save(Reservation.withoutId(member, theme, date, time,
                Waiting.waitingWithoutId(now, ReservationStatus.WAITING)));

        // when
        long order = reservationRepository.countByReservationWaitingOrderByCreatedAt(theme, date, time,
                now.plusMinutes(10));

        // then
        Assertions.assertThat(order).isEqualTo(4);
    }
}
