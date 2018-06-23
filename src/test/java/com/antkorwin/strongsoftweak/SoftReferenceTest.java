package com.antkorwin.strongsoftweak;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.lang.ref.SoftReference;

/**
 * Created on 19.06.2018.
 *
 * @author Korovin Anatoliy
 */
@Slf4j
public class SoftReferenceTest {

    @Test
    public void name() {
        // Arrange
        String instance = new String("123323");
        SoftReference<String> softReference = new SoftReference<>(instance);
        instance = null;
        Assertions.assertThat(softReference).isNotNull();
        Assertions.assertThat(softReference.get()).isNotNull();

        // Act
        GcUtils.tryToAllocateAllAvailableMemory();

        // Asserts
        Assertions.assertThat(softReference.get()).isNull();
    }
}
