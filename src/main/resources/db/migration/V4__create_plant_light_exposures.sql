CREATE TABLE plant_light_exposures (
    plant_id BIGINT NOT NULL REFERENCES plants (id) ON DELETE CASCADE,
    exposure VARCHAR(20) NOT NULL CHECK (exposure IN ('SHADE', 'PARTIAL_SHADE', 'FULL_SUN')),
    PRIMARY KEY (plant_id, exposure)
);

ALTER TABLE plant_light_exposures ENABLE ROW LEVEL SECURITY;
