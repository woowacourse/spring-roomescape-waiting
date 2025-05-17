package roomescape.common.uri;

import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

public class UriFactory {

    public static URI buildPath(final String basePath, final String... pathSegments) {
        validate(basePath, pathSegments);

        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(basePath);

        for (final String path : pathSegments) {
            builder = builder.pathSegment(path);
        }

        return builder.build().toUri();
    }

    private static void validate(final String basePath, final String... pathSegments) {
        validateBasePath(basePath);
        validatePaths(pathSegments);
    }

    private static void validateBasePath(final String basePath) {
        if (basePath == null || basePath.isBlank()) {
            throw new IllegalArgumentException("basePath는 최소한 / 가 필요합니다.");
        }

        if (basePath.startsWith("/")) {
            return;
        }
        throw new IllegalArgumentException("basePath는 /로 시작해야 합니다.");
    }

    private static void validatePaths(final String... pathSegments) {
        if (pathSegments == null) {
            return;
        }

        for (final String path : pathSegments) {
            if (path == null || path.isBlank()) {
                throw new IllegalArgumentException("pathSegment는 null이거나 빈 문자열일 수 없습니다.");
            }

            if (path.contains("/")) {
                throw new IllegalArgumentException("pathSegment는 /를 포함할 수 없습니다.");
            }
        }
    }
}
