-- =============================================================================
-- Warranty Demo: Schema + Seed Data for Remote PostgreSQL (RDS)
-- =============================================================================
-- Single flat file: DDL (CREATE TABLE) + DML (INSERT) for all tables and demo data.
-- Idempotent: DROP TABLE IF EXISTS before create — safe to re-run.
--
-- USAGE (replace host, user, db as needed):
--   psql -h database-1.xxx.rds.amazonaws.com -U postgres -d postgres -f rds_seed.sql
--
-- Or with password prompt:
--   PGPASSWORD=xxx psql -h host -U postgres -d postgres -f warranty_demo/rds_seed.sql
--
-- TABLES: HSAHKLF3 (invoices), HSG71PF (claims), HSG73PF (claim errors), + 8 more
-- SEED: 4 invoices (12345, 99999, 88888, 77777), 1 claim, 1 claim error
-- =============================================================================

-- Drop existing tables (idempotent - safe to re-run)
DROP TABLE IF EXISTS itlsmf3 CASCADE;
DROP TABLE IF EXISTS hsgpspf CASCADE;
DROP TABLE IF EXISTS hsgpslf3 CASCADE;
DROP TABLE IF EXISTS hsg73pf CASCADE;
DROP TABLE IF EXISTS hsg71pf CASCADE;
DROP TABLE IF EXISTS hsg70f CASCADE;
DROP TABLE IF EXISTS hsflalf1 CASCADE;
DROP TABLE IF EXISTS hsepaf CASCADE;
DROP TABLE IF EXISTS hsahwpf CASCADE;
DROP TABLE IF EXISTS hsahkpf CASCADE;
DROP TABLE IF EXISTS hsahklf3 CASCADE;

