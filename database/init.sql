-- Script d'initialisation de la base de données AgriConnect
-- PostgreSQL 15+

-- Création de la base de données
CREATE DATABASE agriconnect_db
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- Connexion à la base de données
\c agriconnect_db;

-- Extension pour les UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Création des types enum
CREATE TYPE role_type AS ENUM ('ACHETEUR', 'PRODUCTEUR', 'ADMIN');
CREATE TYPE categorie_produit_type AS ENUM ('LEGUMES', 'FRUITS', 'CEREALES', 'LEGUMINEUSES', 'HERBES_AROMATIQUES', 'PRODUITS_LAITIERS', 'VIANDES', 'OEUFS', 'MIEL', 'AUTRES');
CREATE TYPE statut_commande_type AS ENUM ('EN_COURS', 'CONFIRMEE', 'EN_PREPARATION', 'EN_LIVRAISON', 'LIVREE', 'ANNULEE');
CREATE TYPE methode_paiement_type AS ENUM ('CARTE_BANCAIRE', 'MOBILE_MONEY', 'VIREMENT_BANCAIRE', 'ESPECES_LIVRAISON', 'PAYPAL');

-- Table des entreprises
CREATE TABLE entreprises (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nom VARCHAR(100) NOT NULL,
    description TEXT,
    adresse TEXT,
    ville VARCHAR(50),
    code_postal VARCHAR(10),
    pays VARCHAR(50),
    telephone VARCHAR(20),
    email VARCHAR(100),
    site_web VARCHAR(255),
    logo VARCHAR(255),
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table des utilisateurs (table parent)
CREATE TABLE utilisateurs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nom VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,
    telephone VARCHAR(20),
    role role_type NOT NULL,
    email_verifie BOOLEAN DEFAULT FALSE,
    email_verification_token VARCHAR(255),
    from_google BOOLEAN DEFAULT FALSE,
    adresse TEXT,
    ville VARCHAR(50),
    code_postal VARCHAR(10),
    pays VARCHAR(50),
    avatar VARCHAR(255),
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    actif BOOLEAN DEFAULT TRUE,
    entreprise_id UUID REFERENCES entreprises(id),
    type_utilisateur VARCHAR(20) NOT NULL
);

-- Table des acheteurs
CREATE TABLE acheteurs (
    id UUID PRIMARY KEY REFERENCES utilisateurs(id) ON DELETE CASCADE
);

-- Table des producteurs
CREATE TABLE producteurs (
    id UUID PRIMARY KEY REFERENCES utilisateurs(id) ON DELETE CASCADE,
    nom_exploitation VARCHAR(100),
    description_exploitation TEXT,
    certificat_bio VARCHAR(255),
    certifie_bio BOOLEAN DEFAULT FALSE,
    adresse_exploitation TEXT,
    ville_exploitation VARCHAR(50),
    code_postal_exploitation VARCHAR(10),
    pays_exploitation VARCHAR(50),
    telephone_exploitation VARCHAR(20),
    site_web VARCHAR(255),
    note_moyenne DECIMAL(3,2) DEFAULT 0.0,
    nombre_evaluations INTEGER DEFAULT 0,
    verifie BOOLEAN DEFAULT FALSE
);

-- Table des produits
CREATE TABLE produits (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nom VARCHAR(100) NOT NULL,
    description TEXT,
    prix DECIMAL(10,2) NOT NULL,
    quantite_disponible INTEGER NOT NULL,
    categorie categorie_produit_type NOT NULL,
    unite VARCHAR(20),
    bio BOOLEAN DEFAULT FALSE,
    origine VARCHAR(100),
    image_principale VARCHAR(255),
    note_moyenne DECIMAL(3,2) DEFAULT 0.0,
    nombre_avis INTEGER DEFAULT 0,
    disponible BOOLEAN DEFAULT TRUE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    producteur_id UUID NOT NULL REFERENCES producteurs(id) ON DELETE CASCADE
);

