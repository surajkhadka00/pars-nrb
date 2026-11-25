package np.org.nrb.pars.common.query;

import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.metamodel.SingularAttribute;
import np.org.nrb.pars.common.exception.Exceptions;

import java.util.HashMap;
import java.util.Map;

final class EntityPathResolver {

    private static final int MAX_DEPTH = 3;

    private final Metamodel metamodel;
    private final Map<String, Path<?>> joins = new HashMap<>();

    EntityPathResolver(Metamodel metamodel) {
        this.metamodel = metamodel;
    }

    Path<?> resolve(Root<?> root, String property) {
        String[] parts = normalize(property).split("\\.");
        if (parts.length == 0 || parts.length > MAX_DEPTH) {
            throw Exceptions.badRequest("Invalid field: " + property);
        }
        From<?, ?> from = root;
        ManagedType<?> type = metamodel.managedType(root.getJavaType());
        String joinKey = "";
        for (int i = 0; i < parts.length; i++) {
            String name = parts[i];
            Attribute<?, ?> attribute = attribute(type, name);
            if (attribute == null && i == parts.length - 1 && name.endsWith("Id") && name.length() > 2) {
                String association = name.substring(0, name.length() - 2);
                Attribute<?, ?> assoc = attribute(type, association);
                if (assoc != null && assoc.isAssociation()) {
                    from = join(from, joinKey, association);
                    return from.get("id");
                }
            }
            if (attribute == null) {
                throw Exceptions.badRequest("Unknown field: " + property);
            }
            boolean last = i == parts.length - 1;
            if (attribute.isAssociation() && !last) {
                joinKey = joinKey.isEmpty() ? name : joinKey + "." + name;
                from = join(from, joinKey, name);
                type = metamodel.managedType(attribute.getJavaType());
            } else if (last) {
                return from.get(name);
            } else {
                throw Exceptions.badRequest("Cannot navigate field: " + property);
            }
        }
        throw Exceptions.badRequest("Unknown field: " + property);
    }

    String canonicalSortProperty(Class<?> entityType, String property) {
        if (!exists(entityType, property)) {
            throw Exceptions.badRequest("Unknown sort field: " + property);
        }
        String[] parts = normalize(property).split("\\.");
        ManagedType<?> type = metamodel.managedType(entityType);
        StringBuilder canonical = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String name = parts[i];
            Attribute<?, ?> attribute = attribute(type, name);
            if (attribute == null && i == parts.length - 1 && name.endsWith("Id") && name.length() > 2) {
                String association = name.substring(0, name.length() - 2);
                if (canonical.length() > 0) {
                    canonical.append('.');
                }
                canonical.append(association).append(".id");
                return canonical.toString();
            }
            if (canonical.length() > 0) {
                canonical.append('.');
            }
            canonical.append(name);
            if (i < parts.length - 1 && attribute != null && attribute.isAssociation()) {
                type = metamodel.managedType(attribute.getJavaType());
            }
        }
        return canonical.toString();
    }

    boolean exists(Class<?> entityType, String property) {
        try {
            ManagedType<?> type = metamodel.managedType(entityType);
            String[] parts = normalize(property).split("\\.");
            if (parts.length == 0 || parts.length > MAX_DEPTH) {
                return false;
            }
            for (int i = 0; i < parts.length; i++) {
                String name = parts[i];
                Attribute<?, ?> attribute = attribute(type, name);
                if (attribute == null && i == parts.length - 1 && name.endsWith("Id") && name.length() > 2) {
                    Attribute<?, ?> assoc = attribute(type, name.substring(0, name.length() - 2));
                    return assoc != null && assoc.isAssociation();
                }
                if (attribute == null) {
                    return false;
                }
                boolean last = i == parts.length - 1;
                if (!last) {
                    if (!attribute.isAssociation()) {
                        return false;
                    }
                    type = metamodel.managedType(attribute.getJavaType());
                }
            }
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private From<?, ?> join(From<?, ?> from, String joinKey, String name) {
        Path<?> existing = joins.get(joinKey);
        if (existing instanceof From<?, ?> joined) {
            return joined;
        }
        From<?, ?> joined = from.join(name, JoinType.LEFT);
        joins.put(joinKey, joined);
        return joined;
    }

    private static Attribute<?, ?> attribute(ManagedType<?> type, String name) {
        try {
            SingularAttribute<?, ?> attr = type.getSingularAttribute(name);
            return attr;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static String normalize(String property) {
        return property.replace('/', '.');
    }
}