-- ============ DDL ============
create table hsahklf3 (
        "an-zeit" varchar(4) not null,
        anrede varchar(1) not null,
        "ba-schlüssel" varchar(2) not null,
        berei varchar(1) not null,
        bfort varchar(1) not null,
        bj varchar(4) not null,
        "bonit#t" varchar(1) not null,
        branch varchar(3) not null,
        "eg-umsatz" varchar(1) not null,
        "f:b>0" integer not null,
        "f:v>0" integer not null,
        "fert-zeit" varchar(4) not null,
        "kampagne-nr" integer not null,
        "ken-av" varchar(2) not null,
        "ken-pe" varchar(2) not null,
        "kl-ausnahme idnr" varchar(3) not null,
        "kl-erweiterung" integer not null,
        "klr-berech" varchar(1) not null,
        "klr-betrag" numeric(5,2) not null,
        "kostencode konzint_" varchar(3) not null,
        "kz s" varchar(1) not null,
        land varchar(3) not null,
        leitzahl varchar(3) not null,
        "mwst %" numeric(5,2) not null,
        "mwst % r_" numeric(5,2) not null,
        "mwst y/n" varchar(1) not null,
        pakz varchar(3) not null,
        partikelfilter varchar(1) not null,
        rc varchar(3) not null,
        "re anrede" varchar(1) not null,
        "re bonit#t" varchar(1) not null,
        "re land" varchar(3) not null,
        "re rc" varchar(3) not null,
        "re valuta" varchar(1) not null,
        "re zart" varchar(1) not null,
        reserve1 numeric(5,2) not null,
        reserve2 numeric(9,2) not null,
        reserve3 integer not null,
        reserve4 integer not null,
        "rgs basis at" numeric(9,2) not null,
        "rgs basis mwst" numeric(9,2) not null,
        "rgs ges brutto" numeric(9,2) not null,
        "rgs mwst" numeric(9,2) not null,
        "rgs mwst at" numeric(9,2) not null,
        "rgs netto" numeric(9,2) not null,
        splitt varchar(2) not null,
        "steuerfrei drittland" varchar(1) not null,
        "tx_anf" varchar(3) not null,
        "tx_ende" varchar(3) not null,
        valuta varchar(1) not null,
        "verbucht?" varchar(1) not null,
        "w/t" varchar(1) not null,
        "wkt-id" integer not null,
        "wrg_" varchar(3) not null,
        zahlungsart varchar(1) not null,
        anr varchar(5) not null,
        "is-art" varchar(5) not null,
        "kor-bez-re" varchar(5) not null,
        "kst int auf_" varchar(5) not null,
        "kst lohn" varchar(5) not null,
        "kst teile" varchar(5) not null,
        "l_rnr" varchar(5) not null,
        match varchar(5) not null,
        plz varchar(5) not null,
        "re match" varchar(5) not null,
        "re plz" varchar(5) not null,
        rnr varchar(5) not null,
        "sto-bez-re" varchar(5) not null,
        au varchar(6) not null,
        "fibu interim" varchar(6) not null,
        "fibu mwst" varchar(6) not null,
        "fibu mwst at" varchar(6) not null,
        hu varchar(6) not null,
        "kto intauf_" varchar(6) not null,
        "kunden-nr_" varchar(6) not null,
        "re kunden-nr_" varchar(6) not null,
        sp varchar(6) not null,
        "ktr int auf_" varchar(7) not null,
        adat varchar(8) not null,
        "an-tag" varchar(8) not null,
        "fert-tag" varchar(8) not null,
        ga varchar(8) not null,
        "ga-übern_" varchar(8) not null,
        id bigserial not null,
        km varchar(8) not null,
        "kor-bez-redat" varchar(8) not null,
        rdat varchar(8) not null,
        "sto-bez-redat" varchar(8) not null,
        tacho varchar(8) not null,
        "zaga-gueltig" varchar(8) not null,
        zdat varchar(8) not null,
        "euro-norm" varchar(10) not null,
        "motor-nr" varchar(10) not null,
        "prod-code" varchar(10) not null,
        projekt varchar(10) not null,
        "rg-nr_ 10a" varchar(10) not null,
        "spez-code" varchar(10) not null,
        "spo order" varchar(10) not null,
        "user auftrag" varchar(10) not null,
        "user rechnung" varchar(10) not null,
        kz varchar(12) not null,
        "assi-vorgang-nr" varchar(15) not null,
        "r&w freigabe-nr" varchar(15) not null,
        typ varchar(15) not null,
        "fahrg_-nr_" varchar(17) not null,
        "re tele_" varchar(17) not null,
        telefon varchar(17) not null,
        aufbauart varchar(20) not null,
        berater varchar(20) not null,
        "besteller kunde" varchar(20) not null,
        dokumentennummer varchar(20) not null,
        einsatzart varchar(20) not null,
        "fahrzeug-art" varchar(20) not null,
        hersteller varchar(20) not null,
        "hersteller aufbau" varchar(20) not null,
        "hersteller zusatz 1" varchar(20) not null,
        "hersteller zusatz 2" varchar(20) not null,
        "hersteller zusatz 3" varchar(20) not null,
        "motor-typ" varchar(20) not null,
        ort varchar(20) not null,
        "re ort" varchar(20) not null,
        "ust-id-nr/ok" varchar(20) not null,
        "zusatzausrüstung 1" varchar(20) not null,
        "zusatzausrüstung 2" varchar(20) not null,
        "zusatzausrüstung 3" varchar(20) not null,
        branche varchar(25) not null,
        "re branche" varchar(25) not null,
        "re strasse" varchar(25) not null,
        strasse varchar(25) not null,
        name varchar(30) not null,
        "re name" varchar(30) not null,
        atext varchar(40) not null,
        "kl-ausnahme klartext" varchar(40) not null,
        "mail cc" varchar(200) not null,
        "mail to" varchar(200) not null,
        primary key (id)
    );

    create table hsahkpf (
        ahk000 varchar(255) not null,
        ahk010 varchar(255) not null,
        ahk020 varchar(255) not null,
        ahk030 varchar(255) not null,
        ahk040 varchar(255) not null,
        ahk050 varchar(255) not null,
        ahk060 varchar(255) not null,
        ahk070 varchar(255) not null,
        ahk080 varchar(255),
        primary key (ahk000, ahk010, ahk020, ahk030, ahk040, ahk050, ahk060, ahk070)
    );

    create table hsahwpf (
        "aw-stunden" numeric(5,2) not null,
        bc varchar(2) not null,
        berei varchar(1) not null,
        ec varchar(2) not null,
        einstandspreis numeric(9,2) not null,
        "eps minderung %" numeric(5,2) not null,
        "gew-ze" varchar(1) not null,
        "ken_re2sum" varchar(1) not null,
        "kz s" varchar(1) not null,
        "kz s/aw" varchar(1) not null,
        "kz-mwst" varchar(1) not null,
        "l_nr_" varchar(3) not null,
        "lnr pak" integer not null,
        "lnr rz" integer not null,
        "m-stunden" numeric(5,2) not null,
        monteur varchar(3) not null,
        pakz varchar(3) not null,
        pe integer not null,
        "pos_" integer not null,
        preis numeric(9,2) not null,
        rabatt numeric(5,2) not null,
        "rg brutto" numeric(9,2) not null,
        "rg netto" numeric(9,2) not null,
        "rg rabatt" numeric(9,2) not null,
        "satz-pe" numeric(5,2) not null,
        "sort rz" integer not null,
        splitt varchar(2) not null,
        "txt-key" varchar(3) not null,
        "urspr-fak/h mon" numeric(5,2) not null,
        "urspr-netto mon" numeric(9,2) not null,
        "v-dm brutto" numeric(9,2) not null,
        "v-dm-netto" numeric(9,2) not null,
        "v-satz" numeric(5,2) not null,
        "v-stunden" numeric(9,2) not null,
        verdichten varchar(1) not null,
        "w/t" varchar(1) not null,
        werkszeit numeric(5,2) not null,
        ze integer not null,
        zuschlag numeric(5,2) not null,
        anr varchar(5) not null,
        rnr varchar(5) not null,
        ag varchar(8) not null,
        id bigserial not null,
        "paket-nr_" varchar(8) not null,
        rdat varchar(8) not null,
        "rg-nr_ 10a" varchar(10) not null,
        "eps name" varchar(20) not null,
        "bez_" varchar(40) not null,
        variante varchar(500) not null,
        arbeitsbeschreibung varchar(2000) not null,
        rechnungstext varchar(2000) not null,
        primary key (id)
    );

    create table hsepaf (
        epa_datv varchar(255),
        epa_key1 varchar(255) not null,
        epa_key2 varchar(255) not null,
        epa_key3 varchar(255) not null,
        epa_key4 varchar(255) not null,
        epa_key5 varchar(255) not null,
        primary key (epa_key1, epa_key2, epa_key3, epa_key4, epa_key5)
    );

    create table hsflalf1 (
        berei varchar(1) not null,
        "ek-preis" numeric(7,2) not null,
        "ek-wert" numeric(9,2) not null,
        "erl-grp" varchar(2) not null,
        lnr integer not null,
        "lnr-fl" integer not null,
        menge integer not null,
        pkz varchar(3) not null,
        "pos_" integer not null,
        spl varchar(2) not null,
        status varchar(1) not null,
        textzeilen integer not null,
        "vk-wert" numeric(9,2) not null,
        wt varchar(1) not null,
        "zus %" varchar(3) not null,
        aufnr varchar(5) not null,
        "bes-nr" varchar(5) not null,
        recnr varchar(5) not null,
        aufdat varchar(8) not null,
        "bes-dat" varchar(8) not null,
        "ek-redat" varchar(8) not null,
        id bigserial not null,
        "ken-fl" varchar(8) not null,
        recdat varchar(8) not null,
        "ek-renr" varchar(10) not null,
        beschreibung varchar(40) not null,
        "sdps fla uuid" varchar(40) not null,
        "sdps job uuid" varchar(40) not null,
        "ek bemerkungen 1" varchar(60) not null,
        "ek bemerkungen 2" varchar(60) not null,
        primary key (id)
    );

    create table hsg70f (
        "cus_no_" integer not null,
        "d_c_no_" integer not null,
        kzl varchar(3) not null,
        status varchar(1) not null,
        "d_c_fn_" varchar(5) not null,
        "r_nr_" varchar(5) not null,
        id bigserial not null,
        "r_dat" varchar(8) not null,
        "rep_dat_" varchar(8) not null,
        "fgnr_" varchar(17) not null,
        primary key (id)
    );

    create table hsg71pf (
        g71000 varchar(3) not null,
        g71010 varchar(5) not null,
        g71020 varchar(8) not null,
        g71030 varchar(5) not null,
        g71040 varchar(1) not null,
        g71050 varchar(8) not null,
        g71060 varchar(7) not null,
        g71070 varchar(10) not null,
        g71080 integer not null,
        g71090 integer not null,
        g71100 integer not null,
        g71110 integer not null,
        g71120 varchar(1) not null,
        g71130 varchar(1) not null,
        g71140 varchar(6) not null,
        g71150 varchar(30) not null,
        g71160 varchar(8) not null,
        g71170 integer not null,
        g71180 integer not null,
        g71190 varchar(1) not null,
        g71200 varchar(10) not null,
        primary key (g71000, g71010, g71020, g71030, g71040)
    );

    create table hsg73pf (
        "action code" integer not null,
        anhang varchar(1) not null,
        "beantr_ mat_" numeric(11,2) not null,
        "beantrg_ arb_" numeric(11,2) not null,
        "beantrg_ spez_" numeric(11,2) not null,
        bereich varchar(1) not null,
        "bew_ code1" varchar(2) not null,
        "bew_ code2" integer not null,
        "bew_ datum" integer not null,
        "claim-art" integer not null,
        "erg_code" varchar(2) not null,
        explanation1 varchar(2) not null,
        fault1 varchar(2) not null,
        "fehler-nr_" varchar(2) not null,
        "fehler-nr_ sde" varchar(2) not null,
        "feldtest-nr_" integer not null,
        "folge-nr_" varchar(2) not null,
        hauptgruppe varchar(2) not null,
        nebengruppe varchar(2) not null,
        pakz varchar(3) not null,
        reply1 varchar(2) not null,
        result1 varchar(2) not null,
        "schad_-c1" varchar(2) not null,
        "schad_-c2" varchar(2) not null,
        "status code" integer not null,
        "steuer code" varchar(2) not null,
        "v_-km-stand" integer not null,
        "v_-rep_-datum" integer not null,
        "variant code" integer not null,
        "verg_ arb_" integer not null,
        "verg_ mat_" integer not null,
        "verg_ spez_" integer not null,
        "auftrags-nr_" varchar(5) not null,
        complain varchar(5) not null,
        explanation2 varchar(5) not null,
        failure varchar(5) not null,
        fault2 varchar(5) not null,
        location varchar(5) not null,
        "rech_-nr_" varchar(5) not null,
        repair varchar(5) not null,
        reply2 varchar(5) not null,
        result2 varchar(5) not null,
        source varchar(5) not null,
        symptom varchar(5) not null,
        "claim-nr_" varchar(8) not null,
        "kampagnen-nr_" varchar(8) not null,
        "rech_-datum" varchar(8) not null,
        "fehler-teil" varchar(18) not null,
        eps varchar(20) not null,
        text1 varchar(65) not null,
        text2 varchar(65) not null,
        text3 varchar(65) not null,
        text4 varchar(65) not null,
        primary key (bereich, "fehler-nr_", pakz, "auftrags-nr_", "rech_-nr_", "claim-nr_", "rech_-datum")
    );

    create table hsgpslf3 (
        aufteilung numeric(3,0) not null,
        categorys varchar(2) not null,
        "code type" varchar(2) not null,
        compamount numeric(15,2) not null,
        "compfac_" numeric(3,0) not null,
        compqty numeric(7,0) not null,
        discount numeric(5,2) not null,
        "fehler-nr_" varchar(2) not null,
        "folge-nr_" varchar(2) not null,
        grossprice numeric(13,2) not null,
        grund numeric(4,0) not null,
        hauptgruppe varchar(2) not null,
        kampagne numeric(8,0) not null,
        kuerzel varchar(3) not null,
        manuell varchar(1) not null,
        menge numeric(5,0) not null,
        "mia status" varchar(1) not null,
        "pos_-nr_" numeric(3,0) not null,
        resultcode varchar(2) not null,
        satzart varchar(3) not null,
        vergütung numeric(3,0) not null,
        wert numeric(11,2) not null,
        zeile numeric(3,0) not null,
        zeit numeric(3,1) not null,
        "code id" varchar(5) not null,
        type varchar(5) not null,
        "claim-nr_" varchar(8) not null,
        "nr_" varchar(18) not null,
        "steuer-code" varchar(40) not null,
        text varchar(2000) not null,
        primary key ("fehler-nr_", "folge-nr_", kuerzel, "claim-nr_")
    );

    create table hsgpspf (
        gps030 integer not null,
        gps150 integer,
        gps000 varchar(255) not null,
        gps010 varchar(255) not null,
        gps_code_type varchar(255),
        gps_record_type varchar(255),
        gps_result_code varchar(255),
        primary key (gps030, gps000, gps010)
    );

    create table itlsmf3 (
        sfran varchar(2) not null,
        gps050 varchar(18) not null,
        item_number varchar(18) not null,
        part_no varchar(18) not null,
        sfnr varchar(18) not null,
        primary key (gps050)
    );

