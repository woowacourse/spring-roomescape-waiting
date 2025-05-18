package roomescape.common.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class BaseEntityTest {

    @Test
    @DisplayName("ID가 할당된 경우 정상적으로 생성된다")
    void createWithId() {
        // when
        final TestEntity entity = new TestEntity(1L);

        // then
        assertThat(entity.id).isEqualTo(1L);
    }

    @Test
    @DisplayName("ID가 할당되지 않은 경우 예외가 발생한다")
    void throwExceptionWhenIdIsNull() {
        // when & then
        assertThatThrownBy(() -> new TestEntity(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Identifier has not been assigned.");
    }

    @Test
    @DisplayName("같은 ID를 가진 엔티티는 equals에서 true를 반환한다")
    void equalsWithSameId() {
        // given
        final TestEntity entity1 = new TestEntity(1L);
        final TestEntity entity2 = new TestEntity(1L);

        // when & then
        assertAll(() -> {
            assertThat(entity1).isEqualTo(entity1); // 자기 자신과 비교
            assertThat(entity1).isEqualTo(entity2); // 같은 ID를 가진 다른 객체와 비교
            assertThat(entity2).isEqualTo(entity1); // 대칭성 확인
        });
    }

    @Test
    @DisplayName("다른 ID를 가진 엔티티는 equals에서 false를 반환한다")
    void equalsWithDifferentId() {
        // given
        final TestEntity entity1 = new TestEntity(1L);
        final TestEntity entity2 = new TestEntity(2L);

        // when & then
        assertThat(entity1).isNotEqualTo(entity2);
    }

    @Test
    @DisplayName("타입이 다른 객체와 비교시 equals에서 false를 반환한다")
    void equalsWithDifferentType() {
        // given
        final TestEntity entity = new TestEntity(1L);
        final Object differentType = "not an entity";

        // when & then
        assertThat(entity).isNotEqualTo(differentType);
    }
    
    @Test
    @DisplayName("null과 비교시 equals에서 false를 반환한다")
    void equalsWithNull() {
        // given
        final TestEntity entity = new TestEntity(1L);

        // when & then
        assertThat(entity).isNotEqualTo(null);
    }

    @Test
    @DisplayName("ID가 null인 엔티티들은 서로 다른 것으로 간주된다")
    void equalsWithNullId() {
        // given
        final TestEntity entity1 = new TestEntity(1L);
        final TestEntity entity2 = new TestEntity(1L);
        entity1.setId(null);
        entity2.setId(null);

        // when & then
        assertAll(() -> {
            assertThat(entity1).isNotEqualTo(entity2);
            assertThat(entity2).isNotEqualTo(entity1);
        });
    }

    @Test
    @DisplayName("같은 ID를 가진 엔티티는 동일한 hashCode를 반환한다")
    void hashCodeWithSameId() {
        // given
        final TestEntity entity1 = new TestEntity(1L);
        final TestEntity entity2 = new TestEntity(1L);

        // when & then
        assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
    }

    @Test
    @DisplayName("다른 ID를 가진 엔티티는 다른 hashCode를 반환할 가능성이 높다")
    void hashCodeWithDifferentId() {
        // given
        final TestEntity entity1 = new TestEntity(1L);
        final TestEntity entity2 = new TestEntity(2L);

        // when & then
        // 해시 충돌 가능성이 있으므로 항상 다르다고 할 수는 없음
        assertThat(entity1.hashCode()).isNotEqualTo(entity2.hashCode());
    }

    @Test
    @DisplayName("ID가 null인 엔티티는 HashCode를 사용할 수 없다.")
    void hashCodeWithNullId() {
        // given
        final TestEntity entity = new TestEntity(1L);
        entity.setId(null);

        // when & then
        assertThatThrownBy(()->entity.hashCode())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("hashCode() called on entity without ID");
    }

    @Test
    @DisplayName("엔티티는 HashSet에서 ID를 기준으로 동작한다")
    void entityInHashSet() {
        // given
        final TestEntity entity1 = new TestEntity(1L);
        final TestEntity entity2 = new TestEntity(1L);
        final TestEntity entity3 = new TestEntity(2L);

        final Set<TestEntity> entitySet = new HashSet<>();
        entitySet.add(entity1);

        // when & then
        assertAll(() -> {
            assertThat(entitySet).contains(entity2); // 같은 ID를 가진 다른 객체
            assertThat(entitySet).doesNotContain(entity3); // 다른 ID를 가진 객체
        });
    }

    @Test
    @DisplayName("프록시처럼 서브클래싱된 엔티티도 같은 타입으로 간주된다")
    void equalsWithProxyEntity() {
        // given
        final TestEntity originalEntity = new TestEntity(1L);
        final TestEntityProxy proxyEntity = new TestEntityProxy(1L);

        // when & then
        assertAll(() -> {
            // 프록시와 양방 비교
            assertThat(originalEntity).isEqualTo(proxyEntity);
            assertThat(proxyEntity).isEqualTo(originalEntity);
            assertThat(originalEntity.hashCode()).isEqualTo(proxyEntity.hashCode());
            
            // HashSet에서도 동일하게 취급
            final Set<BaseEntity> entities = new HashSet<>();
            entities.add(originalEntity);
            assertThat(entities).contains(proxyEntity);
        });
    }

    @Test
    @DisplayName("프록시와 ID가 다른 경우 다른 객체로 간주된다")
    void notEqualsWithDifferentIdProxyEntity() {
        // given
        final TestEntity originalEntity = new TestEntity(1L);
        final TestEntityProxy proxyEntity = new TestEntityProxy(2L);

        // when & then
        assertThat(originalEntity).isNotEqualTo(proxyEntity);
    }

    // ID를 null로 설정하는 테스트 지원 메서드


    // 테스트용 구현 클래스
    private static class TestEntity extends BaseEntity {
        public TestEntity(final Long id) {
            super(id);
        }

        public void setId(final Long id) {
            this.id = id;
        }
    }
    
    // JPA 프록시 시뮬레이션
    private static class TestEntityProxy extends TestEntity {
        public TestEntityProxy(final Long id) {
            super(id);
        }

        private final boolean initialized = false;
    }
} 
