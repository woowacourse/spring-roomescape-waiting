package roomescape.common.domain;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.util.Objects;

@FieldNameConstants
@NoArgsConstructor
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    protected BaseEntity(final Long id) {
        if (id == null) {
            throw new IllegalStateException("Identifier has not been assigned.");
        }
        this.id = id;
    }
    
    // 프록시 객체도 같은 타입 계열로 간주
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof final BaseEntity that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        if (id == null) {
            throw new IllegalStateException("hashCode() called on entity without ID");
        }
        return Objects.hash(id);
    }
}


