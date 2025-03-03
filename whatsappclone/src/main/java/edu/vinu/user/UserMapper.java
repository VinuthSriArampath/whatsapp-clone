package edu.vinu.user;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class UserMapper {
    public User fromTokenAttributes(Map<String, Object> attributes) {
        User user = new User();
        if (attributes.containsKey("sub")){
            user.setId(attributes.get("sub").toString());
        }
        if (attributes.containsKey("given_name")){
            user.setId(attributes.get("given_name").toString());
        }else if (attributes.containsKey("nickname")){
            user.setId(attributes.get("nickname").toString());
        }
        if (attributes.containsKey("family_name")){
            user.setId(attributes.get("family_name").toString());
        }
        if (attributes.containsKey("email")){
            user.setId(attributes.get("email").toString());
        }
        user.setLastSeen(LocalDateTime.now());
        return user;
    }
}
