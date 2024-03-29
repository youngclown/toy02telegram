package bot.bymin.telegram.service;

import bot.bymin.telegram.config.TelegramConfig;
import bot.bymin.telegram.service.exception.NoneException;
import bot.bymin.telegram.service.receiver.ScreenReceiver;
import bot.bymin.telegram.service.receiver.Receiver;
import bot.bymin.telegram.service.receiver.PropertiesReceiver;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.InputStream;

public class PlayBot extends TelegramLongPollingBot {

    String name;
    String token;


    public PlayBot(TelegramConfig config) {
        this.name = config.getTelegramBotName();
        this.token = config.getTelegramBotToken();
    }


    @Override
    public String getBotToken() {
        return this.token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        String scouterName = update.getMessage().getFrom().getFirstName();
        String text = update.getMessage().getText();
        System.out.println(scouterName+ " > " +text + " > " + update);

        // 앞자리가 "/" 일 경우에만 동작한다.
        if(text != null && text.startsWith("/")) {
            // Chain Of Responsibility
            Receiver screenReceiver = new ScreenReceiver(update);
            Receiver propertiesReceiver = new PropertiesReceiver(update);
            screenReceiver.setNext(propertiesReceiver);
            try {
                String type = screenReceiver.support(text);
                Object result = type.equals("screen") ? screenReceiver.getResult() : propertiesReceiver.getResult();

                if (result instanceof InputStream) {
                    SendPhoto sendPhotoRequest = new SendPhoto();sendPhotoRequest.setChatId(update.getMessage().getChatId());
                    sendPhotoRequest.setPhoto("image",(InputStream)result);
                    // Execute the method
                    try {
                        execute(sendPhotoRequest);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            } catch (NoneException e) {
                SendMessage message = new SendMessage();
                message.setChatId(update.getMessage().getChatId());
                message.setText("잘못된 명령어입니다.");
                try {
                    execute(message);
                } catch (TelegramApiException e1) {
                    e1.printStackTrace();
                }
            }


        }
    }

    @Override
    public String getBotUsername() {
        return this.name;
    }
}
