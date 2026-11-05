package org.pancakelab.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Knows which buildings exist in the dojo and how many rooms each has. Immutable.
 */
public final class BuildingRegistry {
    private static final String DEFAULT_RESOURCE = "/buildings.properties";

    private final Map<Integer, Integer> highestRoomByBuilding;

    public BuildingRegistry(Map<Integer, Integer> highestRoomByBuilding) {
        this.highestRoomByBuilding = Map.copyOf(highestRoomByBuilding);
    }

    public static BuildingRegistry defaultRegistry() {
        try (InputStream in = BuildingRegistry.class.getResourceAsStream(DEFAULT_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Missing resource " + DEFAULT_RESOURCE);
            }
            Properties properties = new Properties();
            properties.load(in);
            return new BuildingRegistry(properties.stringPropertyNames().stream()
                    .collect(Collectors.toMap(key -> Integer.parseInt(key.trim()),
                                              key -> Integer.parseInt(properties.getProperty(key).trim()))));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * @throws IllegalArgumentException if the building or the room does not exist
     */
    public void validate(int building, int room) {
        if (building <= 0) {
            throw new IllegalArgumentException("Building must be positive: " + building);
        }
        if (room <= 0) {
            throw new IllegalArgumentException("Room must be positive: " + room);
        }
        Integer highestRoom = highestRoomByBuilding.get(building);
        if (highestRoom == null) {
            throw new IllegalArgumentException("Building does not exist: " + building);
        }
        if (room > highestRoom) {
            throw new IllegalArgumentException("Room %d does not exist in building %d".formatted(room, building));
        }
    }
}
