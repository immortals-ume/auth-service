package com.immortals.auth.service.unit.exception;

import com.immortals.auth.service.service.exception.UserException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserExceptionTest {

    @Test
    void constructor_withMessageOnly_setsMessage() {
        UserException ex = new UserException("bad user input");
        assertThat(ex.getMessage()).isEqualTo("bad user input");
        assertThat(ex.getCause()).isNull();
    }

    @Test
    void constructor_withMessageAndCause_setsBoth() {
        Throwable cause = new IllegalArgumentException("root cause");
        UserException ex = new UserException("wrapper", cause);
        assertThat(ex.getMessage()).isEqualTo("wrapper");
        assertThat(ex.getCause()).isSameAs(cause);
    }

    @Test
    void isRuntimeException() {
        assertThat(new UserException("x")).isInstanceOf(RuntimeException.class);
    }
}
