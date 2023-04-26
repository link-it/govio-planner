-- Applicazioni

INSERT INTO public.govhub_applications (id, application_id, name, deployed_uri) VALUES (2, 'govio', 'GovIO', 'http://localhost/govio');

-- Utenze

INSERT INTO govhub_users (id, principal, full_name, email, enabled) VALUES (nextval('public.seq_govhub_users'), 'user_govio_sender', 'Marco Sender', 'govio_sender@govio.it', true);
INSERT INTO govhub_users (id, principal, full_name, email, enabled) VALUES (nextval('public.seq_govhub_users'), 'user_govio_viewer', 'Angovio Visore', 'govio_viewer@govio.it', true);
INSERT INTO govhub_users (id, principal, full_name, email, enabled) VALUES (nextval('public.seq_govhub_users'), 'amministratore', 'Giovadmin', 'sysadmin@govio.it', true);
INSERT INTO govhub_users (id, principal, full_name, email, enabled) VALUES (nextval('public.seq_govhub_users'), 'ospite', 'Antonio Rossi', 'guest@govio.it', true);
INSERT INTO govhub_users (id, principal, full_name, email, enabled) VALUES (nextval('public.seq_govhub_users'), 'disabiledUser', 'Mario Rossi', 'utente_disabled@govio.it', false);
INSERT INTO govhub_users (id, principal, full_name, email, enabled) VALUES (nextval('public.seq_govhub_users'), 'user_govio_sender_si', 'Silvio Innocenti', 'govio_sender_si@govio.it', true);

-- ALTER SEQUENCE SEQ_GOVHUB_USERS RESTART WITH 4;

-- Ruoli

INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), (SELECT id FROM govhub_applications WHERE application_id='govio'), 'govio_sender');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), (SELECT id FROM govhub_applications WHERE application_id='govio'), 'govio_viewer');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), (SELECT id FROM govhub_applications WHERE application_id='govio'), 'govio_sysadmin');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), (SELECT id FROM govhub_applications WHERE application_id='govio'), 'govio_service_instance_viewer');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), (SELECT id FROM govhub_applications WHERE application_id='govio'), 'govio_service_instance_editor');

INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), (SELECT id FROM govhub_applications WHERE application_id='govio'), 'govhub_sysadmin');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), (SELECT id FROM govhub_applications WHERE application_id='govio'), 'govio_planner_operator');

-- INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), (SELECT id FROM govhub_applications WHERE application_id='govio'), 'govio_sender_si1');

-- ALTER SEQUENCE SEQ_GOVHUB_ROLES RESTART WITH 4;

-- Organizations

INSERT INTO public.govhub_organizations (id, tax_code, legal_name) VALUES (nextval('public.seq_govhub_organizations'), '80015010723', 'Cie org');
INSERT INTO public.govhub_organizations (id, tax_code, legal_name) VALUES (nextval('public.seq_govhub_organizations'), '12345678901', 'Ente Creditore');
INSERT INTO public.govhub_organizations (id, tax_code, legal_name) VALUES (nextval('public.seq_govhub_organizations'), '12345678902', 'Ente Creditore 2');
INSERT INTO public.govhub_organizations (id, tax_code, legal_name) VALUES (nextval('public.seq_govhub_organizations'), '12345678903', 'Ente Creditore 3');

UPDATE public.govhub_organizations SET logo = '0x{0}', logo_media_type = 'image/png' WHERE tax_code = '12345678903';
UPDATE public.govhub_organizations SET logo_miniature = '0x{0}', logo_miniature_media_type = 'image/png' WHERE tax_code = '12345678903';


-- ALTER SEQUENCE SEQ_GOVHUB_ORGANIZATIONS RESTART WITH 3;

-- Services

INSERT INTO public.govhub_services (id, name, description) VALUES (nextval('public.seq_govhub_services'), 'CIE', 'Servizio dev');
INSERT INTO public.govhub_services (id, name, description) VALUES (nextval('public.seq_govhub_services'), 'Servizio Generico', 'Esempio di servizio');
INSERT INTO public.govhub_services (id, name, description) VALUES (nextval('public.seq_govhub_services'), 'Servizio senza autorizzazioni', 'Servizio non autorizzato');
INSERT INTO public.govhub_services (id, name, description) VALUES (nextval('public.seq_govhub_services'), 'SUAP-Integrazione', 'Service for customer management');
INSERT INTO public.govhub_services (id, name, description) VALUES (nextval('public.seq_govhub_services'), 'IMU-ImpostaMunicipaleUnica', 'Imposta municipale unica');
INSERT INTO public.govhub_services (id, name, description) VALUES (nextval('public.seq_govhub_services'), 'TARI', 'Tassa sui rifiuti');
INSERT INTO public.govhub_services (id, name, description) VALUES (nextval('public.seq_govhub_services'), 'Portale ZTL', 'Servizio di registrazione accessi ZTL comunale');
INSERT INTO public.govhub_services (id, name, description) VALUES (nextval('public.seq_govhub_services'), 'Variazione Residenza', 'Richieste di variazione residenza');
INSERT INTO public.govhub_services (id, name, description) VALUES (nextval('public.seq_govhub_services'), 'Servizi Turistici', 'Portale di riferimento per i turisti');

UPDATE public.govhub_services SET logo = '0x{0}', logo_media_type = 'image/png' WHERE name = 'Servizio Generico';
UPDATE public.govhub_services SET logo_miniature = '0x{0}', logo_miniature_media_type = 'image/png' WHERE name = 'Servizio Generico';

-- ALTER SEQUENCE SEQ_GOVHUB_SERVICES RESTART WITH 9;

-- Autorizzazioni

