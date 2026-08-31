-- RGPD: Columna para registrar la aceptación explícita de la política de privacidad
ALTER TABLE users ADD COLUMN privacy_policy_accepted BOOLEAN NOT NULL DEFAULT FALSE;
