package np.org.nrb.pars.common.query;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.metamodel.Metamodel;
import np.org.nrb.pars.common.exception.Exceptions;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

final class ODataSpecifications {

    private ODataSpecifications() {
    }

    static <T> Specification<T> of(Class<T> entityType, String filter, Metamodel metamodel) {
        ODataFilterParser.Node node = ODataFilterParser.parse(filter);
        if (node == null) {
            return null;
        }
        return (root, query, cb) -> {
            if (query != null) {
                query.distinct(true);
            }
            EntityPathResolver resolver = new EntityPathResolver(metamodel);
            return toPredicate(node, resolver, root, cb);
        };
    }

    private static Predicate toPredicate(
            ODataFilterParser.Node node,
            EntityPathResolver resolver,
            jakarta.persistence.criteria.Root<?> root,
            CriteriaBuilder cb
    ) {
        if (node instanceof ODataFilterParser.And and) {
            return cb.and(toPredicate(and.left(), resolver, root, cb), toPredicate(and.right(), resolver, root, cb));
        }
        if (node instanceof ODataFilterParser.Or or) {
            return cb.or(toPredicate(or.left(), resolver, root, cb), toPredicate(or.right(), resolver, root, cb));
        }
        if (node instanceof ODataFilterParser.Comparison comparison) {
            return compare(comparison, resolver, root, cb);
        }
        throw Exceptions.badRequest("Invalid filter");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Predicate compare(
            ODataFilterParser.Comparison comparison,
            EntityPathResolver resolver,
            jakarta.persistence.criteria.Root<?> root,
            CriteriaBuilder cb
    ) {
        Path<?> path = resolver.resolve(root, comparison.path());
        if (comparison.valueKind() == ODataFilterParser.ValueKind.NULL) {
            return switch (comparison.operator()) {
                case "eq" -> cb.isNull(path);
                case "ne" -> cb.isNotNull(path);
                default -> throw Exceptions.badRequest("Operator not supported with null: " + comparison.operator());
            };
        }
        Object value = convert(path.getJavaType(), comparison);
        Expression expression = path;
        return switch (comparison.operator()) {
            case "eq" -> cb.equal(expression, value);
            case "ne" -> cb.notEqual(expression, value);
            case "gt" -> cb.greaterThan(expression, (Comparable) value);
            case "ge" -> cb.greaterThanOrEqualTo(expression, (Comparable) value);
            case "lt" -> cb.lessThan(expression, (Comparable) value);
            case "le" -> cb.lessThanOrEqualTo(expression, (Comparable) value);
            default -> throw Exceptions.badRequest("Unsupported operator: " + comparison.operator());
        };
    }

    private static Object convert(Class<?> target, ODataFilterParser.Comparison comparison) {
        String raw = comparison.rawValue();
        Class<?> type = unwrap(target);
        try {
            if (type == String.class) {
                return raw;
            }
            if (type == UUID.class) {
                return UUID.fromString(raw);
            }
            if (type == Boolean.class || type == boolean.class) {
                return Boolean.parseBoolean(raw);
            }
            if (Number.class.isAssignableFrom(type) || type.isPrimitive()) {
                return toNumber(type, raw);
            }
            if (type == Instant.class) {
                return parseInstant(raw);
            }
            if (type == OffsetDateTime.class) {
                return OffsetDateTime.parse(raw);
            }
            if (type == LocalDate.class) {
                return LocalDate.parse(raw);
            }
            if (type == LocalDateTime.class) {
                return LocalDateTime.parse(raw);
            }
            if (type.isEnum()) {
                @SuppressWarnings({"rawtypes", "unchecked"})
                Object value = Enum.valueOf((Class) type, raw);
                return value;
            }
        } catch (RuntimeException ex) {
            throw Exceptions.badRequest("Invalid value for " + comparison.path() + ": " + raw);
        }
        throw Exceptions.badRequest("Unsupported field type for " + comparison.path());
    }

    private static Instant parseInstant(String raw) {
        if (raw.length() <= 10) {
            return LocalDate.parse(raw).atStartOfDay().toInstant(ZoneOffset.UTC);
        }
        if (raw.endsWith("Z") || raw.contains("+")) {
            return Instant.parse(raw);
        }
        return LocalDateTime.parse(raw).toInstant(ZoneOffset.UTC);
    }

    private static Object toNumber(Class<?> type, String raw) {
        if (type == Integer.class || type == int.class) {
            return Integer.valueOf(raw.contains(".") ? raw.substring(0, raw.indexOf('.')) : raw);
        }
        if (type == Long.class || type == long.class) {
            return Long.valueOf(raw.contains(".") ? raw.substring(0, raw.indexOf('.')) : raw);
        }
        if (type == BigDecimal.class) {
            return new BigDecimal(raw);
        }
        if (type == Double.class || type == double.class) {
            return Double.valueOf(raw);
        }
        if (type == Float.class || type == float.class) {
            return Float.valueOf(raw);
        }
        return new BigDecimal(raw);
    }

    private static Class<?> unwrap(Class<?> type) {
        return type;
    }
}
