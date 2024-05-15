package roomescape.service.mapper;

import static roomescape.fixture.MemberFixture.DEFAULT_MEMBER;
import static roomescape.fixture.MemberFixture.DEFAULT_MEMBER_INFO;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.dto.MemberInfo;

class MemberInfoMapperTest {

    @Test
    @DisplayName("도메인을 응답으로 잘 변환하는지 확인")
    void toResponse() {
        MemberInfo response = MemberInfoMapper.toResponse(DEFAULT_MEMBER);

        Assertions.assertThat(response)
                .isEqualTo(DEFAULT_MEMBER_INFO);
    }
}
