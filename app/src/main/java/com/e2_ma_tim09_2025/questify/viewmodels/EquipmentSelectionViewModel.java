package com.e2_ma_tim09_2025.questify.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.e2_ma_tim09_2025.questify.models.MyEquipment;
import com.e2_ma_tim09_2025.questify.services.EquipmentService;
import com.e2_ma_tim09_2025.questify.services.UserService;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class EquipmentSelectionViewModel extends ViewModel {

    private final EquipmentService equipmentService;
    private final UserService userService;
    private final MutableLiveData<List<MyEquipment>> userEquipment = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    @Inject
    public EquipmentSelectionViewModel(EquipmentService equipmentService, UserService userService) {
        this.equipmentService = equipmentService;
        this.userService = userService;
    }

    public void loadUserEquipment() {
        String userId = userService.getCurrentUserId();
        if (userId == null) {
            errorMessage.setValue("User not logged in");
            return;
        }

        isLoading.setValue(true);
        
        // Get user's equipment from their profile
        userService.getUser(userId, task -> {
            isLoading.setValue(false);
            
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                com.e2_ma_tim09_2025.questify.models.User user = task.getResult().toObject(com.e2_ma_tim09_2025.questify.models.User.class);
                if (user != null && user.getEquipment() != null) {
                    // Filter equipment that has uses left AND is not activated
                    List<MyEquipment> availableEquipment = new ArrayList<>();
                    for (MyEquipment equipment : user.getEquipment()) {
                        if (equipment.getLeftAmount() > 0 && !equipment.isActivated()) {
                            availableEquipment.add(equipment);
                        }
                    }
                    userEquipment.setValue(availableEquipment);
                } else {
                    userEquipment.setValue(new ArrayList<>());
                }
            } else {
                errorMessage.setValue("Error loading equipment");
                userEquipment.setValue(new ArrayList<>());
            }
        });
    }

    public void activateEquipment(MyEquipment equipment) {
        String userId = userService.getCurrentUserId();
        if (userId == null) {
            errorMessage.setValue("User not logged in");
            return;
        }

        if (equipment == null) {
            errorMessage.setValue("No equipment selected");
            return;
        }

        isLoading.setValue(true);
        
        //NINO NINO NINO NINO OVDE DOAJES AKTIVACIJU!!!! 

        isLoading.setValue(false);
        errorMessage.setValue("Equipment activation function is temporarily disabled");
    }

    public LiveData<List<MyEquipment>> getUserEquipment() {
        return userEquipment;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}
