package com.boutique.catalog;

import jakarta.persistence.*;

@Entity
@Table(name = "brands")
public class Brand extends NamedEntity {

    @Column(nullable = false)
    private boolean premium;

    @Column(name = "logo_url")
    private String logoUrl;

    public boolean isPremium() { return premium; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public void setPremium(boolean premium) { this.premium = premium; }
}
