/* drop table renglon_entrega;
drop table entrega;
drop table video;
drop table nacional;
drop table internacional;
alter table empleado drop foreign key empleado_id_distribuidor_fkey;
drop table departamento;
drop table distribuidor;
drop table empleado;
drop table tarea;
drop table pelicula;
drop table empresa_productora;
drop table ciudad;
drop table pais;
*/

CREATE TABLE ciudad (
    id_ciudad numeric(6,0) NOT NULL,
    nombre_ciudad character varying(100),
    id_pais character(2),
    CONSTRAINT ciudad_id_ciudad_check CHECK ((id_ciudad IS NOT NULL)),
    CONSTRAINT ciudad_id_pais_check CHECK ((id_pais IS NOT NULL)),
    CONSTRAINT ciudad_nombre_ciudad_check CHECK ((nombre_ciudad IS NOT NULL))
);

CREATE TABLE departamento (
    id_departamento numeric(4,0) NOT NULL,
    id_distribuidor numeric(5,0) NOT NULL,
    nombre_departamento character varying(30),
    calle character varying(40),
    numero numeric(6,0),
    id_ciudad numeric(6,0),
    jefe_departamento numeric(6,0),
    CONSTRAINT departamento_id_ciudad_check CHECK ((id_ciudad IS NOT NULL)),
    CONSTRAINT departamento_id_departamento_check CHECK ((id_departamento IS NOT NULL)),
    CONSTRAINT departamento_id_distribuidor_check CHECK ((id_distribuidor IS NOT NULL)),
    CONSTRAINT departamento_jefe_departamento_check CHECK ((jefe_departamento IS NOT NULL)),
    CONSTRAINT departamento_nombre_departamento_check CHECK ((nombre_departamento IS NOT NULL))
);

CREATE TABLE distribuidor (
    id_distribuidor numeric(5,0) NOT NULL,
    nombre character varying(80),
    direccion character varying(120),
    telefono character varying(20),
    tipo character(1),
    CONSTRAINT distribuidor_direccion_check CHECK ((direccion IS NOT NULL)),
    CONSTRAINT distribuidor_id_distribuidor_check CHECK ((id_distribuidor IS NOT NULL)),
    CONSTRAINT distribuidor_nombre_check CHECK ((nombre IS NOT NULL)),
    CONSTRAINT distribuidor_tipo_check CHECK ((tipo IS NOT NULL))
);

CREATE TABLE empleado (
    id_empleado numeric(6,0) NOT NULL,
    nombre character varying(30),
    apellido character varying(30),
    porc_comision numeric(6,2),
    sueldo numeric(8,2),
    e_mail character varying(120),
    fecha_nacimiento date,
    telefono character varying(20),
    id_tarea character varying(10),
    id_departamento numeric(4,0),
    id_distribuidor numeric(5,0),
    id_jefe numeric(6,0),
    CONSTRAINT empleado_apellido_check CHECK ((apellido IS NOT NULL)),
    CONSTRAINT empleado_e_mail_check CHECK ((e_mail IS NOT NULL)),
    CONSTRAINT empleado_fecha_nacimiento_check CHECK ((fecha_nacimiento IS NOT NULL)),
    CONSTRAINT empleado_id_empleado_check CHECK ((id_empleado IS NOT NULL)),
    CONSTRAINT empleado_id_tarea_check CHECK ((id_tarea IS NOT NULL))
);


CREATE TABLE empresa_productora (
    codigo_productora character varying(6) NOT NULL,
    nombre_productora character varying(60),
    id_ciudad numeric(6,0),
    CONSTRAINT empresa_productora_codigo_productora_check CHECK ((codigo_productora IS NOT NULL)),
    CONSTRAINT empresa_productora_nombre_productora_check CHECK ((nombre_productora IS NOT NULL))
);


CREATE TABLE entrega (
    nro_entrega numeric(10,0) NOT NULL,
    fecha_entrega date,
    id_video numeric(5,0),
    id_distribuidor numeric(5,0),
    CONSTRAINT entrega_fecha_entrega_check CHECK ((fecha_entrega IS NOT NULL)),
    CONSTRAINT entrega_id_distribuidor_check CHECK ((id_distribuidor IS NOT NULL)),
    CONSTRAINT entrega_id_video_check CHECK ((id_video IS NOT NULL)),
    CONSTRAINT entrega_nro_entrega_check CHECK ((nro_entrega IS NOT NULL))
);

CREATE TABLE internacional (
    id_distribuidor numeric(5,0) NOT NULL,
    codigo_pais character varying(5),
    CONSTRAINT internacional_codigo_pais_check CHECK ((codigo_pais IS NOT NULL)),
    CONSTRAINT internacional_id_distribuidor_check CHECK ((id_distribuidor IS NOT NULL))
);

