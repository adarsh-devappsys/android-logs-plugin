package com.devappsys.logsplugin.grpc

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import log.Log.*
import log.LogServiceGrpc

class GrpcClient(
    private val host: String = "89.233.104.140",
    private val port: Int = 50051
) {
    private val channel: ManagedChannel = ManagedChannelBuilder
        .forAddress(host, port)
        .usePlaintext() // Use TLS in production
        .build()

    private val stub = LogServiceGrpc.newBlockingStub(channel)

    fun uploadLog(logEvent: LogEvent): LogResponse {
        return stub.uploadLog(logEvent)
    }

    fun uploadLogs(logEvents: List<LogEvent>): LogResponse {
        val bulkRequest = BulkLogRequest.newBuilder()
            .addAllLogs(logEvents)
            .build()
        return stub.uploadLogs(bulkRequest)
    }

    fun shutdown() {
        channel.shutdownNow()
    }
}