-- ============ SEED DATA ============
INSERT INTO hsahklf3 ("an-zeit", anrede, "ba-schlüssel", berei, bfort, bj, "bonit#t", branch, "eg-umsatz", "f:b>0", "f:v>0", "fert-zeit", "kampagne-nr", "ken-av", "ken-pe", "kl-ausnahme idnr", "kl-erweiterung", "klr-berech", "klr-betrag", "kostencode konzint_", "kz s", land, leitzahl, "mwst %", "mwst % r_", "mwst y/n", pakz, partikelfilter, rc, "re anrede", "re bonit#t", "re land", "re rc", "re valuta", "re zart", reserve1, reserve2, reserve3, reserve4, "rgs basis at", "rgs basis mwst", "rgs ges brutto", "rgs mwst", "rgs mwst at", "rgs netto", splitt, "steuerfrei drittland", "tx_anf", "tx_ende", valuta, "verbucht?", "w/t", "wkt-id", "wrg_", zahlungsart, anr, "is-art", "kor-bez-re", "kst int auf_", "kst lohn", "kst teile", "l_rnr", match, plz, "re match", "re plz", rnr, "sto-bez-re", au, "fibu interim", "fibu mwst", "fibu mwst at", hu, "kto intauf_", "kunden-nr_", "re kunden-nr_", sp, "ktr int auf_", adat, "an-tag", "fert-tag", ga, "ga-übern_", km, "kor-bez-redat", rdat, "sto-bez-redat", tacho, "zaga-gueltig", zdat, "euro-norm", "motor-nr", "prod-code", projekt, "rg-nr_ 10a", "spez-code", "spo order", "user auftrag", "user rechnung", kz, "assi-vorgang-nr", "r&w freigabe-nr", typ, "fahrg_-nr_", "re tele_", telefon, aufbauart, berater, "besteller kunde", dokumentennummer, einsatzart, "fahrzeug-art", hersteller, "hersteller aufbau", "hersteller zusatz 1", "hersteller zusatz 2", "hersteller zusatz 3", "motor-typ", ort, "re ort", "ust-id-nr/ok", "zusatzausrüstung 1", "zusatzausrüstung 2", "zusatzausrüstung 3", branche, "re branche", "re strasse", strasse, name, "re name", atext, "kl-ausnahme klartext", "mail cc", "mail to") VALUES
  ('0000', '', '', '1', '', '', '', '', '', 0, 0, '0000', 0, '', '', '', 0, '', 0, '', '', '', '000', 0, 0, '', '001', '', '', '', '', '', '', '', '', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, '04', '', '000', '000', '', '', 'A', 0, '', '', '001', '', '', '', '', '', '', '', '12345', '', '', '12345', '', '', '', '', '', '', '', '100001', '100001', '', '', '20240115', '20240115', '20240115', '', '', '45000', '', '20240115', '', '', '', '20230101', '', '', '', '', '12345', '', '', '', '', 'ABC123', '', '', '', '00000001234567', '', '', '', '', '', '', '', '', '', '', '', '', '', '', 'City', '', '', '', '', '', '', '', '', '', 'Demo Customer', 'Demo Customer', '', '', '', '');