CREATE TABLE nacional (
    id_distribuidor numeric(5,0) NOT NULL,
    nro_inscripcion numeric(8,0),
    encargado character varying(60),
    id_distrib_mayorista numeric(5,0),
    CONSTRAINT nacional_encargado_check CHECK ((encargado IS NOT NULL)),
    CONSTRAINT nacional_id_distribuidor_check CHECK ((id_distribuidor IS NOT NULL)),
    CONSTRAINT nacional_nro_inscripcion_check CHECK ((nro_inscripcion IS NOT NULL))
);

CREATE TABLE pais (
    id_pais character(2) NOT NULL,
    nombre_pais character varying(40),
    CONSTRAINT pais_id_pais_check CHECK ((id_pais IS NOT NULL))
);

CREATE TABLE pelicula (
    codigo_pelicula numeric(5,0) NOT NULL,
    titulo character varying(60),
    idioma character varying(20),
    formato character varying(20),
    genero character varying(30),
    codigo_productora character varying(6),
    CONSTRAINT pelicula_codigo_pelicula_check CHECK ((codigo_pelicula IS NOT NULL)),
    CONSTRAINT pelicula_codigo_productora_check CHECK ((codigo_productora IS NOT NULL)),
    CONSTRAINT pelicula_formato_check CHECK ((formato IS NOT NULL)),
    CONSTRAINT pelicula_genero_check CHECK ((genero IS NOT NULL)),
    CONSTRAINT pelicula_idioma_check CHECK ((idioma IS NOT NULL)),
    CONSTRAINT pelicula_titulo_check CHECK ((titulo IS NOT NULL))
);


CREATE TABLE renglon_entrega (
    nro_entrega numeric(10,0) NOT NULL,
    codigo_pelicula numeric(5,0) NOT NULL,
    cantidad numeric(5,0),
    CONSTRAINT renglon_entrega_cantidad_check CHECK ((cantidad IS NOT NULL)),
    CONSTRAINT renglon_entrega_codigo_pelicula_check CHECK ((codigo_pelicula IS NOT NULL)),
    CONSTRAINT renglon_entrega_nro_entrega_check CHECK ((nro_entrega IS NOT NULL))
);


CREATE TABLE tarea (
    id_tarea character varying(10) NOT NULL,
    nombre_tarea character varying(35),
    sueldo_maximo numeric(6,0),
    sueldo_minimo numeric(6,0),
    CONSTRAINT tarea_id_tarea_check CHECK ((id_tarea IS NOT NULL)),
    CONSTRAINT tarea_nombre_tarea_check CHECK ((nombre_tarea IS NOT NULL)),
    CONSTRAINT tarea_sueldo_maximo_check CHECK ((sueldo_maximo IS NOT NULL)),
    CONSTRAINT tarea_sueldo_minimo_check CHECK ((sueldo_minimo IS NOT NULL))
);


CREATE TABLE video (
    id_video numeric(5,0) NOT NULL,
    razon_social character varying(60),
    direccion character varying(80),
    telefono character varying(15),
    propietario character varying(60),
    CONSTRAINT video_direccion_check CHECK ((direccion IS NOT NULL)),
    CONSTRAINT video_id_video_check CHECK ((id_video IS NOT NULL)),
    CONSTRAINT video_propietario_check CHECK ((propietario IS NOT NULL)),
    CONSTRAINT video_razon_social_check CHECK ((razon_social IS NOT NULL))
);


ALTER TABLE ciudad
    ADD CONSTRAINT pk_ciudad PRIMARY KEY (id_ciudad);

ALTER TABLE departamento
    ADD CONSTRAINT pk_departamento PRIMARY KEY (id_distribuidor, id_departamento);

ALTER TABLE distribuidor
    ADD CONSTRAINT pk_distribuidor PRIMARY KEY (id_distribuidor);

ALTER TABLE empleado
    ADD CONSTRAINT pk_empleado PRIMARY KEY (id_empleado);

ALTER TABLE empresa_productora
    ADD CONSTRAINT pk_empresa_productora PRIMARY KEY (codigo_productora);

ALTER TABLE entrega
    ADD CONSTRAINT pk_entrega PRIMARY KEY (nro_entrega);

ALTER TABLE internacional
    ADD CONSTRAINT pk_internacional PRIMARY KEY (id_distribuidor);

ALTER TABLE nacional
    ADD CONSTRAINT pk_nacional PRIMARY KEY (id_distribuidor);

ALTER TABLE pais
    ADD CONSTRAINT pk_pais PRIMARY KEY (id_pais);

