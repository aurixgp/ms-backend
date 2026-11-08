package com.boutique.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.*;

/**
 * Paramètres de liste au format de ra-data-simple-rest :
 * ?sort=["price","DESC"]&range=[0,24]&filter={"q":"sac","brandId":3}
 */
public record RaQuery(String sortField, Sort.Direction direction, int start, int end, Map<String, Object> filter) {

    private static final ObjectMapper JSON = new ObjectMapper();

    public static RaQuery parse(String sort, String range, String filter) {
        try {
            List<String> s = sort == null ? List.of("id", "ASC") : JSON.readValue(sort, new TypeReference<List<String>>() {});
            List<Integer> r = range == null ? List.of(0, 24) : JSON.readValue(range, new TypeReference<List<Integer>>() {});
            Map<String, Object> f = filter == null ? Map.of() : JSON.readValue(filter, new TypeReference<Map<String, Object>>() {});
            return new RaQuery(
                    s.get(0),
                    "DESC".equalsIgnoreCase(s.get(1)) ? Sort.Direction.DESC : Sort.Direction.ASC,
                    Math.max(r.get(0), 0),
                    Math.max(r.get(1), r.get(0)),
                    f);
        } catch (Exception e) {
            throw new Exceptions.BadRequest("Paramètres de liste invalides");
        }
    }

    /**
     * @param allowedSorts champ react-admin → propriété JPA (les autres champs retombent sur "id")
     */
    public PageRequest pageable(Map<String, String> allowedSorts) {
        int size = Math.min(end - start + 1, 500);
        String property = allowedSorts.getOrDefault(sortField, "id");
        Sort order = Sort.by(direction, property);
        if (!"id".equals(property)) order = order.and(Sort.by(Sort.Direction.DESC, "id"));
        return PageRequest.of(start / size, size, order);
    }

    public String text(String key) {
        Object v = filter.get(key);
        return v == null || v.toString().isBlank() ? null : v.toString().trim();
    }

    public Long longValue(String key) {
        Object v = filter.get(key);
        if (v == null || v.toString().isBlank()) return null;
        return v instanceof Number n ? n.longValue() : Long.valueOf(v.toString());
    }

    public Boolean bool(String key) {
        Object v = filter.get(key);
        if (v == null) return null;
        return v instanceof Boolean b ? b : Boolean.valueOf(v.toString());
    }

    /** Liste d'ids passée par getMany : filter={"id":[1,2,3]} */
    public List<Long> ids() {
        Object v = filter.get("id");
        if (v == null) return null;
        Collection<?> raw = v instanceof Collection<?> c ? c : List.of(v);
        List<Long> out = new ArrayList<>();
        for (Object o : raw) out.add(o instanceof Number n ? n.longValue() : Long.valueOf(o.toString()));
        return out;
    }
}
