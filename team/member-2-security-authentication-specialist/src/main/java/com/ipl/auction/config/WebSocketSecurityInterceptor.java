package com.ipl.auction.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class WebSocketSecurityInterceptor implements ChannelInterceptor {

    private final TokenUtil tokenUtil;

    public WebSocketSecurityInterceptor(TokenUtil tokenUtil) {
        this.tokenUtil = tokenUtil;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = null;
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            } else if (accessor.getSessionAttributes() != null) {
                token = (String) accessor.getSessionAttributes().get("token");
            }

            if (token != null) {
                TokenUtil.UserTokenState state = tokenUtil.validateToken(token);
                if (state != null) {
                    // Authenticate the session by setting the user principal
                    accessor.setUser(new Principal() {
                        @Override
                        public String getName() {
                            return state.getUsername();
                        }
                    });
                    return message;
                }
            }
            throw new AccessDeniedException("Unauthorized connection attempt!");
        }
        return message;
    }
}
