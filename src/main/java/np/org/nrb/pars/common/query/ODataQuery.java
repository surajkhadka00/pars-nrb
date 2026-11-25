package np.org.nrb.pars.common.query;

import jakarta.persistence.EntityManager;
import np.org.nrb.pars.common.web.Pageables;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Component;

@Component
public class ODataQuery {

    private final EntityManager entityManager;

    public ODataQuery(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public <T> Page<T> findAll(JpaSpecificationExecutor<T> repository, Class<T> entityType, String filter, Pageable pageable) {
        Specification<T> spec = ODataSpecifications.of(entityType, filter, entityManager.getMetamodel());
        if (spec == null) {
            return repository.findAll(null, pageable);
        }
        return repository.findAll(spec, pageable);
    }

    public <T> Pageable pageable(Class<T> entityType, int page, int size, String sort) {
        if (sort == null || sort.isBlank()) {
            return Pageables.of(page, size, sort);
        }
        EntityPathResolver resolver = new EntityPathResolver(entityManager.getMetamodel());
        String[] parts = sort.split(",", 2);
        String property = resolver.canonicalSortProperty(entityType, parts[0].trim().replace('/', '.'));
        String rewritten = parts.length > 1 ? property + "," + parts[1] : property;
        return Pageables.of(page, size, rewritten);
    }
}
