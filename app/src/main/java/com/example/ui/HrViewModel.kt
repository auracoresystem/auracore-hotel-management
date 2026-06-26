package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Employee(
    val id: String = "",
    val employeeId: String = "",
    val name: String = "",
    val role: String = "",
    val department: String = "",
    val contactNumber: String = "",
    val salary: Double = 0.0,
    val performanceRating: Double = 0.0,
    val documentUrl: String = ""
)

data class Attendance(
    val id: String = "",
    val employeeId: String = "",
    val date: Long = 0L,
    val checkInTime: Long = 0L,
    val checkOutTime: Long = 0L,
    val status: String = "Present", // Present, Absent, Late, Half Day
    val location: String = ""
)

data class LeaveRequest(
    val id: String = "",
    val employeeId: String = "",
    val employeeName: String = "",
    val startDate: Long = 0L,
    val endDate: Long = 0L,
    val reason: String = "",
    val status: String = "Pending" // Pending, Approved, Rejected
)

class HrViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _employees = MutableStateFlow<List<Employee>>(emptyList())
    val employees: StateFlow<List<Employee>> = _employees.asStateFlow()

    private val _attendances = MutableStateFlow<List<Attendance>>(emptyList())
    val attendances: StateFlow<List<Attendance>> = _attendances.asStateFlow()

    private val _leaveRequests = MutableStateFlow<List<LeaveRequest>>(emptyList())
    val leaveRequests: StateFlow<List<LeaveRequest>> = _leaveRequests.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadEmployees()
        loadLeaveRequests()
    }

    private fun loadEmployees() {
        firestore.collection("employees")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _employees.value = snapshot.toObjects(Employee::class.java)
                }
            }
    }

    private fun loadLeaveRequests() {
        firestore.collection("leave_requests")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _leaveRequests.value = snapshot.toObjects(LeaveRequest::class.java)
                }
            }
    }
    
    fun checkIn(employeeId: String, onSuccess: () -> Unit) {
         viewModelScope.launch {
            _isLoading.value = true
            try {
                val attendance = Attendance(
                    id = firestore.collection("attendance").document().id,
                    employeeId = employeeId,
                    date = System.currentTimeMillis(),
                    checkInTime = System.currentTimeMillis(),
                    status = "Present"
                )
                firestore.collection("attendance").document(attendance.id).set(attendance).await()
                onSuccess()
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
         }
    }
    
    fun requestLeave(employeeId: String, name: String, reason: String, onSuccess: () -> Unit) {
         viewModelScope.launch {
            _isLoading.value = true
            try {
                val leave = LeaveRequest(
                    id = firestore.collection("leave_requests").document().id,
                    employeeId = employeeId,
                    employeeName = name,
                    startDate = System.currentTimeMillis(),
                    reason = reason
                )
                firestore.collection("leave_requests").document(leave.id).set(leave).await()
                onSuccess()
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
         }
    }
}
