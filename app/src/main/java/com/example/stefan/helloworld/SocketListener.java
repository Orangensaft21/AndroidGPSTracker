package com.example.stefan.helloworld;

/**
 * Created by stefan on 09.08.17.
 *
 * Dieses Interface wird auf Listenern (z.B. einer Activity) implementiert,
 * die mit dem Nodejs Server kommunizieren wollen. Der SocketHelper ruft,
 * wenn er etwas vom Nodejs server bekommt, bei seinen Listenern die Methode
 * getSocketResult auf.
 */

public interface SocketListener {
    void getSocketResult(Object... args);
}
