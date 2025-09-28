package com.n1netails.n1netails.slack.service;

import com.n1netails.n1netails.slack.model.SlackMessage;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import lombok.SneakyThrows;

/**
 * Slack Bot Service
 * @author shahid foy
 */
public class BotService {

    private final String token;

    /**
     * Bot Service Constructor
     * @param token slack bot token
     */
    public BotService(String token) {
        this.token = token;
    }

    @SneakyThrows
    public void send(SlackMessage slackMessage) {
        MethodsClient methods = Slack.getInstance().methods(token);
        ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                .channel(slackMessage.getChannel())
                .text(slackMessage.getText())
                .build();
        methods.chatPostMessage(request);
    }
}
