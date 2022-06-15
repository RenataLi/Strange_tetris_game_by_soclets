package org.client;

import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.*;

class FieldTest {

    @org.junit.jupiter.api.Test
    void isFit() {
        Field field = new Field();
        assertEquals(false,field.isFit(Figure.getFigure((0)),0,0));
        assertEquals(false,field.isFit(Figure.getFigure((5)),0,0));
        assertEquals(true,field.isFit(Figure.getFigure((21)),0,0));
        assertEquals(true,field.isFit(Figure.getFigure((21)),8,8));
        assertEquals(false,field.isFit(Figure.getFigure((19)),8,8));
        assertEquals(true,field.isFit(Figure.getFigure((21)),6,4));
        assertEquals(true,field.isFit(Figure.getFigure((15)),3,5));
    }

    @org.junit.jupiter.api.Test
    void makeMove() {
        Field field = new Field();
        assertEquals(false, field.makeMove(Figure.getFigure(0),0,1));
        assertEquals(false, field.makeMove(Figure.getFigure(0),0,1));
        assertEquals(true,field.makeMove(Figure.getFigure(2),1,2));
        assertEquals(true,field.makeMove(Figure.getFigure(1),1,8));
        assertEquals(true,field.makeMove(Figure.getFigure(3),2,7));

        assertEquals(true, field.makeMove(Figure.getFigure(4),8,1));
        assertEquals(true,field.makeMove(Figure.getFigure(6),7,2));
        assertEquals(false, field.makeMove(Figure.getFigure(5),7,8));
        assertEquals(true,field.makeMove(Figure.getFigure(7),6,7));

        field.print();

    }

    @org.junit.jupiter.api.Test
    void canPlace() {
        Field field = new Field();
        assertEquals(false, field.makeMove(Figure.getFigure(0),0,1));
        assertEquals(false, field.makeMove(Figure.getFigure(0),0,1));
        assertEquals(true,field.makeMove(Figure.getFigure(2),1,2));
        assertEquals(true,field.makeMove(Figure.getFigure(1),1,8));
        assertEquals(true,field.makeMove(Figure.getFigure(3),2,7));

        assertEquals(true, field.makeMove(Figure.getFigure(4),8,1));
        assertEquals(true,field.makeMove(Figure.getFigure(6),7,2));
        assertEquals(false, field.makeMove(Figure.getFigure(5),7,8));
        assertEquals(true,field.makeMove(Figure.getFigure(7),6,7));
        assertEquals(false,field.makeMove(Figure.getFigure(8),8,7));
        assertEquals(true,field.canPlace(Figure.getFigure(15)));
        assertEquals(true,field.canPlace(Figure.getFigure(24)));
    }

    @org.junit.jupiter.api.Test
    void getMoves() {
        Field field = new Field();
        assertEquals(false, field.makeMove(Figure.getFigure(0),0,1));
        assertEquals(false, field.makeMove(Figure.getFigure(0),0,1));
        assertEquals(true,field.makeMove(Figure.getFigure(2),1,2));
        assertEquals(true,field.makeMove(Figure.getFigure(1),1,8));
        assertEquals(true,field.makeMove(Figure.getFigure(3),2,7));

        assertEquals(true, field.makeMove(Figure.getFigure(4),8,1));
        assertEquals(true,field.makeMove(Figure.getFigure(6),7,2));
        assertEquals(false, field.makeMove(Figure.getFigure(5),7,8));
        assertEquals(true,field.makeMove(Figure.getFigure(7),6,7));
        assertEquals(false,field.makeMove(Figure.getFigure(8),8,7));

        assertEquals(6,field.getMoves());
    }
}