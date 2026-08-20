package com.kereis.tahore.documentprocessing.domain.model;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Un lot est un groupe de reception. Il n'est pas cree par le module : il se
 * lit, en regroupant les documents par identifiant de reception.
 */
public record Lot(String identifiant, List<DocumentEntrant> documents) {

    public Set<TypeDocument> typesPresents() {
        return documents.stream().map(DocumentEntrant::type).collect(Collectors.toSet());
    }

    /** Plusieurs bulletins dans un meme lot : le lot repart au gestionnaire. */
    public boolean multiAdhesion() {
        return documents.stream().filter(d -> d.type() == TypeDocument.BULLETIN_ADHESION).count() > 1;
    }
}
