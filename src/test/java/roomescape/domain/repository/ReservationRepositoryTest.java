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
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.config.JpaAuditingConfiguration;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Status;
import roomescape.domain.Theme;
import roomescape.testFixture.JdbcHelper;

@DataJpaTest
@Import(JpaAuditingConfiguration.class)
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private TimeRepository timeRepository;
    @Autowired
    private WaitingRepository waitingRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Member member;
    private Theme theme;
    private ReservationTime time;
    private LocalDate date;

    @BeforeEach
    void setup() {
        JdbcHelper.truncateAll(jdbcTemplate);
        member = memberRepository.save(Member.withoutId("user", "user@email.com", "password", Role.USER));
        theme = themeRepository.save(Theme.withoutId("테마", "설명", "썸네일"));
        time = timeRepository.save(ReservationTime.withoutId(LocalTime.of(10, 0)));
        date = LocalDate.of(2025, 5, 10);
    }

    @DisplayName("이미 예약이 존재하는지 확인한다")
    @Test
    void existsByDateAndTimeIdAndThemeIdAndStatusStatus() {
        // given
        Status status = Status.statusWithoutId(ReservationStatus.RESERVED);
        waitingRepository.saveAndFlush(status);

        reservationRepository.save(
                Reservation.withoutId(member, theme, date, time, status)
        );

        // when
        boolean exists = reservationRepository.existsByDateAndTimeIdAndThemeIdAndStatusStatus(
                date, time.getId(), theme.getId(), ReservationStatus.RESERVED
        );

        // then
        Assertions.assertThat(exists).isTrue();
    }

    @DisplayName("특정 사용자의 특정 테마의 +-1일 사이의 예약 목록을 조회한다")
    @Test
    void findByMemberAndThemeAndDateRange() {
        // given
        Status status = Status.statusWithoutId(ReservationStatus.RESERVED);
        waitingRepository.saveAndFlush(status);

        reservationRepository.save(Reservation.withoutId(member, theme, date, time, status));

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
    void countByReservationStatusOrderByCreatedAt() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Status status1 = waitingRepository.saveAndFlush(
                Status.statusWithoutId(now.minusMinutes(2), ReservationStatus.WAITING));
        Status status2 = waitingRepository.saveAndFlush(
                Status.statusWithoutId(now.minusMinutes(1), ReservationStatus.WAITING));
        Status status3 = waitingRepository.saveAndFlush(
                Status.statusWithoutId(now, ReservationStatus.WAITING));

        reservationRepository.saveAndFlush(Reservation.withoutId(member, theme, date, time, status1));
        reservationRepository.saveAndFlush(Reservation.withoutId(member, theme, date, time, status2));
        reservationRepository.saveAndFlush(Reservation.withoutId(member, theme, date, time, status3));

        // when
        long order = reservationRepository.countByReservationStatusOrderByCreatedAt(
                theme, date, time, now.plusMinutes(10)
        );

        // then
        Assertions.assertThat(order).isEqualTo(4);
    }
}
