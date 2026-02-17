package com.n1netails.n1netails.slack.api;

import com.n1netails.n1netails.slack.exception.SlackClientException;
import com.n1netails.n1netails.slack.model.SlackMessage;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;

import java.io.IOException;

/**
 * Slack Bot Service
 *
 * @author shahid foy
 */
class BotService {
    private final MethodsClient methods;

    /**
     * Bot Service Constructor
     *
     * @param token slack bot token
     */
    public BotService(String token) {
        this.methods = Slack.getInstance().methods(token);
    }

    public void send(SlackMessage slackMessage) throws SlackClientException {
        validateSlackMessage(slackMessage);
        try {
            ChatPostMessageRequest.ChatPostMessageRequestBuilder requestBuilder =
                    ChatPostMessageRequest.builder()
                            .channel(slackMessage.getChannel())
                            .text(slackMessage.getText());

            if (slackMessage.getBlocks() != null && !slackMessage.getBlocks().isEmpty()) {
                requestBuilder.blocks(slackMessage.getBlocks());
            }

            ChatPostMessageResponse response = methods.chatPostMessage(requestBuilder.build());

            if (!response.isOk()) {
                throw new SlackClientException("Slack API error: " + response.getError());
            }

        } catch (IOException | SlackApiException e) {
            throw new SlackClientException("Failed to send Slack message to channel: " + slackMessage.getChannel(), e);
        }
    }

    private void validateSlackMessage(SlackMessage slackMessage) throws SlackClientException {
        if (slackMessage == null) {
            throw new SlackClientException("slackMessage cannot be null");
        }

        if (slackMessage.getChannel() == null || slackMessage.getChannel().isBlank()) {
            throw new SlackClientException("Channel must be provided");
        }
    }
}
