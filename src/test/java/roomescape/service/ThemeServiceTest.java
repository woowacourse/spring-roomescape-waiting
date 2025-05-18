package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.TestFixture.DEFAULT_DATE;

import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.TestFixture;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.exception.DeletionNotAllowedException;
import roomescape.exception.NotFoundException;
import roomescape.service.param.CreateThemeParam;
import roomescape.service.result.ThemeResult;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ThemeServiceTest {

    @Autowired
    private ThemeService themeService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 테마를_전체_조회할_수_있다() {
        //given
        Theme theme1 = themeRepository.save(TestFixture.createThemeByName("theme1"));
        Theme theme2 = themeRepository.save(TestFixture.createThemeByName("theme2"));

        //when
        List<ThemeResult> themeResults = themeService.getAll();

        //then
        assertThat(themeResults).isEqualTo(List.of(
                ThemeResult.from(theme1),
                ThemeResult.from(theme2)
        ));
    }

    @Test
    void 테마를_생성할_수_있다() {
        //given
        CreateThemeParam createThemeParam = new CreateThemeParam("test1", "description1", "thumbnail1");

        //when
        ThemeResult themeResult = themeService.create(createThemeParam);

        //then
        assertThat(themeRepository.findById(themeResult.id()))
                .hasValue(new Theme(themeResult.id(), themeResult.name(), themeResult.description(), themeResult.thumbnail()));
    }

    @Test
    void id값으로_테마를_찾을_수_있다() {
        //given
        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());

        //when
        ThemeResult themeResult = themeService.getById(theme.getId());

        //then
        assertThat(themeResult).isEqualTo(ThemeResult.from(theme));
    }

    @Test
    void id값으로_테마를_찾을때_없다면_예외가_발생한다() {
        // given & when & then
        assertThatThrownBy(() -> themeService.getById(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void id값으로_테마를_삭제할_수_있다() {
        //given
        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());

        //when
        themeService.deleteById(theme.getId());

        //then
        assertThat(themeRepository.findById(theme.getId())).isEmpty();
    }

    @Test
    void id값으로_테마를_삭제할떄_예약에서_id가_사용중이라면_예외를_발생시킨다() {
        //given
        Theme theme = themeRepository.save(TestFixture.createDefaultTheme());
        ReservationTime reservationTime = reservationTimeRepository.save(TestFixture.createDefaultReservationTime());
        Member member = memberRepository.save(TestFixture.createDefaultMember());
        Reservation reservation = TestFixture.createNewReservation(member, DEFAULT_DATE, reservationTime, theme);
        reservationRepository.save(reservation);

        //when & then
        assertThatThrownBy(() -> themeService.deleteById(theme.getId()))
                .isInstanceOf(DeletionNotAllowedException.class)
                .hasMessage("해당 테마에 예약이 존재합니다.");
    }
}
