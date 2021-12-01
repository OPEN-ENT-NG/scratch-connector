package fr.openent.scratch.utils;

import java.util.HashMap;
import java.util.Map;

public enum ScratchType {
    DIRECTORY("directory"),
    FILE("file"),
    NOTEBOOK("notebook"),
    ERROR("error");

    private final String name;

    private static final Map<String, ScratchType> lookup = new HashMap<>();

    static {
        for (ScratchType type : ScratchType.values()) {
            lookup.put(type.getName(), type);
        }
    }

    ScratchType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static ScratchType get(String name) {
        return lookup.getOrDefault(name, ScratchType.ERROR);
    }

}
