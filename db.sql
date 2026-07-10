-- public.clientes definition

-- Drop table

-- DROP TABLE public.clientes;

CREATE TABLE public.clientes (
	id_cliente serial4 NOT NULL,
	ruc varchar(20) NULL,
	ci varchar(20) NULL,
	nombre varchar(100) NOT NULL,
	apellido varchar(100) NOT NULL,
	fecha_registro timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT clientes_pkey PRIMARY KEY (id_cliente)
);


-- public.configuracion definition

-- Drop table

-- DROP TABLE public.configuracion;

CREATE TABLE public.configuracion (
	id_configuracion serial4 NOT NULL,
	ruc_empresa varchar(20) NOT NULL,
	razon_social_empresa varchar(100) NULL,
	timbrado varchar(20) NOT NULL,
	fecha_inicio_timbrado date NOT NULL,
	fecha_fin_timbrado date NOT NULL,
	codigo_establecimiento varchar(3) NOT NULL,
	codigo_punto_expedicion varchar(3) NOT NULL,
	numero_secuencial_actual int4 DEFAULT 1 NOT NULL,
	activo bool DEFAULT true NULL,
	CONSTRAINT configuracion_pkey PRIMARY KEY (id_configuracion)
);


-- public.cliente_contactos definition

-- Drop table

-- DROP TABLE public.cliente_contactos;

CREATE TABLE public.cliente_contactos (
	id_contacto serial4 NOT NULL,
	id_cliente int4 NOT NULL,
	nombre varchar(100) NOT NULL,
	cargo varchar(50) NULL,
	telefono varchar(20) NULL,
	email varchar(100) NULL,
	es_principal bool DEFAULT false NULL,
	CONSTRAINT cliente_contactos_pkey PRIMARY KEY (id_cliente, id_contacto),
	CONSTRAINT fk_contacto_cliente FOREIGN KEY (id_cliente) REFERENCES public.clientes(id_cliente) ON DELETE CASCADE
);


-- public.cliente_direcciones definition

-- Drop table

-- DROP TABLE public.cliente_direcciones;

CREATE TABLE public.cliente_direcciones (
	id_direccion serial4 NOT NULL,
	id_cliente int4 NOT NULL,
	tipo varchar(20) NULL,
	calle_principal varchar(100) NULL,
	calle_secundaria varchar(100) NULL,
	numero_casa varchar(10) NULL,
	barrio varchar(50) NULL,
	ciudad varchar(50) NULL,
	latitud numeric(10, 8) NULL,
	longitud numeric(11, 8) NULL,
	referencia text NULL,
	CONSTRAINT cliente_direcciones_pkey PRIMARY KEY (id_cliente, id_direccion),
	CONSTRAINT fk_direccion_cliente FOREIGN KEY (id_cliente) REFERENCES public.clientes(id_cliente) ON DELETE CASCADE
);


-- public.ventas definition

-- Drop table

-- DROP TABLE public.ventas;

CREATE TABLE public.ventas (
	id_venta serial4 NOT NULL,
	id_cliente int4 NOT NULL,
	fecha_emision timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	nro_timbrado varchar(20) NULL,
	numero_factura varchar(15) NULL,
	condicion_venta bool DEFAULT false NULL,
	subtotal_iva5 numeric(12, 2) DEFAULT 0 NULL,
	subtotal_iva10 numeric(12, 2) DEFAULT 0 NULL,
	subtotal_exenta numeric(12, 2) DEFAULT 0 NULL,
	monto_total numeric(12, 2) NOT NULL,
	estado varchar(20) NULL,
	CONSTRAINT ventas_pkey PRIMARY KEY (id_venta),
	CONSTRAINT fk_venta_cliente FOREIGN KEY (id_cliente) REFERENCES public.clientes(id_cliente) ON DELETE RESTRICT
);


-- public.ventas_detalle definition

-- Drop table

-- DROP TABLE public.ventas_detalle;

CREATE TABLE public.ventas_detalle (
	id_detalle serial4 NOT NULL,
	id_venta int4 NOT NULL,
	id_producto int4 NOT NULL,
	cantidad int4 NOT NULL,
	precio_unitario numeric(12, 2) NOT NULL,
	iva_5 numeric(12, 2) DEFAULT 0 NULL,
	iva_10 numeric(12, 2) DEFAULT 0 NULL,
	exenta numeric(12, 2) DEFAULT 0 NULL,
	subtotal numeric(12, 2) NOT NULL,
	CONSTRAINT ventas_detalle_pkey PRIMARY KEY (id_venta, id_detalle),
	CONSTRAINT fk_detalle_venta FOREIGN KEY (id_venta) REFERENCES public.ventas(id_venta) ON DELETE CASCADE
);

-- public.productos definition

CREATE TABLE public.productos (
	id_producto serial4 NOT NULL,
	codigo_interno varchar(50) NULL,
	nombre varchar(150) NOT NULL,
	descripcion text NULL,
	precio_unitario numeric(12, 2) NOT NULL,
	tipo_impuesto varchar(10) DEFAULT 'IVA_10' NOT NULL,
	activo bool DEFAULT true NULL,
	CONSTRAINT productos_pkey PRIMARY KEY (id_producto)
);

-- Add id_reserva to public.ventas
ALTER TABLE public.ventas ADD COLUMN id_reserva int4 NULL;

-- Insert default products for testing
INSERT INTO public.productos (codigo_interno, nombre, descripcion, precio_unitario, tipo_impuesto, activo)
VALUES 
('INF-006', 'Teclado Satellite AK-910 USB / Negro', 'Teclado USB con cable, color negro', 45760.00, 'IVA_10', true),
('CEL-001', 'Celular Apple iPhone 17 256GB', 'Smartphone Apple, 256GB de almacenamiento', 8500000.00, 'IVA_10', true),
('MED-001', 'Paracetamol 500mg', 'Analgésico y antipirético', 5000.00, 'EXENTA', true);

-- Insert configuration if none exists
INSERT INTO public.configuracion (ruc_empresa, razon_social_empresa, timbrado, fecha_inicio_timbrado, fecha_fin_timbrado, codigo_establecimiento, codigo_punto_expedicion, numero_secuencial_actual, activo)
VALUES ('80012345-6', 'Empresa Ventas S.A.', '12345678', '2026-01-01', '2026-12-31', '001', '001', 1, true);