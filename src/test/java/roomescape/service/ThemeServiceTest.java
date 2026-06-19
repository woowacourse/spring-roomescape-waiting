package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.application.service.ThemeService;
import roomescape.application.service.command.ThemeRegisterCommand;
import roomescape.application.service.result.ThemeRegisterResult;
import roomescape.exception.DuplicateEntityException;
import roomescape.persistence.ThemeRepository;
import roomescape.service.fake.FakeThemeRepository;

class ThemeServiceTest {

    private ThemeRepository themeRepository;
    private ThemeService themeService;

    @BeforeEach
    void setUp() {
        this.themeRepository = new FakeThemeRepository();
        this.themeService = new ThemeService(themeRepository);
    }

    @Test
    void 새로운_테마를_정상적으로_등록한다() {
        // given: 관리자 권한과 등록 정보가 주어짐
        ThemeRegisterCommand command = new ThemeRegisterCommand("공포테마", "무서운 테마입니다.", "http://image.png");

        // when: 테마 등록 진행
        ThemeRegisterResult result = themeService.register(command);

        // then: 등록된 정보가 입력값과 일치하며 ID가 발급됨
        assertThat(result)
                .extracting(
                        ThemeRegisterResult::id,
                        ThemeRegisterResult::name,
                        ThemeRegisterResult::description,
                        ThemeRegisterResult::thumbnailImageUrl
                )
                .containsExactly(1L, "공포테마", "무서운 테마입니다.", "http://image.png");
    }

    @Test
    void 이미_존재하는_이름으로_테마_등록을_시도하면_예외가_발생한다() {
        // given: '공포테마'가 이미 등록되어 있음
        themeService.register(new ThemeRegisterCommand("공포테마", "설명", "http://image.png"));

        ThemeRegisterCommand duplicateCommand = new ThemeRegisterCommand("공포테마", "다른 설명", "http://image2.png");

        // when & then: DuplicateEntityException 발생 확인
        assertThatThrownBy(() -> themeService.register(duplicateCommand))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("이미 존재하는 테마입니다. 테마 명: 공포테마");
    }

    @Test
    void 테마_식별자로_테마를_비활성화_할_수_있다() {
        // given
        ThemeRegisterCommand command = new ThemeRegisterCommand("공포테마", "설명", "http://image.png");
        ThemeRegisterResult registerResult = themeService.register(command);

        // when
        themeService.deactivate(registerResult.id());

        // then: 테마가 비활성화됨
        assertThat(themeRepository.findById(registerResult.id()).orElseThrow().isActive()).isFalse();
    }

    @Test
    void 테마_식별자로_테마를_활성화_할_수_있다() {
        // given
        ThemeRegisterCommand command = new ThemeRegisterCommand("공포테마", "설명", "http://image.png");
        ThemeRegisterResult registerResult = themeService.register(command);
        themeService.deactivate(registerResult.id());

        // when
        themeService.activate(registerResult.id());

        // then: 테마가 활성화됨
        assertThat(themeRepository.findById(registerResult.id()).orElseThrow().isActive()).isTrue();
    }

}
