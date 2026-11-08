package com.boutique.common;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.function.Function;

public final class RaResponse {

    private RaResponse() {}

    /** Réponse de liste avec l'en-tête Content-Range attendu par react-admin. */
    public static <E, D> ResponseEntity<List<D>> list(String resource, Page<E> page, Function<E, D> mapper) {
        long from = page.getPageable().isPaged() ? page.getPageable().getOffset() : 0;
        long to = page.getNumberOfElements() == 0 ? from : from + page.getNumberOfElements() - 1;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, resource + " " + from + "-" + to + "/" + page.getTotalElements())
                .body(page.getContent().stream().map(mapper).toList());
    }
}
