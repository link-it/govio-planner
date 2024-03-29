create sequence seq_govio_planner_exp_files start 1 increment 1;
create sequence seq_govio_planner_ntfy_files start 1 increment 1;

create table govio_planner_exp_files (
    id int8 not null,
	creation_date timestamp not null,
	location varchar(2048) not null,
	name varchar(255) not null,
	plan_id varchar(512) not null,
	processing_date timestamp,
	size int8 not null,
	status varchar(255) not null,
	id_govauth_user int8 not null,
	primary key (id)
);

create table govio_planner_ntfy_files (
   id int8 not null,
	creation_date timestamp not null,
	location varchar(255) not null,
	message_count BIGINT not null,
	name varchar(255) not null,
	size int8 not null,
	status varchar(255) not null,
	id_govio_planner_file int8 not null,
	primary key (id)
);

alter table govio_planner_ntfy_files 
   add constraint UK_lrb0pnqq64pk8jd0pd0ji6d6q unique (name);


alter table govio_planner_exp_files 
   add constraint GovioFile_GovhubUser 
   foreign key (id_govauth_user) 
   references govhub_users;

alter table govio_planner_ntfy_files 
   add constraint GovioFileProducedEntity_GovioPlannerFile 
   foreign key (id_govio_planner_file) 
   references govio_planner_exp_files;