-- Table des images de produits
CREATE TABLE produit_images (
    produit_id UUID REFERENCES produits(id) ON DELETE CASCADE,
    image_url VARCHAR(255) NOT NULL,
    PRIMARY KEY (produit_id, image_url)
);

-- Table des paniers
CREATE TABLE paniers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    acheteur_id UUID NOT NULL REFERENCES acheteurs(id) ON DELETE CASCADE,
    code_promo VARCHAR(20),
    sous_total DECIMAL(10,2) DEFAULT 0.0,
    frais_livraison DECIMAL(10,2) DEFAULT 0.0,
    remise DECIMAL(10,2) DEFAULT 0.0,
    total DECIMAL(10,2) DEFAULT 0.0,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table des éléments du panier
CREATE TABLE elements_panier (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    panier_id UUID NOT NULL REFERENCES paniers(id) ON DELETE CASCADE,
    produit_id UUID NOT NULL REFERENCES produits(id) ON DELETE CASCADE,
    quantite INTEGER NOT NULL,
    prix_unitaire DECIMAL(10,2) NOT NULL,
    prix_total DECIMAL(10,2) NOT NULL
);

-- Table des commandes
CREATE TABLE commandes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    numero_commande VARCHAR(50) UNIQUE NOT NULL,
    acheteur_id UUID NOT NULL REFERENCES acheteurs(id) ON DELETE CASCADE,
    statut statut_commande_type DEFAULT 'EN_COURS',
    methode_paiement methode_paiement_type,
    sous_total DECIMAL(10,2) NOT NULL,
    frais_livraison DECIMAL(10,2) NOT NULL,
    remise DECIMAL(10,2) NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    code_promo VARCHAR(20),
    adresse_livraison TEXT,
    ville_livraison VARCHAR(50),
    code_postal_livraison VARCHAR(10),
    pays_livraison VARCHAR(50),
    telephone_livraison VARCHAR(20),
    notes TEXT,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_livraison TIMESTAMP
);

-- Table des éléments de commande
CREATE TABLE elements_commande (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    commande_id UUID NOT NULL REFERENCES commandes(id) ON DELETE CASCADE,
    produit_id UUID NOT NULL REFERENCES produits(id) ON DELETE CASCADE,
    quantite INTEGER NOT NULL,
    prix_unitaire DECIMAL(10,2) NOT NULL,
    prix_total DECIMAL(10,2) NOT NULL,
    nom_produit VARCHAR(100),
    description_produit TEXT
);

-- Table des évaluations
CREATE TABLE evaluations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    acheteur_id UUID NOT NULL REFERENCES acheteurs(id) ON DELETE CASCADE,
    produit_id UUID NOT NULL REFERENCES produits(id) ON DELETE CASCADE,
    producteur_id UUID NOT NULL REFERENCES producteurs(id) ON DELETE CASCADE,
    note INTEGER NOT NULL CHECK (note >= 1 AND note <= 5),
    commentaire TEXT,
    verifie BOOLEAN DEFAULT FALSE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index pour améliorer les performances
CREATE INDEX idx_utilisateurs_email ON utilisateurs(email);
CREATE INDEX idx_utilisateurs_role ON utilisateurs(role);
CREATE INDEX idx_produits_categorie ON produits(categorie);
CREATE INDEX idx_produits_producteur ON produits(producteur_id);
CREATE INDEX idx_produits_disponible ON produits(disponible);
CREATE INDEX idx_produits_prix ON produits(prix);
CREATE INDEX idx_produits_nom ON produits(nom);
CREATE INDEX idx_commandes_acheteur ON commandes(acheteur_id);
CREATE INDEX idx_commandes_statut ON commandes(statut);
CREATE INDEX idx_commandes_date_creation ON commandes(date_creation);
CREATE INDEX idx_paniers_acheteur ON paniers(acheteur_id);
CREATE INDEX idx_evaluations_produit ON evaluations(produit_id);
CREATE INDEX idx_evaluations_producteur ON evaluations(producteur_id);

