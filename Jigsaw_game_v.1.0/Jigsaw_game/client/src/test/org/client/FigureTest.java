package org.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FigureTest {

    @Test
    void isFit() {
        Field field = new Field();
        field.place(Figure.getFigure(0),0,7);
        field.place(Figure.getFigure(1),1, 0);
        field.place(Figure.getFigure(2),8, 1);
        field.place(Figure.getFigure(3),7, 8);

        assertEquals(false,field.isFit(Figure.getFigure(0),1,0));
    }
}