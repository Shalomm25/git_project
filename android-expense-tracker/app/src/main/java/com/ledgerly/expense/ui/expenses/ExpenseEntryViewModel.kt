package com.ledgerly.expense.ui.expenses

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledgerly.expense.core.AppResult
import com.ledgerly.expense.core.util.DateUtils
import com.ledgerly.expense.core.util.Money
import com.ledgerly.expense.domain.model.Category
import com.ledgerly.expense.domain.model.Client
import com.ledgerly.expense.domain.model.PaymentMethod
import com.ledgerly.expense.domain.repository.AuthRepository
import com.ledgerly.expense.domain.repository.CategoryRepository
import com.ledgerly.expense.domain.repository.ClientRepository
import com.ledgerly.expense.domain.repository.PaymentMethodRepository
import com.ledgerly.expense.domain.usecase.DeleteExpenseUseCase
import com.ledgerly.expense.domain.usecase.ExpenseDraft
import com.ledgerly.expense.domain.usecase.SaveExpenseUseCase
import com.ledgerly.expense.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ExpenseFormState(
    val id: String? = null,
    val date: LocalDate = DateUtils.today(),
    val merchant: String = "",
    val amountText: String = "",
    val categoryId: String? = null,
    val paymentMethod: String? = null,
    val businessPurpose: String = "",
    val notes: String = "",
    val receiptLocalPath: String? = null,
    val mileage: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val tagsText: String = "",
    val clientId: String? = null,
    val isSaving: Boolean = false,
    val isEditing: Boolean = false,
    val error: String? = null,
) {
    val isValid: Boolean
        get() = merchant.isNotBlank() && (Money.parseToCents(amountText) ?: 0) > 0 && categoryId != null
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExpenseEntryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    categoryRepository: CategoryRepository,
    paymentMethodRepository: PaymentMethodRepository,
    clientRepository: ClientRepository,
    private val saveExpenseUseCase: SaveExpenseUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val expenseRepository: com.ledgerly.expense.domain.repository.ExpenseRepository,
) : ViewModel() {

    private val editingId: String? =
        savedStateHandle.get<String>(Routes.EXPENSE_ENTRY_ARG_ID)?.takeIf { it.isNotBlank() }

    private val _form = MutableStateFlow(ExpenseFormState(isEditing = editingId != null))
    val form: StateFlow<ExpenseFormState> = _form.asStateFlow()

    private val user = authRepository.currentUser.filterNotNull()

    val categories: StateFlow<List<Category>> = user
        .flatMapLatest { categoryRepository.observeCategories(it.uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val paymentMethods: StateFlow<List<PaymentMethod>> = user
        .flatMapLatest { paymentMethodRepository.observePaymentMethods(it.uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val clients: StateFlow<List<Client>> = user
        .flatMapLatest { clientRepository.observeClients(it.uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        if (editingId != null) loadExisting(editingId)
    }

    private fun loadExisting(id: String) {
        viewModelScope.launch {
            val expense = expenseRepository.getById(id) ?: return@launch
            _form.value = ExpenseFormState(
                id = expense.id,
                date = expense.date,
                merchant = expense.merchant,
                amountText = Money.centsToPlainString(expense.amountCents),
                categoryId = expense.expenseCategoryId,
                paymentMethod = expense.paymentMethod,
                businessPurpose = expense.businessPurpose.orEmpty(),
                notes = expense.notes.orEmpty(),
                receiptLocalPath = expense.receiptLocalPath,
                mileage = expense.mileage?.toString().orEmpty(),
                latitude = expense.latitude,
                longitude = expense.longitude,
                tagsText = expense.tags.joinToString(", "),
                clientId = expense.clientId,
                isEditing = true,
            )
        }
    }

    fun update(transform: (ExpenseFormState) -> ExpenseFormState) {
        _form.value = transform(_form.value)
    }

    fun setReceipt(path: String?) = update { it.copy(receiptLocalPath = path) }
    fun setLocation(lat: Double, lng: Double) = update { it.copy(latitude = lat, longitude = lng) }

    fun save(onSaved: () -> Unit) {
        val state = _form.value
        if (!state.isValid) {
            _form.value = state.copy(error = "Enter a merchant, amount and category.")
            return
        }
        viewModelScope.launch {
            _form.value = state.copy(isSaving = true, error = null)
            val category = categories.value.firstOrNull { it.id == state.categoryId }
            val draft = ExpenseDraft(
                id = state.id,
                date = state.date,
                merchant = state.merchant,
                amountCents = Money.parseToCents(state.amountText) ?: 0,
                categoryId = state.categoryId!!,
                scheduleCLine = category?.scheduleCLine ?: "27a",
                paymentMethod = state.paymentMethod,
                businessPurpose = state.businessPurpose.ifBlank { null },
                notes = state.notes.ifBlank { null },
                receiptLocalPath = state.receiptLocalPath,
                mileage = state.mileage.toDoubleOrNull(),
                latitude = state.latitude,
                longitude = state.longitude,
                tags = state.tagsText.split(",").map(String::trim).filter(String::isNotEmpty),
                clientId = state.clientId,
            )
            when (val result = saveExpenseUseCase(draft)) {
                is AppResult.Success -> onSaved()
                is AppResult.Error -> _form.value =
                    _form.value.copy(isSaving = false, error = result.message ?: "Could not save.")
            }
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val id = _form.value.id ?: return
        viewModelScope.launch {
            deleteExpenseUseCase(id)
            onDeleted()
        }
    }
}
