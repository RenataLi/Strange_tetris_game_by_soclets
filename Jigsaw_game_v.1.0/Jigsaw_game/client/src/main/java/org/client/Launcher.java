package org.client;

public class Launcher {

    // костыль чтобы работало в jar
    // см  https://stackoverflow.com/questions/56894627/how-to-fix-error-javafx-runtime-components-are-missing-and-are-required-to-ru
    //
    public static void main(String[] args) {
        GameApplication.main(args);
    }

}
