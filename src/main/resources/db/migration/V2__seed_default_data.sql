INSERT INTO origins (name, position) VALUES
    ('Floresta Ombrófila Mista', 1),
    ('Mata Atlântica', 2);

INSERT INTO plant_types (name, position) VALUES
    ('Forração', 1),
    ('Arbusto', 2),
    ('Árvore', 3),
    ('Palmeira', 4);

INSERT INTO sizes (name, description, position) VALUES
    ('Pequeno', 'P · baixa', 1),
    ('Médio', 'M · média', 2),
    ('Grande', 'G · alta', 3);

INSERT INTO image_views (name, position) VALUES
    ('Elevação', 1),
    ('Vista superior', 2),
    ('Perspectiva', 3);

INSERT INTO collections (name, is_favorites) VALUES
    ('Favoritos', TRUE);
