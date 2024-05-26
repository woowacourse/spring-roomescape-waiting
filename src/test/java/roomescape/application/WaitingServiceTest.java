package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.fixture.Fixture.MEMBER_1;
import static roomescape.fixture.Fixture.MEMBER_2;
import static roomescape.fixture.Fixture.RESERVATION_TIME_1;
import static roomescape.fixture.Fixture.THEME_1;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.dto.request.WaitingRequest;
import roomescape.application.dto.response.MemberResponse;
import roomescape.application.dto.response.ReservationResponse;
import roomescape.application.dto.response.ReservationTimeResponse;
import roomescape.application.dto.response.ThemeResponse;
import roomescape.application.dto.response.WaitingResponse;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.WaitingRepository;
import roomescape.domain.reservation.detail.ReservationDetail;
import roomescape.domain.reservation.detail.ReservationTime;
import roomescape.domain.reservation.detail.ReservationTimeRepository;
import roomescape.domain.reservation.detail.Theme;
import roomescape.domain.reservation.detail.ThemeRepository;
import roomescape.exception.BadRequestException;
import roomescape.exception.UnauthorizedException;

class WaitingServiceTest extends BaseServiceTest {

    @Autowired
    private WaitingService waitingService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    private Member member1;
    private Member member2;
    private ReservationTime time1;
    private Theme theme;

    @BeforeEach
    void setUp() {
        member1 = memberRepository.save(MEMBER_1);
        member2 = memberRepository.save(MEMBER_2);
        theme = themeRepository.save(THEME_1);
        time1 = reservationTimeRepository.save(RESERVATION_TIME_1);
    }

    @Nested
    @DisplayName("예약 대기를 추가하는 경우")
    class AddReservationWaiting {

