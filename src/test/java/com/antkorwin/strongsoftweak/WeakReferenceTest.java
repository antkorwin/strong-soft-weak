package com.antkorwin.strongsoftweak;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 * Created on 17.06.2018.
 *
 * @author Korovin Anatoliy
 */
public class WeakReferenceTest {


    @Test
    public void testWeak() {
        // Arrange
        String instance = new String("123");
        WeakReference<String> ref = new WeakReference<>(instance);

        // Act
        instance = null;

        // Asserts
        Assertions.assertThat(ref.get()).isNotNull();
    }

    @Test
    public void testWeakAfterGC() {
        // Arrange
        String instance = new String("123");
        WeakReference<String> ref = new WeakReference<>(instance);

        // Act
        instance = null;
        System.gc();

        // Asserts
        Assertions.assertThat(ref.get()).isNull();
    }

    @Test
    public void testWeakMap() throws InterruptedException {
        // Arrange
        WeakHashMap<String, Boolean> map = new WeakHashMap<>();
        String instance = new String("123");
        map.put(instance, true);

        // Act
        instance = null;
        System.gc();

        // Asserts
        Thread.sleep(100);
        Assertions.assertThat(map).isEmpty();
    }

    @Test
    public void testWeakValue() throws InterruptedException {
        // Arrange
        WeakHashMap<Integer, String> map = new WeakHashMap<>();
        String instance = new String("123");
        map.put(1, instance);

        // Act
        instance = null;
        System.gc();

        // Asserts
        Thread.sleep(3000);

        Assertions.assertThat(map.isEmpty()).isFalse();
    }
}