INSERT INTO hsahklf3 ("an-zeit", anrede, "ba-schlüssel", berei, bfort, bj, "bonit#t", branch, "eg-umsatz", "f:b>0", "f:v>0", "fert-zeit", "kampagne-nr", "ken-av", "ken-pe", "kl-ausnahme idnr", "kl-erweiterung", "klr-berech", "klr-betrag", "kostencode konzint_", "kz s", land, leitzahl, "mwst %", "mwst % r_", "mwst y/n", pakz, partikelfilter, rc, "re anrede", "re bonit#t", "re land", "re rc", "re valuta", "re zart", reserve1, reserve2, reserve3, reserve4, "rgs basis at", "rgs basis mwst", "rgs ges brutto", "rgs mwst", "rgs mwst at", "rgs netto", splitt, "steuerfrei drittland", "tx_anf", "tx_ende", valuta, "verbucht?", "w/t", "wkt-id", "wrg_", zahlungsart, anr, "is-art", "kor-bez-re", "kst int auf_", "kst lohn", "kst teile", "l_rnr", match, plz, "re match", "re plz", rnr, "sto-bez-re", au, "fibu interim", "fibu mwst", "fibu mwst at", hu, "kto intauf_", "kunden-nr_", "re kunden-nr_", sp, "ktr int auf_", adat, "an-tag", "fert-tag", ga, "ga-übern_", km, "kor-bez-redat", rdat, "sto-bez-redat", tacho, "zaga-gueltig", zdat, "euro-norm", "motor-nr", "prod-code", projekt, "rg-nr_ 10a", "spez-code", "spo order", "user auftrag", "user rechnung", kz, "assi-vorgang-nr", "r&w freigabe-nr", typ, "fahrg_-nr_", "re tele_", telefon, aufbauart, berater, "besteller kunde", dokumentennummer, einsatzart, "fahrzeug-art", hersteller, "hersteller aufbau", "hersteller zusatz 1", "hersteller zusatz 2", "hersteller zusatz 3", "motor-typ", ort, "re ort", "ust-id-nr/ok", "zusatzausrüstung 1", "zusatzausrüstung 2", "zusatzausrüstung 3", branche, "re branche", "re strasse", strasse, name, "re name", atext, "kl-ausnahme klartext", "mail cc", "mail to") VALUES
  ('0000', '', '', '1', '', '', '', '', '', 0, 0, '0000', 0, '', '', '', 0, '', 0, '', '', '', '000', 0, 0, '', '001', '', '', '', '', '', '', '', '', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, '04', '', '000', '000', '', '', 'A', 0, '', '', '002', '', '', '', '', '', '', '', '12345', '', '', '99999', '', '', '', '', '', '', '', '100002', '100002', '', '', '20240115', '20240115', '20240115', '', '', '45000', '', '20240115', '', '', '', '20230101', '', '', '', '', '99999', '', '', '', '', 'ABC123', '', '', '', '00000001234567', '', '', '', '', '', '', '', '', '', '', '', '', '', '', 'City', '', '', '', '', '', '', '', '', '', 'Demo Customer 2', 'Demo Customer 2', '', '', '', '');

