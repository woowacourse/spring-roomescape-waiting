package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
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
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.reservation.dto.response.BookedReservationTimeResponse;
import roomescape.reservation.dto.request.ReservationCreateRequest;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.ReservationTimeResponse;
import roomescape.theme.dto.response.ThemeResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.theme.repository.ThemeRepository;

@ActiveProfiles("test")
@DataJpaTest
@Import(ReservationService.class)
class ReservationServiceTest {

    private final LocalDateTime now = LocalDateTime.now();

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ReservationService reservationService;

    @DisplayName("모든 예약 정보를 가져온다")
    @Test
    void test1() {
        // given
        Theme savedTheme = themeRepository.save(new Theme("포스티", "공포", "wwww.um.com"));

        LocalTime time = LocalTime.of(8, 0);
        ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(time));
        Member member = new Member("포스티", "test@test.com", "12341234", Role.MEMBER);
        Member savedMember = memberRepository.save(member);

        LocalDate date = LocalDate.of(2024, 4, 29);
        reservationRepository.save(new Reservation(savedMember, date, savedTime, savedTheme));

        // when
        List<ReservationResponse> response = reservationService.getAll();

        // then
        assertThat(response).hasSize(1);
    }

    @DisplayName("예약 정보가 없다면 빈 리스트를 반환한다.")
    @Test
    void test2() {
        List<ReservationResponse> result = reservationService.getAll();

        assertThat(result).isEmpty();
    }

    @DisplayName("예약을 추가한다.")
    @Test
    void test3() {
        // given
        Theme savedTheme = themeRepository.save(new Theme("포스티", "공포", "wwww.um.com"));
        Long themeId = savedTheme.getId();

        LocalTime time = LocalTime.of(8, 0);
        ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(time));
        Long timeId = savedTime.getId();
        Member savedMember = memberRepository.save(new Member("포스티", "test@test.com", "12341234", Role.MEMBER));

        LocalDate date = nextDay();

        ReservationCreateRequest requestDto =
                new ReservationCreateRequest(date, timeId, themeId, LoginMember.of(savedMember));

        // when
        ReservationResponse result = reservationService.create(requestDto);

        // then
        SoftAssertions softAssertions = new SoftAssertions();

        softAssertions.assertThat(result.member().name()).isEqualTo("포스티");
        softAssertions.assertThat(result.date()).isEqualTo(date);
        softAssertions.assertThat(result.time()).isEqualTo(new ReservationTimeResponse(timeId, time));
        softAssertions.assertThat(result.theme())
                .isEqualTo(new ThemeResponse(themeId, savedTheme.getName(), savedTheme.getDescription(),
                        savedTheme.getThumbnail()));

        softAssertions.assertAll();
    }

    @DisplayName("이미 존재하는 예약과 동일하면 예외가 발생한다.")
    @Test
    void test4() {
        // given
        Theme savedTheme = themeRepository.save(new Theme("포스티", "공포", "wwww.um.com"));
        Long themeId = savedTheme.getId();

        LocalTime time = LocalTime.of(8, 0);
        ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(time));
        Long timeId = savedTime.getId();
        Member member = new Member("포스티", "test@test.com", "12341234", Role.MEMBER);
        Member savedMember = memberRepository.save(member);
        LocalDate date = nextDay();

        ReservationCreateRequest requestDto =
                new ReservationCreateRequest(date, timeId, themeId, LoginMember.of(savedMember));
        reservationService.create(requestDto);

        // when & then
        assertThatThrownBy(() -> reservationService.create(requestDto))
                .isInstanceOf(AlreadyInUseException.class);
    }

    @DisplayName("과거 날짜에 예약을 추가하면 예외가 발생한다.")
    @Test
    void test5() {
        // given
        Theme savedTheme = themeRepository.save(new Theme("포스티", "공포", "wwww.um.com"));
        Long themeId = savedTheme.getId();
        Member member = new Member("포스티", "test@test.com", "12341234", Role.MEMBER);
        Member savedMember = memberRepository.save(member);

        LocalDate date = now.toLocalDate();
        LocalTime pastTime = now.toLocalTime().minusMinutes(1);

        ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(pastTime));
        Long timeId = savedTime.getId();

        ReservationCreateRequest requestDto =
                new ReservationCreateRequest(date, timeId, themeId, LoginMember.of(savedMember));

        // when & then
        assertThatThrownBy(() -> reservationService.create(requestDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("존재하지 않는 예약 시간 ID로 저장하면 예외를 반환한다.")
    @Test
    void test6() {
        Theme savedTheme = themeRepository.save(new Theme("포스티", "공포", "wwww.um.com"));
        Long themeId = savedTheme.getId();
        Member member = new Member("포스티", "test@test.com", "12341234", Role.MEMBER);
        Member savedMember = memberRepository.save(member);

        LocalDate date = nextDay();

        Long notExistId = 1000L;
        ReservationCreateRequest requestDto =
                new ReservationCreateRequest(date, notExistId, themeId, LoginMember.of(savedMember));

        assertThatThrownBy(() -> reservationService.create(requestDto))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @DisplayName("존재하지 않는 테마 ID로 저장하면 예외를 반환한다.")
    @Test
    void notExistThemeId() {
        LocalDate date = now.toLocalDate().plusDays(1);

        LocalTime time = LocalTime.of(8, 0);
        ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(time));
        Long timeId = savedTime.getId();
        Member member = new Member("포스티", "test@test.com", "12341234", Role.MEMBER);
        Member savedMember = memberRepository.save(member);

        Long notExistId = 1000L;
        ReservationCreateRequest requestDto =
                new ReservationCreateRequest(date, timeId, notExistId, LoginMember.of(savedMember));

        assertThatThrownBy(() -> reservationService.create(requestDto))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @DisplayName("예약을 삭제한다")
    @Test
    void test7() {
        // given
        Theme savedTheme = themeRepository.save(new Theme("포스티", "공포", "wwww.um.com"));

        LocalTime time = LocalTime.of(8, 0);
        ReservationTime savedTime = reservationTimeRepository.save(new ReservationTime(time));
        Member member = new Member("포스티", "test@test.com", "12341234", Role.MEMBER);
        Member savedMember = memberRepository.save(member);

        LocalDate date = nextDay();
        Reservation savedReservation = reservationRepository.save(
                new Reservation(savedMember, date, savedTime, savedTheme));

        Long id = savedReservation.getId();

        // then
        assertThatCode(() -> reservationService.delete(id))
                .doesNotThrowAnyException();
    }

    @DisplayName("예약이 존재하지 않으면 예외를 반환한다.")
    @Test
    void test8() {
        Long id = 1L;
        assertThatThrownBy(() -> reservationService.delete(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @DisplayName("가능한 시간 대에 대하여 반환한다.")
    @Test
    void test9() {
        // given
        LocalDate date = nextDay();
        LocalTime time1 = LocalTime.of(8, 0);
        LocalTime time2 = LocalTime.of(9, 0);
        ReservationTime reservationTime1 = reservationTimeRepository.save(new ReservationTime(time1));
        ReservationTime reservationTime2 = reservationTimeRepository.save(new ReservationTime(time2));
        Theme savedTheme = themeRepository.save(new Theme("포스티", "공포", "wwww.um.com"));
        Long themeId = savedTheme.getId();
        Member member = new Member("포스티", "test@test.com", "12341234", Role.MEMBER);
        Member savedMember = memberRepository.save(member);
        reservationRepository.save(new Reservation(savedMember, date, reservationTime1, savedTheme));

        // when
        List<BookedReservationTimeResponse> responses = reservationService.getAvailableTimes(date, themeId);

        // then
        List<Boolean> booleans = responses.stream()
                .map(BookedReservationTimeResponse::alreadyBooked)
                .toList();
        assertThat(booleans).containsExactlyInAnyOrder(true, false);
    }

    private LocalDate nextDay() {
        return LocalDate.now().plusDays(1);
    }
}
