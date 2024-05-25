package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.domain.reservationwaiting.ReservationWaitingRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.service.dto.request.CreateReservationRequest;
import roomescape.service.dto.response.ReservationResponse;
import roomescape.support.fixture.MemberFixture;
import roomescape.support.fixture.ReservationFixture;
import roomescape.support.fixture.ReservationTimeFixture;
import roomescape.support.fixture.ReservationWaitingFixture;
import roomescape.support.fixture.ThemeFixture;

class ReservationWaitingServiceTest extends BaseServiceTest {

    @Autowired
    private ReservationWaitingService reservationWaitingService;

    @Autowired
    private ReservationWaitingRepository reservationWaitingRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    private Theme theme;

    private ReservationTime time;

    private Reservation notSavedReservation;

    private Member prin;

    @BeforeEach
    void setUp() {
        Member member = memberRepository.save(MemberFixture.user());
        theme = themeRepository.save(ThemeFixture.theme());
        time = reservationTimeRepository.save(ReservationTimeFixture.ten());
        notSavedReservation = ReservationFixture.create("2024-05-24", member, time, theme);
        prin = memberRepository.save(MemberFixture.prin());
    }

    @Test
    @DisplayName("예약 대기를 생성한다.")
    void createReservationWaiting() {
        Reservation reservation = reservationRepository.save(notSavedReservation);
        CreateReservationRequest request = new CreateReservationRequest(reservation.getDate(), time.getId(),
                theme.getId(), prin.getId());

        ReservationResponse response = reservationWaitingService.addReservationWaiting(request);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.date()).isEqualTo(reservation.getDate());
            softly.assertThat(response.name()).isEqualTo(prin.getName());
            softly.assertThat(response.theme()).isEqualTo(theme.getRawName());
            softly.assertThat(response.startAt()).isEqualTo(time.getStartAt());
        });
    }

    @Test
    @DisplayName("확정된 예약이 존재하지 않으면 예약 대기를 생성할 수 없다.")
    void createReservationWaitingFailWhenReservationNotFound() {
        LocalDate date = LocalDate.parse("2024-05-24");
        CreateReservationRequest request = new CreateReservationRequest(date, time.getId(), theme.getId(),
                prin.getId());

        assertThatThrownBy(() -> reservationWaitingService.addReservationWaiting(request))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("예약이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("이미 예약한 멤버는 예약 대기를 생성할 수 없다")
    void createReservationWaitingFailWhenAlreadyReserved() {
        Reservation reservation = reservationRepository.save(notSavedReservation);
        CreateReservationRequest request = new CreateReservationRequest(reservation.getDate(), time.getId(),
                theme.getId(), reservation.getMember().getId());

        assertThatThrownBy(() -> reservationWaitingService.addReservationWaiting(request))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("예약자와 대기자가 동일합니다.");
    }

    @Test
    @DisplayName("예약 대기 제한 개수를 초과하면 예약 대기를 생성할 수 없다.")
    void createReservationWaitingFailWhenExceedLimit() {
        Reservation reservation = reservationRepository.save(notSavedReservation);
        for (int count = 0; count < 10; count++) {
            Member prevWaitingMember = memberRepository.save(MemberFixture.create("waiting" + count + "@email.com"));
            reservationWaitingRepository.save(ReservationWaitingFixture.create(reservation, prevWaitingMember));
        }
        CreateReservationRequest request = new CreateReservationRequest(reservation.getDate(), time.getId(),
                theme.getId(), prin.getId());

        assertThatThrownBy(() -> reservationWaitingService.addReservationWaiting(request))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("예약 대기열이 가득 찼습니다.");
    }

    @Test
    @DisplayName("멤버는 한 예약에 대해 두 개 이상의 예약 대기를 생성할 수 없다.")
    void createReservationWaitingFailWhenAlreadyWaiting() {
        Reservation reservation = reservationRepository.save(notSavedReservation);
        reservationWaitingRepository.save(ReservationWaitingFixture.create(reservation, prin));
        CreateReservationRequest request = new CreateReservationRequest(reservation.getDate(), time.getId(),
                theme.getId(), prin.getId());

        assertThatThrownBy(() -> reservationWaitingService.addReservationWaiting(request))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("현재 멤버는 이미 예약 대기 중입니다.");
    }

    @Test
    @DisplayName("예약 대기를 삭제한다.")
    void deleteReservationWaiting() {
        Reservation reservation = reservationRepository.save(notSavedReservation);
        ReservationWaiting savedWaiting = reservationWaitingRepository.save(
                ReservationWaitingFixture.create(reservation, prin));

        reservationWaitingService.deleteReservationWaiting(savedWaiting.getId(), prin.getId());

        assertThat(reservationWaitingRepository.findById(savedWaiting.getId())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않은 예약 대기는 삭제할 수 없다.")
    void deleteReservationWaitingFailWhenNotFound() {
        assertThatThrownBy(
                () -> reservationWaitingService.deleteReservationWaiting(1L, prin.getId()))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 예약 대기입니다.");
    }

    @Test
    @DisplayName("관리자가 아닌 멤버는 다른 멤버의 예약 대기를 삭제할 수 없다.")
    void deleteReservationWaitingFailWhenNotAdmin() {
        Reservation reservation = reservationRepository.save(notSavedReservation);
        ReservationWaiting savedWaiting = reservationWaitingRepository.save(
                ReservationWaitingFixture.create(reservation, prin));

        Member notAdmin = memberRepository.save(MemberFixture.jamie());
        assertThatThrownBy(
                () -> reservationWaitingService.deleteReservationWaiting(savedWaiting.getId(), notAdmin.getId()))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("예약 대기한 회원이 아닙니다.");
    }

    @Test
    @DisplayName("관리자는 다른 멤버의 예약 대기를 삭제할 수 있다.")
    void deleteReservationWaitingSuccessWhenAdmin() {
        Reservation reservation = reservationRepository.save(notSavedReservation);
        ReservationWaiting savedWaiting = reservationWaitingRepository.save(
                ReservationWaitingFixture.create(reservation, prin));

        Member admin = memberRepository.save(MemberFixture.admin());
        reservationWaitingService.deleteReservationWaiting(savedWaiting.getId(), admin.getId());

        assertThat(reservationWaitingRepository.findById(savedWaiting.getId())).isEmpty();
    }
}
