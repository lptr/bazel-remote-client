import build.bazel.remote.execution.v2.*;
import build.bazel.remote.execution.v2.CapabilitiesGrpc.*;
import build.bazel.remote.execution.v2.ContentAddressableStorageGrpc.*;
import com.google.common.collect.*;
import io.grpc.*;

import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws InterruptedException {
//        String target = "localhost:9092";
        String target = "a1c64e3f57f304429a506fa291ab591a-1500560618.us-east-1.elb.amazonaws.com:8980";
        ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create())
            .build();
        try {
            exerciseCapabilities(channel);
            exerciseContentAddressedStorage(channel);
        } finally {
            // ManagedChannels use resources like threads and TCP connections. To prevent leaking these
            // resources the channel should be shut down when it will no longer be used. If it may be used
            // again leave it running.
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private static void exerciseCapabilities(ManagedChannel channel) {
        CapabilitiesBlockingStub capabilitiesService = CapabilitiesGrpc.newBlockingStub(channel);
        ServerCapabilities capabilities = capabilitiesService.getCapabilities(GetCapabilitiesRequest.newBuilder()
            .build());
        System.out.println("CAPS: " + capabilities);
    }

    private static void exerciseContentAddressedStorage(ManagedChannel channel) {
        ContentAddressableStorageBlockingStub cas = ContentAddressableStorageGrpc.newBlockingStub(channel);
        GetTreeRequest.Builder requestBuilder = GetTreeRequest.newBuilder();
        Digest.Builder digest = requestBuilder.getRootDigestBuilder()
            .setHash("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
            .setSizeBytes(0);
        GetTreeRequest request = requestBuilder
            .setRootDigest(digest)
            .setDigestFunction(DigestFunction.Value.MD5)
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
    }
}
