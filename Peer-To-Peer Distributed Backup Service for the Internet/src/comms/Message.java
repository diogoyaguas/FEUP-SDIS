package comms;

import java.io.Serializable;
import java.net.InetSocketAddress;

public class Message<T extends Serializable> implements Serializable {

    static final long serialVersionUID = 42L;

    public enum Type {
        LOOKUP, SUCCESSOR, NOTIFY, PREDECESSOR, OK, NODEKEY, PUT, GET, RESTORE, DELETE, KEEP, RECLAIM, STORED, DECREASE, ERROR
    }

    /**
     * Number of messages created on this address
     */
    private static int message_counter = 0;

    /**
     * This message's id. In case of a new request, a new id is assigned. In case of
     * a response, the id is the request's id this response was created to.
     */
    private int id;

    /**
     * This message's type
     */
    private Type message_type;

    /**
     * This message's argument, in case it exists
     */
    private T argument;

    /**
     * Data for this message, in case it exists
     */
    private Serializable data;

    /**
     * Boolean to verify if message is a request or a response to a
     */
    private boolean is_request;

    /**
     * This message's sender address (<ip:port>)
     */
    private InetSocketAddress sender_address;

    /**
     * Creates a request with a designated type, returning a serializable message
     * that can be sent afterwards
     * 
     * @param message_type the new request type
     * @param argument     an argument that the designated request requires. It can
     *                     be an simply an integer or a more complex argument
     * @param s_address    the request's sender InetSocketAddress (<ip:port>)
     * @return the request
     */
    public static <S extends Serializable> Message<S> createRequest(Type message_type, S argument,
            InetSocketAddress s_address) {

        Message<S> message = new Message<>();

        message.message_type = message_type;
        message.argument = argument;
        message.sender_address = s_address;
        message.id = message_counter++;
        message.is_request = true;

        return message;
    }

    /**
     * Creates a response with a designated type, returning a serializable message
     * that can be sent afterwards
     * 
     * @param message_type the new request type
     * @param argument     an argument that the designated request requires. It can
     *                     be an simply an integer or a more complex argument
     * @param id           the respective request's id
     * @return the response
     */
    static <S extends Serializable> Message<S> createResponse(Type message_type, S argument, int id) {
        Message<S> message = new Message<>();

        message.message_type = message_type;
        message.argument = argument;
        message.id = id;
        message.is_request = false;

        return message;
    }

    /**
     * Creates a request with a designated type and with a data attribute, returning
     * a serializable message that can be sent afterwards
     * 
     * @param message_type the new request type
     * @param argument     an argument that the designated request requires. It can
     *                     be an simply an integer or a more complex argument
     * @param s_address    the request's sender InetSocketAddress (<ip:port>)
     * @param data         the data wanted to be transferred
     * @return the request
     */
    public static <S extends Serializable> Message<S> createRequestWithData(Type message_type, S argument,
            InetSocketAddress s_address, Serializable data) {

        Message<S> message = new Message<>();

        message.message_type = message_type;
        message.argument = argument;
        message.sender_address = s_address;
        message.id = message_counter++;
        message.is_request = true;
        message.data = data;

        return message;
    }

    boolean isRequest() {
        return this.is_request;
    }

    public T getArgument() {
        return this.argument;
    }

    int getId() {
        return this.id;
    }

    /**
     * Get message's type
     * 
     * @return message_type
     */
    public Type getMessageType() {
        return message_type;
    }

    Serializable getData() {
        return data;
    }

    InetSocketAddress getSenderAddress() {
        return sender_address;
    }
}