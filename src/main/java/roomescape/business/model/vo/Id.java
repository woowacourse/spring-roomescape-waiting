package roomescape.business.model.vo;

import java.util.UUID;

public record Id(
        String id
) {
    public static Id create(final String id) {
        return new Id(id);
    }

    public static Id issue() {
        return new Id(UUID.randomUUID().toString().substring(0, 8));
    }

    public String value() {
        return id;
    }

    public boolean isSameId(final String id) {
        return this.id.equals(id);
    }
}
