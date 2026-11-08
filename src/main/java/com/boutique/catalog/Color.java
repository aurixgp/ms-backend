package com.boutique.catalog;

import jakarta.persistence.*;

@Entity
@Table(name = "colors")
public class Color extends NamedEntity {

    @Column(nullable = false, length = 7)
    private String hex;

    public String getHex() { return hex; }
    public void setHex(String hex) { this.hex = hex; }
}
