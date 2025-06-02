package roomescape.business.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.business.domain.Member;
import roomescape.business.domain.Reservation;
import roomescape.business.domain.ReservationTime;
import roomescape.business.domain.Theme;
import roomescape.exception.DuplicateException;
import roomescape.exception.NotFoundException;
import roomescape.persistence.repository.MemberRepository;
import roomescape.persistence.repository.ReservationRepository;
import roomescape.persistence.repository.ReservationTimeRepository;
import roomescape.persistence.repository.ThemeRepository;
import roomescape.presentation.dto.ThemeRequest;
import roomescape.presentation.dto.ThemeResponse;

@DataJpaTest
class ThemeServiceTest {

    private ThemeService themeService;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;

    @Autowired
    public ThemeServiceTest(
            final ReservationRepository reservationRepository,
            final ReservationTimeRepository reservationTimeRepository,
            final MemberRepository memberRepository,
            final ThemeRepository themeRepository
    ) {
        themeService = new ThemeService(themeRepository, reservationRepository, () -> LocalDate.of(2025, 5, 10));
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.memberRepository = memberRepository;
        this.themeRepository = themeRepository;
    }

    @Test
    @DisplayName("테마 요청 객체로 테마를 저장한다")
    void insert() {
        // given
        final ThemeRequest themeRequest = new ThemeRequest("테마", "소개", "썸네일");

        // when
        final ThemeResponse themeResponse = themeService.insert(themeRequest);

        // then
        assertAll(
                () -> assertThat(themeResponse.name()).isEqualTo(themeRequest.name()),
                () -> assertThat(themeResponse.description()).isEqualTo(themeRequest.description()),
                () -> assertThat(themeResponse.thumbnail()).isEqualTo(themeRequest.thumbnail())
        );
    }

    @Test
    @DisplayName("저장하려는 테마의 이름과 동일한 이름이 이미 존재한다면 예외가 발생한다")
    void insertWhenNameIsDuplicate() {
        // given
        final ThemeRequest themeRequest = new ThemeRequest("테마", "소개", "썸네일");
        themeService.insert(themeRequest);

        // when & then
        assertThatThrownBy(() -> themeService.insert(themeRequest))
                .isInstanceOf(DuplicateException.class);
    }

    @Test
    @DisplayName("모든 테마를 조회한다")
    void findAll() {
        // given
        final Theme theme1 = new Theme("테마1", "소개1", "썸네일1");
        themeRepository.save(theme1);
        final Theme theme2 = new Theme("테마2", "소개2", "썸네일2");
        themeRepository.save(theme2);

        // when
        final List<ThemeResponse> themeResponses = themeService.findAll();

        // then
        assertThat(themeResponses).hasSize(2);
    }

    @Test
    @DisplayName("id를 통해 테마를 조회한다")
    void findByIdById() {
        // given
        final ThemeRequest themeRequest = new ThemeRequest("테마", "소개", "썸네일");
        final ThemeResponse themeResponse = themeService.insert(themeRequest);
        final Long id = themeResponse.id();

        // when
        final ThemeResponse findThemeResponse = themeService.findById(id);

        // then
        assertAll(
                () -> assertThat(findThemeResponse.id()).isEqualTo(id),
                () -> assertThat(findThemeResponse.name()).isEqualTo(themeRequest.name()),
                () -> assertThat(findThemeResponse.description()).isEqualTo(themeRequest.description()),
                () -> assertThat(findThemeResponse.thumbnail()).isEqualTo(themeRequest.thumbnail())
        );
    }

