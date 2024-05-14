package roomescape.service.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.*;
import roomescape.exception.InvalidReservationException;
import roomescape.service.reservation.dto.ThemeRequest;
import roomescape.service.reservation.dto.ThemeResponse;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Sql(scripts = {"classpath:truncate-with-guests.sql"})
class ThemeServiceTest {

    @Autowired
    private ThemeService themeService;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("테마를 생성한다.")
    @Test
    void create() {
        //given
        ThemeRequest themeRequest = new ThemeRequest("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");

        //when
        ThemeResponse themeResponse = themeService.create(themeRequest);

        //then
        assertThat(themeResponse.id()).isNotZero();
    }

    @DisplayName("테마를 생성한다.")
    @Test
    void cannotCreateByDuplicatedName() {
        //given
        Theme theme = new Theme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
        themeRepository.save(theme);

        ThemeRequest themeRequest = new ThemeRequest(theme.getName(), "우테코 레벨2를 탈출하는 내용입니다.", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");

        //when&then
        assertThatThrownBy(() -> themeService.create(themeRequest))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessage("이미 존재하는 테마 이름입니다.");
    }

    @DisplayName("모든 테마를 조회한다.")
    @Test
    void findAll() {
        //given
        createTheme("레벨2 탈출");

        //when
        List<ThemeResponse> responses = themeService.findAll();

        //then
        assertThat(responses).hasSize(1);
    }

    @DisplayName("테마를 삭제한다.")
    @Test
    void deleteById() {
        //given
        Theme theme = createTheme("레벨2 탈출");

        //when
        themeService.deleteById(theme.getId());

        //then
        assertThat(themeService.findAll()).hasSize(0);
    }

    @DisplayName("예약이 존재하는 테마를 삭제하면 예외가 발생한다.")
    @Test
    void cannotDeleteByReservation() {
        //given
        Theme theme = createTheme("레벨2 탈출");
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime("21:25"));
        Member member = memberRepository.save(new Member("member", "member@email.com", "member123", Role.GUEST));
        reservationRepository.save(new Reservation("2024-10-04", member, reservationTime, theme));

        //when&then
        assertThatThrownBy(() -> themeService.deleteById(theme.getId()))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessage("해당 테마로 예약이 존재해서 삭제할 수 없습니다.");
    }

    @DisplayName("인기 테마를 조회한다.")
    @Test
    void findPopularThemes() {
        //given
        Theme theme1 = createTheme("레벨1 탈출");
        Theme theme2 = createTheme("레벨2 탈출");
        Theme theme3 = createTheme("레벨3 탈출");

        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime("21:25"));
        Member member = memberRepository.save(new Member("member", "member@email.com", "member123", Role.GUEST));

        reservationRepository.save(new Reservation(LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_DATE), member, reservationTime, theme1));
        reservationRepository.save(new Reservation(LocalDate.now().minusDays(7).format(DateTimeFormatter.ISO_DATE), member, reservationTime, theme2));
        reservationRepository.save(new Reservation(LocalDate.now().minusDays(8).format(DateTimeFormatter.ISO_DATE), member, reservationTime, theme3));

        //when
        List<ThemeResponse> result = themeService.findPopularThemes();

        //then
        assertThat(result).hasSize(2);
    }

    private Theme createTheme(String name) {
        Theme theme = new Theme(name, "우테코 레벨2를 탈출하는 내용입니다.", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
        return themeRepository.save(theme);
    }
}
