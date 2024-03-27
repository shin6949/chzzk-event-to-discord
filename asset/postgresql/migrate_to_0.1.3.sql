DROP TABLE IF EXISTS public.chzzk_subscription_stream_online_form;

CREATE TABLE public.chzzk_subscription_stream_online_form (
    id  bigserial   PRIMARY KEY,
    show_detail  BOOLEAN  NOT NULL DEFAULT false,
    show_thumbnail BOOLEAN  NOT NULL DEFAULT true,
    FOREIGN KEY (id) REFERENCES public.chzzk_subscription_form (id) ON DELETE CASCADE
);

INSERT INTO public.chzzk_subscription_stream_online_form (id, show_detail)
SELECT id, show_detail FROM public.chzzk_subscription_form WHERE type LIKE 'STREAM_ONLINE';

ALTER TABLE public.chzzk_subscription_form DROP COLUMN show_detail;
