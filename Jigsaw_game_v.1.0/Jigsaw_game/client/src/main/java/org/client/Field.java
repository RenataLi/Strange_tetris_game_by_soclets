package org.client;

public class Field {
    public static final int Ncells=9;

    /**
     * состояние клеток
     * 0 - свободная
     * 1 - занятая
     */
    protected int[][] cells;
    /**
     * Count of moves.
     */
    protected int moves;

    public Field() {
        cells = new int[Ncells][Ncells];
        for(int i=0; i<Ncells; i++) {
            for(int j=0; j<Ncells; j++) {
                cells[i][j]=0;
            }
        }
    }

    /**
     * @param fig
     * @param x смещение "корневого" блока фигуры
     * @param y
     * @return
     */
    public boolean isFit(Figure fig, int x, int y) {
        for (int i=0; i<fig.getNBlocks(); i++) {
            int xx=fig.getBlock(i).x()+x;
            int yy=fig.getBlock(i).y()+y;
            if (xx<0 || xx>= Ncells || yy<0 || yy>=Ncells) return false;
            if (cells[xx][yy]!=0) return false;
        }
        return true;
    }

    public void place(Figure fig, int x, int y) {
        if (!isFit(fig, x, y)) return;
        for (int i = 0; i < fig.getNBlocks(); i++) {
            int xx = fig.getBlock(i).x() + x;
            int yy = fig.getBlock(i).y() + y;
            cells[xx][yy] = 1;
        }
    }
    /**
     * Method for making move of figure with coordinates x and y.
     *
     * @param figure - figure.
     * @param x      - x coordinate.
     * @param y      - y coordinate.
     * @return if move is successful return true.
     */
    public boolean makeMove(Figure figure, int x, int y) {
        if (!isFit(figure, x, y)) {
            return false;
        }
        for (int i = 0; i < figure.getNBlocks(); i++) {
            int xx = figure.getBlock(i).x() + x;
            int yy = figure.getBlock(i).y() + y;
            cells[xx][yy] = 1;
        }
        moves++;
        return true;
    }
    public boolean canPlace(Figure fig) {
        for(int i=0; i<Ncells; i++) {
            for(int j=0; j<Ncells; j++) {
                if (isFit(fig,i,j)) return true;
            }
        }
        return false;
    }

    /**
     * Getter of count of moves.
     *
     * @return count of moves.
     */
    public int getMoves() {
        return moves;
    }
    int get(int x, int y) {
        return cells[x][y];
    }


    public void print() {
        for (int row=0; row<Ncells; row++) { // y
            for (int col = 0; col < Ncells; col++) // x
                System.out.print(cells[col][row]);
            System.out.println("");
        }
    }

    public void clean() {
        for (int i = 0; i < Ncells; i++) {
            for (int j = 0; j < Ncells; j++) {
                cells[i][j] = 0;
            }
        }
    }
}
