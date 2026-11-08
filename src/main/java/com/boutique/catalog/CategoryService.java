package com.boutique.catalog;

import com.boutique.catalog.CatalogDtos.CategoryNode;
import com.boutique.catalog.CatalogDtos.CategoryPage;
import com.boutique.catalog.CatalogDtos.Ref;
import com.boutique.common.Exceptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/** Peu de catégories : on charge tout en une requête et on parcourt l'arbre en mémoire. */
@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository repo;

    public CategoryService(CategoryRepository repo) {
        this.repo = repo;
    }

    private record Tree(Map<Long, Category> byId, Map<String, Category> bySlug, Map<Long, List<Category>> children) {}

    private Tree load() {
        Map<Long, Category> byId = new HashMap<>();
        Map<String, Category> bySlug = new HashMap<>();
        Map<Long, List<Category>> children = new HashMap<>();
        for (Category c : repo.findAllWithParent()) {
            byId.put(c.getId(), c);
            bySlug.put(c.getSlug(), c);
            Long parentId = c.getParent() == null ? null : c.getParent().getId();
            children.computeIfAbsent(parentId, k -> new ArrayList<>()).add(c);
        }
        children.values().forEach(l -> l.sort(Comparator.comparingInt(Category::getPosition)));
        return new Tree(byId, bySlug, children);
    }

    public Set<Long> descendantIds(String slug) {
        if (slug == null || slug.isBlank()) return null;
        Tree t = load();
        Category root = t.bySlug().get(slug);
        if (root == null) throw new Exceptions.NotFound("Catégorie inconnue : " + slug);
        return collect(root.getId(), t);
    }

    /** Rubrique demandée, éventuellement restreinte à un rayon (femme / homme). */
    public Set<Long> resolve(String category, String gender) {
        Set<Long> byCategory = descendantIds(category);
        Set<Long> byGender = descendantIds(gender);
        if (byCategory == null) return byGender;
        if (byGender == null) return byCategory;
        Set<Long> both = new HashSet<>(byCategory);
        both.retainAll(byGender);
        return both.isEmpty() ? Set.of(-1L) : both;
    }

    public Set<Long> descendantIds(Long id) {
        Tree t = load();
        if (!t.byId().containsKey(id)) throw new Exceptions.NotFound("Catégorie inconnue : " + id);
        return collect(id, t);
    }

    private Set<Long> collect(Long rootId, Tree t) {
        Set<Long> ids = new HashSet<>();
        Deque<Long> stack = new ArrayDeque<>(List.of(rootId));
        while (!stack.isEmpty()) {
            Long id = stack.pop();
            ids.add(id);
            t.children().getOrDefault(id, List.of()).forEach(c -> stack.push(c.getId()));
        }
        return ids;
    }

    public List<CategoryNode> tree() {
        Tree t = load();
        return t.children().getOrDefault(null, List.of()).stream().map(c -> node(c, t)).toList();
    }

    private CategoryNode node(Category c, Tree t) {
        return new CategoryNode(c.getSlug(), c.getName(), c.getUniverse(),
                t.children().getOrDefault(c.getId(), List.of()).stream().map(ch -> node(ch, t)).toList());
    }

    public CategoryPage page(String slug) {
        Tree t = load();
        Category c = t.bySlug().get(slug);
        if (c == null) throw new Exceptions.NotFound("Catégorie inconnue : " + slug);
        Long parentId = c.getParent() == null ? null : c.getParent().getId();
        Category parent = parentId == null ? null : t.byId().get(parentId);
        return new CategoryPage(
                ref(c),
                breadcrumb(c.getId(), t),
                parent == null ? null : ref(parent),
                t.children().getOrDefault(c.getId(), List.of()).stream().map(CategoryService::ref).toList(),
                t.children().getOrDefault(parentId, List.of()).stream().map(CategoryService::ref).toList());
    }

    public List<Ref> breadcrumb(Long categoryId) {
        return breadcrumb(categoryId, load());
    }

    private List<Ref> breadcrumb(Long categoryId, Tree t) {
        LinkedList<Ref> path = new LinkedList<>();
        Category cur = t.byId().get(categoryId);
        while (cur != null) {
            path.addFirst(ref(cur));
            cur = cur.getParent() == null ? null : t.byId().get(cur.getParent().getId());
        }
        return path;
    }

    private static Ref ref(Category c) {
        return new Ref(c.getSlug(), c.getName());
    }
}
