package me.tucu.schema;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static me.tucu.schema.Properties.*;
import static org.junit.Assert.assertEquals;

public class PropertiesTests {

    @Test
    public void shouldNotLetYouCallConstructor() throws NoSuchMethodException {
        Constructor<Properties> constructor;
        constructor = Properties.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Assertions.assertThrows(InvocationTargetException.class, constructor::newInstance);
    }

    @Test
    public void shouldTestProperties() {
        assertEquals("email", EMAIL);
        assertEquals("name", NAME);
        assertEquals("password", PASSWORD);
        assertEquals("username", USERNAME);
        assertEquals("status", STATUS);
        assertEquals("time", TIME);
    }
}