-- Contraintes d'intégrité
ALTER TABLE utilisateurs ADD CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$');
ALTER TABLE produits ADD CONSTRAINT chk_prix_positif CHECK (prix > 0);
ALTER TABLE produits ADD CONSTRAINT chk_quantite_positive CHECK (quantite_disponible >= 0);
ALTER TABLE elements_panier ADD CONSTRAINT chk_quantite_panier_positive CHECK (quantite > 0);
ALTER TABLE elements_commande ADD CONSTRAINT chk_quantite_commande_positive CHECK (quantite > 0);

-- Triggers pour mettre à jour automatiquement les timestamps
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.date_modification = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_utilisateurs_modification BEFORE UPDATE ON utilisateurs FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_produits_modification BEFORE UPDATE ON produits FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_paniers_modification BEFORE UPDATE ON paniers FOR EACH ROW EXECUTE FUNCTION update_modified_column();
CREATE TRIGGER update_commandes_modification BEFORE UPDATE ON commandes FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- Données de test
INSERT INTO entreprises (nom, description, adresse, ville, code_postal, pays, telephone, email) VALUES
('Ferme Bio du Soleil', 'Exploitation agricole biologique spécialisée dans les légumes et fruits', '123 Route des Champs', 'Lyon', '69000', 'France', '+33123456789', 'contact@fermebio.fr'),
('Coopérative Agricole du Sud', 'Coopérative de producteurs locaux', '456 Avenue des Producteurs', 'Marseille', '13000', 'France', '+33456789012', 'info@coopagricole.fr');

-- Insertion d'utilisateurs de test
INSERT INTO utilisateurs (nom, email, mot_de_passe, telephone, role, email_verifie, adresse, ville, code_postal, pays, type_utilisateur) VALUES
('Jean Dupont', 'jean.dupont@email.com', '$2a$10$encrypted_password_hash', '+33123456789', 'ACHETEUR', true, '789 Rue de la Paix', 'Paris', '75001', 'France', 'ACHETEUR'),
('Marie Martin', 'marie.martin@email.com', '$2a$10$encrypted_password_hash', '+33456789012', 'PRODUCTEUR', true, '321 Chemin des Vignes', 'Bordeaux', '33000', 'France', 'PRODUCTEUR'),
('Pierre Durand', 'pierre.durand@email.com', '$2a$10$encrypted_password_hash', '+33567890123', 'ACHETEUR', true, '654 Avenue des Fleurs', 'Toulouse', '31000', 'France', 'ACHETEUR');

-- Insertion des acheteurs et producteurs
INSERT INTO acheteurs (id) VALUES 
((SELECT id FROM utilisateurs WHERE email = 'jean.dupont@email.com')),
((SELECT id FROM utilisateurs WHERE email = 'pierre.durand@email.com'));

INSERT INTO producteurs (id, nom_exploitation, description_exploitation, certifie_bio, adresse_exploitation, ville_exploitation, code_postal_exploitation, pays_exploitation, verifie) VALUES
((SELECT id FROM utilisateurs WHERE email = 'marie.martin@email.com'), 'Ferme Bio du Soleil', 'Exploitation biologique spécialisée dans les légumes et fruits de saison', true, '123 Route des Champs', 'Lyon', '69000', 'France', true);

-- Insertion de produits de test
INSERT INTO produits (nom, description, prix, quantite_disponible, categorie, unite, bio, origine, image_principale, producteur_id) VALUES
('Tomates Bio', 'Tomates cerises biologiques cultivées en serre', 4.50, 100, 'LEGUMES', 'kg', true, 'Lyon', 'tomates-bio.jpg', (SELECT id FROM producteurs LIMIT 1)),
('Pommes Golden', 'Pommes Golden Delicious fraîches du verger', 3.20, 50, 'FRUITS', 'kg', false, 'Bordeaux', 'pommes-golden.jpg', (SELECT id FROM producteurs LIMIT 1)),
('Carottes Bio', 'Carottes biologiques de saison', 2.80, 75, 'LEGUMES', 'kg', true, 'Lyon', 'carottes-bio.jpg', (SELECT id FROM producteurs LIMIT 1)),
('Miel de Lavande', 'Miel artisanal de lavande', 8.90, 30, 'MIEL', 'pot 500g', true, 'Provence', 'miel-lavande.jpg', (SELECT id FROM producteurs LIMIT 1));

