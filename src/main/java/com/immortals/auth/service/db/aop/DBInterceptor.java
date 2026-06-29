package com.immortals.auth.service.db.aop;

import com.immortals.auth.service.db.context.DbContextHolder;
import com.immortals.auth.service.db.props.DbType;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DBInterceptor {

    @Before("@annotation(com.immortals.auth.service.db.annotation.ReadOnly)")
    public void setReadOnlyDataSource() {
        DbContextHolder.set(DbType.READ);
    }

    @Before("@annotation(com.immortals.auth.service.db.annotation.WriteOnly)")
    public void setWriteOnlyDataSource() {
        DbContextHolder.set(DbType.WRITE);
    }

    @After("@annotation(com.immortals.auth.service.db.annotation.ReadOnly) || @annotation(com.immortals.auth.service.db.annotation.WriteOnly)")
    public void clearContext() {
        DbContextHolder.clear();
    }
}

