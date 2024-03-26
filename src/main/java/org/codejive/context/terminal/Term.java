package org.codejive.context.terminal;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import org.codejive.context.events.Event;
import org.codejive.context.events.EventEmitter;
import org.codejive.context.events.EventTarget;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;

public class Term implements Flushable, Closeable, EventTarget {
    final Terminal terminal;
    final EventEmitter<TermResizeEvent> onResize = new EventEmitter<>();
    private final Attributes savedAttributes;

    public static Term create() throws IOException {
        return new Term();
    }

    private Term() throws IOException {
        terminal = TerminalBuilder.builder().build();
        savedAttributes = terminal.enterRawMode();
        terminal.handle(Terminal.Signal.WINCH, this::handleResize);
    }

    public Size size() {
        return new Size(terminal.getSize().getColumns(), terminal.getSize().getRows());
    }

    public Integer maxColors() {
        return terminal.getNumericCapability(InfoCmp.Capability.max_colors);
    }

    public Screen fullScreen() {
        Size size = size();
        return sizedScreen(size.width(), size.height());
    }

    public Screen sizedScreen(int width, int height) {
        if (width == 0) {
            width = 80;
        }
        if (height == 0) {
            height = 40;
        }
        return new ScreenImpl(this, width, height);
    }

    public Input input() {
        return new InputImpl(this);
    }

    @Override
    public void flush() throws IOException {
        terminal.flush();
    }

    @Override
    public void close() throws IOException {
        terminal.setAttributes(savedAttributes);
        terminal.close();
    }

    public class TermResizeEvent implements Event {
        private final Size size;

        public TermResizeEvent(Size size) {
            this.size = size;
        }

        public Size size() {
            return size;
        }

        @Override
        public EventTarget target() {
            return Term.this;
        }
    }

    private void handleResize(Terminal.Signal signal) {
        onResize.dispatch(new TermResizeEvent(size()));
    }
}
