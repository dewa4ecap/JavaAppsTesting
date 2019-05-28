package com.fourelementscapital.imonitor.utils;

//Java 8
/*import java.nio.channels.CompletionHandler;
import java.util.function.BiConsumer;*/

/*Notes from Gama :
	This class is not used in Java 7 since providing Implementation of CompletionHandler with Java 8 syntax.
	BiConsumer is function interface that only exist on Java 8 and above */

public class SocketUtils {
	
	//Java 8
    /*public static <V, A> CompletionHandler<V, A> createCompletionHandler(BiConsumer<V, A> handler, BiConsumer<Throwable, A> errorHandler) {
        return new CompletionHandler<V, A>() {

            @Override
            public void completed(V result, A attachment) {
                handler.accept(result, attachment);
            }

            @Override
            public void failed(Throwable exc, A attachment) {
                errorHandler.accept(exc, attachment);
            }
        };
    }*/
	
	public SocketUtils() {
		// Constructor
	}
}
