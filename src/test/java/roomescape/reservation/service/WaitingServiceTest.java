package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import roomescape.auth.dto.LoginMember;
import roomescape.common.exception.AlreadyInUseException;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.request.WaitingCreateRequest;
import roomescape.reservation.dto.response.WaitingResponse;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;
import roomescape.reservation.repository.WaitingRepository;

@ActiveProfiles("test")
@DataJpaTest
@Import(WaitingService.class)
class WaitingServiceTest {

    @Autowired
    private WaitingRepository waitingRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private WaitingService waitingService;

    @DisplayName("예약 대기를 생성한다.")
    @Test
    void createWaiting() {
        // given
        LocalDate date = getTomorrow();
        Theme theme = themeRepository.save(new Theme("테마1", "테마1", "www.x.com"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        Member member = memberRepository.save(new Member("로키", "roky@posty.com", "12341234", Role.ADMIN));

        WaitingCreateRequest request =
                new WaitingCreateRequest(date, time.getId(), theme.getId(), LoginMember.of(member));

        // when
        WaitingResponse response = waitingService.createWaiting(request);

        // then
        assertAll(
                () -> assertThat(response.id()).isNotNull(),
                () -> assertThat(response.member().name()).isEqualTo("로키"),
                () -> assertThat(response.date()).isEqualTo(date),
                () -> assertThat(response.time().startAt()).isEqualTo(LocalTime.of(10, 0)),
                () -> assertThat(response.theme().name()).isEqualTo("테마1")
        );
    }

    @DisplayName("같은 사용자가 예약 대기를 여러번 할 수 없다.")
    @Test
    void createDuplicateWaiting() {
        // given
        LocalDate date = getTomorrow();
        Theme theme = themeRepository.save(new Theme("테마1", "테마1", "www.x.com"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        Member member = memberRepository.save(new Member("로키", "roky@posty.com", "12341234", Role.ADMIN));
        Waiting waiting = waitingRepository.save(new Waiting(date, member, time, theme));

        WaitingCreateRequest request =
                new WaitingCreateRequest(date, time.getId(), theme.getId(), LoginMember.of(member));

        // when & then
        assertThatThrownBy(() -> waitingService.createWaiting(request))
                .isInstanceOf(AlreadyInUseException.class);
    }

    @DisplayName("예약대기를 삭제한다.")
    @Test
    void deleteReservationWaiting() {
        // given
        LocalDate date = getTomorrow();
        Theme theme = themeRepository.save(new Theme("테마1", "테마1", "www.x.com"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        Member member = memberRepository.save(new Member("로키", "roky@posty.com", "12341234", Role.ADMIN));
        Waiting waiting = waitingRepository.save(new Waiting(date, member, time, theme));

        // when & then
        assertThatCode(() -> waitingService.deleteWaiting(waiting.getId()))
                .doesNotThrowAnyException();
    }

    @DisplayName("예약이 존재하지 않으면 예외를 반환한다.")
    @Test
    void deleteNonExistsReservationWaiting() {
        assertThatThrownBy(() -> waitingService.deleteWaiting(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @DisplayName("모든 예약대기 목록을 조회한다.")
    @Test
    void getAll() {
        // given
        LocalDate date = getTomorrow();
        Theme theme = themeRepository.save(new Theme("테마1", "테마1", "www.x.com"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));
        Member member = memberRepository.save(new Member("로키", "roky@posty.com", "12341234", Role.ADMIN));
        Waiting waiting = waitingRepository.save(new Waiting(date, member, time, theme));

        // when & then
        assertThat(waitingService.getAll()).hasSize(1);
    }

    private LocalDate getTomorrow() {
        return LocalDate.now().plusDays(1L);
    }
}
