package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.controller.dto.request.ReservationSaveRequest;
import roomescape.reservation.controller.dto.response.MemberReservationResponse;
import roomescape.reservation.controller.dto.response.ReservationDeleteResponse;
import roomescape.reservation.controller.dto.response.ReservationResponse;
import roomescape.reservation.controller.dto.response.SelectableTimeResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationServiceTest {
    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("예약을 성공적으로 저장한다.")
    @Test
    void saveReservation() {
        // given
        Member member = memberRepository.save(Member.of("anna", "brown@gmail.com", "password", "MEMBER"));
        ReservationSaveRequest saveRequest = new ReservationSaveRequest(1L, LocalDate.parse("2024-12-24"), 2L);

        // when
        ReservationResponse response = reservationService.reserve(saveRequest, member);

        // then
        assertNotNull(response);
        assertEquals(saveRequest.date(), response.date());
        assertTrue(reservationRepository.findById(response.id()).isPresent());
    }

    @DisplayName("중복된 예약을 저장하려고 할 때 예외를 던진다.")
    @Test
    void saveDuplicateReservationThrowsException() {
        // given
        Member member = memberRepository.save(Member.of("anna", "brown@gmail.com", "password", "MEMBER"));
        ReservationSaveRequest saveRequest = new ReservationSaveRequest(1L, LocalDate.parse("2024-12-12"), 1L);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> reservationService.reserve(saveRequest, member));
    }

    @DisplayName("모든 예약을 조회한다.")
    @Test
    void getAllReservations() {
        // when
        List<ReservationResponse> responses = reservationService.getAllResponses();

        // then
        assertNotNull(responses);
        assertEquals(3, responses.size());
        assertThat(responses.get(0).date()).isEqualTo(LocalDate.parse("2024-12-12"));
        assertThat(responses.get(1).date()).isEqualTo(LocalDate.parse("2024-12-23"));
        assertThat(responses.get(2).date()).isEqualTo(LocalDate.parse("2024-12-25"));
    }

    @DisplayName("선택 가능한 시간을 조회한다.")
    @Test
    void findSelectableTimes() {
        // when
        List<SelectableTimeResponse> responses = reservationService.findSelectableTimes(
                LocalDate.parse("2024-12-25"), 2L
        );

        // then
        assertNotNull(responses);
        assertEquals(4, responses.size());
        assertFalse(responses.get(0).alreadyBooked());
        assertFalse(responses.get(1).alreadyBooked());
        assertTrue(responses.get(2).alreadyBooked());
        assertFalse(responses.get(3).alreadyBooked());
    }

    @DisplayName("날짜 범위로 예약을 조회한다.")
    @Test
    void findByDateBetween() {
        // given
        LocalDate startDate = LocalDate.parse("2024-12-11");
        LocalDate endDate = LocalDate.parse("2024-12-24");

        // when
        List<Reservation> reservations = reservationService.findByDateBetween(startDate, endDate);

        // then
        assertNotNull(reservations);
        assertEquals(2, reservations.size());
        assertThat(reservations.get(0).getDate()).isEqualTo(LocalDate.parse("2024-12-12"));
        assertThat(reservations.get(1).getDate()).isEqualTo(LocalDate.parse("2024-12-23"));
    }

    @DisplayName("회원 ID로 예약을 조회한다.")
    @Test
    void findAllByMemberId() {
        // when
        List<MemberReservationResponse> responses = reservationService.findAllByMemberId(1L);

        // then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertThat(responses.get(0).date()).isEqualTo(LocalDate.parse("2024-12-12"));
        assertThat(responses.get(1).date()).isEqualTo(LocalDate.parse("2024-12-23"));
    }

    @DisplayName("예약 ID로 예약을 삭제한다.")
    @Test
    void deleteReservation() {
        // when
        ReservationDeleteResponse response = reservationService.delete(1L);

        // then
        assertNotNull(response);
        assertEquals(1, response.updateCount());
    }

    @DisplayName("예약 ID로 예약 삭제 시 존재하지 않으면 예외를 던진다.")
    @Test
    void deleteNonExistingReservationThrowsException() {
        // given
        long reservationId = 999L;

        // when & then
        assertThrows(NoSuchElementException.class, () -> reservationService.delete(reservationId));
    }
}
