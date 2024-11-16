package org.codejive.context.terminal;

import org.codejive.context.events.EventTarget;
import org.jline.utils.AttributedCharSequence;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.Display;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BufferedScreen implements Screen, EventTarget {
    private final Term term;
    private final FlexRect flexRect;
    private final Display display;
    private AttributedStringBuilder[] lines;

    @Override
    public Rect rect() {
        return flexRect.actualRect(term.size());
    }

    protected BufferedScreen(Term term, int left, int top, int width, int height) {
        this(term, new FlexRect(left, top, width, height));
    }

    protected BufferedScreen(Term term, FlexRect flexRect) {
        this.term = term;
        this.flexRect = flexRect;
        this.display = new Display(term.terminal, false);
        term.onResize.addListener(this::handleTermResizeEvent);
        Rect r = rect();
        this.display.resize(r.height(), r.width());
        clear();
    }

    @Override
    public void clear() {
        int width = rect().width();
        int height = rect().height();
        this.lines = new AttributedStringBuilder[height];
        for (int i = 0; i < height; i++) {
            this.lines[i] = new AttributedStringBuilder(width);
        }
    }

    public void printAt(int x, int y, AttributedString str) {
        if (y < rect().top() || y > rect().bottom()) {
            return;
        }
        AttributedStringBuilder line = lines[y];
        if (x > rect().right() || (x + str.length() - 1) < rect().left()) {
            return;
        }
        if (x < rect().left()) {
            str = str.substring(rect().left() - x, str.length());
            x = rect().left();
        }
        if ((x + str.length() - 1) > rect().right()) {
            str = str.substring(0, rect().right() - x + 1);
        }
        if (line.length() < x) {
            pad(line, ' ', x - line.length());
            line.append(str);
        } else if (x + str.length() >= line.length()) {
            line.setLength(x);
            line.append(str);
        } else {
            AttributedStringBuilder ln = new AttributedStringBuilder(rect().width());
            ln.append(line.substring(0, x));
            ln.append(str);
            ln.append(line.substring(x + str.length(), line.length()));
            lines[y] = ln;
        }
    }

    public static void pad(AttributedStringBuilder str, char c, int n) {
        for (int i = 0; i < n; i++) {
            str.append(c);
        }
    }

    protected List<AttributedString> lines() {
        return Arrays.stream(lines)
                .map(AttributedCharSequence::toAttributedString)
                .collect(Collectors.toList());
    }

    public void update() {
        display.update(lines(), 0);
    }

    protected void handleTermResizeEvent(Term.TermResizeEvent event) {
        System.out.println("RESIZE EVENT: " + event.size());
        // TODO Check if the new size affects this Screen
        onResize.dispatch(new ScreenResizeEvent(rect()));
    }
}