INSERT INTO hsahklf3 ("an-zeit", anrede, "ba-schlüssel", berei, bfort, bj, "bonit#t", branch, "eg-umsatz", "f:b>0", "f:v>0", "fert-zeit", "kampagne-nr", "ken-av", "ken-pe", "kl-ausnahme idnr", "kl-erweiterung", "klr-berech", "klr-betrag", "kostencode konzint_", "kz s", land, leitzahl, "mwst %", "mwst % r_", "mwst y/n", pakz, partikelfilter, rc, "re anrede", "re bonit#t", "re land", "re rc", "re valuta", "re zart", reserve1, reserve2, reserve3, reserve4, "rgs basis at", "rgs basis mwst", "rgs ges brutto", "rgs mwst", "rgs mwst at", "rgs netto", splitt, "steuerfrei drittland", "tx_anf", "tx_ende", valuta, "verbucht?", "w/t", "wkt-id", "wrg_", zahlungsart, anr, "is-art", "kor-bez-re", "kst int auf_", "kst lohn", "kst teile", "l_rnr", match, plz, "re match", "re plz", rnr, "sto-bez-re", au, "fibu interim", "fibu mwst", "fibu mwst at", hu, "kto intauf_", "kunden-nr_", "re kunden-nr_", sp, "ktr int auf_", adat, "an-tag", "fert-tag", ga, "ga-übern_", km, "kor-bez-redat", rdat, "sto-bez-redat", tacho, "zaga-gueltig", zdat, "euro-norm", "motor-nr", "prod-code", projekt, "rg-nr_ 10a", "spez-code", "spo order", "user auftrag", "user rechnung", kz, "assi-vorgang-nr", "r&w freigabe-nr", typ, "fahrg_-nr_", "re tele_", telefon, aufbauart, berater, "besteller kunde", dokumentennummer, einsatzart, "fahrzeug-art", hersteller, "hersteller aufbau", "hersteller zusatz 1", "hersteller zusatz 2", "hersteller zusatz 3", "motor-typ", ort, "re ort", "ust-id-nr/ok", "zusatzausrüstung 1", "zusatzausrüstung 2", "zusatzausrüstung 3", branche, "re branche", "re strasse", strasse, name, "re name", atext, "kl-ausnahme klartext", "mail cc", "mail to") VALUES
  ('0000', '', '', '1', '', '', '', '', '', 0, 0, '0000', 0, '', '', '', 0, '', 0, '', '', '', '000', 0, 0, '', '001', '', '', '', '', '', '', '', '', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, '04', '', '000', '000', '', '', 'A', 0, '', '', '003', '', '', '', '', '', '', '', '12345', '', '', '88888', '', '', '', '', '', '', '', '100003', '100003', '', '', '20240115', '20240115', '20240115', '', '', '45000', '', '20240115', '', '', '', '20230101', '', '', '', '', '88888', '', '', '', '', 'ABC123', '', '', '', '00000001234567', '', '', '', '', '', '', '', '', '', '', '', '', '', '', 'City', '', '', '', '', '', '', '', '', '', 'Demo Customer 3', 'Demo Customer 3', '', '', '', '');

