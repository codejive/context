package org.codejive.context.terminal;

import org.jline.utils.AttributedString;

public interface Screen extends Rectangular {
    void printAt(int x, int y, AttributedString str);

    void clear();

    void update();

    void onResize(Size newSize);
}
