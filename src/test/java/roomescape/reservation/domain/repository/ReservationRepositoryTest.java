package roomescape.reservation.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.reservation.domain.BookingStatus;
import roomescape.reservation.domain.Reservation;

@DataJpaTest
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    void 전체_예약을_time_theme_member와_함께_조회한다() {
        List<Reservation> result = reservationRepository.findAllWithAssociations();
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getTheme()).isNotNull();
        assertThat(result.get(0).getTime()).isNotNull();
        assertThat(result.get(0).getMember()).isNotNull();
    }

    @Test
    void 특정_날짜_테마의_예약을_time과_함께_조회한다() {
        LocalDate date = LocalDate.now().minusDays(3);
        Long themeId = 2L; // 라라랜드

        List<Reservation> result = reservationRepository.findByDateAndThemeIdWithAssociations(date, themeId);

        assertThat(result).isNotEmpty();
        assertThat(result).allSatisfy(reservation ->
                assertThat(reservation.getTime()).isNotNull()
        );
    }

    @Test
    void 필터_조건으로_예약을_조회한다() {
        Long themeId = 2L; // 라라랜드
        Long memberId = 1L; // 엠제이
        LocalDate from = LocalDate.now().minusDays(5);
        LocalDate to = LocalDate.now().plusDays(5);
        BookingStatus status = BookingStatus.RESERVED;

        List<Reservation> result = reservationRepository.findByFilteringWithAssociations(themeId, memberId, from, to,
                status);

        assertThat(result).isNotEmpty();
        assertThat(result).allSatisfy(reservation -> {
            assertThat(reservation.getTheme().getId()).isEqualTo(themeId);
            assertThat(reservation.getMember().getId()).isEqualTo(memberId);
            assertThat(reservation.getBookingStatus()).isEqualTo(status);
        });
    }

    @Test
    void 특정_멤버의_예약을_theme_time과_함께_조회한다() {
        Long memberId = 3L; // 리사

        List<Reservation> result = reservationRepository.findByMemberIdWithAssociations(memberId);

        assertThat(result).isNotEmpty();
        assertThat(result).allSatisfy(reservation -> {
            assertThat(reservation.getMember().getId()).isEqualTo(memberId);
            assertThat(reservation.getTheme()).isNotNull();
            assertThat(reservation.getTime()).isNotNull();
        });
    }

    @Test
    void 예약_상태로_예약을_조회한다() {
        BookingStatus status = BookingStatus.WAITING;

        List<Reservation> result = reservationRepository.findByStatusWithAssociations(status);

        assertThat(result).isNotEmpty();
        assertThat(result).allSatisfy(reservation -> {
            assertThat(reservation.getBookingStatus()).isEqualTo(status);
            assertThat(reservation.getTheme()).isNotNull();
            assertThat(reservation.getTime()).isNotNull();
            assertThat(reservation.getMember()).isNotNull();
        });
    }
}
