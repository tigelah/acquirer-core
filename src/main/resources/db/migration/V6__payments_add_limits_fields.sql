alter table payments
    add column if not exists account_id uuid;

alter table payments
    add column if not exists user_id varchar(128);

alter table payments
    add column if not exists pan_hash varchar(128);

-- se quiser obrigar no banco (recomendado quando já estiver sempre presente):
-- alter table payments alter column account_id set not null;
-- alter table payments alter column pan_hash set not null;

create index if not exists idx_payments_account_id on payments(account_id);
create index if not exists idx_payments_user_id on payments(user_id);
create index if not exists idx_payments_pan_hash on payments(pan_hash);