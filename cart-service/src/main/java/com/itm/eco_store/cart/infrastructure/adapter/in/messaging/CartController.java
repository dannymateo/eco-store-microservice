package com.itm.eco_store.cart.infrastructure.adapter.in.messaging;

import com.itm.eco_store.cart.application.port.in.IAddProductToCartUseCase;
import com.itm.eco_store.cart.application.port.in.ICheckoutCartUseCase;
import com.itm.eco_store.cart.application.port.in.IClearCartUseCase;
import com.itm.eco_store.cart.application.port.in.IGetCartUseCase;
import com.itm.eco_store.cart.application.port.in.IRemoveProductFromCartUseCase;
import com.itm.eco_store.cart.infrastructure.adapter.in.messaging.dto.AddProductToCartCommand;
import com.itm.eco_store.cart.infrastructure.adapter.in.messaging.dto.CheckoutCartCommand;
import com.itm.eco_store.cart.infrastructure.adapter.in.messaging.dto.ClearCartCommand;
import com.itm.eco_store.cart.infrastructure.adapter.in.messaging.dto.GetCartCommand;
import com.itm.eco_store.cart.infrastructure.adapter.in.messaging.dto.NatsCommandResponse;
import com.itm.eco_store.cart.infrastructure.adapter.in.messaging.dto.RemoveProductFromCartCommand;
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
import java.util.Objects;

@Component
public class CartController implements InitializingBean, DisposableBean {

    private final NatsCartProperties properties;
    private final ObjectMapper objectMapper;
    private final Connection connection;
    private final IAddProductToCartUseCase addProductToCartUseCase;
    private final IRemoveProductFromCartUseCase removeProductFromCartUseCase;
    private final IGetCartUseCase getCartUseCase;
    private final ICheckoutCartUseCase checkoutCartUseCase;
    private final IClearCartUseCase clearCartUseCase;

    private final Map<String, MessageHandler> handlers = new HashMap<>();
    private Dispatcher dispatcher;

    public CartController(
            NatsCartProperties properties,
            ObjectMapper objectMapper,
            Connection connection,
            IAddProductToCartUseCase addProductToCartUseCase,
            IRemoveProductFromCartUseCase removeProductFromCartUseCase,
            IGetCartUseCase getCartUseCase,
            ICheckoutCartUseCase checkoutCartUseCase,
            IClearCartUseCase clearCartUseCase
    ) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.connection = connection;
        this.addProductToCartUseCase = addProductToCartUseCase;
        this.removeProductFromCartUseCase = removeProductFromCartUseCase;
        this.getCartUseCase = getCartUseCase;
        this.checkoutCartUseCase = checkoutCartUseCase;
        this.clearCartUseCase = clearCartUseCase;
    }

    @Override
    public void afterPropertiesSet() {
        registerHandlers();
        dispatcher = connection.createDispatcher(this::dispatchMessage);
        handlers.keySet().forEach(dispatcher::subscribe);
    }

    @Override
    public void destroy() {
        if (dispatcher != null) {
            dispatcher.unsubscribe(properties.subject().cart().addProduct());
            dispatcher.unsubscribe(properties.subject().cart().removeProduct());
            dispatcher.unsubscribe(properties.subject().cart().get());
            dispatcher.unsubscribe(properties.subject().cart().checkout());
            dispatcher.unsubscribe(properties.subject().cart().clear());
        }
    }

    private void registerHandlers() {
        handlers.put(properties.subject().cart().addProduct(), this::handleAddProduct);
        handlers.put(properties.subject().cart().removeProduct(), this::handleRemoveProduct);
        handlers.put(properties.subject().cart().get(), this::handleGetCart);
        handlers.put(properties.subject().cart().checkout(), this::handleCheckout);
        handlers.put(properties.subject().cart().clear(), this::handleClearCart);
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

    private Object handleAddProduct(Message message) throws Exception {
        AddProductToCartCommand command = readCommand(message, AddProductToCartCommand.class);
        String cartId = requireCartId(command.cartId());
        Long productId = Objects.requireNonNull(command.productId(), "productId es obligatorio");
        int quantity = Objects.requireNonNull(command.quantity(), "quantity es obligatoria");
        return addProductToCartUseCase.addProduct(cartId, productId, quantity);
    }

    private Object handleRemoveProduct(Message message) throws Exception {
        RemoveProductFromCartCommand command = readCommand(message, RemoveProductFromCartCommand.class);
        String cartId = requireCartId(command.cartId());
        Long productId = Objects.requireNonNull(command.productId(), "productId es obligatorio");
        return removeProductFromCartUseCase.removeProduct(cartId, productId);
    }

    private Object handleGetCart(Message message) throws Exception {
        GetCartCommand command = readCommand(message, GetCartCommand.class);
        return getCartUseCase.getCart(requireCartId(command.cartId()));
    }

    private Object handleCheckout(Message message) throws Exception {
        CheckoutCartCommand command = readCommand(message, CheckoutCartCommand.class);
        return checkoutCartUseCase.checkout(requireCartId(command.cartId()));
    }

    private Object handleClearCart(Message message) throws Exception {
        ClearCartCommand command = readCommand(message, ClearCartCommand.class);
        return clearCartUseCase.clearCart(requireCartId(command.cartId()));
    }

    private <T> T readCommand(Message message, Class<T> type) throws Exception {
        byte[] payload = message.getData();
        if (payload == null || payload.length == 0) {
            throw new IllegalArgumentException("El payload del comando es obligatorio");
        }
        return objectMapper.readValue(payload, type);
    }

    private String requireCartId(String cartId) {
        if (cartId == null || cartId.isBlank()) {
            throw new IllegalArgumentException("cartId es obligatorio");
        }
        return cartId.trim();
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
            // Si no se puede responder, no detenemos el consumidor.
        }
    }

    @FunctionalInterface
    private interface MessageHandler {
        Object handle(Message message) throws Exception;
    }
}
