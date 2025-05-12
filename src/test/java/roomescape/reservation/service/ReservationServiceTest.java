package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.member.model.Member;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.dto.request.ReservationCreateRequest;
import roomescape.reservation.exception.DuplicateReservationException;
import roomescape.reservation.exception.NotCorrectDateTimeException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Test
    @DisplayName("예약 추가를 할 수 있다.")
    void createReservation() {
        ReservationCreateRequest request = new ReservationCreateRequest(LocalDate.of(2024, 4, 26), 1L, 1L);
        Member member = Member.generateNormalMember("프리", "phree@woowa.com", "password");
        Reservation actual = reservationService.createReservation(request, member);

        assertAll(() -> {
            assertThat(actual.getId()).isEqualTo(22L);
            assertThat(actual.getMember().getName()).isEqualTo("프리");
            assertThat(actual.getDate()).isEqualTo(LocalDate.of(2024, 4, 26));
            assertThat(actual.getTime().getId()).isEqualTo(1L);
            assertThat(actual.getTheme().getId()).isEqualTo(1L);
        });
    }

    @Test
    @DisplayName("createReservationAfterNow 메서드를 사용하면 이전 시간으로의 예약은 추가할 수 없다.")
    void createReservationAfterNow() {
        // Given
        Member member = Member.generateNormalMember("프리", "phree@woowa.com", "password");
        ReservationCreateRequest request = new ReservationCreateRequest(LocalDate.of(2024, 4, 26), 1L, 1L);

        // When & Then
        assertThatThrownBy(() -> reservationService.createReservationAfterNow(request, member))
                .isInstanceOf(NotCorrectDateTimeException.class)
                .hasMessage("지나간 날짜와 시간에 대한 예약 생성은 불가능하다.");
    }

    @Test
    @DisplayName("날짜와 시간, 테마가 같은 경우에는 예약 추가를 할 수 없다.")
    void cannotCreateReservation() {
        // Given
        Member member = Member.generateNormalMember("프리", "phree@woowa.com", "password");
        ReservationCreateRequest request = new ReservationCreateRequest(LocalDate.of(2024, 4, 26), 1L, 1L);
        ReservationCreateRequest request2 = new ReservationCreateRequest(LocalDate.of(2024, 4, 26), 1L, 1L);
        reservationService.createReservation(request, member);

        // When & Then
        assertThatThrownBy(() -> reservationService.createReservation(request2, member))
                .isInstanceOf(DuplicateReservationException.class);
    }

    @Test
    @DisplayName("모든 예약 정보를 가져올 수 있다.")
    void findAllReservations() {
        assertThat(reservationService.findAllReservations()).hasSize(21);
    }

    @Test
    @DisplayName("예약을 id를 통해 제거할 수 있다.")
    void deleteReservationById() {
        reservationService.deleteReservationById(1L);
        assertThat(reservationService.findAllReservations()).hasSize(20);
    }

    @Test
    void 멤버와_테마_날짜로_필터링하여_검색할_수_있다() {
        // Given
        // When
        // Then
        assertAll(() -> {
            assertThat(reservationService.findReservationByMemberIdAndThemeIdAndStartDateAndEndDate(1L, null, null, null)).hasSize(10);
            assertThat(reservationService.findReservationByMemberIdAndThemeIdAndStartDateAndEndDate(null, 1L, null, null)).hasSize(10);
            assertThat(reservationService.findReservationByMemberIdAndThemeIdAndStartDateAndEndDate(null, null, LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 3))).hasSize(12);
            assertThat(reservationService.findReservationByMemberIdAndThemeIdAndStartDateAndEndDate(1L, 1L, null, null)).hasSize(5);
            assertThat(reservationService.findReservationByMemberIdAndThemeIdAndStartDateAndEndDate(1L, null, LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 3))).hasSize(6);
            assertThat(reservationService.findReservationByMemberIdAndThemeIdAndStartDateAndEndDate(null, 1L, LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 3))).hasSize(6);
            assertThat(reservationService.findReservationByMemberIdAndThemeIdAndStartDateAndEndDate(1L, 1L, LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 3))).hasSize(3);
        });
    }
}
