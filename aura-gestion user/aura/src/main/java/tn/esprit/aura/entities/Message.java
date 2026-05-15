package tn.esprit.aura.entities;

import java.time.LocalDateTime;

public class Message {
    private int id;
    private int senderId;
    private int receiverId;
    private String senderRole; // "patient" ou "doctor"
    private String content;
    private LocalDateTime sentAt;

    public Message() {}

    public Message(int id, int senderId, int receiverId, String senderRole,
                   String content, LocalDateTime sentAt) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.senderRole = senderRole;
        this.content = content;
        this.sentAt = sentAt;
    }

    public int getId() { return id; }
    public int getSenderId() { return senderId; }
    public int getReceiverId() { return receiverId; }
    public String getSenderRole() { return senderRole; }
    public String getContent() { return content; }
    public LocalDateTime getSentAt() { return sentAt; }

    public void setId(int id) { this.id = id; }
    public void setSenderId(int senderId) { this.senderId = senderId; }
    public void setReceiverId(int receiverId) { this.receiverId = receiverId; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }
    public void setContent(String content) { this.content = content; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}