INSERT INTO hsahklf3 ("an-zeit", anrede, "ba-schlüssel", berei, bfort, bj, "bonit#t", branch, "eg-umsatz", "f:b>0", "f:v>0", "fert-zeit", "kampagne-nr", "ken-av", "ken-pe", "kl-ausnahme idnr", "kl-erweiterung", "klr-berech", "klr-betrag", "kostencode konzint_", "kz s", land, leitzahl, "mwst %", "mwst % r_", "mwst y/n", pakz, partikelfilter, rc, "re anrede", "re bonit#t", "re land", "re rc", "re valuta", "re zart", reserve1, reserve2, reserve3, reserve4, "rgs basis at", "rgs basis mwst", "rgs ges brutto", "rgs mwst", "rgs mwst at", "rgs netto", splitt, "steuerfrei drittland", "tx_anf", "tx_ende", valuta, "verbucht?", "w/t", "wkt-id", "wrg_", zahlungsart, anr, "is-art", "kor-bez-re", "kst int auf_", "kst lohn", "kst teile", "l_rnr", match, plz, "re match", "re plz", rnr, "sto-bez-re", au, "fibu interim", "fibu mwst", "fibu mwst at", hu, "kto intauf_", "kunden-nr_", "re kunden-nr_", sp, "ktr int auf_", adat, "an-tag", "fert-tag", ga, "ga-übern_", km, "kor-bez-redat", rdat, "sto-bez-redat", tacho, "zaga-gueltig", zdat, "euro-norm", "motor-nr", "prod-code", projekt, "rg-nr_ 10a", "spez-code", "spo order", "user auftrag", "user rechnung", kz, "assi-vorgang-nr", "r&w freigabe-nr", typ, "fahrg_-nr_", "re tele_", telefon, aufbauart, berater, "besteller kunde", dokumentennummer, einsatzart, "fahrzeug-art", hersteller, "hersteller aufbau", "hersteller zusatz 1", "hersteller zusatz 2", "hersteller zusatz 3", "motor-typ", ort, "re ort", "ust-id-nr/ok", "zusatzausrüstung 1", "zusatzausrüstung 2", "zusatzausrüstung 3", branche, "re branche", "re strasse", strasse, name, "re name", atext, "kl-ausnahme klartext", "mail cc", "mail to") VALUES
  ('0000', '', '', '1', '', '', '', '', '', 0, 0, '0000', 0, '', '', '', 0, '', 0, '', '', '', '000', 0, 0, '', '001', '', '', '', '', '', '', '', '', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, '04', '', '000', '000', '', '', 'A', 0, '', '', '004', '', '', '', '', '', '', '', '12345', '', '', '77777', '', '', '', '', '', '', '', '100004', '100004', '', '', '20240115', '20240115', '20240115', '', '', '45000', '', '20240115', '', '', '', '20230101', '', '', '', '', '77777', '', '', '', '', 'ABC123', '', '', '', '00000001234567', '', '', '', '', '', '', '', '', '', '', '', '', '', '', 'City', '', '', '', '', '', '', '', '', '', 'Demo Customer 4', 'Demo Customer 4', '', '', '', '');

