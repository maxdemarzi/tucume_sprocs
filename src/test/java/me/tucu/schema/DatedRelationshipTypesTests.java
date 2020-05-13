package me.tucu.schema;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static me.tucu.schema.DatedRelationshipTypes.*;
import static org.junit.Assert.assertEquals;

public class DatedRelationshipTypesTests {
    @Test
    public void shouldNotLetYouCallConstructor() throws NoSuchMethodException {
        Constructor<DatedRelationshipTypes> constructor;
        constructor = DatedRelationshipTypes.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Assertions.assertThrows(InvocationTargetException.class, constructor::newInstance);
    }

    @Test
    public void shouldTestTypes() {
        assertEquals("MENTIONED_ON_", MENTIONED_ON);
        assertEquals("POSTED_ON_", POSTED_ON);
        assertEquals("REPOSTED_ON_", REPOSTED_ON);
        assertEquals("TAGGED_ON_", TAGGED_ON);
    }
}