-- Insertion d'images de produits
INSERT INTO produit_images (produit_id, image_url) VALUES
((SELECT id FROM produits WHERE nom = 'Tomates Bio'), 'tomates-bio-1.jpg'),
((SELECT id FROM produits WHERE nom = 'Tomates Bio'), 'tomates-bio-2.jpg'),
((SELECT id FROM produits WHERE nom = 'Pommes Golden'), 'pommes-golden-1.jpg'),
((SELECT id FROM produits WHERE nom = 'Carottes Bio'), 'carottes-bio-1.jpg'),
((SELECT id FROM produits WHERE nom = 'Miel de Lavande'), 'miel-lavande-1.jpg');

-- Création des paniers pour les acheteurs
INSERT INTO paniers (acheteur_id) VALUES
((SELECT id FROM acheteurs LIMIT 1)),
((SELECT id FROM acheteurs OFFSET 1 LIMIT 1));

-- Insertion d'éléments dans les paniers
INSERT INTO elements_panier (panier_id, produit_id, quantite, prix_unitaire, prix_total) VALUES
((SELECT id FROM paniers LIMIT 1), (SELECT id FROM produits WHERE nom = 'Tomates Bio'), 2, 4.50, 9.00),
((SELECT id FROM paniers LIMIT 1), (SELECT id FROM produits WHERE nom = 'Pommes Golden'), 1, 3.20, 3.20),
((SELECT id FROM paniers OFFSET 1 LIMIT 1), (SELECT id FROM produits WHERE nom = 'Carottes Bio'), 3, 2.80, 8.40);

-- Mise à jour des totaux des paniers
UPDATE paniers SET 
    sous_total = (SELECT SUM(prix_total) FROM elements_panier WHERE panier_id = paniers.id),
    total = sous_total + frais_livraison - remise
WHERE id IN (SELECT id FROM paniers);

-- Insertion d'évaluations de test
INSERT INTO evaluations (acheteur_id, produit_id, producteur_id, note, commentaire) VALUES
((SELECT id FROM acheteurs LIMIT 1), (SELECT id FROM produits WHERE nom = 'Tomates Bio'), (SELECT id FROM producteurs LIMIT 1), 5, 'Excellent produit, très frais !'),
((SELECT id FROM acheteurs LIMIT 1), (SELECT id FROM produits WHERE nom = 'Pommes Golden'), (SELECT id FROM producteurs LIMIT 1), 4, 'Pommes délicieuses, bien calibrées'),
((SELECT id FROM acheteurs OFFSET 1 LIMIT 1), (SELECT id FROM produits WHERE nom = 'Carottes Bio'), (SELECT id FROM producteurs LIMIT 1), 5, 'Carottes bio parfaites, goût excellent');

-- Mise à jour des notes moyennes des produits
UPDATE produits SET 
    note_moyenne = (SELECT AVG(note) FROM evaluations WHERE produit_id = produits.id),
    nombre_avis = (SELECT COUNT(*) FROM evaluations WHERE produit_id = produits.id)
WHERE id IN (SELECT DISTINCT produit_id FROM evaluations);

-- Mise à jour des notes moyennes des producteurs
UPDATE producteurs SET 
    note_moyenne = (SELECT AVG(note) FROM evaluations WHERE producteur_id = producteurs.id),
    nombre_evaluations = (SELECT COUNT(*) FROM evaluations WHERE producteur_id = producteurs.id)
WHERE id IN (SELECT DISTINCT producteur_id FROM evaluations);

-- Affichage des statistiques
SELECT 'Base de données AgriConnect initialisée avec succès !' as message;
SELECT COUNT(*) as nombre_utilisateurs FROM utilisateurs;
SELECT COUNT(*) as nombre_produits FROM produits;
SELECT COUNT(*) as nombre_paniers FROM paniers;
SELECT COUNT(*) as nombre_evaluations FROM evaluations; 