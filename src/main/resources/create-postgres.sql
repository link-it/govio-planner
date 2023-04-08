create sequence seq_govio_planner_files start 1 increment 1;
create sequence seq_govio_planner_govio_files start 1 increment 1;

create table govio_planner_files (
    id BIGINT not null,
	creation_date timestamp not null,
	location varchar(2048) not null,
	name varchar(255) not null,
	processing_date timestamp,
	size BIGINT,
	status varchar(255) not null,
	id_govauth_user int8 not null,
	plan_id varchar(255) not null
	primary key (id)
);

create table govio_planner_govio_files (
 	id BIGINT not null,
	creation_date timestamp not null,
	location varchar(1024) not null,
    name varchar(255) not null,
	size BIGINT,
	status varchar(255) not null,
	id_govio_planner_file int8 not null,
	primary key (id)
);

alter table govio_planner_files 
   add constraint GovioFile_GovhubUser 
   foreign key (id_govauth_user) 
   references govhub_users;

alter table govio_planner_govio_files 
   add constraint GovioFileProducedEntity_GovioPlannerFile 
   foreign key (id_govio_planner_file) 
   references govio_planner_files;
