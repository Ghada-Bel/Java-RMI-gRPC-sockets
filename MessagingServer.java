import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import messaging.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MessagingServer {
    private static final int PORT = 50051;
    private Server server;
    private final Map<String, List<Message>> userMessagesMap = new ConcurrentHashMap<>();

    private void start() throws IOException {
        server = ServerBuilder.forPort(PORT)
                .addService(new MessagingService())
                .build()
                .start();
        System.out.println("Server started, listening on port " + PORT);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("*** Shutting down gRPC server since JVM is shutting down");
            MessagingServer.this.stop();
            System.err.println("*** Server shut down");
        }));
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final MessagingServer server = new MessagingServer();
        server.start();
        server.blockUntilShutdown();
    }

    private class MessagingService extends MessagingServiceGrpc.MessagingServiceImplBase {
        @Override
        public void sendMessage(MessageRequest request, StreamObserver<MessageResponse> responseObserver) {
            String messageId = UUID.randomUUID().toString();
            Message message = Message.newBuilder()
                    .setMessageId(messageId)
                    .setSenderId(request.getSenderUserId())
                    .setRecipientId(request.getRecipientUserId())
                    .setMessageText(request.getMessageText())
                    .build();

            userMessagesMap.computeIfAbsent(request.getRecipientUserId(), k -> new ArrayList<>()).add(message);

            MessageResponse response = MessageResponse.newBuilder().setMessageId(messageId).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void getMessagesForUser(UserRequest request, StreamObserver<MessageListResponse> responseObserver) {
            List<Message> messages = userMessagesMap.getOrDefault(request.getUserId(), Collections.emptyList());
            MessageListResponse response = MessageListResponse.newBuilder().addAllMessages(messages).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
