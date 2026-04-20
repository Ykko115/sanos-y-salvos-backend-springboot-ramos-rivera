CREATE TABLE IF NOT EXISTS public.usuario (
    id BIGSERIAL PRIMARY KEY,
    rut VARCHAR(255),
    nombre VARCHAR(255),
    apellido VARCHAR(255),
    email VARCHAR(255),
    telefono INTEGER,
    password VARCHAR(255),
    activo BOOLEAN,
    rol VARCHAR(20)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_usuario_email ON public.usuario (email);
CREATE UNIQUE INDEX IF NOT EXISTS uk_usuario_rut ON public.usuario (rut);
