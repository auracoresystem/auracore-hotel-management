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
    private val firestore: FirebaseFirestore? = try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }

    private val _employees = MutableStateFlow<List<Employee>>(emptyList())
    val employees: StateFlow<List<Employee>> = _employees.asStateFlow()

    private val _attendances = MutableStateFlow<List<Attendance>>(emptyList())
    val attendances: StateFlow<List<Attendance>> = _attendances.asStateFlow()

    private val _leaveRequests = MutableStateFlow<List<LeaveRequest>>(emptyList())
    val leaveRequests: StateFlow<List<LeaveRequest>> = _leaveRequests.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val offlineEmployees = listOf(
        Employee(id = "emp_1", employeeId = "AC-1001", name = "Amit Mishra", role = "Chef", department = "Kitchen", contactNumber = "9876543210"),
        Employee(id = "emp_2", employeeId = "AC-1002", name = "Rohan Sharma", role = "Receptionist", department = "Front Office", contactNumber = "9876543211"),
        Employee(id = "emp_3", employeeId = "AC-1003", name = "Sunita Verma", role = "Housekeeper", department = "Cleaning", contactNumber = "9876543212")
    )
    private val offlineLeaveRequests = listOf(
        LeaveRequest(id = "leave_1", employeeId = "AC-1002", employeeName = "Rohan Sharma", startDate = System.currentTimeMillis() + 86400000, endDate = System.currentTimeMillis() + 259200000, reason = "Personal Work", status = "Pending")
    )

    init {
        loadEmployees()
        loadLeaveRequests()
    }

    private fun loadEmployees() {
        _employees.value = offlineEmployees
        val db = firestore ?: return
        try {
            db.collection("employees")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && !snapshot.isEmpty) {
                        _employees.value = snapshot.toObjects(Employee::class.java)
                    } else {
                        _employees.value = offlineEmployees
                    }
                }
        } catch (e: Exception) {
            _employees.value = offlineEmployees
        }
    }

    private fun loadLeaveRequests() {
        _leaveRequests.value = offlineLeaveRequests
        val db = firestore ?: return
        try {
            db.collection("leave_requests")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && !snapshot.isEmpty) {
                        _leaveRequests.value = snapshot.toObjects(LeaveRequest::class.java)
                    } else {
                        _leaveRequests.value = offlineLeaveRequests
                    }
                }
        } catch (e: Exception) {
            _leaveRequests.value = offlineLeaveRequests
        }
    }
    
    fun checkIn(employeeId: String, onSuccess: () -> Unit) {
         viewModelScope.launch {
            _isLoading.value = true
            try {
                val db = firestore
                val newId = db?.collection("attendance")?.document()?.id ?: "att_${System.currentTimeMillis()}"
                val attendance = Attendance(
                    id = newId,
                    employeeId = employeeId,
                    date = System.currentTimeMillis(),
                    checkInTime = System.currentTimeMillis(),
                    status = "Present"
                )
                if (db != null) {
                    db.collection("attendance").document(attendance.id).set(attendance).await()
                } else {
                    val updated = _attendances.value.toMutableList()
                    updated.add(0, attendance)
                    _attendances.value = updated
                }
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
                val db = firestore
                val newId = db?.collection("leave_requests")?.document()?.id ?: "leave_${System.currentTimeMillis()}"
                val leave = LeaveRequest(
                    id = newId,
                    employeeId = employeeId,
                    employeeName = name,
                    startDate = System.currentTimeMillis(),
                    reason = reason
                )
                if (db != null) {
                    db.collection("leave_requests").document(leave.id).set(leave).await()
                } else {
                    val updated = _leaveRequests.value.toMutableList()
                    updated.add(0, leave)
                    _leaveRequests.value = updated
                }
                onSuccess()
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
         }
    }
}
