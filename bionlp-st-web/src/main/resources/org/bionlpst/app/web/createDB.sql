create table submission (
	id identity,
	creation_date timestamp not null,
	user_gid varchar(24) null,
	user_name varchar(512) null,
	email varchar(512) null,
	private boolean not null,
	task varchar(24) not null,
	data_set varchar(10) not null,
	description varchar(2048) null
);

create table measure (
	ref_submission bigint not null,
	evaluation varchar(255) not null,
	scoring varchar(255) not null,
	name varchar(255) not null,
	val double not null,
	higher boolean not null,
	foreign key (ref_submission) references submission(id)
);
