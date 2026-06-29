package com.immortals.auth.service.config;

import org.hibernate.dialect.H2Dialect;

import java.sql.Types;

/**
 * H2 dialect for tests.
 *
 * H2 in MODE=PostgreSQL rejects TINYINT because PostgreSQL has no such type.
 * Hibernate Envers maps byte → TINYINT for the rev_type audit column, which fails.
 * This dialect remaps TINYINT → SMALLINT so DDL generation succeeds.
 */
public class TestH2Dialect extends H2Dialect {

    @Override
    protected String columnType(int sqlTypeCode) {
        if (sqlTypeCode == Types.TINYINT) {
            return columnType(Types.SMALLINT);
        }
        return super.columnType(sqlTypeCode);
    }
}
