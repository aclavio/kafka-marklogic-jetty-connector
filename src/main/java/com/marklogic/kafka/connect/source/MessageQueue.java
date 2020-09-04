package com.marklogic.kafka.connect.source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class MessageQueue {

    private static MessageQueue ourInstance = new MessageQueue();

    public static MessageQueue getInstance() {
        return ourInstance;
    }

    public static class Message {
        private String key;
        private String topic;
        private String mimeType;
        private String payload;

        public Message(String key, String topic, String mimeType, String payload) {
            this.key = key;
            this.topic = topic;
            this.mimeType = mimeType;
            this.payload = payload;
        }

        public String getKey() { return  key; }

        public String getMimeType() {
            return mimeType;
        }

        public String getPayload() {
            return payload;
        }

        public String getTopic() {
            return topic;
        }
    }

    private List<Message> queue;

    private MessageQueue() {
        queue = Collections.synchronizedList(new ArrayList<>());
    }

    public List<Message> getQueue() {
        return queue;
    }

    public void enqueue(Message msg) {
        queue.add(msg);
    }

    public void enqueue(String topic, String mimeType, String payload) {
        String identifier = UUID.randomUUID().toString();
        queue.add(new Message(identifier, topic, mimeType, payload));
    }

    public void enqueue(String key, String topic, String mimeType, String payload) {
        queue.add(new Message(key, topic, mimeType, payload));
    }

    public void clear() { queue.clear(); }

    public int size() { return queue.size(); }
}