ALTER TABLE pelicula
    ADD CONSTRAINT pk_pelicula PRIMARY KEY (codigo_pelicula);

ALTER TABLE renglon_entrega
    ADD CONSTRAINT pk_renglon_entrega PRIMARY KEY (nro_entrega, codigo_pelicula);

ALTER TABLE tarea
    ADD CONSTRAINT pk_tarea PRIMARY KEY (id_tarea);

ALTER TABLE video
    ADD CONSTRAINT pk_video PRIMARY KEY (id_video);

ALTER TABLE ciudad
    ADD CONSTRAINT ciudad_id_pais_fkey FOREIGN KEY (id_pais) REFERENCES pais(id_pais);

ALTER TABLE departamento
    ADD CONSTRAINT departamento_id_ciudad_fkey FOREIGN KEY (id_ciudad) REFERENCES ciudad(id_ciudad);
ALTER TABLE departamento
    ADD CONSTRAINT departamento_id_distribuidor_fkey FOREIGN KEY (id_distribuidor) REFERENCES distribuidor(id_distribuidor);
ALTER TABLE departamento
    ADD CONSTRAINT departamento_jefe_departamento_fkey FOREIGN KEY (jefe_departamento) REFERENCES empleado(id_empleado);

ALTER TABLE empleado
    ADD CONSTRAINT empleado_id_distribuidor_fkey FOREIGN KEY (id_distribuidor, id_departamento) REFERENCES departamento(id_distribuidor, id_departamento);
ALTER TABLE empleado
    ADD CONSTRAINT empleado_id_jefe_fkey FOREIGN KEY (id_jefe) REFERENCES empleado(id_empleado);
ALTER TABLE empleado
    ADD CONSTRAINT empleado_id_tarea_fkey FOREIGN KEY (id_tarea) REFERENCES tarea(id_tarea);

ALTER TABLE empresa_productora
    ADD CONSTRAINT empresa_productora_id_ciudad_fkey FOREIGN KEY (id_ciudad) REFERENCES ciudad(id_ciudad);

ALTER TABLE entrega
    ADD CONSTRAINT fk_entrega_distribuidor FOREIGN KEY (id_distribuidor) REFERENCES distribuidor(id_distribuidor);
ALTER TABLE entrega
    ADD CONSTRAINT fk_entrega_video FOREIGN KEY (id_video) REFERENCES video(id_video);

ALTER TABLE nacional
    ADD CONSTRAINT fk_nacional_distribuidor FOREIGN KEY (id_distribuidor) REFERENCES distribuidor(id_distribuidor);

ALTER TABLE nacional
    ADD CONSTRAINT fk_nacional_mayorista FOREIGN KEY (id_distrib_mayorista) REFERENCES internacional(id_distribuidor);

ALTER TABLE renglon_entrega
    ADD CONSTRAINT fk_re_entrega FOREIGN KEY (nro_entrega) REFERENCES entrega(nro_entrega);
ALTER TABLE renglon_entrega
    ADD CONSTRAINT fk_re_pelicula FOREIGN KEY (codigo_pelicula) REFERENCES pelicula(codigo_pelicula);

ALTER TABLE internacional
    ADD CONSTRAINT internacional_id_distribuidor_fkey FOREIGN KEY (id_distribuidor) REFERENCES distribuidor(id_distribuidor) ON DELETE CASCADE;

ALTER TABLE pelicula
    ADD CONSTRAINT pelicula_codigo_productora_fkey FOREIGN KEY (codigo_productora) REFERENCES empresa_productora(codigo_productora);

/* Fill esquema peliculas */
INSERT INTO pais (id_pais, nombre_pais) VALUES ('ES', 'España');
INSERT INTO pais (id_pais, nombre_pais) VALUES ('FR', 'Francia');
INSERT INTO pais (id_pais, nombre_pais) VALUES ('GB', 'Reino Unido');
INSERT INTO pais (id_pais, nombre_pais) VALUES ('US', 'Estados Unidos');
INSERT INTO pais (id_pais, nombre_pais) VALUES ('JP', 'Japón');
INSERT INTO pais (id_pais, nombre_pais) VALUES ('IT', 'Italia');
INSERT INTO pais (id_pais, nombre_pais) VALUES ('DE', 'Alemania');
INSERT INTO pais (id_pais, nombre_pais) VALUES ('NL', 'Países Bajos'); 

INSERT INTO ciudad(id_ciudad, nombre_ciudad, id_pais) 
VALUES (1, 'Barcelona', 'ES'),
       (2, 'Madrid', 'ES'),
       (3, 'Lisboa', 'ES'),
       (4, 'París', 'FR'),
       (5, 'Roma', 'IT'),
       (6, 'Berlín', 'DE'),
       (7, 'Londres', 'GB');
 
