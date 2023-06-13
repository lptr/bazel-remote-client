import com.google.protobuf.gradle.*

plugins {
    id("java")
    id("com.google.protobuf") version "0.8.19"
}

repositories {
    mavenCentral()
}

val protobufVersion = "3.23.2"
val grpcVersion = "1.55.1"

dependencies {
    implementation("com.google.protobuf:protobuf-java:${protobufVersion}")

    implementation("io.grpc:grpc-protobuf:${grpcVersion}")
    implementation("io.grpc:grpc-services:${grpcVersion}")
    implementation("io.grpc:grpc-okhttp:${grpcVersion}")
    implementation("io.grpc:grpc-stub:${grpcVersion}")

    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("com.google.guava:guava:32.0.1-jre")

    testImplementation("junit:junit:4.13.2")
}

sourceSets {
    main {
        proto {
            srcDir("remote-apis").include("**/*.proto")
        }
    }
}

protobuf {
    protoc {
        // The artifact spec for the Protobuf Compiler
        artifact = "com.google.protobuf:protoc:${protobufVersion}"
    }
    plugins {
        // Optional: an artifact spec for a protoc plugin, with "grpc" as
        // the identifier, which can be referred to in the "plugins"
        // container of the "generateProtoTasks" closure.
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${grpcVersion}"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                // Apply the "grpc" plugin whose spec is defined above, without
                // options. Note the braces cannot be omitted, otherwise the
                // plugin will not be added. This is because of the implicit way
                // NamedDomainObjectContainer binds the methods.
                id("grpc") { }
            }
        }
    }
}
