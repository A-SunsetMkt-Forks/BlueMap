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
package de.bluecolored.bluemap.core.logger;

import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class PrintStreamLogger extends AbstractLogger {

    private final PrintStream out, err;

    boolean isDebug;

    public PrintStreamLogger(PrintStream out, PrintStream err) {
        this.out = out;
        this.err = err;
        this.isDebug = false;
    }

    public PrintStreamLogger(PrintStream out, PrintStream err, boolean debug) {
        this.out = out;
        this.err = err;
        this.isDebug = debug;
    }

    public boolean isDebug() {
        return isDebug;
    }

    public void setDebug(boolean debug) {
        this.isDebug = debug;
    }

    @Override
    public void logError(String message, @Nullable Throwable throwable) {
        log(err, "ERROR", message);
        if (throwable != null) throwable.printStackTrace(err);
    }

    @Override
    public void logWarning(String message) {
        log(out, "WARNING", message);
    }

    @Override
    public void logInfo(String message) {
        log(out, "INFO", message);
    }

    @Override
    public void logDebug(String message) {
        if (isDebug) log(out, "DEBUG", message);
    }

    @Override
    public void noFloodDebug(String key, String message) {
        if (isDebug) super.noFloodDebug(key, message);
    }

    @Override
    public void noFloodDebug(String message) {
        if (isDebug) super.noFloodDebug(message);
    }

    private void log(PrintStream stream, String level, String message) {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
        stream.printf("[%1$tT %2$s] %3$s%n", zdt, level, message);
    }

}
