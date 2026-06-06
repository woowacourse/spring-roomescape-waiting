package roomescape.infra.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.user.User;
import roomescape.infra.theme.JdbcThemeRepository;
import roomescape.infra.user.JdbcUserRepository;

@DisplayName("예약 JDBC 저장소")
@JdbcTest(properties = "spring.sql.init.mode=always")
@Import({
        JdbcUserRepository.class,
        JdbcThemeRepository.class,
        JdbcReservationTimeRepository.class,
        JdbcReservationSlotRepository.class,
        JdbcReservationRepository.class
})
class JdbcReservationRepositoryTest {

    @Autowired
    private JdbcUserRepository userRepository;

    @Autowired
    private JdbcThemeRepository themeRepository;

    @Autowired
    private JdbcReservationTimeRepository timeRepository;

    @Autowired
    private JdbcReservationSlotRepository slotRepository;

    @Autowired
    private JdbcReservationRepository reservationRepository;

    @DisplayName("예약을 저장할 수 있다")
    @Test
    void save() {
        // when
        Reservation saved = saveReservation("홍길동", "도심 탈출", LocalDate.of(2030, 3, 1), LocalTime.of(13, 0));

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(ReservationStatus.WAITING);
    }

    @DisplayName("예약을 id로 조회할 수 있다")
    @Test
    void findById() {
        // given
        Reservation saved = saveReservation("김철수", "미로 탈출", LocalDate.of(2030, 3, 2), LocalTime.of(15, 0));

        // when & then
        assertThat(reservationRepository.findById(saved.getId()))
                .hasValueSatisfying(found -> {
                    assertThat(found.getUser().getName()).isEqualTo("김철수");
                    assertThat(found.getSlot().getDate()).isEqualTo(LocalDate.of(2030, 3, 2));
                    assertThat(found.getStatus()).isEqualTo(ReservationStatus.WAITING);
                });
    }

    @DisplayName("예약을 삭제할 수 있다")
    @Test
    void deleteById() {
        // given
        Reservation saved = saveReservation("이영희", "우주 탈출", LocalDate.of(2030, 3, 3), LocalTime.of(16, 0));

        // when
        assertThat(reservationRepository.deleteById(saved.getId())).isEqualTo(1);

        // then
        assertThat(reservationRepository.findById(saved.getId())).isEmpty();
    }

    @DisplayName("슬롯과 사용자로 예약 존재 여부를 확인할 수 있다")
    @Test
    void existsBySlotIdAndUserId() {
        // given
        Reservation saved = saveReservation("박민수", "심해 탈출", LocalDate.of(2030, 3, 4), LocalTime.of(11, 0));

        // then
        assertThat(reservationRepository.existsBySlotIdAndUserId(
                saved.getSlot().getId(),
                saved.getUser().getId()
        )).isTrue();
    }

    @DisplayName("슬롯별 예약을 예약 시각 순으로 조회할 수 있다")
    @Test
    void findAllBySlotIdOrderByReservedAt() {
        // given
        Reservation first = saveReservation("정수민", "도심 탈출", LocalDate.of(2030, 4, 1), LocalTime.of(15, 0),
                LocalDateTime.of(2030, 4, 1, 10, 0));
        Reservation second = saveReservation("오지훈", first.getSlot(), LocalDateTime.of(2030, 4, 1, 10, 5));
        Reservation third = saveReservation("최유진", first.getSlot(), LocalDateTime.of(2030, 4, 1, 10, 10));

        // when & then
        assertThat(reservationRepository.findAllBySlotIdOrderByReservedAt(first.getSlot().getId()))
                .extracting(Reservation::getId)
                .containsExactly(first.getId(), second.getId(), third.getId());
    }

    @DisplayName("슬롯별 예약의 대기 순번과 상태를 갱신할 수 있다")
    @Test
    void batchUpdate() {
        // given
        Reservation first = saveReservation("한지우", "도심 탈출", LocalDate.of(2030, 4, 2), LocalTime.of(15, 0),
                LocalDateTime.of(2030, 4, 2, 10, 0));
        Reservation second = saveReservation("서민재", first.getSlot(), LocalDateTime.of(2030, 4, 2, 10, 5));
        Reservation third = saveReservation("배수현", first.getSlot(), LocalDateTime.of(2030, 4, 2, 10, 10));

        // when
        reservationRepository.batchUpdate(List.of(
                first.updateConfirmed(),
                second.updateWaiting(1),
                third.updateWaiting(2)
        ));

        // then
        assertThat(reservationRepository.findById(first.getId()))
                .hasValueSatisfying(found -> {
                    assertThat(found.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
                    assertThat(found.getWaitingNumber()).isEqualTo(0);
                });
        assertThat(reservationRepository.findById(second.getId()))
                .hasValueSatisfying(found -> {
                    assertThat(found.getStatus()).isEqualTo(ReservationStatus.WAITING);
                    assertThat(found.getWaitingNumber()).isEqualTo(1);
                });
        assertThat(reservationRepository.findById(third.getId()))
                .hasValueSatisfying(found -> {
                    assertThat(found.getStatus()).isEqualTo(ReservationStatus.WAITING);
                    assertThat(found.getWaitingNumber()).isEqualTo(2);
                });
    }

    private Reservation saveReservation(String userName, String themeName, LocalDate date, LocalTime startAt) {
        return saveReservation(userName, themeName, date, startAt, LocalDateTime.of(date, startAt));
    }

    private Reservation saveReservation(
            String userName,
            String themeName,
            LocalDate date,
            LocalTime startAt,
            LocalDateTime reservedAt
    ) {
        Theme theme = themeRepository.save(Theme.create(themeName, themeName + " 설명", "/themes/" + themeName));
        ReservationTime time = timeRepository.save(ReservationTime.create(startAt));
        ReservationSlot slot = slotRepository.save(ReservationSlot.create(date, time, theme));
        return reservationRepository.save(Reservation.create(
                userRepository.save(User.create(userName)),
                slot,
                reservedAt
        ));
    }

    private Reservation saveReservation(String userName, ReservationSlot slot, LocalDateTime reservedAt) {
        return reservationRepository.save(Reservation.create(
                userRepository.save(User.create(userName)),
                slot,
                reservedAt
        ));
    }
}
