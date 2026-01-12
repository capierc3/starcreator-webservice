CREATE TABLE government_type_ref
(
    id           SERIAL PRIMARY KEY,
    name         VARCHAR(100),
    faction_type VARCHAR(100),
    description  VARCHAR(255)
);

INSERT INTO government_type_ref (name, faction_type, description) VALUES
-- Classic / Realistic
('Monarchy', 'Classic', 'Rule by a single hereditary sovereign whose authority is often legitimized by tradition or lineage.'),
('Oligarchy', 'Classic', 'Power is concentrated among a small group of elites, often defined by wealth, status, or influence.'),
('Democracy', 'Classic', 'Governance by the populace, where citizens vote directly or through elected representatives.'),
('Republic', 'Classic', 'A representative system where leaders are elected to govern according to established laws.'),
('Theocracy', 'Classic', 'Religious leaders govern according to divine doctrine or sacred law.'),
('Dictatorship', 'Classic', 'Absolute power is held by a single ruler, typically enforced through coercion or control.'),
('Autocracy', 'Classic', 'Centralized authority vested in one individual with minimal legal or institutional restraint.'),
('Meritocracy', 'Classic', 'Leadership roles are assigned based on skill, achievement, or perceived competence.'),
('Guildocracy', 'Classic', 'Trade guilds or professional organizations collectively control political power.'),
('Military Junta', 'Classic', 'A ruling council of military leaders maintains order through force and discipline.'),

-- Galactic / Space-Focused
('Galactic Council', 'Galactic', 'A multi-world representative body that governs through debate, diplomacy, and collective decision-making.'),
('Feudal Space Empire', 'Galactic', 'A vast empire where planetary lords swear fealty to a central ruler in exchange for autonomy.'),
('Planetary Confederacy', 'Galactic', 'A loose alliance of worlds united for mutual defense and economic cooperation.'),
('Stellar Dominion', 'Galactic', 'An expansionist regime asserting control over star systems through authority and conquest.'),
('Interstellar Federation', 'Galactic', 'A structured union of planets sharing power between local governments and a central authority.'),
('Cosmic Assembly', 'Galactic', 'A ceremonial and legislative body representing diverse species and cultures across the galaxy.'),
('Council of Elders', 'Galactic', 'Respected leaders chosen for wisdom and age guide policy across multiple worlds.'),
('Corporate State', 'Galactic', 'Government is operated as a business, prioritizing profit, contracts, and shareholder interests.'),
('Technocracy', 'Galactic', 'Scientists and engineers rule, valuing efficiency, data, and rational planning above all.'),
('AI Governance', 'Galactic', 'An artificial intelligence administers law and policy based on predictive logic and optimization.'),
('Trade Consortium', 'Galactic', 'Economic power blocs dominate governance, regulating commerce and interstellar trade routes.'),

-- Exotic / Alien / Mystical
('Hive Mind', 'Exotic', 'A collective consciousness where individual identity is subsumed into a unified will.'),
('Nomadic Tribal Council', 'Exotic', 'Mobile clans governed by elders who guide survival, migration, and tradition.'),
('Sectocracy', 'Exotic', 'Rule by ideological or religious sects competing or cooperating for dominance.'),
('Eldritch Rule', 'Exotic', 'Governance influenced or controlled by incomprehensible, otherworldly entities.'),
('Chronocracy', 'Exotic', 'Leadership determined by control or mastery over time and temporal phenomena.'),
('Psychic Oligarchy', 'Exotic', 'A small class of powerful psychics governs through mental dominance and foresight.'),
('Starborn Assembly', 'Exotic', 'Celestial or ascended beings collectively rule, claiming cosmic legitimacy.'),
('Arcane Order', 'Exotic', 'Mystics or sorcerers govern through esoteric knowledge and ritual authority.'),
('Celestial Synod', 'Exotic', 'A sacred council interpreting cosmic signs to guide governance.'),
('Void Council', 'Exotic', 'An enigmatic ruling body drawing power from the unknown depths of space.'),

-- Rebel / Unstructured / Outlaw
('Anarchy', 'Outlaw', 'No central authority exists; order is maintained through personal power or local agreements.'),
('Rogue Council', 'Outlaw', 'An informal leadership group operating outside recognized law or authority.'),
('Pirate Coalition', 'Outlaw', 'Independent pirate bands united for mutual profit and protection.'),
('Freehold', 'Outlaw', 'A self-governing settlement claiming independence from external powers.'),
('Shadow Syndicate', 'Outlaw', 'A covert criminal organization exerting influence through secrecy and control.'),
('Underground Network', 'Outlaw', 'Decentralized cells coordinating resistance, smuggling, or espionage.'),
('Resistance Front', 'Outlaw', 'An organized insurgency opposing an established regime.'),
('Nomadic Clan', 'Outlaw', 'A roaming group bound by kinship and survival rather than formal law.'),
('Insurgent Directorate', 'Outlaw', 'A centralized command structure directing revolutionary or guerrilla actions.'),
('Fringe Assembly', 'Outlaw', 'A loose gathering of marginalized groups operating beyond mainstream society.'),

-- Hybrid / Mixed Systems
('Corporate Technocracy', 'Hybrid', 'Corporate leaders and technical experts jointly govern for profit and efficiency.'),
('Feudal Hive Empire', 'Hybrid', 'A hierarchical empire combining feudal loyalty with collective hive control.'),
('Theocratic Galactic Council', 'Hybrid', 'Religious authority blended with interstellar representative governance.'),
('Mercantile Oligarchy', 'Hybrid', 'Merchant elites dominate political power through economic leverage.'),
('Military Corporate Junta', 'Hybrid', 'Corporate and military leaders rule together to enforce order and profitability.'),
('Cybernetic Autocracy', 'Hybrid', 'A single ruler augmented by cybernetic systems wields absolute control.'),
('Stellar Sectocracy', 'Hybrid', 'Religious sects govern star systems according to shared cosmic doctrine.'),
('Pirate Confederation', 'Hybrid', 'Semi-independent pirate factions loosely united under common rules.'),
('Rebel Commune', 'Hybrid', 'Collective self-rule formed from revolutionary or post-collapse communities.'),
('Frontier Council', 'Hybrid', 'Local leaders govern remote territories through pragmatic cooperation.'),
('Nomadic Federation', 'Hybrid', 'Mobile groups united under a shared charter while retaining independence.');