    @Test
    @DisplayName("id를 통해 테마를 조회할 때 대상이 없다면 예외가 발생한다")
    void findByIdWhenNotExists() {
        // given
        final Long notExistsId = 999L;

        // when & then
        assertThatThrownBy(() -> themeService.findById(notExistsId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("id를 통해 테마를 삭제한다")
    void deleteById() {
        // given
        final ThemeRequest themeRequest = new ThemeRequest("테마", "소개", "썸네일");
        final ThemeResponse themeResponse = themeService.insert(themeRequest);
        final Long id = themeResponse.id();

        // when
        themeService.deleteById(id);

        // then
        final Optional<Theme> findTheme = themeRepository.findById(id);
        assertThat(findTheme).isEmpty();
    }

    @Test
    @DisplayName("id를 통해 테마를 삭제할 때 대상이 없다면 예외가 발생한다")
    void deleteByIdWhenNotExists() {
        // given
        final Long notExistsId = 999L;

        // when & then
        assertThatThrownBy(() -> themeService.deleteById(notExistsId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("인기 테마를 조회할 때 결과는 최대 10개를 반환한다")
    void findPopularThemesWhenResultSizeExceedsLimit() {
        // given
        themeRepository.saveAll(List.of(
                new Theme("테마1", "소개1", "썸네일1"),
                new Theme("테마2", "소개2", "썸네일2"),
                new Theme("테마3", "소개3", "썸네일3"),
                new Theme("테마4", "소개4", "썸네일4"),
                new Theme("테마5", "소개5", "썸네일5"),
                new Theme("테마6", "소개6", "썸네일6"),
                new Theme("테마7", "소개7", "썸네일7"),
                new Theme("테마8", "소개8", "썸네일8"),
                new Theme("테마9", "소개9", "썸네일9"),
                new Theme("테마10", "소개10", "썸네일10"),
                new Theme("테마11", "소개11", "썸네일11"),
                new Theme("테마12", "소개12", "썸네일12")
        ));

        // when
        final List<ThemeResponse> themeResponses = themeService.findPopularThemes();

        // then
        assertThat(themeResponses).hasSize(10);
    }

    @Test
    @DisplayName("인기 테마를 조회한다")
    void findPopularThemes() {
        // given
        final ReservationTime time = new ReservationTime(LocalTime.of(14, 0));
        reservationTimeRepository.save(time);

        final Member member = new Member("후유", "ADMIN", "fuyu@test.com", "pass");
        memberRepository.save(member);

        final Theme theme1 = new Theme("미예약", "미예약 테마입니다.", "미예약 썸네일");
        themeRepository.save(theme1);
        final Theme theme2 = new Theme("평범", "평범한 테마입니다.", "평범 썸네일");
        themeRepository.save(theme2);
        final Theme theme3 = new Theme("추천", "추천하는 테마입니다.", "추천 썸네일");
        themeRepository.save(theme3);
        final Theme theme4 = new Theme("강추", "강력 추천하는 테마입니다.", "강추 썸네일");
        themeRepository.save(theme4);

        reservationRepository.saveAll(List.of(
                // 평범
                new Reservation(LocalDate.of(2025, 5, 10), time, theme2),
                new Reservation(LocalDate.of(2025, 5, 2), time, theme2),   // 대상 X
                new Reservation(LocalDate.of(2025, 5, 1), time, theme2),   // 대상 X
                // 추천
                new Reservation(LocalDate.of(2025, 5, 10), time, theme3),
                new Reservation(LocalDate.of(2025, 5, 9), time, theme3),
                // 강추
                new Reservation(LocalDate.of(2025, 5, 10), time, theme4),
                new Reservation(LocalDate.of(2025, 5, 9), time, theme4),
                new Reservation(LocalDate.of(2025, 5, 8), time, theme4)
        ));

        // when
        final List<ThemeResponse> themeResponses = themeService.findPopularThemes();

        // then
        assertAll(
                () -> assertThat(themeResponses.get(0)
                        .name()).isEqualTo("강추"),
                () -> assertThat(themeResponses.get(1)
                        .name()).isEqualTo("추천"),
                () -> assertThat(themeResponses.get(2)
                        .name()).isEqualTo("평범"),
                () -> assertThat(themeResponses.get(3)
                        .name()).isEqualTo("미예약")
        );
    }
}
