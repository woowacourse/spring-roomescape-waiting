package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.config.DatabaseCleaner;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.dto.LoginMemberInToken;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.dto.ReservationCreateRequest;
import roomescape.reservation.dto.ReservationSearchRequest;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ReservationServiceTest {

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationService reservationService;

    @AfterEach
    void init() {
        databaseCleaner.cleanUp();
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간에 예약을 하면 예외가 발생한다.")
    void notExistReservationTimeIdExceptionTest() {
        themeRepository.save(new Theme("공포", "호러 방탈출", "http://asdf.jpg"));
        LoginMemberInToken loginMemberInToken = new LoginMemberInToken(1L, Role.MEMBER, "카키", "kaki@email.com");
        ReservationCreateRequest reservationCreateRequest = new ReservationCreateRequest(
                LocalDate.now(), 1L, 1L);

        assertThatThrownBy(() -> reservationService.save(reservationCreateRequest, loginMemberInToken))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("중복된 예약이 있다면 예외가 발생한다.")
    void duplicateReservationExceptionTest() {
        Theme theme = new Theme("공포", "호러 방탈출", "http://asdf.jpg");
        Long themeId = themeRepository.save(theme).getId();

        LocalTime localTime = LocalTime.parse("10:00");
        ReservationTime reservationTime = new ReservationTime(localTime);
        Long timeId = reservationTimeRepository.save(reservationTime).getId();

        Member member = new Member(1L, Role.MEMBER, "호기", "hogi@email.com", "1234");
        memberRepository.save(member);

        LocalDate localDate = LocalDate.now();
        ReservationCreateRequest reservationCreateRequest = new ReservationCreateRequest(localDate,
                themeId, timeId);
        LoginMemberInToken loginMemberInToken = new LoginMemberInToken(1L, member.getRole(), member.getName(),
                member.getEmail());
        reservationService.save(reservationCreateRequest, loginMemberInToken);

        ReservationCreateRequest duplicateRequest = new ReservationCreateRequest(localDate, themeId, timeId);
        assertThatThrownBy(() -> reservationService.save(duplicateRequest, loginMemberInToken))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void findByMemberAndThemeAndDateBetweenTest() {
        Theme theme = new Theme("공포", "호러 방탈출", "http://asdf.jpg");
        Long themeId = themeRepository.save(theme).getId();
        LocalTime localTime = LocalTime.parse("10:00");
        ReservationTime reservationTime = new ReservationTime(localTime);
        Long timeId = reservationTimeRepository.save(reservationTime).getId();
        Member member = new Member("마크", "mark@woowa.com", "1234");
        memberRepository.save(member);

        // when
        LocalDate localDate = LocalDate.now().plusYears(1);
        ReservationCreateRequest reservationCreateRequest = new ReservationCreateRequest(localDate, themeId, timeId);
        LoginMemberInToken loginMemberInToken = new LoginMemberInToken(1L, member.getRole(), member.getName(),
                member.getEmail());
        reservationService.save(reservationCreateRequest, loginMemberInToken);

        // then
        assertThat(
                reservationService.findAllBySearch(new ReservationSearchRequest(member.getId(), themeId, localDate, localDate)))
                .isNotEmpty();
    }
}
