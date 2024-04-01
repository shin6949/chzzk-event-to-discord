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

ALTER TABLE public.discord_webhook_data ALTER COLUMN webhook_url TYPE VARCHAR(30000);
ALTER TABLE public.chzzk_catgory ALTER COLUMN updated_at DROP DEFAULT;
ALTER TABLE public.chzzk_channel ALTER COLUMN profile_url TYPE VARCHAR(30000);
ALTER TABLE public.discord_bot_profile_data ALTER COLUMN avatar_url TYPE VARCHAR(30000);
ALTER TABLE public.chzzk_subscription_form ALTER COLUMN color_hex SET DEFAULT '000000';