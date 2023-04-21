-- Servizio di test
INSERT INTO govio_planner_exp_files(id,name,location,creation_date) VALUES (1,'pippo','/etc/govio-planner/exp-files/CIE_scadenza_2018_-_dic_2021_-_tracciato.csv','2007-12-30T10:15:30+01:00');
ALTER SEQUENCE seq_govio_planner_files RESTART WITH 2;


INSERT INTO govio_planner_ntfy_files(id,creation_date,status,location,size,id_govio_planner_file) VALUES (1,'2007-12-03T10:15:30+01:00','CREATED','/etc/govio-planner/ntfy-files/CSVTestNotifiche.csv',NULL,'1');
ALTER SEQUENCE seq_govio_planner_govio_files RESTART WITH 2;
