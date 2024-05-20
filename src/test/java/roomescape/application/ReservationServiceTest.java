package roomescape.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.dto.request.ReservationRequest;
import roomescape.application.dto.request.ReservationWaitingRequest;
import roomescape.application.dto.response.MemberResponse;
import roomescape.application.dto.response.MyReservationResponse;
import roomescape.application.dto.response.ReservationResponse;
import roomescape.application.dto.response.ReservationTimeResponse;
import roomescape.application.dto.response.ThemeResponse;
import roomescape.domain.exception.DomainNotFoundException;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;
import roomescape.exception.BadRequestException;
import roomescape.exception.UnauthorizedException;

class ReservationServiceTest extends BaseServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    private Member member1;
    private Member member2;
    private ReservationTime time1;
    private ReservationTime time2;
    private Theme theme;

    @BeforeEach
    void setUp() {
        member1 = memberRepository.save(new Member("ex@gmail.com", "password", "구름", Role.USER));
        member2 = memberRepository.save(new Member("ex2@gmail.com", "password", "바다", Role.USER));
        theme = themeRepository.save(new Theme("테마", "테마 설명", "https://example.com"));
        time1 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 30)));
        time2 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 30)));
    }

    @Nested
    @DisplayName("예약을 추가하는 경우 ")
    class AddReservation {

        @Test
        @DisplayName("성공한다.")
        void success() {
            LocalDate date = LocalDate.of(2024, 4, 9);
            ReservationRequest request = new ReservationRequest(
                    date,
                    time1.getId(),
                    theme.getId(),
                    member1.getId()
            );
            ReservationResponse response = reservationService.addReservation(request);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(response).isNotNull();
                softly.assertThat(response.date()).isEqualTo(date);
                softly.assertThat(response.member()).isEqualTo(MemberResponse.from(member1));
                softly.assertThat(response.time()).isEqualTo(ReservationTimeResponse.from(time1));
                softly.assertThat(response.theme()).isEqualTo(ThemeResponse.from(theme));
            });
        }

        @Test
        @DisplayName("이미 해당 날짜/시간의 테마에 예약이 존재하면 예외를 발생시킨다.")
        void failWhenReservationExists() {
            LocalDate date = LocalDate.of(2024, 4, 9);
            reservationRepository.save(new Reservation(date, member1, time1, theme, ReservationStatus.RESERVED));

            ReservationRequest request = new ReservationRequest(
                    LocalDate.of(2024, 4, 9),
                    time1.getId(),
                    theme.getId(),
                    member1.getId()
            );

            assertThatThrownBy(() -> reservationService.addReservation(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("이미 예약이 존재합니다.");
        }
    }

    @Nested
    @DisplayName("예약 대기를 추가하는 경우")
    class AddReservationWaiting {

        @Test
        @DisplayName("성공한다.")
        void success() {
            LocalDate date = LocalDate.of(2024, 4, 9);
            reservationRepository.save(new Reservation(date, member1, time1, theme, ReservationStatus.RESERVED));

            ReservationWaitingRequest request = new ReservationWaitingRequest(
                    date,
                    time1.getId(),
                    theme.getId(),
                    member2.getId()
            );
            ReservationResponse response = reservationService.addReservationWaiting(request);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(response).isNotNull();
                softly.assertThat(response.date()).isEqualTo(date);
                softly.assertThat(response.member()).isEqualTo(MemberResponse.from(member2));
                softly.assertThat(response.time()).isEqualTo(ReservationTimeResponse.from(time1));
                softly.assertThat(response.theme()).isEqualTo(ThemeResponse.from(theme));
            });
        }

        @Test
        @DisplayName("현재 예약이 없을 경우 예외를 발생시킨다.")
        void failWhenReservationWaitingExists() {
            LocalDate date = LocalDate.of(2024, 4, 9);

            ReservationWaitingRequest request = new ReservationWaitingRequest(
                    date,
                    time1.getId(),
                    theme.getId(),
                    member1.getId()
            );

            assertThatThrownBy(() -> reservationService.addReservationWaiting(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("예약이 존재하지 않아 예약 대기를 할 수 없습니다.");
        }

        @Test
        @DisplayName("해당 회원이 이미 예약을 한 경우 예외를 발생시킨다.")
        void failWhenMemberAlreadyReserved() {
            LocalDate date = LocalDate.of(2024, 4, 9);
            reservationRepository.save(new Reservation(date, member1, time1, theme, ReservationStatus.RESERVED));

            ReservationWaitingRequest request = new ReservationWaitingRequest(
                    date,
                    time1.getId(),
                    theme.getId(),
                    member1.getId()
            );

            assertThatThrownBy(() -> reservationService.addReservationWaiting(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("해당 회원은 이미 예약을 하였습니다.");
        }

        @Test
        @DisplayName("해당 회원이 이미 예약 대기를 한 경우 예외를 발생시킨다.")
        void failWhenMemberAlreadyReservedWaiting() {
            LocalDate date = LocalDate.of(2024, 4, 9);
            reservationRepository.save(new Reservation(date, member1, time1, theme, ReservationStatus.RESERVED));
            reservationRepository.save(new Reservation(date, member2, time1, theme, ReservationStatus.WAITING));

            ReservationWaitingRequest request = new ReservationWaitingRequest(
                    date,
                    time1.getId(),
                    theme.getId(),
                    member2.getId()
            );

            assertThatThrownBy(() -> reservationService.addReservationWaiting(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("해당 회원은 이미 예약 대기를 하였습니다.");
        }
    }

    @Test
    @DisplayName("조건에 맞는 예약들을 조회한다.")
    void getReservationsByConditions() {
        LocalDate date = LocalDate.of(2024, 4, 9);
        reservationRepository.save(new Reservation(date, member1, time1, theme, ReservationStatus.RESERVED));

        List<ReservationResponse> responses = reservationService.getReservationsByConditions(
                member1.getId(),
                theme.getId(),
                date,
                date
        );

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(responses).hasSize(1);
            softly.assertThat(responses.get(0).date()).isEqualTo(date);
            softly.assertThat(responses.get(0).member()).isEqualTo(MemberResponse.from(member1));
            softly.assertThat(responses.get(0).time()).isEqualTo(ReservationTimeResponse.from(time1));
            softly.assertThat(responses.get(0).theme()).isEqualTo(ThemeResponse.from(theme));
        });
    }

    @Test
    @DisplayName("회원 아이디로 예약과 예약 대기들을 예약 대기 순번을 포함해서 조회한다.")
    void getMyReservationWithRanks() {
        LocalDate date = LocalDate.of(2024, 4, 9);
        reservationRepository.save(new Reservation(date, member1, time1, theme, ReservationStatus.RESERVED));
        reservationRepository.save(new Reservation(date, member2, time2, theme, ReservationStatus.RESERVED));
        reservationRepository.save(new Reservation(date, member1, time2, theme, ReservationStatus.WAITING));

        List<MyReservationResponse> responses = reservationService.getMyReservationWithRanks(member1.getId());

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(responses).hasSize(2);
            softly.assertThat(responses.get(0).date()).isEqualTo(date);
            softly.assertThat(responses.get(0).time()).isEqualTo("10:30");
            softly.assertThat(responses.get(0).theme()).isEqualTo("테마");
            softly.assertThat(responses.get(0).rank()).isEqualTo(0);
            softly.assertThat(responses.get(0).status()).isEqualTo(ReservationStatus.RESERVED);

            softly.assertThat(responses.get(1).date()).isEqualTo(date);
            softly.assertThat(responses.get(1).time()).isEqualTo("11:30");
            softly.assertThat(responses.get(1).theme()).isEqualTo("테마");
            softly.assertThat(responses.get(1).rank()).isEqualTo(1);
            softly.assertThat(responses.get(1).status()).isEqualTo(ReservationStatus.WAITING);
        });
    }

    @Test
    @DisplayName("예약 대기들을 조회한다.")
    void getReservationWaitings() {
        LocalDate date = LocalDate.of(2024, 4, 9);
        reservationRepository.save(new Reservation(date, member1, time1, theme, ReservationStatus.RESERVED));
        reservationRepository.save(new Reservation(date, member2, time1, theme, ReservationStatus.WAITING));

        List<ReservationResponse> responses = reservationService.getReservationWaitings();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(responses).hasSize(1);
            softly.assertThat(responses.get(0).member()).isEqualTo(MemberResponse.from(member2));
            softly.assertThat(responses.get(0).time()).isEqualTo(ReservationTimeResponse.from(time1));
            softly.assertThat(responses.get(0).theme()).isEqualTo(ThemeResponse.from(theme));
        });
    }

    @Nested
    @DisplayName("예약을 삭제하는 경우")
    class DeleteReservation {

        @Test
        @DisplayName("성공한다.")
        void success() {
            LocalDate date = LocalDate.of(2024, 4, 9);
            Reservation reservation = reservationRepository
                    .save(new Reservation(date, member1, time1, theme, ReservationStatus.RESERVED));

            reservationService.deleteReservationById(reservation.getId());

            List<Reservation> reservations = reservationRepository.findAll();

            assertThat(reservations).isEmpty();
        }

        @Test
        @DisplayName("해당 id의 예약이 존재하지 않으면 예외를 발생시킨다.")
        void failWhenReservationNotExists() {
            assertThatThrownBy(() -> reservationService.deleteReservationById(-1L))
                    .isInstanceOf(DomainNotFoundException.class)
                    .hasMessage("해당 id의 예약이 존재하지 않습니다.");
        }
    }

    @Nested
    @DisplayName("예약 대기를 삭제하는 경우")
    class DeleteReservationWaiting {

        @Test
        @DisplayName("성공한다.")
        void success() {
            LocalDate date = LocalDate.of(2024, 4, 9);
            reservationRepository.save(new Reservation(date, member1, time1, theme, ReservationStatus.RESERVED));
            Reservation reservation = reservationRepository
                    .save(new Reservation(date, member2, time1, theme, ReservationStatus.WAITING));

            reservationService.deleteReservationWaitingById(reservation.getId(), member2.getId());

            Optional<Reservation> reservationById = reservationRepository.findById(reservation.getId());

            assertThat(reservationById).isEmpty();
        }

        @Test
        @DisplayName("자신의 예약 대기가 아닐 경우 예외를 발생시킨다.")
        void failWhenNotOwnReservationWaiting() {
            LocalDate date = LocalDate.of(2024, 4, 9);
            reservationRepository.save(new Reservation(date, member1, time1, theme, ReservationStatus.RESERVED));
            Reservation reservation = reservationRepository
                    .save(new Reservation(date, member2, time1, theme, ReservationStatus.WAITING));

            Long reservationId = reservation.getId();
            Long memberId = member1.getId();
            assertThatThrownBy(() -> reservationService.deleteReservationWaitingById(reservationId, memberId))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("자신의 예약 대기만 취소할 수 있습니다.");
        }

    }

    @Nested
    @DisplayName("예약 대기에서 예약을 승인하는 경우")
    class ApproveReservationWaiting {

        @Test
        @DisplayName("성공한다.")
        void success() {
            LocalDate date = LocalDate.of(2024, 4, 9);
            Reservation reservation = reservationRepository
                    .save(new Reservation(date, member2, time1, theme, ReservationStatus.WAITING));

            ReservationResponse response = reservationService.approveReservationWaiting(reservation.getId());

            assertThat(response.status()).isEqualTo(ReservationStatus.RESERVED);
        }

        @Test
        @DisplayName("예약 대기가 아닐 경우 예외를 발생시킨다.")
        void failWhenNotReservationWaiting() {
            LocalDate date = LocalDate.of(2024, 4, 9);
            Reservation reservation = reservationRepository.save(
                    new Reservation(date, member1, time1, theme, ReservationStatus.RESERVED));

            Long reservationId = reservation.getId();
            assertThatThrownBy(() -> reservationService.approveReservationWaiting(reservationId))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("예약 대기 상태가 아닙니다.");
        }

        @Test
        @DisplayName("이미 예약이 존재할 경우 예외를 발생시킨다.")
        void failWhenReservationExists() {
            LocalDate date = LocalDate.of(2024, 4, 9);
            reservationRepository.save(new Reservation(date, member1, time1, theme, ReservationStatus.RESERVED));
            Reservation reservation = reservationRepository.save(
                    new Reservation(date, member2, time1, theme, ReservationStatus.WAITING));

            Long reservationId = reservation.getId();
            assertThatThrownBy(() -> reservationService.approveReservationWaiting(reservationId))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("이미 예약이 존재합니다.");
        }
    }

    @Nested
    @DisplayName("예약 대기를 거부하는 경우")
    class RejectReservationWaiting {

        @Test
        @DisplayName("성공한다.")
        void success() {
            LocalDate date = LocalDate.of(2024, 4, 9);
            reservationRepository.save(new Reservation(date, member1, time1, theme, ReservationStatus.RESERVED));
            Reservation reservation = reservationRepository.save(
                    new Reservation(date, member2, time1, theme, ReservationStatus.WAITING));

            reservationService.rejectReservationWaiting(reservation.getId());

            assertThat(reservationRepository.findById(reservation.getId())).isEmpty();
        }

        @Test
        @DisplayName("예약 대기가 아닐 경우 예외를 발생시킨다.")
        void failWhenNotReservationWaiting() {
            LocalDate date = LocalDate.of(2024, 4, 9);
            Reservation reservation = reservationRepository.save(
                    new Reservation(date, member1, time1, theme, ReservationStatus.RESERVED));

            Long reservationId = reservation.getId();
            assertThatThrownBy(() -> reservationService.rejectReservationWaiting(reservationId))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("예약 대기 상태가 아닙니다.");
        }
    }
}
