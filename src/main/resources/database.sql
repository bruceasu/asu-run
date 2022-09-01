
create table if not exists fileInfo
(
   name varchar(1024) not null comment 'name',
   path varchar(1024) not null comment 'path',
   file_type varchar (64) not null comment 'file type',
   file_ext varchar (64) not null comment 'file extend',
   name_lower varchar(1024) as lower(name)
);
create index if not exists index_name on fileInfo(name);

-- alter table fileInfo add (name_lower varchar(100) as lower(name));
create index ix_element_add_low on fileInfo (name_lower);

-- select * from fileInfo e where locate (lower(?), lower(e.name));
-- select * from fileInfo e where locate (lower(?), name_lower);