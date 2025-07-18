/*
 * This file is part of BlueMap, licensed under the MIT License (MIT).
 *
 * Copyright (c) Blue (Lukas Rieger) <https://bluecolored.de>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.bluecolored.bluemap.core.world;

import de.bluecolored.bluemap.core.util.Key;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.bluecolored.bluemap.core.util.StringUtil.intern;

/**
 * Represents a BlockState<br>
 * It is important that {@link #hashCode} and {@link #equals} are implemented correctly, for the caching to work properly.<br>
 * <br>
 * <i>The implementation of this class has to be thread-save!</i><br>
 */
public class BlockState {

    private static final Pattern BLOCKSTATE_SERIALIZATION_PATTERN = Pattern.compile("^(.+?)(?:\\[(.*)])?$");

    private static final Key MISSING_ID = Key.bluemap("missing");
    private static final Key AIR_ID = Key.minecraft("air");
    private static final Key CAVE_AIR_ID = Key.minecraft("cave_air");
    private static final Key VOID_AIR_ID = Key.minecraft("void_air");
    private static final Key WATER_ID = Key.minecraft("water");

    public static final BlockState AIR = new BlockState(AIR_ID);
    public static final BlockState MISSING = new BlockState(MISSING_ID);
    public static final BlockState WATER = new BlockState(WATER_ID);

    private boolean hashed;
    private int hash;

    private final Key id;
    private final Map<String, String> properties;
    private final Property[] propertiesArray;

    private final boolean isAir, isWater, isWaterlogged;
    private int liquidLevel = -1, redstonePower = -1;

    public BlockState(Key id) {
        this(id, Collections.emptyMap());
    }

    public BlockState(Key id, Map<String, String> properties) {
        this.id = id;
        this.hashed = false;
        this.hash = 0;

        this.properties = properties;
        this.propertiesArray = properties.entrySet().stream()
                .map(e -> new Property(e.getKey(), e.getValue()))
                .sorted()
                .toArray(Property[]::new);

        // special fast-access properties
        this.isAir =
                AIR_ID.equals(this.id) ||
                CAVE_AIR_ID.equals(this.id) ||
                VOID_AIR_ID.equals(this.id);

        this.isWater = WATER_ID.equals(this.id);
        this.isWaterlogged = "true".equals(properties.get("waterlogged"));

    }

    public Key getId() {
        return id;
    }

    /**
     * An immutable map of all properties of this block.<br>
     * <br>
     * For Example:<br>
     * <code>
     * facing = east<br>
     * half = bottom<br>
     * </code>
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    public boolean isAir() {
        return isAir;
    }

    public boolean isWater() {
        return isWater;
    }

    public boolean isWaterlogged() {
        return isWaterlogged;
    }

    public int getLiquidLevel() {
        if (liquidLevel == -1) {
            try {
                String levelString = properties.get("level");
                liquidLevel = levelString != null ? Integer.parseInt(levelString) : 0;
                if (liquidLevel > 15) liquidLevel = 15;
                if (liquidLevel < 0) liquidLevel = 0;
            } catch (NumberFormatException ex) {
                liquidLevel = 0;
            }
        }
        return liquidLevel;
    }

    public int getRedstonePower() {
        if (redstonePower == -1) {
            try {
                String levelString = properties.get("power");
                redstonePower = levelString != null ? Integer.parseInt(levelString) : 0;
                if (redstonePower > 15) redstonePower = 15;
                if (redstonePower < 0) redstonePower = 0;
            } catch (NumberFormatException ex) {
                redstonePower = 15;
            }
        }
        return redstonePower;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BlockState b)) return false;
        if (!id.equals(b.id)) return false;
        return Arrays.equals(propertiesArray, b.propertiesArray);
    }

    @Override
    public int hashCode() {
        if (!hashed){
            hash = Objects.hash(id, properties);
            hashed = true;
        }

        return hash;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(",");
        for (Entry<String, String> e : getProperties().entrySet()){
            sj.add(e.getKey() + "=" + e.getValue());
        }

        return id.getFormatted() + "[" + sj + "]";
    }

    public static BlockState fromString(String serializedBlockState) throws IllegalArgumentException {
        try {
            Matcher m = BLOCKSTATE_SERIALIZATION_PATTERN.matcher(serializedBlockState);

            if (!m.find())
                throw new IllegalArgumentException("'" + serializedBlockState + "' could not be parsed to a BlockState!");

            Map<String, String> pt = new HashMap<>();
            String g2 = m.group(2);
            if (g2 != null && !g2.isEmpty()){
                String[] propertyStrings = g2.trim().split(",");
                for (String s : propertyStrings){
                    String[] kv = s.split("=", 2);
                    pt.put(kv[0], kv[1]);
                }
            }

            String blockId = m.group(1).trim();

            return new BlockState(Key.parse(blockId), pt);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("'" + serializedBlockState + "' could not be parsed to a BlockState!");
        }
    }

    public static final class Property implements Comparable<Property> {
        private final String key, value;

        public Property(String key, String value) {
            this.key = intern(key);
            this.value = intern(value);
        }

        @SuppressWarnings("StringEquality")
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Property property = (Property) o;
            return key == property.key && value == property.value;
        }

        @Override
        public int hashCode() {
            return key.hashCode() * 31 ^ value.hashCode();
        }


        @Override
        public int compareTo(@NotNull BlockState.Property o) {
            int keyCompare = key.compareTo(o.key);
            return keyCompare != 0 ? keyCompare : value.compareTo(o.value);
        }

    }

}
