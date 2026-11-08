package com.boutique.catalog;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category extends NamedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @Column(nullable = false)
    private int position;

    /** Univers transversal (sacs, chaussures, montres…) commun aux rayons Femme et Homme. */
    private String universe;

    public Category getParent() { return parent; }
    public void setParent(Category parent) { this.parent = parent; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    public String getUniverse() { return universe; }
    public void setUniverse(String universe) { this.universe = universe; }
}
