package ge.tsepesh.motorental.exception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {
    private final String messageKey;
    private final Object[] messageArgs;

    public ResourceNotFoundException(String messageKey, Object... messageArgs) {
        super(messageKey);
        this.messageKey = messageKey;
        this.messageArgs = messageArgs;
    }

    public ResourceNotFoundException(String messageKey, Throwable cause, Object... messageArgs) {
        super(messageKey, cause);
        this.messageKey = messageKey;
        this.messageArgs = messageArgs;
    }
}