-- amministratore -> govio_sysadmin

INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (nextval('public.seq_govhub_authorizations'), (SELECT id FROM public.govhub_users WHERE principal='amministratore'), (SELECT id FROM public.govhub_roles WHERE name='govio_sysadmin'));

-- amministratore -> govhub_sysadmin

INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (nextval('public.seq_govhub_authorizations'), (SELECT id FROM public.govhub_users WHERE principal='amministratore'), (SELECT id FROM public.govhub_roles WHERE name='govhub_sysadmin'));

-- amministratore -> govio_planner_operator

INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (nextval('public.seq_govhub_authorizations'), (SELECT id FROM public.govhub_users WHERE principal='amministratore'), (SELECT id FROM public.govhub_roles WHERE name='govio_planner_operator'));

-- user_govio_sender -> govio_sender

INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (nextval('public.seq_govhub_authorizations'), (SELECT id FROM public.govhub_users WHERE principal='user_govio_sender'), (SELECT id FROM public.govhub_roles WHERE name='govio_sender'));

-- user_govio_viewer -> govio_viewer

INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (nextval('public.seq_govhub_authorizations'), (SELECT id FROM public.govhub_users WHERE principal='user_govio_viewer'), (SELECT id FROM public.govhub_roles WHERE name='govio_viewer'));

-- user_govio_sender_si -> govio_sender_si1

INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (nextval('public.seq_govhub_authorizations'), (SELECT id FROM public.govhub_users WHERE principal='user_govio_sender_si'), (SELECT id FROM public.govhub_roles WHERE name='govio_sender'));


-- Autorizzazione esplicita su organization e service

INSERT INTO public.govhub_auth_organizations (id_govhub_authorization, id_govhub_organization) VALUES (5, (SELECT id FROM public.govhub_organizations WHERE tax_code='80015010723'));

INSERT INTO public.govhub_auth_services (id_govhub_authorization, id_govhub_service) VALUES (5, (SELECT id FROM public.govhub_services WHERE name='CIE'));


-- ALTER SEQUENCE SEQ_GOVHUB_AUTHORIZATIONS RESTART WITH 3;


-- inserimento dati per la configurazione di un template per la spedizione di un messaggio
-- INSERT INTO govio_templates(id, message_body, subject, has_due_date, has_payment, name, description) VALUES (
--	nextval('public.seq_govio_templates'),
--	'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur',
--	'Lorem ipsum dolor sit amet.',
--	false,
--	false,
--	'Lorem', 'Description...');

-- INSERT INTO govio_templates(id, message_body, subject, has_due_date, has_payment, name, description) VALUES (
--		nextval('public.seq_govio_templates'), 
--		'Salve, con la presente la informiamo che in data ${due_date} scadrà la Carta di Identità elettronica numero ${cie}. Per maggiori informazioni sulle modalità di rinnovo può consultare https://comune.dimostrativo.it.', 
--		'Scadenza CIE n. ${cie}', 
--		true, 
--		false,
--		'CIE', 'Description...');
		
-- INSERT INTO govio_placeholders(id, name, type, example, description) VALUES (nextval('public.seq_govio_placeholders'),'cie','STRING','CA000000AA', 'CIE Serial Number');
-- INSERT INTO govio_placeholders(id, name, type, example) VALUES (nextval('public.seq_govio_placeholders'),'appointment','DATE','2100-12-31T12:00');
-- INSERT INTO govio_placeholders(id, name, type, example) VALUES (nextval('public.seq_govio_placeholders'),'at','STRING','Ufficio numero 1');
-- INSERT INTO govio_placeholders(id, name, type, example) VALUES (nextval('public.seq_govio_placeholders'),'indirizzo','STRING','Palazzo Vecchio 1');
-- INSERT INTO govio_placeholders(id, name, type, example) VALUES (nextval('public.seq_govio_placeholders'),'patente','STRING','PAT 43der54');
-- INSERT INTO govio_placeholders(id, name, type, example) VALUES (nextval('public.seq_govio_placeholders'),'motor','STRING','Motorizzazione di Pisa');

-- template Lorem
-- INSERT INTO govio_template_placeholders(id_govio_template, id_govio_placeholder, mandatory, position) VALUES (1, 1, true, 1);
-- INSERT INTO govio_template_placeholders(id_govio_template, id_govio_placeholder, mandatory, position) VALUES (1, 2, true, 2);

-- template CIE
-- INSERT INTO govio_template_placeholders(id_govio_template, id_govio_placeholder, mandatory, position) VALUES (2, 1, true, 1);

-- INSERT INTO govio_service_instances(id, id_govhub_service, id_govhub_organization, id_govio_template,apikey) VALUES (
-- 		nextval('public.seq_govio_service_instances'), (select id from govhub_services where name='Servizio Generico'), (select id from govhub_organizations where tax_code='12345678901'), 1, '17886617e07d47e8b1ba314f2f1e3052');
		
-- INSERT INTO govio_service_instances(id, id_govhub_service, id_govhub_organization, id_govio_template,apikey, enabled) VALUES (
--		nextval('public.seq_govio_service_instances'), (select id from govhub_services where name='IMU-ImpostaMunicipaleUnica'), (select id from govhub_organizations where tax_code='12345678902'),	1, '17886617e07d47e8b1ba314f2f1e3053', false);

-- INSERT INTO govio_service_instances(id, id_govhub_service, id_govhub_organization, id_govio_template,apikey) VALUES (
--		nextval('public.seq_govio_service_instances'), (select id from govhub_services where name='CIE'), (select id from govhub_organizations where tax_code='80015010723'),	2, '17886617e07d47e8b1ba314f2f1e3054');





























