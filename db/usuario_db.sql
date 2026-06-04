--
-- PostgreSQL database dump
--

\restrict wk0Wb8zbgoZqBV3tm6M3dDMlWqgyaap9OhMJJqYAXVdxKzNL1ECc9DZnYkLHpBd

-- Dumped from database version 18.4
-- Dumped by pg_dump version 18.4

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: usuario; Type: TABLE; Schema: public; Owner: user_adm
--

CREATE TABLE public.usuario (
    id bigint NOT NULL,
    rut character varying(255),
    nombre character varying(255),
    apellido character varying(255),
    email character varying(255),
    telefono integer,
    password character varying(255),
    activo boolean,
    rol character varying(255)
);


ALTER TABLE public.usuario OWNER TO user_adm;

--
-- Name: usuario_id_seq; Type: SEQUENCE; Schema: public; Owner: user_adm
--

CREATE SEQUENCE public.usuario_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.usuario_id_seq OWNER TO user_adm;

--
-- Name: usuario_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: user_adm
--

ALTER SEQUENCE public.usuario_id_seq OWNED BY public.usuario.id;


--
-- Name: usuario id; Type: DEFAULT; Schema: public; Owner: user_adm
--

ALTER TABLE ONLY public.usuario ALTER COLUMN id SET DEFAULT nextval('public.usuario_id_seq'::regclass);


--
-- Data for Name: usuario; Type: TABLE DATA; Schema: public; Owner: user_adm
--

COPY public.usuario (id, rut, nombre, apellido, email, telefono, password, activo, rol) FROM stdin;
1	\N	ADMIN	\N	admin@sanosysalvos.com	\N	Admin12.	t	ADMIN
2	21.714.477-9	Nicolas	Ramos	nicoramosdiaz2111@gmail.com	926003773	$2a$10$PRhwcrAj4enneu1w6.naQu57Brni5CU1MeX0zUkIOUUfb8yDH9W6K	t	USER
\.


--
-- Name: usuario_id_seq; Type: SEQUENCE SET; Schema: public; Owner: user_adm
--

SELECT pg_catalog.setval('public.usuario_id_seq', 2, true);


--
-- Name: usuario usuario_pkey; Type: CONSTRAINT; Schema: public; Owner: user_adm
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_pkey PRIMARY KEY (id);


--
-- Name: uk_usuario_email; Type: INDEX; Schema: public; Owner: user_adm
--

CREATE UNIQUE INDEX uk_usuario_email ON public.usuario USING btree (email);


--
-- Name: uk_usuario_rut; Type: INDEX; Schema: public; Owner: user_adm
--

CREATE UNIQUE INDEX uk_usuario_rut ON public.usuario USING btree (rut);


--
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: pg_database_owner
--

GRANT ALL ON SCHEMA public TO user_adm;


--
-- PostgreSQL database dump complete
--

\unrestrict wk0Wb8zbgoZqBV3tm6M3dDMlWqgyaap9OhMJJqYAXVdxKzNL1ECc9DZnYkLHpBd

