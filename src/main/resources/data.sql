
-- Applicazione

INSERT INTO public.govhub_applications(id, application_id, name, deployed_uri) VALUES (3, 'govio_planner', 'GovIO Planner', 'http://localhost:10003');

-- Ruoli

INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), 3, 'govio_planner_operator');

-- Assegno ruolo amministratore -> govio_planner-operator

INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (
	nextval('public.seq_govhub_authorizations'),
	(SELECT id FROM public.govhub_users WHERE principal='amministratore'),
	(SELECT id FROM public.govhub_roles WHERE name='govio_planner_operator' )
);


