package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.exception.DomainValidationException;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;

class ReservationTest {

    private static final LocalDate DATE = LocalDate.of(2024, 5, 5);
    private static final Member MEMBER = new Member("exmaple@gmail.com", "abc123", "구름", Role.USER);
    private static final ReservationTime RESERVATION_TIME = new ReservationTime(LocalTime.of(10, 0));
    private static final Theme THEME = new Theme("테마", "테마 설명", "https://example.com");
    private static final ReservationStatus STATUS = ReservationStatus.RESERVED;

    @Test
    @DisplayName("예약을 생성한다.")
    void create() {
        assertThatCode(() -> new Reservation(DATE, MEMBER, RESERVATION_TIME, THEME, STATUS))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("날짜가 없으면 예외가 발생한다.")
    void validateDate() {
        assertThatThrownBy(() -> new Reservation(null, MEMBER, RESERVATION_TIME, THEME, STATUS))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("날짜는 필수 값입니다.");
    }

    @Test
    @DisplayName("회원이 없으면 예외가 발생한다.")
    void validateMember() {
        assertThatThrownBy(() -> new Reservation(DATE, null, RESERVATION_TIME, THEME, STATUS))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("회원은 필수 값입니다.");
    }

    @Test
    @DisplayName("예약 시간이 없으면 예외가 발생한다.")
    void validateTime() {
        assertThatThrownBy(() -> new Reservation(DATE, MEMBER, null, THEME, STATUS))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("예약 시간은 필수 값입니다.");
    }

    @Test
    @DisplayName("테마가 없으면 예외가 발생한다.")
    void validateTheme() {
        assertThatThrownBy(() -> new Reservation(DATE, MEMBER, RESERVATION_TIME, null, STATUS))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("테마는 필수 값입니다.");
    }

    @Test
    @DisplayName("예약 상태가 없으면 예외가 발생한다.")
    void validateStatus() {
        assertThatThrownBy(() -> new Reservation(DATE, MEMBER, RESERVATION_TIME, THEME, null))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("예약 상태는 필수 값입니다.");
    }

    @Test
    @DisplayName("예약 대기에서 예약 상태로 바꾼다.")
    void updateToReserved() {
        Reservation reservation = new Reservation(DATE, MEMBER, RESERVATION_TIME, THEME, ReservationStatus.WAITING);
        reservation.updateToReserved();

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
    }

    @Test
    @DisplayName("예약 대기가 아니면 예약 상태로 바꿀 수 없다.")
    void updateToReservedWhenNotWaiting() {
        Reservation reservation = new Reservation(DATE, MEMBER, RESERVATION_TIME, THEME, ReservationStatus.RESERVED);

        assertThatThrownBy(reservation::updateToReserved)
                .isInstanceOf(DomainValidationException.class)
                .hasMessage("예약 대기 상태에서만 예약으로 변경할 수 있습니다.");
    }

    @Test
    @DisplayName("create 메서드로 예약을 생성한다.")
    void createReservation() {
        LocalDateTime currentDateTime = DATE.minusDays(1).atTime(10, 0);

        Reservation reservation = Reservation.create(currentDateTime, DATE, MEMBER, RESERVATION_TIME, THEME, STATUS);

        assertThat(reservation).isNotNull();
    }

    @Test
    @DisplayName("create 메서드로 예약을 생성할 때 지나간 날짜/시간이면 예외가 발생한다.")
    void createReservationWhenPastDateTime() {
        LocalDate pastDate = LocalDate.of(2020, 5, 5);
        LocalDateTime currentDateTime = LocalDateTime.of(2024, 5, 4, 10, 0);

        assertThatThrownBy(() -> Reservation.create(currentDateTime, pastDate, MEMBER, RESERVATION_TIME, THEME, STATUS))
                .isInstanceOf(DomainValidationException.class)
                .hasMessage(String.format("지나간 날짜/시간에 대한 예약은 불가능합니다. (예약 날짜: %s, 예약 시간: %s)", pastDate,
                        RESERVATION_TIME.getStartAt()));
    }
}
