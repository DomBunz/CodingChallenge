-- V2__initial_data.sql
-- Initial data for insurance premium calculator

-- Insert region factors for German federal states
INSERT INTO region_factors (federal_state, factor) VALUES
('DEFAULT', 1.0),
('Baden-Württemberg', 1.2),
('Bayern', 1.1),
('Berlin', 1.4),
('Brandenburg', 1.0),
('Bremen', 1.3),
('Hamburg', 1.4),
('Hessen', 1.2),
('Mecklenburg-Vorpommern', 0.9),
('Niedersachsen', 1.0),
('Nordrhein-Westfalen', 1.3),
('Rheinland-Pfalz', 1.1),
('Saarland', 1.0),
('Sachsen', 0.9),
('Sachsen-Anhalt', 0.9),
('Schleswig-Holstein', 1.0),
('Thüringen', 0.9);

-- Insert vehicle types with their factors
INSERT INTO vehicle_types (name, factor) VALUES
('Kleinwagen', 0.8),
('Kompaktklasse', 1.0),
('Mittelklasse', 1.2),
('Oberklasse', 1.5),
('SUV', 1.3),
('Sportwagen', 1.8),
('Van', 1.1),
('Elektroauto', 0.7);

-- Insert mileage factors as defined in requirements
INSERT INTO mileage_factors (min_mileage, max_mileage, factor) VALUES
(0, 5000, 0.5),
(5001, 10000, 1.0),
(10001, 20000, 1.5),
(20001, NULL, 2.0);

-- Insert system configurations
INSERT INTO system_configurations (config_key, config_value, description) VALUES
('BASE_PREMIUM', '500.00', 'Base premium amount in EUR for insurance calculations');
