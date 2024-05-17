package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import roomescape.IntegrationTestSupport;
import roomescape.controller.dto.ReservationStatusRequest;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;
import roomescape.exception.RoomEscapeBusinessException;
import roomescape.service.dto.ReservationResponse;
import roomescape.service.dto.ReservationSaveRequest;
import roomescape.service.dto.UserReservationResponse;

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

    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("예약 저장")
    @Test
    void saveReservation() {
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.parse("01:00")));
        Theme theme = themeRepository.save(new Theme("이름", "설명", "썸네일"));
        Member member = memberRepository.save(Member.createUser("고구마", "email@email.com", "1234"));

        ReservationSaveRequest reservationSaveRequest = new ReservationSaveRequest(member.getId(),
                LocalDate.parse("2025-11-11"),
                time.getId(), theme.getId(), ReservationStatusRequest.BOOKED);
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

    @DisplayName("예약 대기 저장")
    @Test
    void saveWaitReservation() {
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.parse("01:00")));
        Theme theme = themeRepository.save(new Theme("이름", "설명", "썸네일"));
        Member member = memberRepository.save(Member.createUser("고구마", "email@email.com", "1234"));

        ReservationSaveRequest reservationSaveRequest = new ReservationSaveRequest(member.getId(),
                LocalDate.parse("2025-11-11"),
                time.getId(), theme.getId(), ReservationStatusRequest.WAIT);
        ReservationResponse reservationResponse = reservationService.saveReservation(reservationSaveRequest);

        assertAll(
                () -> assertThat(reservationResponse.member().name()).isEqualTo("고구마"),
                () -> assertThat(reservationResponse.date()).isEqualTo(LocalDate.parse("2025-11-11")),
                () -> assertThat(reservationResponse.time().id()).isEqualTo(time.getId()),
                () -> assertThat(reservationResponse.time().startAt()).isEqualTo(time.getStartAt()),
                () -> assertThat(reservationResponse.theme().id()).isEqualTo(theme.getId()),
                () -> assertThat(reservationResponse.theme().name()).isEqualTo(theme.getName()),
                () -> assertThat(reservationResponse.theme().description()).isEqualTo(theme.getDescription()),
                () -> assertThat(reservationResponse.theme().thumbnail()).isEqualTo(theme.getThumbnail()),
                () -> assertThat(reservationResponse.status()).isEqualTo(ReservationStatus.WAIT.getValue())
        );
    }

    @DisplayName("존재하지 않는 예약 시간으로 예약 저장")
    @Test
    void timeForSaveReservationNotFound() {
        Member member = memberRepository.save(Member.createUser("고구마", "email@email.com", "1234"));

        ReservationSaveRequest reservationSaveRequest = new ReservationSaveRequest(member.getId(),
                LocalDate.parse("2025-11-11"), 100L, 1L, ReservationStatusRequest.BOOKED);
        assertThatThrownBy(() -> {
            reservationService.saveReservation(reservationSaveRequest);
        }).isInstanceOf(RoomEscapeBusinessException.class);
    }

    @DisplayName("예약 삭제")
    @Test
    void deleteReservation() {
        int size = reservationRepository.findAll().size();
        reservationService.deleteReservation(1L);
        assertThat(reservationRepository.findAll()).hasSize(size - 1);
    }

    @DisplayName("존재하지 않는 예약 삭제")
    @Test
    void deleteReservationNotFound() {
        assertThatThrownBy(() -> {
            reservationService.deleteReservation(100L);
        }).isInstanceOf(RoomEscapeBusinessException.class);
    }

    @DisplayName("중복된 예약을 시도하면 예약 대기 상태가 된다.")
    @Test
    void saveDuplicatedReservation() {
        ReservationSaveRequest reservationSaveRequest = new ReservationSaveRequest(1L, LocalDate.parse("2024-05-04"),
                1L, 1L, ReservationStatusRequest.BOOKED);
        ReservationResponse reservationResponse = reservationService.saveReservation(reservationSaveRequest);

        assertThat(reservationResponse.status()).isEqualTo("예약대기");
    }

    @DisplayName("내 예약을 조회한다.")
    @Test
    void findAllMyReservations() {
        // given
        long memberId = 2L;
        ReservationSaveRequest reservationSaveRequest = new ReservationSaveRequest(memberId, LocalDate.now(), 1L, 1L, ReservationStatusRequest.BOOKED);
        ReservationResponse reservationResponse = reservationService.saveReservation(reservationSaveRequest);

        // when
        List<UserReservationResponse> allUserReservation = reservationService.findAllUserReservation(memberId);

        // then
        assertAll(
                () -> assertThat(allUserReservation).hasSize(1),
                () -> assertThat(allUserReservation.get(0).date()).isEqualTo(reservationResponse.date()),
                () -> assertThat(allUserReservation.get(0).time()).isEqualTo(reservationResponse.time().startAt()),
                () -> assertThat(allUserReservation.get(0).theme()).isEqualTo(reservationResponse.theme().name()),
                () -> assertThat(allUserReservation.get(0).status()).isEqualTo(ReservationStatus.BOOKED.getValue())
        );
    }
}
