import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import messaging.MessagingServiceGrpc;
import messaging.MessageRequest;
import messaging.UserRequest;
import messaging.MessageResponse;
import messaging.MessageListResponse;
import messaging.Message;

import java.util.concurrent.TimeUnit;

public class MessagingClient {
    private final ManagedChannel channel;
    private final MessagingServiceGrpc.MessagingServiceBlockingStub blockingStub;
    private final MessagingServiceGrpc.MessagingServiceStub asyncStub;

    public MessagingClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        blockingStub = MessagingServiceGrpc.newBlockingStub(channel);
        asyncStub = MessagingServiceGrpc.newStub(channel);
    }

    public void sendMessage(String senderUserId, String recipientUserId, String messageText) {
        MessageRequest request = MessageRequest.newBuilder()
                .setSenderUserId(senderUserId)
                .setRecipientUserId(recipientUserId)
                .setMessageText(messageText)
                .build();

        MessageResponse response = blockingStub.sendMessage(request);
        System.out.println("Message sent. Message ID: " + response.getMessageId());
    }

    public void getMessagesForUser(String userId) {
        UserRequest request = UserRequest.newBuilder().setUserId(userId).build();
        StreamObserver<MessageListResponse> responseObserver = new StreamObserver<MessageListResponse>() {
            @Override
            public void onNext(MessageListResponse response) {
                for (Message message : response.getMessagesList()) {
                    System.out.println("Message ID: " + message.getMessageId());
                    System.out.println("Sender ID: " + message.getSenderId());
                    System.out.println("Recipient ID: " + message.getRecipientId());
                    System.out.println("Message Text: " + message.getMessageText());
                    System.out.println();
                }
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Error fetching messages: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Messages fetched successfully.");
            }
        };

        asyncStub.getMessagesForUser(request, responseObserver);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws InterruptedException {
        MessagingClient client = new MessagingClient("localhost", 50051);

        // Send message example
        client.sendMessage("sender123", "recipient456", "Hello, how are you?");

        // Get messages for user example
        client.getMessagesForUser("recipient456");

        client.shutdown();
    }
}
