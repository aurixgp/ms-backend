package com.boutique.security;

import com.boutique.common.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Crée le premier compte administrateur si la table est vide. */
@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final AdminUserRepository users;
    private final PasswordEncoder encoder;
    private final AppProperties props;

    public AdminBootstrap(AdminUserRepository users, PasswordEncoder encoder, AppProperties props) {
        this.users = users;
        this.encoder = encoder;
        this.props = props;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (users.count() > 0) return;
        var s = props.security();
        users.save(new AdminUser(s.adminEmail().toLowerCase(), encoder.encode(s.adminPassword()), s.adminName()));
        log.warn("Compte admin créé : {} — changez le mot de passe (ADMIN_PASSWORD) avant la mise en ligne.", s.adminEmail());
    }
}
