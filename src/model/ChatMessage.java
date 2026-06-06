package model;

import java.time.LocalDateTime;

/**
 * ChatMessage — one row in the AI Coach conversation UI.
 *
 * <p>The Coach screen is mocked for the MVP. This class will later be
 * backed by a real assistant API; the UI layer should not need to change.</p>
 */
public class ChatMessage {

    public enum Sender { USER, COACH }

    private final Sender sender;
    private final String content;
    private final LocalDateTime when;

    public ChatMessage(Sender sender, String content, LocalDateTime when) {
        this.sender = sender;
        this.content = content;
        this.when = when;
    }

    public Sender getSender() { return sender; }
    public String getContent() { return content; }
    public LocalDateTime getWhen() { return when; }
}
