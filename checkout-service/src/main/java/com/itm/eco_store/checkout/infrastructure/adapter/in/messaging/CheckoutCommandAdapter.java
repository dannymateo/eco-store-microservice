package com.itm.eco_store.checkout.infrastructure.adapter.in.messaging;

import com.itm.eco_store.checkout.application.port.in.ConfirmCheckoutUseCase;
import com.itm.eco_store.checkout.application.port.in.ProcessCheckoutUseCase;
import com.itm.eco_store.checkout.infrastructure.adapter.in.messaging.dto.ConfirmCheckoutCommandMessage;
import com.itm.eco_store.checkout.infrastructure.adapter.in.messaging.dto.NatsCommandResponse;
import com.itm.eco_store.checkout.infrastructure.adapter.in.messaging.dto.ProcessCheckoutCommandMessage;
import com.itm.eco_store.checkout.infrastructure.config.NatsCheckoutProperties;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class CheckoutCommandAdapter implements InitializingBean, DisposableBean {

    private final NatsCheckoutProperties properties;
    private final ObjectMapper objectMapper;
    private final Connection connection;
    private final ProcessCheckoutUseCase processCheckoutUseCase;
    private final ConfirmCheckoutUseCase confirmCheckoutUseCase;

    private final Map<String, MessageHandler> handlers = new HashMap<>();
    private Dispatcher dispatcher;

    public CheckoutCommandAdapter(
            NatsCheckoutProperties properties,
            ObjectMapper objectMapper,
            Connection connection,
            ProcessCheckoutUseCase processCheckoutUseCase,
            ConfirmCheckoutUseCase confirmCheckoutUseCase
    ) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.connection = connection;
        this.processCheckoutUseCase = processCheckoutUseCase;
        this.confirmCheckoutUseCase = confirmCheckoutUseCase;
    }

    @Override
    public void afterPropertiesSet() {
        String initSubject = properties.subject().checkout().init();
        String confirmSubject = properties.subject().checkout().confirm();
        if (initSubject == null || initSubject.isBlank()) {
            throw new IllegalStateException("nats.subject.checkout.init no configurado");
        }
        if (confirmSubject == null || confirmSubject.isBlank()) {
            throw new IllegalStateException("nats.subject.checkout.confirm no configurado");
        }
        handlers.put(initSubject, this::handleInitCheckout);
        handlers.put(confirmSubject, this::handleConfirmCheckout);
        dispatcher = connection.createDispatcher(this::dispatchMessage);
        handlers.keySet().forEach(dispatcher::subscribe);
    }

    @Override
    public void destroy() {
        if (dispatcher != null) {
            dispatcher.unsubscribe(properties.subject().checkout().init());
            dispatcher.unsubscribe(properties.subject().checkout().confirm());
        }
    }

    private void dispatchMessage(Message message) {
        MessageHandler handler = handlers.get(message.getSubject());
        if (handler == null) {
            return;
        }
        try {
            Object data = handler.handle(message);
            reply(message, NatsCommandResponse.ok(data));
        } catch (Exception ex) {
            reply(message, NatsCommandResponse.error(ex.getMessage()));
        }
    }

    private Object handleInitCheckout(Message message) throws Exception {
        ProcessCheckoutCommandMessage command = readCommand(message, ProcessCheckoutCommandMessage.class);
        return processCheckoutUseCase.process(
                command.cartId(),
                command.paymentMethod(),
                command.payerEmail()
        );
    }

    private Object handleConfirmCheckout(Message message) throws Exception {
        ConfirmCheckoutCommandMessage command = readCommand(message, ConfirmCheckoutCommandMessage.class);
        return confirmCheckoutUseCase.confirm(
                command.paymentMethod(),
                command.orderReference()
        );
    }

    private <T> T readCommand(Message message, Class<T> type) throws Exception {
        byte[] payload = message.getData();
        if (payload == null || payload.length == 0) {
            throw new IllegalArgumentException("El payload del comando es obligatorio");
        }
        return objectMapper.readValue(payload, type);
    }

    private void reply(Message request, NatsCommandResponse response) {
        String replyTo = request.getReplyTo();
        if (replyTo == null || replyTo.isBlank()) {
            return;
        }
        try {
            byte[] responseData = objectMapper.writeValueAsString(response).getBytes(StandardCharsets.UTF_8);
            connection.publish(replyTo, responseData);
        } catch (Exception ignored) {
            // No detenemos el consumidor si falla la respuesta.
        }
    }

    @FunctionalInterface
    private interface MessageHandler {
        Object handle(Message message) throws Exception;
    }
}
