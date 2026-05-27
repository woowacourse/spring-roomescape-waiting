package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import roomescape.auth.Role;

public class MemberTest {

    @Test
    void 이메일이_null이면_회원을_생성할_수_없다() {
        assertThatThrownBy(() -> new Member(
                1L,
                null,
                "password",
                "브라운",
                Role.USER,
                null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일은 비어 있을 수 없습니다.");
    }

    @Test
    void 이메일이_비어있으면_회원을_생성할_수_없다() {
        assertThatThrownBy(() -> new Member(
                1L,
                "",
                "password",
                "브라운",
                Role.USER,
                null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일은 비어 있을 수 없습니다.");
    }

    @Test
    void 이메일_형식이_아니면_회원을_생성할_수_없다() {
        assertThatThrownBy(() -> new Member(
                1L,
                "not-an-email",
                "password",
                "브라운",
                Role.USER,
                null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("올바른 이메일 형식이 아닙니다.");
    }

    @Test
    void 비밀번호가_null이면_회원을_생성할_수_없다() {
        assertThatThrownBy(() -> new Member(
                1L,
                "brown@email.com",
                null,
                "브라운",
                Role.USER,
                null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호는 비어 있을 수 없습니다.");
    }

    @Test
    void 비밀번호가_비어있으면_회원을_생성할_수_없다() {
        assertThatThrownBy(() -> new Member(
                1L,
                "brown@email.com",
                "",
                "브라운",
                Role.USER,
                null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호는 비어 있을 수 없습니다.");
    }

    @Test
    void 이름이_null이면_회원을_생성할_수_없다() {
        assertThatThrownBy(() -> new Member(
                1L,
                "brown@email.com",
                "password",
                null,
                Role.USER,
                null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이름은 비어 있을 수 없습니다.");
    }

    @Test
    void 이름이_비어있으면_회원을_생성할_수_없다() {
        assertThatThrownBy(() -> new Member(
                1L,
                "brown@email.com",
                "password",
                "",
                Role.USER,
                null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이름은 비어 있을 수 없습니다.");
    }

    @Test
    void 이름이_두글자_미만이면_회원을_생성할_수_없다() {
        assertThatThrownBy(() -> new Member(
                1L,
                "brown@email.com",
                "password",
                "브",
                Role.USER,
                null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이름은 2자 이상 10자 이하여야 합니다.");
    }

    @Test
    void 이름이_열글자_초과이면_회원을_생성할_수_없다() {
        assertThatThrownBy(() -> new Member(
                1L,
                "brown@email.com",
                "password",
                "브".repeat(11),
                Role.USER,
                null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이름은 2자 이상 10자 이하여야 합니다.");
    }

    @Test
    void 이름에_특수문자가_포함되면_회원을_생성할_수_없다() {
        assertThatThrownBy(() -> new Member(
                1L,
                "brown@email.com",
                "password",
                "브라운!",
                Role.USER,
                null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이름은 한글과 영문만 입력할 수 있습니다.");
    }

    @Test
    void 입력한_비밀번호가_저장된_비밀번호와_일치하면_true를_반환한다() {
        Member member = new Member(1L, "brown@email.com", "password", "브라운", Role.USER, null);

        assertThat(member.matchesPassword("password")).isTrue();
    }

    @Test
    void 입력한_비밀번호가_저장된_비밀번호와_다르면_false를_반환한다() {
        Member member = new Member(1L, "brown@email.com", "password", "브라운", Role.USER, null);

        assertThat(member.matchesPassword("wrong-password")).isFalse();
    }

    @Test
    void role이_null이면_회원을_생성할_수_없다() {
        assertThatThrownBy(() -> new Member(
                1L,
                "brown@email.com",
                "password",
                "브라운",
                null,
                null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("권한은 비어 있을 수 없습니다.");
    }

    @Test
    void storeId가_0이하면_회원을_생성할_수_없다() {
        assertThatThrownBy(() -> new Member(
                1L,
                "manager@email.com",
                "password",
                "매니저",
                Role.MANAGER,
                0L
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("매장 ID는 양수여야 합니다.");
    }

    @Test
    void USER는_storeId가_null이어도_정상_생성된다() {
        assertThatCode(() -> new Member(
                1L,
                "user@email.com",
                "password",
                "유저",
                Role.USER,
                null
        )).doesNotThrowAnyException();
    }

    @Test
    void MANAGER는_storeId를_가질_수_있다() {
        Member manager = new Member(
                4L,
                "manager-gangnam@email.com",
                "password",
                "강남매니저",
                Role.MANAGER,
                1L
        );

        assertThat(manager.getRole()).isEqualTo(Role.MANAGER);
        assertThat(manager.getStoreId()).isEqualTo(1L);
    }

    @Test
    void MANAGER는_storeId가_null이면_회원을_생성할_수_없다() {
        assertThatThrownBy(() -> new Member(
                1L,
                "manager@email.com",
                "password",
                "매니저",
                Role.MANAGER,
                null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("매니저는 매장에 소속되어야 합니다.");
    }

    @Test
    void USER는_storeId를_가지면_회원을_생성할_수_없다() {
        assertThatThrownBy(() -> new Member(
                1L,
                "brown@email.com",
                "password",
                "브라운",
                Role.USER,
                1L
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("일반 사용자는 매장을 가질 수 없습니다.");
    }
}
