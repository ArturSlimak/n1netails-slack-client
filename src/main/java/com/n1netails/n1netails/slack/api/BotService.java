package com.n1netails.n1netails.slack.api;

import com.n1netails.n1netails.slack.exception.SlackApiExceptionWrapper;
import com.n1netails.n1netails.slack.exception.SlackClientException;
import com.n1netails.n1netails.slack.exception.SlackTransportException;
import com.n1netails.n1netails.slack.exception.SlackValidationException;
import com.n1netails.n1netails.slack.fallback.SlackFallbackHandler;
import com.n1netails.n1netails.slack.model.SlackBlock;
import com.n1netails.n1netails.slack.model.SlackMessage;
import com.n1netails.n1netails.slack.validation.SlackValidators;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.block.LayoutBlock;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
            ChatPostMessageRequest request = buildRequest(slackMessage);
            ChatPostMessageResponse response = executeRequest(request);
            if (!response.isOk()) {
                fallbackToText(slackMessage, response);
            }
        } catch (IOException e) {
            throw new SlackTransportException("Network error while calling Slack API", e);
        } catch (SlackApiException e) {
            throw new SlackTransportException("Slack SDK failure" + slackMessage.getChannel(), e);
        }
    }

    private void fallbackToText(SlackMessage message, ChatPostMessageResponse originalResponse) {
        try {
            ChatPostMessageRequest fallbackRequest =
                    ChatPostMessageRequest.builder()
                            .channel(message.getChannel())
                            .text(message.getText())
                            .build();

            ChatPostMessageResponse fallbackResponse =
                    executeRequest(fallbackRequest);

            if (!fallbackResponse.isOk()) {
                throw new SlackApiExceptionWrapper(originalResponse.getError());
            }

        } catch (Exception e) {
            throw new SlackApiExceptionWrapper(originalResponse.getError());
        }
    }


    private ChatPostMessageResponse executeRequest(ChatPostMessageRequest request) throws SlackApiException, IOException {
        return methods.chatPostMessage(request);
    }

    private ChatPostMessageRequest buildRequest(SlackMessage slackMessage) {
        ChatPostMessageRequest.ChatPostMessageRequestBuilder requestBuilder =
                ChatPostMessageRequest.builder()
                        .channel(slackMessage.getChannel())
                        .text(slackMessage.getText());

        List<LayoutBlock> blocks = resolveBlocks(slackMessage);
        if ((blocks != null && !blocks.isEmpty()))
            requestBuilder.blocks(blocks);
        return requestBuilder.build();
    }

    private List<LayoutBlock> resolveBlocks(SlackMessage message) {
        if (message.getRawBlocks() != null && !message.getRawBlocks().isEmpty()) {
            return message.getRawBlocks();
        }

        if (message.getBlocks() == null || message.getBlocks().isEmpty()) {
            return null;
        }

        List<LayoutBlock> result = new ArrayList<>();

        for (SlackBlock block : message.getBlocks()) {
            try {
                SlackValidators.validate(block);
                result.add(block.toLayoutBlock());
            } catch (Exception e) {
                SlackBlock fallback = SlackFallbackHandler.handle(block, e);
                result.add(fallback.toLayoutBlock());
            }
        }

        return result;
    }

    private void validateSlackMessage(SlackMessage slackMessage) throws SlackClientException {
        if (slackMessage == null) {
            throw new SlackValidationException("slackMessage cannot be null");
        }
    }
}