INSERT INTO distribuidor(id_distribuidor, nombre, direccion, telefono, tipo) 
VALUES (1, 'Distribuciones García', 'Calle Valencia 20', '123456789', 'N'),
       (2, 'Distribuciones López', 'Calle Goya 15', '987654321', 'I'),
       (3, 'Distribuciones Pérez', 'Calle Alcalá 30', '654987321', 'N'),
       (4, 'Distribuciones Sánchez', 'Calle Diagonal 100', '789456123', 'I'),
       (5, 'Distribuciones Martínez', 'Calle Gran Vía 55', '456123789', 'N'),
       (6, 'Distribuciones Ruiz', 'Calle Castellana 80', '321654987', 'I'),
       (7, 'Distribuciones Hernández', 'Calle Princesa 10', '654123987', 'N');
    
alter table departamento drop foreign key departamento_jefe_departamento_fkey;    
INSERT INTO departamento (id_departamento, id_distribuidor, nombre_departamento, calle, numero, id_ciudad, jefe_departamento) VALUES
(1, 1, 'Ventas', 'Av. Siempreviva', 123, 1, 2),
(2, 2, 'Contabilidad', 'Calle Falsa', 321, 1, 2),
(1, 3, 'Ventas', 'Av. del Sol', 456, 2, 2),
(4, 4, 'Recursos Humanos', 'Calle Falsa', 654, 2, 2),
(1, 5, 'Ventas', 'Av. Principal', 789, 3, 7),
(6, 6, 'Marketing', 'Calle del Bosque', 987, 3, 7),
(1, 7, 'Ventas', 'Av. Central', 321, 4, 3);

INSERT INTO internacional (id_distribuidor, codigo_pais) VALUES
  (2, 'FR'),
  (4, 'GB'),
  (6, 'DE');

INSERT INTO nacional (id_distribuidor, nro_inscripcion, encargado, id_distrib_mayorista) VALUES
  (1, 12345678, 'Juan García', 2),
  (3, 24681357, 'Pedro Pérez', 2),
  (5, 36925814, 'Carlos Martínez', 4),
  (7, 46927817, 'Carlos Hernández', 6);

INSERT INTO empresa_productora (codigo_productora, nombre_productora, id_ciudad)
VALUES
('ABC123', 'Productora A', 1),
('DEF456', 'Productora B', 2),
('GHI789', 'Productora C', 3),
('JKL012', 'Productora D', 5),
('MNO345', 'Productora E', 4),
('PQR678', 'Productora F', 6),
('STU901', 'Productora G', 7);

INSERT INTO pelicula (codigo_pelicula, titulo, idioma, formato, genero, codigo_productora) VALUES
(10001, 'The Godfather', 'English', 'Blu-ray', 'Crime', 'ABC123'),
(10002, 'The Shawshank Redemption', 'English', 'DVD', 'Drama', 'DEF456'),
(10003, 'The Dark Knight', 'English', 'Digital', 'Action', 'GHI789'),
(10004, 'Pulp Fiction', 'English', 'Blu-ray', 'Crime', 'JKL012'),
(10005, 'Forrest Gump', 'English', 'DVD', 'Drama', 'MNO345'),
(10006, 'The Matrix', 'English', 'Digital', 'Sci-Fi', 'PQR678'),
(10007, 'Inception', 'English', 'Blu-ray', 'Sci-Fi', 'STU901'),
(10008, 'El tiempo entre costuras', 'Spanish', 'Blu-ray', 'Romance', 'STU901');

INSERT INTO entrega (nro_entrega, fecha_entrega, id_video, id_distribuidor) VALUES
(1, '2022-01-15', 1, 5),
(2, '2022-02-20', 2, 3),
(3, '2022-03-05', 3, 4),
(4, '2022-04-10', 4, 2),
(5, '2022-05-15', 5, 1),
(6, '2022-06-20', 6, 3),
(7, '2022-07-25', 7, 5);

INSERT INTO renglon_entrega (nro_entrega, codigo_pelicula, cantidad) VALUES
(1, 10001, 5),
(2, 10002, 3),
(3, 10003, 4),
(4, 10004, 2),
(5, 10005, 6),
(6, 10006, 1),
(6, 10005, 2),
(7, 10007, 3),
(7, 10008, 6),
(7, 10002, 1),
(7, 10005, 3);

ALTER TABLE departamento 
	ADD CONSTRAINT departamento_jefe_departamento_fkey FOREIGN KEY (jefe_departamento) REFERENCES empleado(id_empleado);

