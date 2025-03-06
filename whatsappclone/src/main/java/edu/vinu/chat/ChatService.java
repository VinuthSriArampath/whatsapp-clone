package edu.vinu.chat;

import edu.vinu.user.User;
import edu.vinu.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ChatMapper mapper;

    public List<ChatResponse> getChatsBYReceiverId(Authentication currentUser){
        final String userId = currentUser.getName();
        return chatRepository.findChatsBySenderId(userId)
                .stream()
                .map(chat -> mapper.toChatResponse(chat,userId))
                .toList();
    }

    public String createChat(String senderId,String recipientId){
        Optional<Chat> existingChat = chatRepository.findChatReceiverAndSender(senderId,recipientId);
        if (existingChat.isPresent()){
            return existingChat.get().getId();
        }

        User sender = userRepository.findByPublicId(senderId)
                .orElseThrow(()->new EntityNotFoundException("User with id "+senderId+" not found"));

        User receiver = userRepository.findByPublicId(recipientId)
                .orElseThrow(()->new EntityNotFoundException("User with id "+recipientId+" not found"));

        Chat chat = new Chat();
        chat.setSender(sender);
        chat.setRecipient(receiver);

        Chat saveChat = chatRepository.save(chat);
        return saveChat.getId();
    }
}
