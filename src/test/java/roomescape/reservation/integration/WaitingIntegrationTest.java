package roomescape.reservation.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.error.exception.BadRequestException;
import roomescape.global.error.exception.UnauthorizedException;
import roomescape.member.entity.Member;
import roomescape.member.entity.RoleType;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.dto.request.WaitingCreateRequest;
import roomescape.reservation.entity.Reservation;
import roomescape.reservation.entity.ReservationTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.service.WaitingService;
import roomescape.theme.entity.Theme;
import roomescape.theme.repository.ThemeRepository;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Transactional
class WaitingIntegrationTest {

    @Autowired
    private WaitingService waitingService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("대기 예약을 생성한다.")
    void createWaiting() {
        // given
        var member = memberRepository.save(new Member("미소", "miso@email.com", "password", RoleType.USER));
        var theme = themeRepository.save(new Theme("테마", "설명", "썸네일"));
        var time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        var date = LocalDate.now().plusDays(1);
        var otherMember = memberRepository.save(new Member("크루", "crew@email.com", "password", RoleType.USER));
        reservationRepository.save(new Reservation(date, time, theme, otherMember));
        var request = new WaitingCreateRequest(date, time.getId(), theme.getId());

        // when
        var response = waitingService.createWaiting(member.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.date()).isEqualTo(date),
                () -> assertThat(response.startAt()).isEqualTo(time.getStartAt()),
                () -> assertThat(response.themeName()).isEqualTo(theme.getName()),
                () -> assertThat(response.memberName()).isEqualTo(member.getName())
        );
    }

    @Test
    @DisplayName("과거 날짜로 대기 예약을 생성하면 예외가 발생한다.")
    void createWaitingWithPastDate() {
        // given
        var member = memberRepository.save(new Member("미소", "miso@email.com", "password", RoleType.USER));
        var theme = themeRepository.save(new Theme("테마", "설명", "썸네일"));
        var time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        var date = LocalDate.now().minusDays(1);
        var otherMember = memberRepository.save(new Member("크루", "crew@email.com", "password", RoleType.USER));
        reservationRepository.save(new Reservation(date, time, theme, otherMember));
        var request = new WaitingCreateRequest(date, time.getId(), theme.getId());

        // when & then
        assertThatThrownBy(() -> waitingService.createWaiting(member.getId(), request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("과거 날짜는 예약할 수 없습니다.");
    }

    @Test
    @DisplayName("이미 예약한 사용자가 대기 예약을 생성하면 예외가 발생한다.")
    void createWaitingByReservedMember() {
        // given
        var member = memberRepository.save(new Member("미소", "miso@email.com", "password", RoleType.USER));
        var theme = themeRepository.save(new Theme("테마", "설명", "썸네일"));
        var time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        var date = LocalDate.now().plusDays(1);
        reservationRepository.save(new Reservation(date, time, theme, member));
        var request = new WaitingCreateRequest(date, time.getId(), theme.getId());

        // when & then
        assertThatThrownBy(() -> waitingService.createWaiting(member.getId(), request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이미 예약한 사용자는 해당 예약에 대기 신청할 수 없습니다.");
    }

    @Test
    @DisplayName("모든 대기 예약을 조회한다.")
    void getAllWaitings() {
        // given
        var member = memberRepository.save(new Member("미소", "miso@email.com", "password", RoleType.USER));
        var theme = themeRepository.save(new Theme("테마", "설명", "썸네일"));
        var time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        var date = LocalDate.now().plusDays(1);
        var otherMember = memberRepository.save(new Member("크루", "crew@email.com", "password", RoleType.USER));
        reservationRepository.save(new Reservation(date, time, theme, otherMember));
        var request = new WaitingCreateRequest(date, time.getId(), theme.getId());
        waitingService.createWaiting(member.getId(), request);

        // when
        var responses = waitingService.getAllWaitings();

        // then
        var response = responses.getFirst();
        assertAll(
                () -> assertThat(responses).hasSize(1),
                () -> assertThat(response.id()).isNotNull(),
                () -> assertThat(response.date()).isEqualTo(date),
                () -> assertThat(response.startAt()).isEqualTo(time.getStartAt()),
                () -> assertThat(response.memberName()).isEqualTo(member.getName()),
                () -> assertThat(response.themeName()).isEqualTo(theme.getName())
        );
    }

    @Test
    @DisplayName("대기 예약을 삭제한다.")
    void deleteWaiting() {
        // given
        var member = memberRepository.save(new Member("미소", "miso@email.com", "password", RoleType.USER));
        var theme = themeRepository.save(new Theme("테마", "설명", "썸네일"));
        var time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        var date = LocalDate.now().plusDays(1);
        var otherMember = memberRepository.save(new Member("크루", "crew@email.com", "password", RoleType.USER));
        reservationRepository.save(new Reservation(date, time, theme, otherMember));
        var request = new WaitingCreateRequest(date, time.getId(), theme.getId());
        var response = waitingService.createWaiting(member.getId(), request);

        // when
        waitingService.deleteWaiting(member.getId(), response.id());

        // then
        var waitings = waitingService.getAllWaitings();
        assertThat(waitings).isEmpty();
    }

    @Test
    @DisplayName("다른 사용자의 대기 예약을 삭제하려고 하면 예외가 발생한다.")
    void deleteWaitingByOtherMember() {
        // given
        var member = memberRepository.save(new Member("미소", "miso@email.com", "password", RoleType.USER));
        var theme = themeRepository.save(new Theme("테마", "설명", "썸네일"));
        var time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        var date = LocalDate.now().plusDays(1);
        var otherMember = memberRepository.save(new Member("크루", "crew@email.com", "password", RoleType.USER));
        reservationRepository.save(new Reservation(date, time, theme, otherMember));
        var request = new WaitingCreateRequest(date, time.getId(), theme.getId());
        var response = waitingService.createWaiting(member.getId(), request);

        // when & then
        assertThatThrownBy(() -> waitingService.deleteWaiting(otherMember.getId(), response.id()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("예약 대기는 본인만 삭제할 수 있습니다.");
    }

    @Test
    @DisplayName("대기 예약을 승인한다.")
    void approveWaiting() {
        // given
        var member = memberRepository.save(new Member("미소", "miso@email.com", "password", RoleType.USER));
        var theme = themeRepository.save(new Theme("테마", "설명", "썸네일"));
        var time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        var date = LocalDate.now().plusDays(1);
        var otherMember = memberRepository.save(new Member("크루", "crew@email.com", "password", RoleType.USER));
        var reservation = reservationRepository.save(new Reservation(date, time, theme, otherMember));
        var request = new WaitingCreateRequest(date, time.getId(), theme.getId());
        var response = waitingService.createWaiting(member.getId(), request);

        // when
        reservationRepository.delete(reservation);
        waitingService.approveWaiting(response.id());

        // then
        assertAll(
                () -> assertThat(waitingService.getAllWaitings()).isEmpty(),
                () -> assertThat(reservationRepository.findAll()).hasSize(1)
        );
    }

    @Test
    @DisplayName("이미 예약이 있는 상태에서 대기 예약을 승인하면 예외가 발생한다.")
    void approveWaitingWithExistingReservation() {
        // given
        var member = memberRepository.save(new Member("미소", "miso@email.com", "password", RoleType.USER));
        var theme = themeRepository.save(new Theme("테마", "설명", "썸네일"));
        var time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        var date = LocalDate.now().plusDays(1);
        var otherMember = memberRepository.save(new Member("크루", "crew@email.com", "password", RoleType.USER));
        reservationRepository.save(new Reservation(date, time, theme, otherMember));
        var request = new WaitingCreateRequest(date, time.getId(), theme.getId());
        var response = waitingService.createWaiting(member.getId(), request);

        // when & then
        assertThatThrownBy(() -> waitingService.approveWaiting(response.id()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이미 예약이 존재하여 대기자를 승인할 수 없습니다.");
    }
}
