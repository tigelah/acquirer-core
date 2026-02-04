alter table payments
    add column if not exists installments int not null default 1;
