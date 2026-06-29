package com.immortals.auth.service.unit.context;

import com.immortals.auth.service.context.ApplicationContextHolder;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplicationContextHolderTest {

    @Test
    void setApplicationContext_makesIsReadyReturnTrue() {
        ApplicationContextHolder holder = new ApplicationContextHolder();
        ApplicationContext ctx = mock(ApplicationContext.class);

        holder.setApplicationContext(ctx);

        assertThat(ApplicationContextHolder.isReady()).isTrue();
    }

    @Test
    void getBean_whenContextSet_returnsBean() {
        ApplicationContextHolder holder = new ApplicationContextHolder();
        ApplicationContext ctx = mock(ApplicationContext.class);
        when(ctx.getBean(String.class)).thenReturn("test-bean");

        holder.setApplicationContext(ctx);

        assertThat(ApplicationContextHolder.getBean(String.class)).isEqualTo("test-bean");
    }
}
