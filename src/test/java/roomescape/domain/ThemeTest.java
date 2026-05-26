package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

public class ThemeTest {

    @Test
    void 이름이_null이면_테마를_생성할_수_없다() {
        assertThatThrownBy(() -> new Theme(
                1L,
                null,
                "이든이 귀신으로 나옴",
                "https://images.example.com/themes/horror-house.jpg"
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 이름이_비어있으면_테마를_생성할_수_없다() {
        assertThatThrownBy(() -> new Theme(
                1L,
                "",
                "이든이 귀신으로 나옴",
                "https://images.example.com/themes/horror-house.jpg"
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 설명이_null이면_테마를_생성할_수_없다() {
        assertThatThrownBy(() -> new Theme(
                1L,
                "이든의 공포 하우스",
                null,
                "https://images.example.com/themes/horror-house.jpg"
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 설명이_비어있으면_테마를_생성할_수_없다() {
        assertThatThrownBy(() -> new Theme(
                1L,
                "이든의 공포 하우스",
                "",
                "https://images.example.com/themes/horror-house.jpg"
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 이미지_URL이_null이면_테마를_생성할_수_없다() {
        assertThatThrownBy(() -> new Theme(
                1L,
                "이든의 공포 하우스",
                "이든이 귀신으로 나옴",
                null
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 이미지_URL이_비어있으면_테마를_생성할_수_없다() {
        assertThatThrownBy(() -> new Theme(
                1L,
                "이든의 공포 하우스",
                "이든이 귀신으로 나옴",
                ""
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 이미지_URL_형식이_아니면_테마를_생성할_수_없다() {
        assertThatThrownBy(() -> new Theme(
                1L,
                "이든의 공포 하우스",
                "이든이 귀신으로 나옴",
                "이미지"
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 이미지_URL의_scheme이_http나_https가_아니면_테마를_생성할_수_없다() {
        assertThatThrownBy(() -> new Theme(
                1L,
                "이든의 공포 하우스",
                "이든이 귀신으로 나옴",
                "ftp://images.example.com/themes/horror-house.jpg"
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 이미지_URL에_host가_없으면_테마를_생성할_수_없다() {
        assertThatThrownBy(() -> new Theme(
                1L,
                "이든의 공포 하우스",
                "이든이 귀신으로 나옴",
                "https:///themes/horror-house.jpg"
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
