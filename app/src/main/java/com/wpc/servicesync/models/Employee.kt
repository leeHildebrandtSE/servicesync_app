package com.wpc.servicesync.models

data class Employee(
    val employeeId: String,
    val name: String,
    val shiftSchedule: String,
    val hospitalAssignment: String,
    val wardAssignments: List<String>
)