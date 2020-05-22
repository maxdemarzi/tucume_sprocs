package me.tucu.fixtures;

import java.lang.reflect.Field;

public class Graph {
    public static String getGraph() {
        StringBuilder graph = new StringBuilder();
        for (Field field : Nodes.class.getDeclaredFields()) {
            try {
                graph.append(field.get(null));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        for (Field field : Relationships.class.getDeclaredFields()) {
            try {
                graph.append(field.get(null));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return graph.toString();
    }
}
