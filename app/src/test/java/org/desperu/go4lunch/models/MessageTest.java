package org.desperu.go4lunch.models;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class MessageTest {

    private String textMessage = "A message !";
    private Date dateCreated = new Date();
    private User userSender = new User();
    private String urlImage = "http://duckduckgo.com";

    @Test
    public void Given_messageWithoutData_When_createMessage_Then_noMessageData() {
        Message message = new Message();

        assertThat("No message test", message.getMessage() == null);
        assertThat("No message created date", message.getDateCreated() == null);
        assertThat("No message user sender", message.getUserSender() == null);
        assertThat("No message image url", message.getUrlImage() == null);
    }

    @Test
    public void Given_textMessage_When_createMessage_Then_checkMessageData() {
        Message message = new Message(textMessage, userSender);

        assertEquals(textMessage, message.getMessage());
        assertThat("No message created date", message.getDateCreated() == null);
        assertEquals(userSender, message.getUserSender());
        assertThat("No message image url", message.getUrlImage() == null);
    }

    @Test
    public void Given_messageWithTextAndImage_When_createMessage_Then_checkMessageData() {
        Message message = new Message(textMessage, urlImage, userSender);

        assertEquals(textMessage, message.getMessage());
        assertThat("No message created date", message.getDateCreated() == null);
        assertEquals(userSender, message.getUserSender());
        assertEquals(urlImage, message.getUrlImage());
    }

    @Test
    public void Given_messageWithoutData_When_useMessageSetters_Then_checkMessageData() {
        Message message = new Message();

        message.setMessage(textMessage);
        message.setDateCreated(dateCreated);
        message.setUserSender(userSender);
        message.setUrlImage(urlImage);

        assertEquals(textMessage, message.getMessage());
        assertEquals(dateCreated, message.getDateCreated());
        assertEquals(userSender, message.getUserSender());
        assertEquals(urlImage, message.getUrlImage());
    }
}