        @Test
        @DisplayName("성공한다.")
        void success() {
            // given
            LocalDateTime currentDateTime = LocalDateTime.of(2024, 4, 8, 10, 0);
            LocalDate waitingDate = LocalDate.of(2024, 4, 9);
            ReservationDetail detail = new ReservationDetail(waitingDate, time1, theme);
            reservationRepository.save(new Reservation(detail, member1));

            // when
            WaitingRequest request = new WaitingRequest(
                    currentDateTime,
                    waitingDate,
                    time1.getId(),
                    theme.getId(),
                    member2.getId()
            );

            WaitingResponse response = waitingService.addWaiting(request);

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(response).isNotNull();
                softly.assertThat(response.date()).isEqualTo(waitingDate);
                softly.assertThat(response.member()).isEqualTo(MemberResponse.from(member2));
                softly.assertThat(response.time()).isEqualTo(ReservationTimeResponse.from(time1));
                softly.assertThat(response.theme()).isEqualTo(ThemeResponse.from(theme));
            });
        }

        @Test
        @DisplayName("현재 예약이 없을 경우 예외를 발생시킨다.")
        void failWhenReservationWaitingExists() {
            LocalDateTime currentDateTime = LocalDateTime.of(2024, 4, 8, 10, 0);
            LocalDate waitingDate = LocalDate.of(2024, 4, 9);

            WaitingRequest request = new WaitingRequest(
                    currentDateTime,
                    waitingDate,
                    time1.getId(),
                    theme.getId(),
                    member1.getId()
            );

            assertThatThrownBy(() -> waitingService.addWaiting(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("예약이 존재하지 않아 예약 대기를 할 수 없습니다.");
        }

        @Test
        @DisplayName("해당 회원이 이미 예약을 한 경우 예외를 발생시킨다.")
        void failWhenMemberAlreadyReserved() {
            // given
            LocalDateTime currentDateTime = LocalDateTime.of(2024, 4, 8, 10, 0);
            LocalDate waitingDate = LocalDate.of(2024, 4, 9);
            ReservationDetail detail = new ReservationDetail(waitingDate, time1, theme);
            reservationRepository.save(new Reservation(detail, member1));

            // when
            WaitingRequest request = new WaitingRequest(
                    currentDateTime,
                    waitingDate,
                    time1.getId(),
                    theme.getId(),
                    member1.getId()
            );

            // then
            assertThatThrownBy(() -> waitingService.addWaiting(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("해당 회원은 이미 예약을 하였습니다.");
        }

        @Test
        @DisplayName("해당 회원이 이미 예약 대기를 한 경우 예외를 발생시킨다.")
        void failWhenMemberAlreadyReservedWaiting() {
            // given
            LocalDateTime currentDateTime = LocalDateTime.of(2024, 4, 8, 10, 0);
            LocalDate waitingDate = LocalDate.of(2024, 4, 9);
            ReservationDetail detail = new ReservationDetail(waitingDate, time1, theme);
            reservationRepository.save(new Reservation(detail, member2));
            waitingRepository.save(new Waiting(detail, member1));

            // when
            WaitingRequest request = new WaitingRequest(
                    currentDateTime,
                    waitingDate,
                    time1.getId(),
                    theme.getId(),
                    member1.getId()
            );

            // then
            assertThatThrownBy(() -> waitingService.addWaiting(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("해당 회원은 이미 예약 대기를 하였습니다.");
        }
    }

    @Test
    @DisplayName("예약 대기들을 조회한다.")
    void getReservationWaitings() {
        LocalDate date = LocalDate.of(2024, 4, 9);
        ReservationDetail detail = new ReservationDetail(date, time1, theme);
        reservationRepository.save(new Reservation(detail, member1));
        waitingRepository.save(new Waiting(detail, member2));

        List<WaitingResponse> responses = waitingService.getWaitings();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(responses).hasSize(1);
            softly.assertThat(responses.get(0).date()).isEqualTo(date);
            softly.assertThat(responses.get(0).member()).isEqualTo(MemberResponse.from(member2));
            softly.assertThat(responses.get(0).time()).isEqualTo(ReservationTimeResponse.from(time1));
            softly.assertThat(responses.get(0).theme()).isEqualTo(ThemeResponse.from(theme));
        });
    }

    @Nested
    @DisplayName("예약 대기에서 예약을 승인하는 경우")
    class ApproveReservationWaiting {

        @Test
        @DisplayName("성공한다.")
        void success() {
            LocalDateTime currentDateTime = LocalDateTime.of(2024, 4, 8, 10, 0);
            LocalDate waitingDate = LocalDate.of(2024, 4, 9);
            ReservationDetail detail = new ReservationDetail(waitingDate, time1, theme);
            Waiting waiting = waitingRepository.save(new Waiting(detail, member2));

            ReservationResponse reservationResponse = waitingService.approveWaitingToReservation(currentDateTime,
                    waiting.getId());

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(waitingRepository.findById(waiting.getId())).isEmpty();
                softly.assertThat(reservationRepository.findById(reservationResponse.id())).isPresent();
            });
        }

        @Test
        @DisplayName("이미 예약이 존재할 경우 예외를 발생시킨다.")
        void failWhenReservationExists() {
            LocalDateTime currentDateTime = LocalDateTime.of(2024, 4, 8, 10, 0);
            LocalDate waitingDate = LocalDate.of(2024, 4, 9);
            ReservationDetail detail = new ReservationDetail(waitingDate, time1, theme);
            reservationRepository.save(new Reservation(detail, member2));
            Waiting waiting = waitingRepository.save(new Waiting(detail, member2));

            Long waitingId = waiting.getId();
            assertThatThrownBy(() -> waitingService.approveWaitingToReservation(currentDateTime, waitingId))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("이미 예약이 존재합니다.");
        }
    }

    @Nested
    @DisplayName("예약 대기를 삭제하는 경우")
    class DeleteReservationWaiting {

        @Test
        @DisplayName("성공한다.")
        void success() {
            LocalDate date = LocalDate.of(2024, 4, 9);
            ReservationDetail detail = new ReservationDetail(date, time1, theme);
            Waiting waiting = waitingRepository.save(new Waiting(detail, member2));

            waitingService.deleteWaitingById(waiting.getId(), member2.getId());

            assertThat(waitingRepository.findById(waiting.getId())).isEmpty();
        }

        @Test
        @DisplayName("자신의 예약 대기가 아닐 경우 예외를 발생시킨다.")
        void failWhenNotOwnReservationWaiting() {
            LocalDate date = LocalDate.of(2024, 4, 9);
            ReservationDetail detail = new ReservationDetail(date, time1, theme);
            Waiting waiting = waitingRepository.save(new Waiting(detail, member1));

            Long waitingId = waiting.getId();
            Long memberId = member2.getId();
            assertThatThrownBy(() -> waitingService.deleteWaitingById(waitingId, memberId))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("자신의 예약 대기만 취소할 수 있습니다.");
        }

    }

    @Test
    @DisplayName("예약 대기를 거부하는 경우 성공한다.")
    void success() {
        LocalDate date = LocalDate.of(2024, 4, 9);
        ReservationDetail detail = new ReservationDetail(date, time1, theme);
        Waiting waiting = waitingRepository.save(new Waiting(detail, member1));

        waitingService.rejectWaitingToReservation(waiting.getId());

        assertThat(waitingRepository.findById(waiting.getId())).isEmpty();
    }
}
