// File: app/src/main/java/com/wpc/servicesync/models/SessionStatus.kt
package com.wpc.servicesync.models

enum class SessionStatus(val displayName: String) {
    NOT_STARTED("Not Started"),
    IN_TRANSIT("In Transit"),
    WAITING_NURSE("Waiting for Nurse"),
    READY_TO_SERVE("Ready to Serve"),
    SERVING("Serving Patients"),
    COMPLETED("Service Complete")
}