-- Applicazione

INSERT INTO public.govhub_applications(id, application_id, name, deployed_uri, logo_type, logo_color, logo_bg_color) VALUES (3, 'govio_planner', 'GovIO Planner', 'http://localhost:10003', 'SVG', '#FFFF00', '#0000FF');

-- Utenti

INSERT INTO public.govhub_users (id, principal, full_name, email, enabled) VALUES (nextval('public.seq_govhub_users'), 'pianificatore-scadenze', 'Giulio Piani', 'govio-planner@govhub.it', true);

-- Ruoli

INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), 3, 'govio_planner_sysadmin');

-- Organizzazioni

INSERT INTO public.govhub_organizations (id, tax_code, legal_name, office_at, office_address, office_email_address) VALUES (nextval('public.seq_govhub_organizations'), '00000000000', 'org-0', 'PoloTecnologico', 'Via Strali 10', 'org-0@zion.ix');

-- Servizi

INSERT INTO public.govhub_services (id, name, description) VALUES (nextval('public.seq_govhub_services'), 'service-1', 'Servizio per fare cose');


-- Assegno ruolo pianificatore-scadenze -> govio_planner-operator

INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (
	nextval('public.seq_govhub_authorizations'),
	(SELECT id FROM public.govhub_users WHERE principal='pianificatore-scadenze'),
	(SELECT id FROM public.govhub_roles WHERE name='govio_planner_sysadmin' )
);

do $$
declare
    service_instance_id integer;
	template_id integer;
	scaduta_placeholder_id integer;
	cie_placeholder_id integer;
begin
	INSERT INTO govio_templates(id, name, message_body, subject, has_due_date, has_payment) 
	VALUES (nextval('public.seq_govio_templates'), 'Scadenza CIE', 'Salve, con la presente la informiamo che in data ${due_date} ${scade-scaduta} la Carta di Identità elettronica numero ${cie}. Per maggiori informazioni sulle modalità di rinnovo può consultare https://comune.dimostrativo.it.', 'Scadenza CIE n. ${cie}', true, false) 
	returning id into template_id;

	INSERT INTO govio_placeholders(id, name, type, example) VALUES (
		nextval('seq_govio_placeholders'), 
		'cie', 
		'STRING', 
		'CA000000AA') returning id into cie_placeholder_id;

	INSERT INTO govio_placeholders(id, name, type, example) VALUES (
		nextval('seq_govio_placeholders'), 
		'scade-scaduta', 
		'STRING', 
		'è scaduta') returning id into scaduta_placeholder_id;

	INSERT INTO govio_template_placeholders(id_govio_template, id_govio_placeholder, mandatory, position) VALUES (template_id, cie_placeholder_id, true, 1);
	INSERT INTO govio_template_placeholders(id_govio_template, id_govio_placeholder, mandatory, position) VALUES (template_id, scaduta_placeholder_id, true, 2);

	INSERT INTO govio_service_instances(id, id_govhub_service, id_govhub_organization, id_govio_template,apikey) 
	VALUES (
		nextval('public.seq_govio_service_instances'), 	
		(select id from govhub_services where name='service-1'),
		(select id from govhub_organizations where tax_code='00000000000'),
		template_id,
		'17886617e07d47e8b1ba314f2f1e3052') returning id into service_instance_id;


end $$;


-- Configurazione Service Instance Scadenza CIE 

-- INSERT INTO govio_templates(id, message_body, subject, has_due_date, has_payment) VALUES (nextval('public.seq_govio_templates'), 'Salve, con la presente la informiamo che in data ${due_date} ${scade-scaduta} la Carta di Identità elettronica numero ${cie.uppercase}. Per maggiori informazioni sulle modalità di rinnovo può consultare https://comune.dimostrativo.it.', 'Scadenza CIE n. ${cie.uppercase}', true, false);

-- INSERT INTO govio_service_instances(id, id_govhub_service, id_govhub_organization, id_govio_template,apikey) VALUES (
--	nextval('public.seq_govio_service_instances'), 
--	(select id from govhub_services where name='service-1'),
--	(select id from govhub_organizations where tax_code='00000000000'),
--	1,
--	'17886617e07d47e8b1ba314f2f1e3052');

--INSERT INTO govio_placeholders(id, name, type, example) VALUES (nextval('public.seq_govio_placeholders'), 'cie', 'STRING', 'CA000000AA');
--INSERT INTO govio_placeholders(id, name, type, example) VALUES (nextval('public.seq_govio_placeholders'), 'scade-scaduta', 'STRING', 'è scaduta');

--INSERT INTO govio_template_placeholders(id_govio_template, id_govio_placeholder, mandatory, position) VALUES (1, 1, true, 1);
--INSERT INTO govio_template_placeholders(id_govio_template, id_govio_placeholder, mandatory, position) VALUES (1, 2, true, 1);

