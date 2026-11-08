-- Logos de marque (affichés sur les cartes produit)
ALTER TABLE brands ADD COLUMN logo_url VARCHAR(500);

-- Tailles, pointures ou contenances proposées, séparées par des virgules : "36,37,38" / "S,M,L" / "100 ml"
ALTER TABLE products ADD COLUMN sizes VARCHAR(200);

-- La taille choisie suit l'article dans le panier et la commande
ALTER TABLE cart_items ADD COLUMN size VARCHAR(20) NOT NULL DEFAULT '';
ALTER TABLE cart_items DROP CONSTRAINT cart_items_cart_id_product_id_key;
ALTER TABLE cart_items ADD CONSTRAINT cart_items_line_key UNIQUE (cart_id, product_id, size);
ALTER TABLE order_items ADD COLUMN size VARCHAR(20);

-- Pointures
UPDATE products p SET sizes = CASE p.id % 3
        WHEN 0 THEN '35,36,37,38,39,40' WHEN 1 THEN '36,37,38,39,40,41' ELSE '35,36,37,38,39,40,41' END
  FROM categories c WHERE c.id = p.category_id AND c.universe = 'chaussures' AND c.slug LIKE '%-femme';
UPDATE products p SET sizes = CASE p.id % 3
        WHEN 0 THEN '39,40,41,42,43,44' WHEN 1 THEN '40,41,42,43,44,45' ELSE '39,40,41,42,43,44,45' END
  FROM categories c WHERE c.id = p.category_id AND c.universe = 'chaussures' AND c.slug LIKE '%-homme';

-- Tailles de vêtements
UPDATE products p SET sizes = CASE WHEN p.id % 2 = 0 THEN 'XS,S,M,L,XL' ELSE 'S,M,L,XL' END
  FROM categories c WHERE c.id = p.category_id AND c.universe = 'vetements' AND c.slug LIKE '%-femme';
UPDATE products p SET sizes = 'S,M,L,XL,XXL'
  FROM categories c WHERE c.id = p.category_id AND c.universe = 'vetements' AND c.slug LIKE '%-homme' AND c.slug <> 'costumes-homme';
UPDATE products p SET sizes = '46,48,50,52,54'
  FROM categories c WHERE c.id = p.category_id AND c.slug = 'costumes-homme';

-- Contenance des parfums (retirée du nom, affichée en pastille)
UPDATE products p SET sizes = CASE p.id % 5 WHEN 0 THEN '50 ml' WHEN 1 THEN '75 ml' WHEN 2 THEN '90 ml' WHEN 3 THEN '100 ml' ELSE '125 ml' END,
                      name = replace(p.name, ' 100 ml', ''),
                      description = replace(p.description, 'flacon de 100 ml', 'flacon vaporisateur')
  FROM categories c WHERE c.id = p.category_id AND c.universe = 'parfums';

-- Passage des prix en Ariary (1 € ≈ 5 000 Ar), arrondis au millier
UPDATE products SET price = round(price * 5000 / 1000) * 1000,
                    original_price = CASE WHEN original_price IS NULL THEN NULL ELSE round(original_price * 5000 / 1000) * 1000 END;
UPDATE products SET original_price = NULL WHERE original_price <= price;
UPDATE order_items SET unit_price = round(unit_price * 5000 / 1000) * 1000;
UPDATE order_items SET line_total = unit_price * quantity;
UPDATE orders o SET subtotal = (SELECT coalesce(sum(line_total), 0) FROM order_items i WHERE i.order_id = o.id);
UPDATE orders SET shipping = CASE WHEN subtotal >= 200000 THEN 0 ELSE 15000 END;
UPDATE orders SET total = subtotal + shipping;
