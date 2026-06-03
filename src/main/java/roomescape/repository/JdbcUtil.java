package roomescape.repository;

import org.springframework.jdbc.support.KeyHolder;

public final class JdbcUtil {

    private JdbcUtil() {
    }

    public static long extractGeneratedKey(final KeyHolder keyHolder) {
        final Number generatedKey = keyHolder.getKey();
        if (generatedKey == null) {
            throw new IllegalStateException("데이터베이스에서 생성된 키를 가져오는 데 실패했습니다.");
        }
        return generatedKey.longValue();
    }
}
