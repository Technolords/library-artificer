package net.technolords.tools.artificer.exception;

/**
 * Created by Technolords on 2015-Aug-18.
 */
public class ArtificerException extends Exception {

    public ArtificerException(String message) {
        super(message);
    }

    public ArtificerException(Throwable cause) {
        super(cause);
    }

    public ArtificerException(String message,Throwable cause) {
        super(message, cause);
    }
}
