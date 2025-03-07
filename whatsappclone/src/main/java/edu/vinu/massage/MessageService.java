package edu.vinu.massage;

import edu.vinu.chat.Chat;
import edu.vinu.chat.ChatRepository;
import edu.vinu.file.FileService;
import edu.vinu.file.FileUtils;
import edu.vinu.notification.Notification;
import edu.vinu.notification.NotificationService;
import edu.vinu.notification.NotificationType;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final MessageMapper mapper;
    private final FileService fileService;
    private final NotificationService notificationService;

    private static final String CHAT_NOT_FOUND_ERROR = "Chat not found";

    public void saveMessage(MessageRequest messageRequest){
        Chat chat = chatRepository.findById(messageRequest.getChatId())
                .orElseThrow(() -> new EntityNotFoundException(CHAT_NOT_FOUND_ERROR));

        Message  message = new Message();
        message.setContent(messageRequest.getContent());
        message.setChat(chat);
        message.setSenderId(messageRequest.getSenderId());
        message.setReceiverId(messageRequest.getReceiverId());
        message.setType(messageRequest.getType());
        message.setState(MessageState.SENT);

        messageRepository.save(message);

        Notification notification = Notification.builder()
                .chatId(chat.getId())
                .messageType(messageRequest.getType())
                .content(message.getContent())
                .senderId(message.getSenderId())
                .receiverId(messageRequest.getReceiverId())
                .type(NotificationType.MESSAGE)
                .chatName(chat.getChatName(message.getSenderId()))
                .build();
        notificationService.sendNotification(messageRequest.getReceiverId(),notification);
    }

    public List<MessageResponse> findChatMessages(String chatId){
        return  messageRepository.findMessagesByChatId(chatId)
                .stream()
                .map(mapper::toMessageResponse)
                .toList();
    }
    @Transactional
    public void setMessagesToSeen(String chatId, Authentication authentication){
        Chat chat= chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException(CHAT_NOT_FOUND_ERROR));
        final String recipientId = getRecipientId(chat,authentication);

        messageRepository.setMessagesToSeenByChatId(chatId,MessageState.SEEN);

        Notification notification = Notification.builder()
                .chatId(chat.getId())
                .senderId(getSenderId(chat,authentication))
                .receiverId(recipientId)
                .type(NotificationType.SEEN)
                .build();
        notificationService.sendNotification(recipientId,notification);
    }

    public void uploadMediaMessage(String chatId, MultipartFile file,Authentication authentication){
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException(CHAT_NOT_FOUND_ERROR));
        final String senderId = getSenderId(chat,authentication);
        final String recipientId = getRecipientId(chat,authentication);
        final String filePath = fileService.saveFile(file,senderId);

        Message  message = new Message();
        message.setContent(null);
        message.setChat(chat);
        message.setSenderId(senderId);
        message.setReceiverId(recipientId);
        message.setType(MessageType.IMAGE);
        message.setState(MessageState.SENT);
        message.setMediaFilePath(filePath);

        messageRepository.save(message);

        Notification notification = Notification.builder()
                .chatId(chat.getId())
                .messageType(MessageType.IMAGE)
                .content(message.getContent())
                .senderId(senderId)
                .receiverId(recipientId)
                .type(NotificationType.IMAGE)
                .chatName(chat.getChatName(message.getSenderId()))
                .media(FileUtils.readFileFromLocation(filePath))
                .build();
        notificationService.sendNotification(recipientId,notification);
    }

    private String getSenderId(Chat chat, Authentication authentication) {
        if (chat.getSender().getId().equals(authentication.getName())) {
            return chat.getSender().getId();
        }
        return chat.getRecipient().getId();
    }

    private String getRecipientId(Chat chat, Authentication authentication) {
        if (chat.getSender().getId().equals(authentication.getName())){
            return chat.getRecipient().getId();
        }
        return chat.getSender().getId();
    }
}
