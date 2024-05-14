package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import roomescape.IntegrationTestSupport;
import roomescape.domain.Member;
import roomescape.domain.MemberRepository;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimeRepository;
import roomescape.domain.Theme;
import roomescape.domain.ThemeRepository;
import roomescape.exception.RoomEscapeBusinessException;
import roomescape.service.dto.ReservationConditionRequest;
import roomescape.service.dto.ReservationResponse;
import roomescape.service.dto.ReservationSaveRequest;

@Transactional
class ReservationServiceTest extends IntegrationTestSupport {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("예약 저장")
    @Test
    void saveReservation() {
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.parse("10:00")));
        Theme theme = themeRepository.save(new Theme("이름", "설명", "썸네일"));
        Member member = memberRepository.save(Member.createUser("고구마", "email@email.com", "1234"));

        ReservationSaveRequest reservationSaveRequest = new ReservationSaveRequest(member.getId(), LocalDate.parse("2025-11-11"),
                time.getId(), theme.getId());
        ReservationResponse reservationResponse = reservationService.saveReservation(reservationSaveRequest);

        assertAll(
                () -> assertThat(reservationResponse.member().name()).isEqualTo("고구마"),
                () -> assertThat(reservationResponse.date()).isEqualTo(LocalDate.parse("2025-11-11")),
                () -> assertThat(reservationResponse.time().id()).isEqualTo(time.getId()),
                () -> assertThat(reservationResponse.time().startAt()).isEqualTo(time.getStartAt()),
                () -> assertThat(reservationResponse.theme().id()).isEqualTo(theme.getId()),
                () -> assertThat(reservationResponse.theme().name()).isEqualTo(theme.getName()),
                () -> assertThat(reservationResponse.theme().description()).isEqualTo(theme.getDescription()),
                () -> assertThat(reservationResponse.theme().thumbnail()).isEqualTo(theme.getThumbnail())
        );
    }

    @DisplayName("존재하지 않는 예약 시간으로 예약 저장")
    @Test
    void timeForSaveReservationNotFound() {
        Member member = memberRepository.save(Member.createUser("고구마", "email@email.com", "1234"));

        ReservationSaveRequest reservationSaveRequest = new ReservationSaveRequest(member.getId(), LocalDate.parse("2025-11-11"), 100L, 1L);
        assertThatThrownBy(() -> {
            reservationService.saveReservation(reservationSaveRequest);
        }).isInstanceOf(RoomEscapeBusinessException.class);
    }

    @DisplayName("예약 삭제")
    @Test
    void deleteReservation() {
        reservationService.deleteReservation(1L);
        assertThat(reservationService.findReservationsByCondition(new ReservationConditionRequest(null, null, null, null))).hasSize(12);
    }

    @DisplayName("존재하지 않는 예약 삭제")
    @Test
    void deleteReservationNotFound() {
        assertThatThrownBy(() -> {
            reservationService.deleteReservation(100L);
        }).isInstanceOf(RoomEscapeBusinessException.class);
    }

    @DisplayName("중복된 예약 저장")
    @Test
    void saveDuplicatedReservation() {
        ReservationSaveRequest reservationSaveRequest = new ReservationSaveRequest(1L, LocalDate.parse("2024-05-04"),
                1L, 1L);
        assertThatThrownBy(() -> reservationService.saveReservation(reservationSaveRequest))
                .isInstanceOf(RoomEscapeBusinessException.class);
    }
}
