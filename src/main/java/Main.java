import build.bazel.remote.execution.v2.*;
import com.google.common.collect.*;
import io.grpc.*;

import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = Grpc.newChannelBuilder("localhost:9092", InsecureChannelCredentials.create())
            .build();
        try {
            ContentAddressableStorageGrpc.ContentAddressableStorageBlockingStub cas = ContentAddressableStorageGrpc.newBlockingStub(channel);
            GetTreeRequest.Builder requestBuilder = GetTreeRequest.newBuilder();
            GetTreeRequest request = requestBuilder
                .setRootDigest(requestBuilder.getRootDigestBuilder().setHash("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"))
                .build();
            Streams.stream(cas.getTree(request))
                .forEach(response -> {
                    System.out.println(response);
                    System.out.println("DIRS: " + response.getDirectoriesCount());
                    response.getDirectoriesList().forEach(directory -> {
                        System.out.printf("DIR: %s (files: %d, dirs: %d)%n",
                            directory, directory.getFilesCount(), directory.getDirectoriesCount());
                        directory.getDirectoriesList().forEach(directoryNode -> {
                            System.out.println("DIR NODE:  " + directoryNode.getName());
                        });
                        directory.getFilesList().forEach(fileNode -> {
                            System.out.println("FILE NODE: " + fileNode.getName());
                        });
                    });
                });
        } finally {
            // ManagedChannels use resources like threads and TCP connections. To prevent leaking these
            // resources the channel should be shut down when it will no longer be used. If it may be used
            // again leave it running.
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
