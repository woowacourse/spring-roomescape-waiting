package roomescape.domain.member;

import roomescape.exception.AccessDeniedException;

public enum Role {
    USER(() -> {
        throw new AccessDeniedException("[ERROR] 관리자만 접근 가능합니다.");
    }),
    ADMIN(() -> {
    });

    private final Runnable accessPolicy;

    Role(Runnable accessPolicy) {
        this.accessPolicy = accessPolicy;
    }

    public void checkAdminAccess() {
        accessPolicy.run();
    }
}
