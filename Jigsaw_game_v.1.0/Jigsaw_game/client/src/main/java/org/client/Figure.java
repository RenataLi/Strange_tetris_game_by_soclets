package org.client;

import java.util.ArrayList;
import java.util.List;

public class Figure {

    protected static List<Block[]> configs;


    protected Block blocks[];

    protected  Figure(){
        blocks=null;
    }

    Block getBlock(int i) {
        return blocks[i];
    }

    int getNBlocks() {
        return blocks.length;
    }

    protected static void initConfig()     {
        configs=new ArrayList<Block[]>();
        String[] figures={
                "0,1;0,0;0,-1;-1,1",
                "-1,-1;-1,0;0,0;1,0",
                "0,-1;0,0;0,1;-1,1",
                "-1,0;0,0;1,0;1,1",
                "-1,-1;0,-1;0,0;0,1",
                "-1,0;0,0;1,0;1,1",
                "0,-1;0,0;0,1;1,1",
                "-1,1;-1,0;0,0;1,0",
                "-1,-1;-1,0;0,0;0,1",
                "-1,1;0,1;0,0;1,0",
                "-1,1;-1,0;0,0;0,-1",
                "-1,0;0,0;0,1;1,1",
                "-1,1;0,1;1,1;1,0;1,-1",
                "-1,-1;-1,0;-1,1;0,1;1,1",
                "-1,1;-1,0;-1,-1;0,-1;1,-1",
                "0,-1;0,0;-1,1;0,1;1,1",
                "-1,-1;0,-1;1,-1;0,0;0,1",
                "-1,-1;-1,0;-1,1;0,0;1,0",
                "-1,0;0,0;1,-1;1,0;1,1",
                "-1,0;0,0;1,0",
                "0,-1;0,0;0,1",
                "0,0",
                "0,1;0,0;1,0",
                "-1,0;0,0;0,1",
                "-1,0;0,0;0,-1",
                "0,-1;0,0;1,0",
                "0,-1;0,0;0,1;1,0",
                "-1,0;0,0;1,0;0,1",
                "-1,0;0,0;0,-1;0,1",
                "-1,0;0,0;1,0;-1,0"};
        for (int i=0; i< figures.length; i++) {
            String[] pts=figures[i].split(";");
            Block[] blocks=new Block[pts.length];
            for (int j=0; j<pts.length; j++){
                blocks[j]=Block.parse(pts[j]);
            }
            configs.add(blocks);
        }

    }

    public static Figure getRandomFigure() {
        if (configs==null) {
            initConfig();
        }
        int N=configs.size();
        int k=(int)(Math.random()*N);
        Figure f = new Figure();
        System.out.println("Figure No "+k);
        f.blocks=configs.get(k);
        return f;
    }

    public static Figure getFigure(int i) {
        if (configs==null) {
            initConfig();
        }
        Figure f = new Figure();
        f.blocks=configs.get(i);
        return f;
    }

    public static int getNumOfPossibleFigures() {
        if (configs==null) initConfig();
        return configs.size();
    }

}
