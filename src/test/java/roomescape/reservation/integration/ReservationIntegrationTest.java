package roomescape.reservation.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.global.error.exception.BadRequestException;
import roomescape.global.error.exception.ConflictException;
import roomescape.global.error.exception.NotFoundException;
import roomescape.member.entity.Member;
import roomescape.member.entity.RoleType;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.dto.request.ReservationRequest.ReservationAdminCreateRequest;
import roomescape.reservation.dto.request.ReservationRequest.ReservationCreateRequest;
import roomescape.reservation.dto.request.ReservationRequest.ReservationReadFilteredRequest;
import roomescape.reservation.entity.ReservationTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.service.ReservationService;
import roomescape.theme.entity.Theme;
import roomescape.theme.repository.ThemeRepository;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationIntegrationTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("예약을 생성한다.")
    void createReservation() {
        // given
        var member = memberRepository.save(new Member(0L, "미소", "miso@email.com", "password", RoleType.USER));
        var theme = themeRepository.save(new Theme(1L, "테마", "설명", "썸네일"));
        var time = reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(10, 0)));
        var date = LocalDate.now().plusDays(1);
        var request = new ReservationCreateRequest(date, time.getId(), theme.getId());

        // when
        var response = reservationService.createReservation(member.getId(), request);

        // then
        assertThat(response.date()).isEqualTo(date);
        assertThat(response.time().getId()).isEqualTo(time.getId());
        assertThat(response.theme().getId()).isEqualTo(theme.getId());
    }

    @Test
    @DisplayName("관리자가 예약을 생성한다.")
    void createReservationByAdmin() {
        // given
        var member = memberRepository.save(new Member(0L, "미소", "miso@email.com", "password", RoleType.USER));
        var theme = themeRepository.save(new Theme(1L, "테마", "설명", "썸네일"));
        var time = reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(10, 0)));
        var date = LocalDate.now().plusDays(1);
        var request = new ReservationAdminCreateRequest(date, theme.getId(), time.getId(), member.getId());

        // when
        var response = reservationService.createReservationByAdmin(request);

        // then
        assertThat(response.date()).isEqualTo(date);
        assertThat(response.time().getId()).isEqualTo(time.getId());
        assertThat(response.theme().getId()).isEqualTo(theme.getId());
    }

    @Test
    @DisplayName("과거 날짜로 예약을 생성하면 예외가 발생한다.")
    void createReservationWithPastDate() {
        // given
        var member = memberRepository.save(new Member(0L, "미소", "miso@email.com", "password", RoleType.USER));
        var theme = themeRepository.save(new Theme(1L, "테마", "설명", "썸네일"));
        var time = reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(10, 0)));
        var date = LocalDate.now().minusDays(1);
        var request = new ReservationCreateRequest(date, time.getId(), theme.getId());

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(member.getId(), request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("과거 날짜는 예약할 수 없습니다.");
    }

    @Test
    @DisplayName("중복된 시간에 예약을 생성하면 예외가 발생한다.")
    void createReservationWithDuplicateTime() {
        // given
        var member = memberRepository.save(new Member(0L, "미소", "miso@email.com", "password", RoleType.USER));
        var theme = themeRepository.save(new Theme(1L, "테마", "설명", "썸네일"));
        var time = reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(10, 0)));
        var date = LocalDate.now().plusDays(1);
        var request = new ReservationCreateRequest(date, time.getId(), theme.getId());
        reservationService.createReservation(member.getId(), request);

        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(member.getId(), request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("해당 날짜와 시간에 이미 예약이 존재합니다.");
    }

    @Test
    @DisplayName("모든 예약을 조회한다.")
    void getAllReservations() {
        // given
        var member = memberRepository.save(new Member(0L, "미소", "miso@email.com", "password", RoleType.USER));
        var theme = themeRepository.save(new Theme(1L, "테마", "설명", "썸네일"));
        var time = reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(10, 0)));
        var date = LocalDate.now().plusDays(1);
        var request = new ReservationCreateRequest(date, time.getId(), theme.getId());
        reservationService.createReservation(member.getId(), request);

        // when
        var responses = reservationService.getAllReservations();

        // then
        assertThat(responses).hasSize(1);
        var response = responses.getFirst();
        assertThat(response.id()).isNotNull();
        assertThat(response.date()).isEqualTo(date);
        assertThat(response.time().getId()).isEqualTo(time.getId());
        assertThat(response.member().getId()).isEqualTo(member.getId());
        assertThat(response.theme().getId()).isEqualTo(theme.getId());
    }

    @Test
    @DisplayName("필터링된 예약을 조회한다.")
    void getFilteredReservations() {
        // given
        var member = memberRepository.save(new Member(0L, "미소", "miso@email.com", "password", RoleType.USER));
        var theme = themeRepository.save(new Theme(1L, "테마", "설명", "썸네일"));
        var time = reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(10, 0)));
        var date = LocalDate.now().plusDays(1);
        var request = new ReservationCreateRequest(date, time.getId(), theme.getId());
        reservationService.createReservation(member.getId(), request);

        var filterRequest = new ReservationReadFilteredRequest(
                theme.getId(),
                member.getId(),
                date,
                date
        );

        // when
        var responses = reservationService.getFilteredReservations(filterRequest);

        // then
        assertThat(responses).hasSize(1);
        var response = responses.getFirst();
        assertThat(response.id()).isNotNull();
        assertThat(response.date()).isEqualTo(date);
        assertThat(response.time().getId()).isEqualTo(time.getId());
        assertThat(response.member().getId()).isEqualTo(member.getId());
        assertThat(response.theme().getId()).isEqualTo(theme.getId());
    }

    @Test
    @DisplayName("예약을 삭제한다.")
    void deleteReservation() {
        // given
        var member = memberRepository.save(new Member(0L, "미소", "miso@email.com", "password", RoleType.USER));
        var theme = themeRepository.save(new Theme(1L, "테마", "설명", "썸네일"));
        var time = reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(10, 0)));
        var date = LocalDate.now().plusDays(1);
        var request = new ReservationCreateRequest(date, time.getId(), theme.getId());
        reservationService.createReservation(member.getId(), request);

        // when
        reservationService.deleteReservation(1L);

        // then
        var reservations = reservationService.getAllReservations();
        assertThat(reservations).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 예약을 삭제하면 예외가 발생한다.")
    void deleteNonExistentReservation() {
        // when & then
        assertThatThrownBy(() -> reservationService.deleteReservation(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 id 입니다.");
    }
}
