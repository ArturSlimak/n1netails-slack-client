package com.n1netails.n1netails.slack.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Slack Message
 * @author shahid foy
 */
@Getter
@Setter
public class SlackMessage {

    private String channel;
    private String text;

    /**
     * Slack Message Constructor
     */
    public SlackMessage() {}
}
