package org.talos.teltestspringbot.bot;

import com.vdurmont.emoji.EmojiParser;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.talos.teltestspringbot.model.Anime;
import org.talos.teltestspringbot.service.AnimeService;
import org.talos.teltestspringbot.service.UserService;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Component
public class TelBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;
    private final UserService userService;
    private boolean animeRequested;

    public TelBot(UserService userService, AnimeService animeService) {
        telegramClient = new OkHttpTelegramClient(getBotToken());
        this.userService = userService;
    }

    @Override
    public String getBotToken() {
        return System.getenv("TG_TOKEN");
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String user_first_name = update.getMessage().getChat().getFirstName();
            String user_last_name = update.getMessage().getChat().getLastName();
            String user_username = update.getMessage().getChat().getUserName();
            long user_id = update.getMessage().getChat().getId();

            String message = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();

            if (message.equals("/start") && !animeRequested) {
                String answer = EmojiParser.parseToUnicode(
                        """
                                Добро пожаловать сюда!
                                Надеюсь мы с вами сочтемся и
                                найдем общий язык.
                                :sparkles::sparkles::sparkles:
                                                                
                                Все команды бота :)
                                /start - начальное приветствие
                                /pictures - картинки
                                /hide - спрятать кнопки
                                /anime - тайтл и постер аниме
                                """
                );

                SendMessage sendMessage = SendMessage
                        .builder()
                        .chatId(chat_id)
                        .text(answer)
                        .build();

                log(user_first_name, user_last_name, Long.toString(user_id), message, answer);

                try {
                    telegramClient.execute(sendMessage);
                    userService.check(user_first_name, user_last_name, (int) user_id, user_username);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (message.equals("/anime")) {
                String answer = EmojiParser.parseToUnicode("Please enter anime title :confounded:");
                animeRequested = true;

                SendMessage sendMessage = SendMessage
                        .builder()
                        .chatId(chat_id)
                        .text(answer)
                        .build();

                try {
                    telegramClient.execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (animeRequested) {
                try {
                    List<Anime> animeList = AnimeService.getAnimeTitle(message);

                    if (animeList.isEmpty()) {
                        SendMessage sendMessage = SendMessage
                                .builder()
                                .chatId(chat_id)
                                .text("There is no such an anime title!")
                                .build();

                        try {
                            telegramClient.execute(sendMessage);
                        } catch (TelegramApiException te) {
                            te.printStackTrace();
                        }
                    } else {
                        for (Anime anime : animeList) {

                            String answer = String.format("""
                                    ID: %d
                                    Название: %s
                                    Описание: %s
                                    """, anime.getId(), anime.getNameRu(), anime.getDescription());

                            SendPhoto sendPhoto = SendPhoto
                                    .builder()
                                    .chatId(chat_id)
                                    .photo(new InputFile("https://anilibria.tv" + anime.getPosterUrl()))
                                    .caption(answer)
                                    .build();

                            telegramClient.execute(sendPhoto);
                        }
                    }

                    animeRequested = false;
                } catch (IOException | TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (message.equals("/pictures")) {
                String answer = EmojiParser.parseToUnicode("My favorite pics :smile:");

                SendMessage sendMessage = SendMessage
                        .builder()
                        .chatId(chat_id)
                        .text(answer)
                        .replyMarkup(ReplyKeyboardMarkup
                                .builder()
                                .keyboardRow(new KeyboardRow("#Cat", "#Man"))
                                .keyboardRow(new KeyboardRow("#Birds", "#More_birds"))
                                .build()
                        )
                        .build();

                log(user_first_name, user_last_name, Long.toString(user_id), message, answer);

                try {
                    telegramClient.execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (message.equals("#Cat")) {
                String caption = EmojiParser.parseToUnicode("Cat :cat:");

                SendPhoto sendPhoto = SendPhoto
                        .builder()
                        .chatId(chat_id)
                        .photo(new InputFile("https://assets.rawpixel.com/image_png_600/dG9waWNzL2ltYWdlcy9zY3JlZW5zaG90LTIwMjEtMDktMDEtYXQtMTEuMzkuNTkucG5n.png"))
                        .caption(caption)
                        .replyMarkup(InlineKeyboardMarkup
                                .builder()
                                .keyboardRow(
                                        new InlineKeyboardRow(
                                                InlineKeyboardButton
                                                        .builder()
                                                        .text("Man")
                                                        .callbackData("#Man")
                                                        .build())
                                )
                                .keyboardRow(
                                        new InlineKeyboardRow(
                                                InlineKeyboardButton
                                                        .builder()
                                                        .text("Birds")
                                                        .callbackData("#Birds")
                                                        .build(),
                                                InlineKeyboardButton
                                                        .builder()
                                                        .text("More birds")
                                                        .callbackData("#More_birds")
                                                        .build()
                                        )
                                )
                                .build()
                        )
                        .build();

                log(user_first_name, user_last_name, Long.toString(user_id), message, caption);

                try {
                    telegramClient.execute(sendPhoto);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (message.equals("#Man")) {
                String caption = EmojiParser.parseToUnicode("Man :man:");

                SendPhoto sendPhoto = SendPhoto
                        .builder()
                        .chatId(chat_id)
                        .photo(new InputFile("https://assets.rawpixel.com/image_png_600/dG9waWNzL2ltYWdlcy9zY3JlZW5zaG90LTIwMjEtMDktMDEtYXQtMTIuNTMuMTkucG5n.png"))
                        .caption(caption)
                        .replyMarkup(InlineKeyboardMarkup
                                .builder()
                                .keyboardRow(
                                        new InlineKeyboardRow(
                                                InlineKeyboardButton
                                                        .builder()
                                                        .text("Cat")
                                                        .callbackData("#Cat")
                                                        .build())
                                )
                                .keyboardRow(
                                        new InlineKeyboardRow(
                                                InlineKeyboardButton
                                                        .builder()
                                                        .text("Birds")
                                                        .callbackData("#Birds")
                                                        .build(),
                                                InlineKeyboardButton
                                                        .builder()
                                                        .text("More birds")
                                                        .callbackData("#More_birds")
                                                        .build()
                                        )
                                )
                                .build()
                        )
                        .build();

                log(user_first_name, user_last_name, Long.toString(user_id), message, caption);

                try {
                    telegramClient.execute(sendPhoto);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (message.equals("#Birds")) {
                String caption = EmojiParser.parseToUnicode("Birds :bird:");

                SendPhoto sendPhoto = SendPhoto
                        .builder()
                        .chatId(chat_id)
                        .photo(new InputFile("https://assets.rawpixel.com/image_600/dG9waWNzL2ltYWdlcy9kZXNpZ24tMTY5Njk4NjcxMDA3NC0xLmpwZw.jpg"))
                        .caption(caption)
                        .replyMarkup(InlineKeyboardMarkup
                                .builder()
                                .keyboardRow(
                                        new InlineKeyboardRow(
                                                InlineKeyboardButton
                                                        .builder()
                                                        .text("Cat")
                                                        .callbackData("#Cat")
                                                        .build(),
                                                InlineKeyboardButton
                                                        .builder()
                                                        .text("Man")
                                                        .callbackData("#Man")
                                                        .build())
                                )
                                .keyboardRow(
                                        new InlineKeyboardRow(
                                                InlineKeyboardButton
                                                        .builder()
                                                        .text("More birds")
                                                        .callbackData("#More_birds")
                                                        .build()
                                        )
                                )
                                .build()
                        )
                        .build();

                log(user_first_name, user_last_name, Long.toString(user_id), message, caption);

                try {
                    telegramClient.execute(sendPhoto);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (message.equals("#More_birds")) {
                String caption = EmojiParser.parseToUnicode("More birds :bird::bird::bird:");

                SendPhoto sendPhoto = SendPhoto
                        .builder()
                        .chatId(chat_id)
                        .photo(new InputFile("https://assets.rawpixel.com/image_png_600/dG9waWNzL2ltYWdlcy9zY3JlZW5zaG90LTIwMjEtMDktMDEtYXQtMTguNTUuMzAucG5n.png"))
                        .caption(caption)
                        .replyMarkup(InlineKeyboardMarkup
                                .builder()
                                .keyboardRow(
                                        new InlineKeyboardRow(
                                                InlineKeyboardButton
                                                        .builder()
                                                        .text("Cat")
                                                        .callbackData("#Cat")
                                                        .build(),
                                                InlineKeyboardButton
                                                        .builder()
                                                        .text("Man")
                                                        .callbackData("#Man")
                                                        .build())
                                )
                                .keyboardRow(
                                        new InlineKeyboardRow(
                                                InlineKeyboardButton
                                                        .builder()
                                                        .text("Birds")
                                                        .callbackData("#Birds")
                                                        .build()
                                        )
                                )
                                .build()
                        )
                        .build();

                log(user_first_name, user_last_name, Long.toString(user_id), message, caption);

                try {
                    telegramClient.execute(sendPhoto);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (message.equals("/hide")) {
                String answer = "Pictures are hidden!";

                SendMessage sendMessage = SendMessage
                        .builder()
                        .chatId(chat_id)
                        .text(answer)
                        .replyMarkup(ReplyKeyboardRemove.builder().build())
                        .build();

                log(user_first_name, user_last_name, Long.toString(user_id), message, answer);

                try {
                    telegramClient.execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else {
                String answer = "I can't understand, what you mean!";

                SendMessage sendMessage = SendMessage
                        .builder()
                        .chatId(chat_id)
                        .text(answer)
                        .build();

                log(user_first_name, user_last_name, Long.toString(user_id), message, answer);

                try {
                    telegramClient.execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        } else if (update.hasMessage() && update.getMessage().hasPhoto()) {
            String user_first_name = update.getMessage().getChat().getFirstName();
            String user_last_name = update.getMessage().getChat().getLastName();
            String user_username = update.getMessage().getChat().getUserName();
            long user_id = update.getMessage().getChat().getId();

            String message = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();

            List<PhotoSize> photos = update.getMessage().getPhoto();

            String f_id = photos.stream()
                    .max(Comparator.comparing(PhotoSize::getFileSize))
                    .map(PhotoSize::getFileId)
                    .orElse("");
            int f_width = photos.stream()
                    .max(Comparator.comparing(PhotoSize::getFileSize))
                    .map(PhotoSize::getWidth)
                    .orElse(0);
            int f_height = photos.stream()
                    .max(Comparator.comparing(PhotoSize::getFileSize))
                    .map(PhotoSize::getHeight)
                    .orElse(0);

            String caption = String.format("""
                    Id: %s
                    Width: %d
                    Height: %d
                    """, f_id, f_width, f_height);

            SendPhoto sendPhoto = SendPhoto
                    .builder()
                    .chatId(chat_id)
                    .photo(new InputFile(f_id))
                    .caption(caption)
                    .build();

            log(user_first_name, user_last_name, Long.toString(user_id), message, caption);

            try {
                telegramClient.execute(sendPhoto);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else if (update.hasCallbackQuery()) {
            long chat_id = update.getCallbackQuery().getMessage().getChatId();
            String call_data = update.getCallbackQuery().getData();
            int message_id = update.getCallbackQuery().getMessage().getMessageId();

            switch (call_data) {
                case "#Cat" -> {
                    EditMessageMedia editMessageMedia = EditMessageMedia
                            .builder()
                            .chatId(chat_id)
                            .messageId(message_id)
                            .media(new InputMediaPhoto("https://assets.rawpixel.com/image_png_600/dG9waWNzL2ltYWdlcy9zY3JlZW5zaG90LTIwMjEtMDktMDEtYXQtMTEuMzkuNTkucG5n.png"))
                            .replyMarkup(InlineKeyboardMarkup
                                    .builder()
                                    .keyboardRow(new InlineKeyboardRow(
                                            InlineKeyboardButton
                                                    .builder()
                                                    .text("Man")
                                                    .callbackData("#Man")
                                                    .build()
                                    ))
                                    .keyboardRow(new InlineKeyboardRow(
                                            InlineKeyboardButton
                                                    .builder()
                                                    .text("Birds")
                                                    .callbackData("#Birds")
                                                    .build(),
                                            InlineKeyboardButton
                                                    .builder()
                                                    .text("More birds")
                                                    .callbackData("#More_birds")
                                                    .build()
                                    ))
                                    .build()
                            )
                            .build();

                    try {
                        telegramClient.execute(editMessageMedia);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
                case "#Man" -> {
                    EditMessageMedia editMessageMedia = EditMessageMedia
                            .builder()
                            .chatId(chat_id)
                            .messageId(message_id)
                            .media(new InputMediaPhoto("https://assets.rawpixel.com/image_png_600/dG9waWNzL2ltYWdlcy9zY3JlZW5zaG90LTIwMjEtMDktMDEtYXQtMTIuNTMuMTkucG5n.png"))
                            .replyMarkup(InlineKeyboardMarkup
                                    .builder()
                                    .keyboardRow(new InlineKeyboardRow(
                                            InlineKeyboardButton
                                                    .builder()
                                                    .text("Cat")
                                                    .callbackData("#Cat")
                                                    .build()
                                    ))
                                    .keyboardRow(new InlineKeyboardRow(
                                            InlineKeyboardButton
                                                    .builder()
                                                    .text("Birds")
                                                    .callbackData("#Birds")
                                                    .build(),
                                            InlineKeyboardButton
                                                    .builder()
                                                    .text("More birds")
                                                    .callbackData("#More_birds")
                                                    .build()
                                    ))
                                    .build()
                            )
                            .build();

                    try {
                        telegramClient.execute(editMessageMedia);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
                case "#Birds" -> {
                    EditMessageMedia editMessageMedia = EditMessageMedia
                            .builder()
                            .chatId(chat_id)
                            .messageId(message_id)
                            .media(new InputMediaPhoto("https://assets.rawpixel.com/image_600/dG9waWNzL2ltYWdlcy9kZXNpZ24tMTY5Njk4NjcxMDA3NC0xLmpwZw.jpg"))
                            .replyMarkup(InlineKeyboardMarkup
                                    .builder()
                                    .keyboardRow(new InlineKeyboardRow(
                                            InlineKeyboardButton
                                                    .builder()
                                                    .text("Cat")
                                                    .callbackData("#Cat")
                                                    .build(),
                                            InlineKeyboardButton
                                                    .builder()
                                                    .text("Man")
                                                    .callbackData("#Man")
                                                    .build()
                                    ))
                                    .keyboardRow(new InlineKeyboardRow(
                                            InlineKeyboardButton
                                                    .builder()
                                                    .text("More birds")
                                                    .callbackData("#More_birds")
                                                    .build()
                                    ))
                                    .build()
                            )
                            .build();

                    try {
                        telegramClient.execute(editMessageMedia);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
                case "#More_birds" -> {
                    EditMessageMedia editMessageMedia = EditMessageMedia
                            .builder()
                            .chatId(chat_id)
                            .messageId(message_id)
                            .media(new InputMediaPhoto("https://assets.rawpixel.com/image_png_600/dG9waWNzL2ltYWdlcy9zY3JlZW5zaG90LTIwMjEtMDktMDEtYXQtMTguNTUuMzAucG5n.png"))
                            .replyMarkup(InlineKeyboardMarkup
                                    .builder()
                                    .keyboardRow(new InlineKeyboardRow(
                                            InlineKeyboardButton
                                                    .builder()
                                                    .text("Cat")
                                                    .callbackData("#Cat")
                                                    .build(),
                                            InlineKeyboardButton
                                                    .builder()
                                                    .text("Man")
                                                    .callbackData("#Man")
                                                    .build()
                                    ))
                                    .keyboardRow(new InlineKeyboardRow(
                                            InlineKeyboardButton
                                                    .builder()
                                                    .text("Birds")
                                                    .callbackData("#Birds")
                                                    .build()
                                    ))
                                    .build()
                            )
                            .build();

                    try {
                        telegramClient.execute(editMessageMedia);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void log(
            String first_name,
            String last_name,
            String user_id,
            String message,
            String bot_answer
    ) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        System.out.printf("""
                %s
                ------------------------------
                Message from %s %s. (id = %s)
                 Text - %s
                Bot answer:
                 Text - %s
                """, dateFormat.format(date), first_name, last_name, user_id, message, bot_answer);
    }

    @AfterBotRegistration
    public void afterRegistration(BotSession botSession) {
        System.out.println("Registered bot running state is: " + botSession.isRunning());
    }
}
