package org.client;

/**
 *  определяет один "квадратик" фигуры
 *  x,y, -- координаты квадратика внутри фигуры относительно "главного" квадратика
 *  x -> влево
 *  y -> вниз
 */
public record Block(int x, int y) {

    public static Block parse(String str) {
        String[] xs=str.split(",");
        int xx = Integer.valueOf(xs[0]);
        int yy = Integer.valueOf(xs[1]);
        return new Block(xx,yy);
    }
}