INSERT INTO hsg71pf (g71000, g71010, g71020, g71030, g71040, g71050, g71060, g71070, g71080, g71090, g71100, g71110, g71120, g71130, g71140, g71150, g71160, g71170, g71180, g71190, g71200) VALUES
  ('001', '12345', '20240115', '001', 'A', '00000001', '1234567', 'ABC123', 20230101, 20240115, 45, 1, ' ', ' ', '100001', 'Demo Customer', '', 0, 0, '1', '001A104'),
  ('001', '99999', '20240115', '002', 'A', '00000002', '1234567', 'ABC123', 20230101, 20240115, 45, 1, ' ', ' ', '100002', 'Demo Customer 2', '', 0, 0, '1', '002A104');

INSERT INTO hsg73pf ("action code", anhang, "beantr_ mat_", "beantrg_ arb_", "beantrg_ spez_", bereich, "bew_ code1", "bew_ code2", "bew_ datum", "claim-art", "erg_code", explanation1, fault1, "fehler-nr_", "fehler-nr_ sde", "feldtest-nr_", "folge-nr_", hauptgruppe, nebengruppe, pakz, reply1, result1, "schad_-c1", "schad_-c2", "status code", "steuer code", "v_-km-stand", "v_-rep_-datum", "variant code", "verg_ arb_", "verg_ mat_", "verg_ spez_", "auftrags-nr_", complain, explanation2, failure, fault2, location, "rech_-nr_", repair, reply2, result2, source, symptom, "claim-nr_", "kampagnen-nr_", "rech_-datum", "fehler-teil", eps, text1, text2, text3, text4) VALUES
  (0, '', 0, 0, 0, '1', '', 0, 0, 0, '', '', '', '01', '', 0, '01', '01', '01', '001', '', '', '', '', 0, '', 0, 0, 0, 0, 0, 0, '001', '', '', '', '', '', '12345', '', '', '', '', '', '00000001', '', '20240115', '', '', 'Demo failure description', '', '', ''),
  (0, '', 0, 0, 0, '1', '', 0, 0, 0, '', '', '', '01', '', 0, '01', '01', '01', '001', '', '', '', '', 0, '', 0, 0, 0, 0, 0, 0, '002', '', '', '', '', '', '99999', '', '', '', '', '', '00000002', '', '20240115', '', '', 'Demo failure description', '', '', '');