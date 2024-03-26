package examples;

import static examples.Util.setBorderWidth;
import static examples.Util.setPos;
import static examples.Util.setSize;

import java.io.IOException;
import java.util.Collections;
import org.codejive.context.render.BorderRenderer;
import org.codejive.context.render.Box;
import org.codejive.context.terminal.Screen;
import org.codejive.context.terminal.Term;
import org.jline.utils.AttributedStringBuilder;

public class FullPanel {
    private final Term term;

    public FullPanel(Term term) {
        this.term = term;
    }

    public static void main(String... args) throws IOException {
        try (Term terminal = Term.create()) {
            new FullPanel(terminal).run();
        }
    }

    public int run() throws IOException {
        Screen screen = term.fullScreen();
        draw(screen);

        screen.update();
        term.input().readChar();

        return 0;
    }

    private void draw(Screen screen) throws IOException {
        int displayWidth = screen.rect().width();
        int displayHeight = screen.rect().height();

        Box b = createBox(displayWidth, displayHeight);
        BorderRenderer br = new BorderRenderer(screen);
        br.render(b);

        AttributedStringBuilder cb = new AttributedStringBuilder();
        cb.append(displayWidth + "x" + displayHeight);
        screen.printAt(
                displayWidth / 2 - cb.length() / 2, displayHeight / 2, cb.toAttributedString());
    }

    private Box createBox(int w, int h) {
        Box b = new Box(Collections.emptyList());
        setSize(b, w - 2, h - 2);
        setPos(b, 1, 1);
        setBorderWidth(b, 1);
        return b;
    }
}
