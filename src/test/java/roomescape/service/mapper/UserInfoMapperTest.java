package roomescape.service.mapper;

import static roomescape.fixture.MemberFixture.DEFAULT_MEMBER;
import static roomescape.fixture.MemberFixture.DEFAULT_MEMBER_INFO;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.dto.UserInfo;

class UserInfoMapperTest {

    @Test
    @DisplayName("도메인을 응답으로 잘 변환하는지 확인")
    void toResponse() {
        UserInfo response = UserInfoMapper.toResponse(DEFAULT_MEMBER);

        Assertions.assertThat(response)
                .isEqualTo(DEFAULT_MEMBER_INFO);
    }
}
