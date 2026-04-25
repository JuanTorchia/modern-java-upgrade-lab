package dev.modernjava.upgrade.example;

import java.lang.reflect.Field;

final class LegacyReflectiveAccess {

    void makeAccessible(Field field) {
        field.setAccessible(true);
    }
}
