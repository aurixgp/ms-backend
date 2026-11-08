-- Commandes d'exemple pour le back-office (à supprimer en production)
DO $$
DECLARE
    firsts TEXT[] := ARRAY['Camille','Léa','Sarah','Manon','Chloé','Inès','Julie','Emma','Clara','Nadia','Aïcha','Sophie','Hanta','Fanja','Lucie','Margaux'];
    lasts  TEXT[] := ARRAY['Martin','Bernard','Rakoto','Petit','Durand','Leroy','Moreau','Randria','Fournier','Girard','Bonnet','Lambert','Faure','Rousseau'];
    cities TEXT[] := ARRAY['Paris','Lyon','Marseille','Toulouse','Nantes','Bordeaux','Lille','Rennes','Strasbourg','Montpellier'];
    statuses TEXT[] := ARRAY['LIVREE','LIVREE','LIVREE','EXPEDIEE','EXPEDIEE','PAYEE','PAYEE','EN_ATTENTE','ANNULEE'];
    o_id BIGINT; fn TEXT; ln TEXT; city TEXT; created TIMESTAMPTZ;
    n_items INT; sub NUMERIC(10,2); ship NUMERIC(10,2);
    p RECORD; q INT;
BEGIN
    PERFORM setseed(0.42);
    FOR i IN 1..48 LOOP
        fn := firsts[1 + floor(random() * array_length(firsts, 1))::int];
        ln := lasts[1 + floor(random() * array_length(lasts, 1))::int];
        city := cities[1 + floor(random() * array_length(cities, 1))::int];
        created := now() - (random() * interval '30 days');

        INSERT INTO orders (number, status, email, first_name, last_name, phone, address, postal_code, city, country,
                            subtotal, shipping, total, created_at, updated_at)
        VALUES ('CMD-' || nextval('order_number_seq'),
                statuses[1 + floor(random() * array_length(statuses, 1))::int],
                lower(translate(fn, 'ÉéèëÏïîôç', 'EeeeIiioc')) || '.' || lower(ln) || '@exemple.fr', fn, ln,
                '06 ' || regexp_replace(lpad((floor(random() * 99999999))::text, 8, '0'), '(..)(..)(..)(..)', '\1 \2 \3 \4'),
                (1 + floor(random() * 120))::int || ' rue de la République',
                lpad((10000 + floor(random() * 85000))::int::text, 5, '0'), city, 'France',
                0, 0, 0, created, created)
        RETURNING id INTO o_id;

        n_items := 1 + floor(random() * 3)::int;
        FOR p IN SELECT pr.id, pr.name, pr.price, b.name AS brand, c.name AS color
                 FROM products pr JOIN brands b ON b.id = pr.brand_id JOIN colors c ON c.id = pr.color_id
                 ORDER BY random() LIMIT n_items LOOP
            q := CASE WHEN random() < 0.85 THEN 1 ELSE 2 END;
            INSERT INTO order_items (order_id, product_id, product_name, brand_name, color_name, unit_price, quantity, line_total)
            VALUES (o_id, p.id, p.name, p.brand, p.color, p.price, q, p.price * q);
        END LOOP;

        SELECT sum(line_total) INTO sub FROM order_items WHERE order_id = o_id;
        ship := CASE WHEN sub >= 34 THEN 0 ELSE 4.90 END;
        UPDATE orders SET subtotal = sub, shipping = ship, total = sub + ship WHERE id = o_id;
    END LOOP;
END $$;
