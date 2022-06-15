package org.client;

import static org.junit.jupiter.api.Assertions.*;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

class BlockTest {


    @Test
    void parse() {
        Block block = new Block(1, 1);
        String str = "1,1";
        Block block1 = block.parse(str);
        assertEquals(block1, block);

        str = "-1,0";
        block = new Block(-1, 0);
        block1 = block.parse(str);
        assertEquals(block1, block);


        Throwable throwable = assertThrows(NumberFormatException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                String str = "-1,,;0";
                Block block = new Block(-1, 0);
                Block block1 = block.parse(str);
            }
        });
    }

}