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
package de.bluecolored.bluemap.common.commands.checks;

import de.bluecolored.bluemap.common.plugin.Plugin;
import de.bluecolored.bluemap.core.map.BmMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

import static de.bluecolored.bluemap.common.commands.TextFormat.*;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

@Getter
@RequiredArgsConstructor
public class MapIsNotFrozenCheck implements Check {

    private final Plugin plugin;
    private final BmMap map;

    @Override
    public CheckResult getResult() {
        return this.plugin.getPluginState().getMapState(map).isUpdateEnabled() ? CheckResult.OK : CheckResult.BAD;
    }

    @Override
    public Component getFailureDescription() {
        return format(
                lines(
                        text("⚠ map % is frozen"),
                        text("a frozen map will not be updated"),
                        empty(),
                        text("you can use % to unfreeze the map").color(BASE_COLOR)
                ),
                formatMap(map).color(HIGHLIGHT_COLOR),
                command("/bluemap unfreeze " + map.getId()).color(HIGHLIGHT_COLOR)
        );
    }

}
