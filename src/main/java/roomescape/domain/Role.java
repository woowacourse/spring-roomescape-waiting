package roomescape.domain;

import java.util.Map;

public enum Role {

    ADMIN,
    CUSTOMER,
    ;

    private static final Map<String, Role> CACHE = Map.of(
            "ADMIN", ADMIN,
            "CUSTOMER", CUSTOMER
    );

    public static Role findByName(String name) {
        return CACHE.get(name);
    }
}
